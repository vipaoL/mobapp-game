// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public class EndPoint extends Element {
    public static final int COLOR = 0xff0000;
    public static final int R = 3;

    public EndPoint() {
        color = COLOR;
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        g.setColor(getColor(drawAsSelected));
        g.fillArc(xToPX(x, zoomOut, offsetX) - R, yToPX(y, zoomOut, offsetY) - R, R*2, R*2, 0, 360);
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
        return Element.END_POINT;
    }

    public String getName() {
        return "End point";
    }

    public short[] getStartPoint() {
        return new short[] {x, y};
    }

    public short[] getEndPoint() throws Exception {
        throw new Exception("Never ask end point its end point");
    }

    public static boolean compare(short[] oldEndPoint, short[] newEndPoint) {
        short oldX = oldEndPoint[0];
        short oldY = oldEndPoint[1];
        short newX = newEndPoint[0];
        short newY = newEndPoint[1];
        return (newX >= oldX) && (newX > oldX || newY > oldY);
    }

    public static short[] compareAsEndPoints(short[] a, short[] b) {
        if (compare(a, b)) {
            return b;
        } else {
            return a;
        }
    }

    public static short[] findEndPoint(Element[] elements) {
        short[] endPoint = {0, 0};
        short[] potentialEndPoint;
        for (int i = 1; i < elements.length; i++) {
            try {
                potentialEndPoint = elements[i].getEndPoint();
                if (EndPoint.compare(endPoint, potentialEndPoint)) {
                    endPoint = potentialEndPoint;
                }
            } catch (Exception ex) {
                Logger.log(ex);
            }
        }
        return endPoint;
    }

    public boolean isBody() {
        return false;
    }

    public void recalcCalculatedArgs() { }
}
