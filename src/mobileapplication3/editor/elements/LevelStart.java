// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public class LevelStart extends Element {
    public static final int COLOR = 0x00ff00;
    public static final int R = 3;

    public LevelStart() {
        color = COLOR;
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        g.setColor(getColor(drawAsSelected));
        g.fillArc(xToPX(x, zoomOut, offsetX) - R, yToPX(y, zoomOut, offsetY) - R, R *2, R *2, 0, 360);
    }

    public Element setArgs(short[] args) {
        x = args[0];
        y = args[1];
        return this;
    }

    public short[] getArgs() {
        return new short[]{x, y};
    }

    public short getID() {
        return Element.LEVEL_START;
    }

    public String getName() {
        return "Level Start";
    }

    public short[] getStartPoint() {
        return new short[] {x, y};
    }

    public short[] getEndPoint() {
        return new short[] {x, y};
    }

    public boolean isBody() {
        return false;
    }

    public void recalcCalculatedArgs() { }
}
