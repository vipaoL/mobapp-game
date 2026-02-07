// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.ui.Property;

public class Trampoline extends AbstractRectBodyElement {
    public static final int COLOR = 0xffaa00;

    private short elasticity = 100;

    public Trampoline() {
        color = COLOR;
    }

    public Element setArgs(short[] args) {
        elasticity = args[5];
        return super.setArgs(args);
    }

    public short[] getArgsValues() {
        return new short[] {x, y, l, thickness, angle, elasticity};
    }

    public Property[] getArgs() {
        return concatArrays(super.getArgs(), new Property[] {
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
                }
        });
    }

    public short getID() {
        return TRAMPOLINE;
    }

    public String getName() {
        return "Trampoline";
    }

}
