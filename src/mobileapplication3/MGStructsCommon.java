// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Common configuration and utilities for MGStruct format.
 *
 * Flags bitmask structure (1 or 2 bytes):
 *
 * Byte 1:
 * [ 7 ] - HAS_EXTENDED_FLAGS (if 1, read next byte for more flag bits)
 * [ 6 ] - MASK_VARIABLE_LENGTH (if 1, read variable length blocks)
 * [0-5] - Optional arguments mask (ID-specific)
 *
 * Byte 2 (Optional, bits 8-15):
 * [ 0-7 ] - Extra optional flags
 *
 * Element-specific bits (0-5 in Byte 1):
 * - CIRCLE: bit 0: arcAngle, bit 1: startAngle, bit 2: kX, bit 3: kY
 * - BODY: bit 0: elasticity, bit 1: mass, bit 2: friction, bit 3: fallDelay, bit 4: color
 * - ACCELERATOR: bit 0: offset, bit 1: speed, bit 2: duration
 * - TRAMPOLINE: bit 0: elasticity
 * - There will be more in future versions
 */
public class MGStructsCommon {
    public static final short EOF = 0;
    public static final short END_POINT = 1;
    public static final short LINE = 2;
    public static final short CIRCLE = 3;
    public static final short BROKEN_LINE = 4;
    public static final short BROKEN_CIRCLE = 5;
    public static final short SINE = 6;
    public static final short ACCELERATOR = 7;
    public static final short TRAMPOLINE = 8;
    public static final short LEVEL_START = 9;
    public static final short LEVEL_FINISH = 10;
    public static final short LAVA = 11;
    public static final short SQUARE_BODY = 12;
    public static final short ROUND_BODY = 13;
    public static final short SINE_FACE_UP = 14;
    public static final short SINE_FACE_DOWN = 15;
    public static final short LINE_FACE_UP = 16;
    public static final short LINE_FACE_DOWN = 17;
    public static final short CIRCLE_FACE_OUTSIDE = 18;
    public static final short CIRCLE_FACE_INSIDE = 19;
    public static final short LINK = 20;

    // Mask bits for optional arguments
    // CIRCLE / CIRCLE_FACE_OUTSIDE / CIRCLE_FACE_INSIDE
    public static final int MASK_CIRCLE_ARC_ANGLE   = (1 << 0);
    public static final int MASK_CIRCLE_START_ANGLE = (1 << 1);
    public static final int MASK_CIRCLE_KX          = (1 << 2);
    public static final int MASK_CIRCLE_KY          = (1 << 3);

    // SQUARE_BODY / ROUND_BODY
    public static final int MASK_BODY_ELASTICITY    = (1 << 0);
    public static final int MASK_BODY_MASS          = (1 << 1);
    public static final int MASK_BODY_FRICTION      = (1 << 2);
    public static final int MASK_BODY_FALL_DELAY    = (1 << 3);
    public static final int MASK_BODY_COLOR         = (1 << 4);
    public static final int MASK_BODY_COLLISION_MASK = (1 << 5);
    public static final int MASK_BODY_VX             = (1 << 8);
    public static final int MASK_BODY_VY             = (1 << 9);
    public static final int MASK_BODY_VA             = (1 << 10);

    // Reserved layers
    public static final int COLLISION_LAYER_CAR = 0;
    public static final int COLLISION_LAYER_GROUND = 15;

    // ACCELERATOR
    public static final int MASK_ACCELERATOR_OFFSET   = (1 << 0);
    public static final int MASK_ACCELERATOR_SPEED    = (1 << 1);
    public static final int MASK_ACCELERATOR_DURATION = (1 << 2);

    // TRAMPOLINE
    public static final int MASK_TRAMPOLINE_ELASTICITY = (1 << 0);

    // Generic flags
    public static final int MASK_VARIABLE_LENGTH    = (1 << 6);
    public static final int MASK_HAS_EXTENDED_FLAGS = (1 << 7);

    public static final int[] ARGS_NUMBER = {
            0,    // id0    EOF
            2,    // id1    END_POINT
            4,    // id2    LINE
            7,    // id3    CIRCLE
            9,    // id4    BROKEN_LINE
            10,   // id5    BROKEN_CIRCLE
            6,    // id6    SINE
            8,    // id7    ACCELERATOR
            6,    // id8    TRAMPOLINE
            2,    // id9    LEVEL_START
            5,    // id10   LEVEL_FINISH
            5,    // id11   LAVA
            14,   // id12   SQUARE_BODY
            12,   // id13   ROUND_BODY
            6,    // id14   SINE_FACE_UP
            6,    // id15   SINE_FACE_DOWN
            4,    // id16   LINE_FACE_UP
            4,    // id17   LINE_FACE_DOWN
            7,    // id18   CIRCLE_FACE_OUTSIDE
            7,    // id19   CIRCLE_FACE_INSIDE
            3,    // id20   LINK
    };

    public static final int[] ARGS_NUMBER_V1 = {
            0,    // id0    EOF
            2,    // id1    END_POINT
            4,    // id2    LINE
            7,    // id3    CIRCLE
            9,    // id4    BROKEN_LINE
            10,   // id5    BROKEN_CIRCLE
            6,    // id6    SINE
            8,    // id7    ACCELERATOR
            6,    // id8    TRAMPOLINE
            2,    // id9    LEVEL_START
            5,    // id10   LEVEL_FINISH
            5,    // id11   LAVA
            10,   // id12   SQUARE_BODY
            8,    // id13   ROUND_BODY
            6,    // id14   SINE_FACE_UP
            6,    // id15   SINE_FACE_DOWN
            4,    // id16   LINE_FACE_UP
            4,    // id17   LINE_FACE_DOWN
            7,    // id18   CIRCLE_FACE_OUTSIDE
            7,    // id19   CIRCLE_FACE_INSIDE
            3,    // id20   LINK
    };

    public static final int[] REQUIRED_ARGS_NUMBER = {
            0,    // id0    EOF
            2,    // id1    END_POINT
            4,    // id2    LINE
            3,    // id3    CIRCLE
            9,    // id4    BROKEN_LINE
            3,    // id5    BROKEN_CIRCLE
            6,    // id6    SINE
            5,    // id7    ACCELERATOR
            5,    // id8    TRAMPOLINE
            2,    // id9    LEVEL_START
            5,    // id10   LEVEL_FINISH
            5,    // id11   LAVA
            5,    // id12   SQUARE_BODY
            3,    // id13   ROUND_BODY
            6,    // id14   SINE_FACE_UP
            6,    // id15   SINE_FACE_DOWN
            4,    // id16   LINE_FACE_UP
            4,    // id17   LINE_FACE_DOWN
            3,    // id18   CIRCLE_FACE_OUTSIDE
            3,    // id19   CIRCLE_FACE_INSIDE
            3,    // id20   LINK
    };

    public static short[] readShortenedElement(int id, DataInputStream dis) throws IOException {
        int flags = dis.readByte() & 0xFF;

        if ((flags & MASK_HAS_EXTENDED_FLAGS) != 0) {
            int extendedFlags = dis.readByte() & 0xFF;
            flags |= (extendedFlags << 8);
        }

        int requiredCount = REQUIRED_ARGS_NUMBER[id];
        int totalExpected = ARGS_NUMBER[id];

        short[] data = new short[1 + totalExpected];
        data[0] = (short) id;

        // read required arguments
        for (int i = 1; i <= requiredCount; i++) {
            data[i] = dis.readShort();
        }

        // pre-fill optional arguments with default values
        fillDefaults(id, data, 1 + requiredCount);

        // overwrite those optional arguments that are present
        switch (id) {
            case CIRCLE:
            case CIRCLE_FACE_OUTSIDE:
            case CIRCLE_FACE_INSIDE:
                if ((flags & MASK_CIRCLE_ARC_ANGLE) != 0) {
                    data[4] = dis.readShort();
                }
                if ((flags & MASK_CIRCLE_START_ANGLE) != 0) {
                    data[5] = dis.readShort();
                }
                if ((flags & MASK_CIRCLE_KX) != 0) {
                    data[6] = dis.readShort();
                }
                if ((flags & MASK_CIRCLE_KY) != 0) {
                    data[7] = dis.readShort();
                }
                break;
            case SQUARE_BODY:
            case ROUND_BODY:
                int startIdx = (id == SQUARE_BODY) ? 6 : 4;
                if ((flags & MASK_BODY_ELASTICITY) != 0) {
                    data[startIdx]   = dis.readShort();
                }
                if ((flags & MASK_BODY_MASS) != 0) {
                    data[startIdx+1] = dis.readShort();
                }
                if ((flags & MASK_BODY_FRICTION) != 0) {
                    data[startIdx+2] = dis.readShort();
                }
                if ((flags & MASK_BODY_FALL_DELAY) != 0) {
                    data[startIdx+3] = dis.readShort();
                }
                if ((flags & MASK_BODY_COLOR) != 0) {
                    data[startIdx+4] = dis.readShort();
                }
                if ((flags & MASK_BODY_COLLISION_MASK) != 0) {
                    data[startIdx+5] = dis.readShort();
                }
                if ((flags & MASK_BODY_VX) != 0) {
                    data[startIdx+6] = dis.readShort();
                }
                if ((flags & MASK_BODY_VY) != 0) {
                    data[startIdx+7] = dis.readShort();
                }
                if ((flags & MASK_BODY_VA) != 0) {
                    data[startIdx+8] = dis.readShort();
                }
                break;
            case ACCELERATOR:
                if ((flags & MASK_ACCELERATOR_OFFSET) != 0) {
                    data[6] = dis.readShort();
                }
                if ((flags & MASK_ACCELERATOR_SPEED) != 0) {
                    data[7] = dis.readShort();
                }
                if ((flags & MASK_ACCELERATOR_DURATION) != 0) {
                    data[8] = dis.readShort();
                }
                break;
            case TRAMPOLINE:
                if ((flags & MASK_TRAMPOLINE_ELASTICITY) != 0) {
                    data[6] = dis.readShort();
                }
                break;
        }

        // handle variable length arguments
        if ((flags & MASK_VARIABLE_LENGTH) != 0) {
            while (true) {
                int len = dis.readShort() & 0xFFFF;
                if (len == 0) break;
                short[] newData = new short[data.length + 1 + len];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = (short) len;
                for (int i = 0; i < len; i++) {
                    newData[data.length + 1 + i] = dis.readShort();
                }
                data = newData;
            }
        }

        return data;
    }

    public static void fillDefaults(int id, short[] data, int startIndex) {
        for (int i = startIndex; i < data.length; i++) {
            data[i] = getDefaultForArg(id, i);
        }
    }

    private static short getDefaultForArg(int id, int i) {
        switch (id) {
            case CIRCLE:
            case CIRCLE_FACE_OUTSIDE:
            case CIRCLE_FACE_INSIDE:
                if (i == 4) return 360; // arcAngle
                if (i == 5) return 0; // startAngle
                if (i == 6) return 100; // kX
                if (i == 7) return 100; // kY
                break;
            case ACCELERATOR:
                if (i == 6) return 0; // directionOffset (m)
                if (i == 7) return 150; // speedMultiplier (m)
                if (i == 8) return 30; // effectDuration
                break;
            case TRAMPOLINE:
                if (i == 6) return 100; // elasticity
                break;
            case SQUARE_BODY:
            case ROUND_BODY:
                int base = (id == SQUARE_BODY) ? 6 : 4;
                if (i == base)     return 0; // elasticity
                if (i == base + 1) return 1; // mass
                if (i == base + 2) return 10; // friction
                if (i == base + 3) return Short.MIN_VALUE; // fallDelay: DYNAMIC
                if (i == base + 4) return (short) 0xFFFF; // color: White (RGB565)
                if (i == base + 5) return 0; // collision mask (0 = collides with everything)
                if (i == base + 6) return 0; // VX
                if (i == base + 7) return 0; // VY
                if (i == base + 8) return 0; // VA
                break;
        }
        return 0;
    }
}
