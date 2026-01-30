// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

public class LevelFinish extends AbstractRectBodyElement {
    public static final int COLOR = 0x00ff00;

    public LevelFinish() {
        color = COLOR;
    }

    public short getID() {
        return LEVEL_FINISH;
    }

    public String getName() {
        return "Level Finish";
    }

}
