#!/usr/bin/env python3
"""Convert unsigned APK resource string pools for Android 1.0, then zipalign.

Format reference: AOSP libs/androidfw/include/androidfw/ResourceTypes.h.
Only pool payloads and their enclosing sizes/package pool offsets change.
Resource indexes, values, configurations, styles and nonresource entry bytes
are preserved. Signing must happen after this step.
"""

import argparse
import copy
import os
from pathlib import Path
import struct
import subprocess
import tempfile
import zipfile


STRING_POOL = 0x0001
TABLE = 0x0002
XML = 0x0003
PACKAGE = 0x0200
UTF8_FLAG = 0x0100
NO_ENTRY = 0xFFFFFFFF


class FormatError(ValueError):
    """The input cannot safely be transformed without changing its meaning."""


def u32(data, offset):
    if offset < 0 or offset + 4 > len(data):
        raise FormatError(f"truncated uint32 at {offset}")
    return struct.unpack_from("<I", data, offset)[0]


def chunk_header(data):
    if len(data) < 8:
        raise FormatError("truncated chunk header")
    kind, header_size, size = struct.unpack_from("<HHI", data)
    if header_size < 8 or header_size > size or size != len(data) or size % 4:
        raise FormatError(f"invalid chunk size/header: {kind:#x}, {header_size}, {size}")
    return kind, header_size, size


def children(data, start):
    """Yield validated immediate children with offsets relative to this container."""
    offset = start
    while offset < len(data):
        if offset + 8 > len(data):
            raise FormatError(f"truncated child header at {offset}")
        size = u32(data, offset + 4)
        if size < 8 or size > len(data) - offset:
            raise FormatError(f"invalid child size at {offset}")
        child = data[offset:offset + size]
        chunk_header(child)
        yield offset, child
        offset += size


def length8(data, offset, end):
    if offset >= end:
        raise FormatError("truncated UTF-8 length")
    first = data[offset]
    offset += 1
    if first & 0x80:
        if offset >= end:
            raise FormatError("truncated extended UTF-8 length")
        return ((first & 0x7F) << 8) | data[offset], offset + 1
    return first, offset


def length16(data, offset, end):
    if offset + 2 > end:
        raise FormatError("truncated UTF-16 length")
    first = struct.unpack_from("<H", data, offset)[0]
    offset += 2
    if first & 0x8000:
        if offset + 2 > end:
            raise FormatError("truncated extended UTF-16 length")
        low = struct.unpack_from("<H", data, offset)[0]
        return ((first & 0x7FFF) << 16) | low, offset + 2
    return first, offset


def encode_length16(length):
    if not 0 <= length <= 0x7FFFFFFF:
        raise FormatError("UTF-16 length exceeds resource format")
    if length < 0x8000:
        return struct.pack("<H", length)
    return struct.pack("<HH", 0x8000 | (length >> 16), length & 0xFFFF)


def convert_string_pool(data):
    """Return UTF-16 pool bytes, preserving all string indexes/style references.

    Existing UTF-16 pools are validated and returned byte for byte.
    """
    kind, header_size, size = chunk_header(data)
    if kind != STRING_POOL or header_size != 28:
        raise FormatError("unsupported string pool header")
    count, style_count, flags, strings_start, styles_start = struct.unpack_from("<5I", data, 8)
    if flags & ~0x0101:
        raise FormatError("unsupported string pool flags")
    offsets_end = header_size + 4 * (count + style_count)
    if offsets_end > strings_start or strings_start > size or strings_start % 4:
        raise FormatError("invalid pool string/offset region")
    if style_count > count:
        raise FormatError("more style indexes than strings")
    if style_count:
        if styles_start < strings_start or styles_start >= size or styles_start % 4:
            raise FormatError("invalid pool style region")
    elif styles_start:
        raise FormatError("unsupported stylesStart without style indexes")
    strings_end = styles_start or size
    string_offsets = [u32(data, header_size + 4 * i) for i in range(count)]
    utf16_strings = []
    unit_counts = []
    for relative in string_offsets:
        offset = strings_start + relative
        if offset >= strings_end:
            raise FormatError("string offset outside string region")
        if flags & UTF8_FLAG:
            units, offset = length8(data, offset, strings_end)
            byte_count, offset = length8(data, offset, strings_end)
            end = offset + byte_count
            if end >= strings_end or data[end] != 0:
                raise FormatError("truncated/nonterminated UTF-8 string")
            try:
                encoded = data[offset:end].decode("utf-8").encode("utf-16le")
            except UnicodeError as error:
                raise FormatError(f"invalid UTF-8 string: {error}") from error
            if len(encoded) // 2 != units:
                raise FormatError("UTF-8 string UTF-16 length mismatch")
        else:
            if relative % 2:
                raise FormatError("unaligned UTF-16 string offset")
            units, offset = length16(data, offset, strings_end)
            end = offset + units * 2
            if end + 2 > strings_end or data[end:end + 2] != b"\0\0":
                raise FormatError("truncated/nonterminated UTF-16 string")
            encoded = data[offset:end]
        utf16_strings.append(encoded)
        unit_counts.append(units)

    # Style offsets are relative to stylesStart, and spans use string indexes
    # and UTF-16 character positions. Neither changes with the conversion.
    for index in range(style_count):
        relative = u32(data, header_size + 4 * (count + index))
        if relative == NO_ENTRY:
            continue
        offset = styles_start + relative
        if relative % 4 or offset >= size:
            raise FormatError("invalid style offset")
        while True:
            name = u32(data, offset)
            if name == NO_ENTRY:
                break
            if offset + 12 > size or name >= count:
                raise FormatError("truncated style span/invalid name index")
            first, last = struct.unpack_from("<II", data, offset + 4)
            if first > last or last >= unit_counts[index]:
                raise FormatError("style character range outside string")
            offset += 12
    if not flags & UTF8_FLAG:
        return data

    new_strings = bytearray()
    rewritten_offsets = []
    source_to_new = {}
    for relative, encoded, units in zip(string_offsets, utf16_strings, unit_counts):
        if relative not in source_to_new:
            source_to_new[relative] = len(new_strings)
            new_strings.extend(encode_length16(units) + encoded + b"\0\0")
        rewritten_offsets.append(source_to_new[relative])
    new_strings.extend(b"\0" * (-len(new_strings) % 4))
    prefix = bytearray(data[:strings_start])
    for index, relative in enumerate(rewritten_offsets):
        struct.pack_into("<I", prefix, header_size + 4 * index, relative)
    struct.pack_into("<I", prefix, 16, flags & ~UTF8_FLAG)
    new_styles_start = strings_start + len(new_strings) if style_count else 0
    struct.pack_into("<I", prefix, 24, new_styles_start)
    result = prefix + new_strings + (data[styles_start:] if style_count else b"")
    struct.pack_into("<I", result, 4, len(result))
    return bytes(result)


def convert_resource(data, expected_kind):
    """Transform a resources.arsc or binary XML; retain known leaf chunks."""
    kind, header_size, _ = chunk_header(data)
    if kind != expected_kind or kind not in (TABLE, XML):
        raise FormatError(f"expected resource container {expected_kind:#x}, found {kind:#x}")
    if header_size != (12 if kind == TABLE else 8):
        raise FormatError("unsupported resource container header")
    header = bytearray(data[:header_size])
    body = bytearray()
    packages = 0
    for _, child in children(data, header_size):
        child_kind = struct.unpack_from("<H", child)[0]
        if child_kind == STRING_POOL:
            child = convert_string_pool(child)
        elif child_kind == PACKAGE:
            if kind != TABLE:
                raise FormatError("package inside XML")
            packages += 1
            child = convert_package(child)
        elif kind != XML or child_kind not in (0x0180, 0x0100, 0x0101, 0x0102, 0x0103, 0x0104):
            raise FormatError(f"unsupported resource chunk {child_kind:#x}")
        body.extend(child)
    if kind == TABLE and packages != u32(data, 8):
        raise FormatError("table packageCount differs from actual packages")
    struct.pack_into("<I", header, 4, len(header) + len(body))
    return bytes(header + body)


def convert_package(data):
    kind, header_size, _ = chunk_header(data)
    if kind != PACKAGE or header_size not in (284, 288):
        raise FormatError("unsupported package header")
    header = bytearray(data[:header_size])
    old_pool_offsets = {field: u32(data, field) for field in (268, 276)}
    relocated_pools = {}
    body = bytearray()
    for old_offset, child in children(data, header_size):
        child_kind = struct.unpack_from("<H", child)[0]
        if child_kind == STRING_POOL:
            relocated_pools[old_offset] = header_size + len(body)
            child = convert_string_pool(child)
        elif child_kind not in (0x0201, 0x0202, 0x0203, 0x0204, 0x0205, 0x0206):
            raise FormatError(f"unsupported package chunk {child_kind:#x}")
        body.extend(child)
    for field, old_offset in old_pool_offsets.items():
        if old_offset:
            if old_offset not in relocated_pools:
                raise FormatError("package pool offset does not point to immediate string pool")
            struct.pack_into("<I", header, field, relocated_pools[old_offset])
    struct.pack_into("<I", header, 4, len(header) + len(body))
    return bytes(header + body)


def signing_metadata(name):
    upper = name.upper()
    if not upper.startswith("META-INF/"):
        return False
    leaf = upper[len("META-INF/"):]
    if "/" in leaf:
        return False
    return leaf == "MANIFEST.MF" or leaf.startswith("SIG-") or leaf.endswith((".SF", ".RSA", ".DSA", ".EC"))


def convert_archive(source, destination):
    """Rebuild an unsigned APK without compressor-dependent output bytes."""
    source, destination = Path(source), Path(destination)
    if source.resolve() == destination.resolve():
        raise ValueError("input and output must be different paths")
    converted = []
    temp_name = None
    try:
        with zipfile.ZipFile(source) as original:
            infos = original.infolist()
            names = [info.filename for info in infos]
            if len(names) != len(set(names)):
                raise FormatError("duplicate ZIP entry names")
            if not {"AndroidManifest.xml", "resources.arsc"}.issubset(names):
                raise FormatError("APK must contain a manifest and resource table")
            if any(signing_metadata(name) for name in names):
                raise FormatError("APK is signed; disable release signing and sign the converted APK externally")
            # v2/v3 signatures sit outside ZIP entries, just before its directory.
            with source.open("rb") as stream:
                stream.seek(max(0, original.start_dir - 16))
                if stream.read(16) == b"APK Sig Block 42":
                    raise FormatError("APK has a signing block; sign the converted APK externally")
            fd, temp_name = tempfile.mkstemp(prefix=destination.name + ".", suffix=".tmp", dir=destination.parent)
            os.close(fd)
            with zipfile.ZipFile(temp_name, "w") as output:
                output.comment = original.comment
                for info in infos:
                    if info.flag_bits & 1:
                        raise FormatError("encrypted ZIP entries are unsupported")
                    data = original.read(info)
                    is_table = info.filename == "resources.arsc"
                    is_xml = info.filename == "AndroidManifest.xml" or (info.filename.startswith("res/") and info.filename.endswith(".xml"))
                    if is_table or is_xml:
                        try:
                            changed = convert_resource(data, TABLE if is_table else XML)
                        except FormatError as error:
                            raise FormatError(f"{info.filename}: {error}") from error
                        if changed != data:
                            converted.append(info.filename)
                        data = changed
                    output_info = copy.copy(info)
                    # Python/zlib versions produce different DEFLATE bytes. This
                    # small APK stores every entry for reproducible F-Droid builds.
                    output_info.compress_type = zipfile.ZIP_STORED
                    output.writestr(output_info, data)
        os.replace(temp_name, destination)
        temp_name = None
    finally:
        if temp_name is not None:
            os.unlink(temp_name)
    return converted


def convert_apk(source, destination, zipalign):
    """Publish an aligned APK only after resource conversion succeeds."""
    destination = Path(destination)
    destination.parent.mkdir(parents=True, exist_ok=True)
    if Path(source).resolve() == destination.resolve():
        raise ValueError("input and output must be different paths")
    with tempfile.TemporaryDirectory(prefix="utf16-resources-", dir=destination.parent) as temporary:
        unaligned = Path(temporary) / "unaligned.apk"
        aligned = Path(temporary) / "aligned.apk"
        converted = convert_archive(source, unaligned)
        subprocess.run([str(zipalign), "-p", "-f", "4", str(unaligned), str(aligned)], check=True)
        os.replace(aligned, destination)
    return converted


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--zipalign", required=True, type=Path)
    args = parser.parse_args()
    try:
        converted = convert_apk(args.input, args.output, args.zipalign)
    except (FormatError, ValueError, OSError, zipfile.BadZipFile, subprocess.CalledProcessError) as error:
        parser.exit(1, f"error: {error}\n")
    print(f"Converted {len(converted)} resource files; aligned unsigned APK: {args.output}")


if __name__ == "__main__":
    main()
