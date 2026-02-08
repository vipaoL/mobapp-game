// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public class SquareBody extends Body {

    // *############    "*" - (anchorX;anchorY)
    // #     @     #    "@" - (x;y)
    // #############

    protected short l, thickness = 100, angle;
    protected short anchorX, anchorY;

    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        setAnchorPoint(pointX, pointY);
                    }

                    public String getName() {
                        return "Move";
                    }

                    public String getCurrentStepInfo() {
                        return "x=" + anchorX + "y=" + anchorY;
                    }
                },
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        short dx = (short) (pointX - anchorX);
                        short dy = (short) (pointY - anchorY);
                        l = calcDistance(dx, dy);
                        angle = (short) Mathh.arctg(dx, dy);
                        calcCenterPoint();
                    }

                    public String getName() {
                        return "Change length and angle";
                    }

                    public String getCurrentStepInfo() {
                        return "l=" + l + "angle=" + angle;
                    }
                }
        };
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int dx = l * Mathh.cos(angle) / 1000;
        int dy = l * Mathh.sin(angle) / 1000;

        g.setColor(getColor(drawAsSelected));
        g.drawLine(
                xToPX(x - dx/2, zoomOut, offsetX),
                yToPX(y - dy/2, zoomOut, offsetY),
                xToPX(x + dx/2, zoomOut, offsetX),
                yToPX(y + dy/2, zoomOut, offsetY),
                thickness,
                zoomOut,
                true,
                true,
                false,
                false
        );
    }

    public Element setArgs(short[] args) {
        l = args[2];
        thickness = args[3];
        angle = args[4];
        setCenterPoint(args[0], args[1]);

        parseBodyArgs(args, 5);

        return this;
    }

    public short[] getArgsValues() {
        return concatArrays(new short[] {x, y, l, thickness, angle}, getBodyArgsValues());
    }

    public Property[] getArgs() {
        Property[] squareBodyArgs = new Property[] {
                xProp,
                yProp,
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
                        return (short) (l*2);
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
        };

        return concatArrays(squareBodyArgs, getBodyArgs());
    }

    public short getID() {
        return SQUARE_BODY;
    }

    public String getName() {
        return "Square body";
    }

    public short[] getStartPoint() {
        return getCornerPoint(((angle+90)%360 < 180) ? 0 : 2);
    }

    public short[] getEndPoint() {
        return getCornerPoint(((angle+90)%360 < 180) ? 1 : 3);
    }

    public void recalcCalculatedArgs() {
        calcAnchorPoint();
    }

    public void move(short dx, short dy) {
        super.move(dx, dy);
        calcAnchorPoint();
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
                (short) (x + m1 * l * Mathh.cos(angle) / 2000 + m2 * thickness * Mathh.cos(angle + 90) / 2000),
                (short) (y + m1 * l * Mathh.sin(angle) / 2000 + m2 * thickness * Mathh.sin(angle + 90) / 2000)
        };
    }

    private void setCenterPoint(short x, short y) {
        if (x == this.x && y == this.y) {
            return;
        }
        this.x = x;
        this.y = y;
        calcAnchorPoint();
    }

    private void setAnchorPoint(short x, short y) {
        if (x == anchorX && y == anchorY) {
            return;
        }
        anchorX = x;
        anchorY = y;
        calcCenterPoint();
    }

    private void calcCenterPoint() {
        x = (short) (anchorX + l * Mathh.cos(angle) / 2000 + thickness * Mathh.cos(angle + 90) / 2000);
        y = (short) (anchorY + l * Mathh.sin(angle) / 2000 + thickness * Mathh.sin(angle + 90) / 2000);
    }

    private void calcAnchorPoint() {
        anchorX = (short) (x - l * Mathh.cos(angle) / 2000 - thickness * Mathh.cos(angle + 90) / 2000);
        anchorY = (short) (y - l * Mathh.sin(angle) / 2000 - thickness * Mathh.sin(angle + 90) / 2000);
    }
}
