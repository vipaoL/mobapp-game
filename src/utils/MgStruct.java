// SPDX-License-Identifier: GPL-3.0-or-later

package utils;

import mobileapplication3.MGStructsCommon;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author vipaol
 */
public class MgStruct {
    private static final int STRUCTURE_STORAGE_SIZE = 32;
    public static final String PREFIX = "/s";
    public static final String EXTENSION = ".mgstruct";

    public static final short[] SUPPORTED_FORMAT_VERSIONS = {
            2,
            1,
            0
    };

    public static short[][][] structStorage = new short[STRUCTURE_STORAGE_SIZE][][];

    public static int loadedTotal = 0;
    public static int loadedFromRes = 0;
    public static boolean loadCancelled = false;

    private static boolean isInited = false;

    public static void init() {
        if (!isInited) {
            Logger.log("mgs init");
            loadFromRes();
        }
        Logger.log("inited");
        isInited = true;
    }

    private static void loadFromRes() {
        loadedTotal = 0;
        loadedFromRes = 0;

        for (int i = 1; readFromRes(PREFIX + i + EXTENSION); i++) {
            Logger.log(i + EXTENSION);
            loadedFromRes++;
        }

        Logger.log("MGStruct:loaded " + loadedFromRes + " from resources");
    }

    private static boolean readFromRes(String path) {
        InputStream is = null;
        try {
            is = Platform.getResource(path);
            DataInputStream dis = new DataInputStream(is);
            try {
                saveStructToStorage(readFromDataInputStream(dis));
            } finally {
                try {
                    dis.close();
                } catch (IOException ignored) { }
            }
            return true;
        } catch (Exception ex) {
            Logger.log(path + " " + ex);
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ignored) { }
        }
    }

    public static boolean loadFromFiles() {
        Logger.log("mgs load()");
        if (!isInited) {
            init();
        } else {
            loadFromRes();
        }

        String[] paths;
        try {
            paths = GameFileUtils.listFilesInAllPlaces("MobappGame/MGStructs");
        } catch (SecurityException ex) {
            loadCancelled = true;
            Logger.log(ex);
            return false;
        }

        loadCancelled = false;
        loadedTotal = loadedFromRes;

        int loadedFromFiles = 0;
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            DataInputStream dis = null;
            try {
                dis = FileUtils.fileToDataInputStream(path);
            } catch (SecurityException sex) {
                Logger.log("mgs:load cancelled");
                Logger.log(sex);
                loadCancelled = true;
            } catch (NullPointerException ex) {
                Logger.log(ex);
            } catch (NoClassDefFoundError err) {
                Platform.showError(err);
                return false;
            }
            if (dis != null) {
                try {
                    short[][] structure = readFromDataInputStream(dis);
                    if (structure != null) {
                        int targetIndex = getTargetIndexFromPath(path);

                        if (targetIndex >= 0 && targetIndex < loadedFromRes) {
                            Logger.log("overriding built-in s" + (targetIndex + 1) + " with " + path);
                            structStorage[targetIndex] = structure;
                        } else {
                            saveStructToStorage(structure);
                        }

                        loadedFromFiles += 1;
                        Logger.log(path + " loaded");
                    }
                } catch (IOException ex) {
                    Logger.log(ex);
                }
                try {
                    dis.close();
                } catch (Exception ignored) { }
            }
        }
        Logger.log("mg:loaded: " + loadedFromFiles);
        return loadedFromFiles > 0;
    }

    public static short[][] readFromDataInputStream(DataInputStream dis) throws IOException {
        if (dis == null) {
            return null;
        }

        try {
            short fileFormatVersion = dis.readShort();
            if (Utils.isArrContain(SUPPORTED_FORMAT_VERSIONS, fileFormatVersion)) {
                // number of elements in the structure
                int count = 16;
                if (fileFormatVersion > 0) {
                    count = dis.readShort();
                }
                Logger.log("reading: ver=" + fileFormatVersion + " count=" + count);

                short[][] structure = new short[count][];
                int c = 0;

                for (int e = 0; e < count; e++) {
                    int id;
                    try {
                        if (fileFormatVersion >= 2) {
                            id = dis.readByte() & 0xFF;
                        } else {
                            id = dis.readShort();
                        }
                    } catch (EOFException ex) {
                        break;
                    }

                    if (id == 0) { // EOF mark
                        break;
                    }

                    /*
                     *
                     * Old format (v0, v1):
                     * Example:
                     * data = {2, 0, 0, 100, 0}:
                     * id=2 (LINE), x1=0, y1=0, x2=100, y2=0
                     *
                     * data = {3, 400, -50, 100, 90, 0, 100, 50}
                     * id=3 (CIRCLE), x=400, y=-50, r=100, arcAngle=90, startAngle=0, kX=100, kY=50
                     *
                     *
                     * v2:
                     * Example:
                     * data = {(2,0b00000000), 0, 0, 100, 0}:
                     * id=2 (LINE), flags=0b00000000 (no optional arguments, no extra flags), x1=0, y1=0, x2=100, y2=0
                     *
                     * data = {(3,0b00001001), 400, -50, 100, 90, 50}
                     * id=3 (CIRCLE), flags=0b00001001 (Bit 3: kY, Bit 0: arcAngle),
                     * x=400, y=-50, r=100 (mandatory), arcAngle=90, kY=50 (optional)
                     * (startAngle and kX are default)
                     *
                     * Flags bitmask structure (1 byte):
                     * [ 7 ] - HAS_EXTENDED_FLAGS (if 1, read next byte for more flag bits)
                     * [ 6 ] - MASK_VARIABLE_LENGTH (if 1, read variable length blocks)
                     * [0-5] - Optional arguments mask (specific for each element ID)
                     */
                    try {
                        if (fileFormatVersion >= 2) {
                            structure[e] = MGStructsCommon.readShortenedElement(id, dis);
                        } else {
                            short[] data = new short[MGStructsCommon.ARGS_NUMBER[id] + 1];
                            // first cell is ID of the element, next cells are arguments (properties)
                            data[0] = (short) id;
                            for (int i = 1; i < data.length; i++) {
                                data[i] = dis.readShort();
                            }
                            structure[e] = data;
                        }
                    } catch (EOFException ex) {
                        break;
                    }
                    c++;
                }

                // always return array of exact size without null elements
                if (c < structure.length) {
                    short[][] finalStructure = new short[c][];
                    System.arraycopy(structure, 0, finalStructure, 0, c);
                    structure = finalStructure;
                }

                try {
                    dis.close();
                } catch (Exception ignored) { }
                return structure;
            } else {
                Logger.log("Unsupported file format version: " + fileFormatVersion);
                try {
                    dis.close();
                } catch (Exception ignored) { }
                return null;
            }
        } catch (Exception ex) {
            Logger.log("error parsing file " + ex);
            ex.printStackTrace();
            try {
                dis.close();
            } catch (Exception ignored) { }
            return null;
        }
    }

    private static void saveStructToStorage(short[][] data) {
        Logger.log("saving new structure, i=" + loadedTotal);
        structStorage = ensureCapacity(structStorage, loadedTotal, 8);
        structStorage[loadedTotal] = data;
        loadedTotal++;
    }

    private static short[][][] ensureCapacity(short[][][] array, int newElementIndex, int inc) {
        if (newElementIndex >= array.length) {
            short[][][] newArray = new short[array.length + inc][][];
            System.arraycopy(array, 0, newArray, 0, array.length);
            return newArray;
        }
        return array;
    }

    private static int getTargetIndexFromPath(String path) {
        if (path == null) return -1;

        String fileName = path.substring(Utils.lastIndexOf(path, '/') + 1).toLowerCase();
        String prefix = PREFIX.substring(1);
        if (fileName.startsWith(prefix) && fileName.endsWith(EXTENSION)) {
            try {
                String numberStr = fileName.substring(prefix.length(), fileName.length() - EXTENSION.length());
                int id = Integer.valueOf(numberStr).intValue();
                if (id > 0) {
                    return id - 1;
                }
            } catch (NumberFormatException ignored) { }
        }
        return -1;
    }
}
