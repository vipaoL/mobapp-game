// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public abstract class AbstractRectBodyElement extends Element {
    // *############    "*" - (x;y)
    // #     @     #    "@" - (x0;y0) in the file
    // #############

    protected short l, thickness = 20, angle;

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int x0 = getX0();
        int y0 = getY0();
        int dx = l * Mathh.cos(angle) / 1000;
        int dy = l * Mathh.sin(angle) / 1000;
        g.setColor(getColor(drawAsSelected));
        g.drawLine(
                xToPX(x0 - dx/2, zoomOut, offsetX),
                yToPX(y0 - dy/2, zoomOut, offsetY),
                xToPX(x0 + dx/2, zoomOut, offsetX),
                yToPX(y0 + dy/2, zoomOut, offsetY),
                thickness,
                zoomOut,
                true,
                true,
                false,
                false
        );
    }

    public PlacementStep[] getPlacementSteps() {
        return concatArrays(super.getPlacementSteps(), new PlacementStep[]{
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        short dx = (short) (pointX - x);
                        short dy = (short) (pointY - y);
                        l = calcDistance(dx, dy);
                        angle = (short) Mathh.arctg(dx, dy);
                    }

                    public String getName() {
                        return "Change length and angle";
                    }

                    public String getCurrentStepInfo() {
                        return "l=" + l + "angle=" + angle;
                    }
                }
        });
    }

    public int getStepsToPlace() {
        return 2;
    }

    public Element setArgs(short[] args) {
        l = args[2];
        thickness = args[3];
        angle = args[4];

        setX0(args[0]);
        setY0(args[1]);

        return this;
    }

    public short[] getArgs() {
        return new short[] {getX0(), getY0(), l, thickness, angle};
    }

    public Property[] getProperties() {
        return concatArrays(super.getProperties(), new Property[]{
                new Property("L") {
                    public void setValue(int value) {
                        l = (short) value;
                    }

                    public int getValue() {
                        return l;
                    }

                    public int getMinValue() {
                        return 0;
                    }
                },
                new Property("Thickness") {
                    public void setValue(int value) {
                        thickness = (short) value;
                    }

                    public int getValue() {
                        return thickness;
                    }

                    public int getMinValue() {
                        return 1;
                    }

                    public int getMaxValue() {
                        return (short) (l * 2);
                    }
                },
                new Property("Angle") {
                    public void setValue(int value) {
                        angle = (short) value;
                    }

                    public int getValue() {
                        return angle;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public int getMaxValue() {
                        return 360;
                    }
                }
        });
    }

    public short[] getStartPoint() {
        return getCornerPoint(((angle+90)%360 < 180) ? 0 : 2);
    }

    public short[] getEndPoint() {
        return getCornerPoint(((angle+90)%360 < 180) ? 1 : 3);
    }

    public boolean isBody() {
        return true;
    }

    private short[] getCornerPoint(int i) {
        // -- +-
        // -+ ++
        int m1, m2;
        if (i == 0) {
            m1 = m2 = -1;
        } else if (i == 1) {
            m1 = 1;
            m2 = -1;
        } else if (i == 2) {
            m1 = m2 = 1;
        } else {
            m1 = -1;
            m2 = 1;
        }

        return new short[] {
                (short) (getX0() + m1 * l * Mathh.cos(angle) / 2000 + m2 * thickness * Mathh.cos(angle + 90) / 2000),
                (short) (getY0() + m1 * l * Mathh.sin(angle) / 2000 + m2 * thickness * Mathh.sin(angle + 90) / 2000)
        };
    }

    protected short getX0() {
        return (short) (getX() + l * Mathh.cos(angle) / 2000 + thickness * Mathh.cos(angle + 90) / 2000);
    }

    protected short getY0() {
        return (short) (getY() + l * Mathh.sin(angle) / 2000 + thickness * Mathh.sin(angle + 90) / 2000);
    }

    protected void setX0(short x0) {
        setX((short) (x0 - l * Mathh.cos(angle) / 2000 - thickness * Mathh.cos(angle + 90) / 2000));
    }

    protected void setY0(short y0) {
        setY((short) (y0 - l * Mathh.sin(angle) / 2000 - thickness * Mathh.sin(angle + 90) / 2000));
    }

    public void recalcCalculatedArgs() { }
}
