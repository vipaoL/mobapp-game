// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

public class Lava extends AbstractRectBodyElement {
    public static final int COLOR = 0xff5500;

    public Lava() {
        color = COLOR;
    }

    public short getID() {
        return LAVA;
    }

    public String getName() {
        return "Lava";
    }
}
