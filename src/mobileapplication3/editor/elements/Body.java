package mobileapplication3.editor.elements;

import mobileapplication3.MGStructsCommon;
import mobileapplication3.ui.BitmaskProperty;
import mobileapplication3.ui.Property;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Body extends Element {
    protected short fallDelay = DYNAMIC;
    protected short elasticity = 0, mass = 1, friction = 10;
    protected short collisionMask = 0; // collide with everything by default
    protected short vx = 0, vy = 0, va = 0;
    protected short cx = 0, cy = 0;
    protected boolean gravityAffected = true;
    protected boolean isLava = false;

    // RGB565
    protected int red = 31;
    protected int green = 63;
    protected int blue = 31;

    protected static final short STATIC = -1;
    protected static final short DYNAMIC = Short.MIN_VALUE;
    protected static final short DEFAULT_FALL_DELAY = 600;

    protected int getColor(boolean isSelected) {
        if (isLava) {
            return Lava.COLOR;
        } else {
            return super.getColor(isSelected);
        }
    }

    protected void parseBodyArgs(short[] args, int startIndex) {
        elasticity = args[startIndex];
        mass = args[startIndex + 1] >= 0 ? args[startIndex + 1] : (short) -args[startIndex + 1];
        gravityAffected = args[startIndex + 1] >= 0; // use the sign bit as a boolean
        friction = args[startIndex + 2] >= 0 ? args[startIndex + 2] : (short) (-args[startIndex + 2] - 1);
        isLava = args[startIndex + 2] < 0;

        fallDelay = args[startIndex + 3];
        int color = args[startIndex + 4] & 0xFFFF;

        setColorRGB565(((color >> 11) & 0x1F), ((color >> 5) & 0x3F), (color & 0x1F));

        if (args.length > startIndex + 5) {
            collisionMask = args[startIndex + 5];
        }
        if (args.length > startIndex + 6) {
            vx = args[startIndex + 6];
        }
        if (args.length > startIndex + 7) {
            vy = args[startIndex + 7];
        }
        if (args.length > startIndex + 8) {
            va = args[startIndex + 8];
        }
        if (args.length > startIndex + 9) {
            cx = args[startIndex + 9];
        }
        if (args.length > startIndex + 10) {
            cy = args[startIndex + 10];
        }
    }

    public short[] getBodyArgsValues() {
        return new short[] {
                elasticity,
                gravityAffected ? mass : (short) -mass,
                !isLava ? friction : (short) (-friction - 1),
                fallDelay,
                (short) ((red << 11) | (green << 5) | blue),
                collisionMask,
                vx,
                vy,
                va,
                cx,
                cy,
        };
    }

    public int getOptionalArgsMask() {
        short[] bodyArgs = getBodyArgsValues();
        int mask = 0;
        if (bodyArgs[0] != 0) {
            mask |= MGStructsCommon.MASK_BODY_ELASTICITY;
        }
        if (bodyArgs[1] != 1) {
            mask |= MGStructsCommon.MASK_BODY_MASS;
        }
        if (bodyArgs[2] != 10) {
            mask |= MGStructsCommon.MASK_BODY_FRICTION;
        }
        if (bodyArgs[3] != DYNAMIC) {
            mask |= MGStructsCommon.MASK_BODY_FALL_DELAY;
        }
        if (bodyArgs[4] != (short) 0xFFFF) {
            mask |= MGStructsCommon.MASK_BODY_COLOR;
        }
        if (collisionMask != 0) {
            mask |= MGStructsCommon.MASK_BODY_COLLISION_MASK;
        }
        if (vx != 0) {
            mask |= MGStructsCommon.MASK_BODY_VX;
        }
        if (vy != 0) {
            mask |= MGStructsCommon.MASK_BODY_VY;
        }
        if (va != 0) {
            mask |= MGStructsCommon.MASK_BODY_VA;
        }
        if (cx != 0) {
            mask |= MGStructsCommon.MASK_BODY_CX;
        }
        if (cy != 0) {
            mask |= MGStructsCommon.MASK_BODY_CY;
        }
        return mask;
    }

    protected void writeOptionalArgs(DataOutputStream dos) throws IOException {
        int mask = getOptionalArgsMask();
        short[] bodyArgs = getBodyArgsValues();
        if ((mask & MGStructsCommon.MASK_BODY_ELASTICITY) != 0) {
            dos.writeShort(bodyArgs[0]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_MASS) != 0) {
            dos.writeShort(bodyArgs[1]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_FRICTION) != 0) {
            dos.writeShort(bodyArgs[2]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_FALL_DELAY) != 0) {
            dos.writeShort(bodyArgs[3]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_COLOR) != 0) {
            dos.writeShort(bodyArgs[4]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_COLLISION_MASK) != 0) {
            dos.writeShort(bodyArgs[5]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_VX) != 0) {
            dos.writeShort(bodyArgs[6]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_VY) != 0) {
            dos.writeShort(bodyArgs[7]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_VA) != 0) {
            dos.writeShort(bodyArgs[8]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_CX) != 0) {
            dos.writeShort(bodyArgs[9]);
        }
        if ((mask & MGStructsCommon.MASK_BODY_CY) != 0) {
            dos.writeShort(bodyArgs[10]);
        }
    }

    public Property[] getBodyProperties() {
        return new Property[] {
                new Property("Is lava") {
                    public void setValue(int value) {
                        isLava = value == 1;
                    }
                    public int getValue() {
                        return (short) (isLava ? 1 : 0);
                    }
                    public int getMinValue() {
                        return 0;
                    }
                    public int getMaxValue() {
                        return 1;
                    }
                },
                new Property("Static") {
                    public void setValue(int value) {
                        if (value == 1) {
                            fallDelay = DEFAULT_FALL_DELAY;
                        } else {
                            fallDelay = DYNAMIC;
                        }
                    }
                    public int getValue() {
                        return (short) (fallDelay >= STATIC ? 1 : 0);
                    }
                    public int getMinValue() {
                        return 0;
                    }
                    public int getMaxValue() {
                        return 1;
                    }
                },
                new Property("Fall Delay (" + STATIC + "=never)") {
                    public void setValue(int value) {
                        fallDelay = (short) value;
                    }
                    public int getValue() {
                        return fallDelay;
                    }
                    public boolean isActive() {
                        return fallDelay >= STATIC;
                    }
                    public int getMinValue() {
                        return STATIC;
                    }
                },
                new Property("Elasticity") {
                    public void setValue(int value) {
                        elasticity = (short) value;
                    }
                    public int getValue() {
                        return elasticity;
                    }
                    public int getMinValue() {
                        return 0;
                    }
                    public int getMaxValue() {
                        return 1000;
                    }
                },
                new Property("Mass") {
                    public void setValue(int value) {
                        mass = (short) value;
                    }
                    public int getValue() {
                        return mass;
                    }
                    public int getMinValue() {
                        return 1;
                    }
                },
                new Property("Gravity affected") {
                    public void setValue(int value) {
                        gravityAffected = value == 1;
                    }
                    public int getValue() {
                        return (short) (gravityAffected ? 1 : 0);
                    }
                    public int getMinValue() {
                        return 0;
                    }
                    public int getMaxValue() {
                        return 1;
                    }
                },
                new Property("Friction") {
                    public void setValue(int value) {
                        friction = (short) value;
                    }
                    public int getValue() {
                        return friction;
                    }
                    public int getMinValue() {
                        return 0;
                    }
                    public int getMaxValue() {
                        return 100;
                    }
                },
                new Property("Red") {
                    public void setValue(int value) {
                        red = value;
                        updateColor();
                    }
                    public int getValue() {
                        return red;
                    }
                    public int getMinValue() {
                        return 0;
                    }
                    public int getMaxValue() {
                        return 31;
                    }
                },
                new Property("Green") {
                    public void setValue(int value) {
                        green = value;
                        updateColor();
                    }
                    public int getValue() {
                        return green;
                    }
                    public int getMinValue() {
                        return 0;
                    }
                    public int getMaxValue() {
                        return 63;
                    }
                },
                new Property("Blue") {
                    public void setValue(int value) {
                        blue = value;
                        updateColor();
                    }
                    public int getValue() { return blue; }
                    public int getMinValue() { return 0; }
                    public int getMaxValue() { return 31; }
                },
                new BitmaskProperty("Collision mask", 16) {
                    public void setValue(int value) {
                        collisionMask = (short) value;
                    }
                    public int getValue() {
                        return collisionMask & 0xFFFF;
                    }
                },
                new Property("Initial VX") {
                    public void setValue(int value) {
                        vx = (short) value;
                    }
                    public int getValue() {
                        return vx;
                    }
                },
                new Property("Initial VY") {
                    public void setValue(int value) {
                        vy = (short) value;
                    }
                    public int getValue() {
                        return vy;
                    }
                },
                new Property("Initial VA") {
                    public void setValue(int value) {
                        va = (short) value;
                    }
                    public int getValue() {
                        return va;
                    }
                },
                new Property("Centroid CX") {
                    public void setValue(int value) { cx = (short) value; }
                    public int getValue() { return cx; }
                    public boolean isActive() { return isCentroidSupported(); }
                },
                new Property("Centroid CY") {
                    public void setValue(int value) { cy = (short) value; }
                    public int getValue() { return cy; }
                    public boolean isActive() { return isCentroidSupported(); }
                }
        };
    }

    protected boolean isCentroidSupported() {
        return true;
    }

    protected void setColorRGB565(int r5, int g6, int b5) {
        this.red = r5;
        this.green = g6;
        this.blue = b5;
        this.color = ((r5 * 255 / 31) << 16) | ((g6 * 255 / 63) << 8) | b5 * 255 / 31;
    }

    protected void updateColor() {
        setColorRGB565(red, green, blue);
    }

    public int getStepsToPlace() {
        return 2;
    }

    public boolean isBody() {
        return true;
    }
}
