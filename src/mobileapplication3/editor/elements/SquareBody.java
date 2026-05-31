// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.MGStructsCommon;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public class SquareBody extends Body {
    // *############    "*" - (x;y)
    // #     @     #    "@" - (x0;y0) in the file
    // #############

    protected short l, thickness = 100, angle;

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        g.setColor(getColor(drawAsSelected));

        if ((collisionMask & (1 << MGStructsCommon.COLLISION_LAYER_CAR)) != 0) {
            short[] p0 = getCornerPoint(0);
            short[] p1 = getCornerPoint(1);
            short[] p2 = getCornerPoint(2);
            short[] p3 = getCornerPoint(3);

            int x0 = xToPX(p0[0], zoomOut, offsetX);
            int y0 = yToPX(p0[1], zoomOut, offsetY);
            int x1 = xToPX(p1[0], zoomOut, offsetX);
            int y1 = yToPX(p1[1], zoomOut, offsetY);
            int x2 = xToPX(p2[0], zoomOut, offsetX);
            int y2 = yToPX(p2[1], zoomOut, offsetY);
            int x3 = xToPX(p3[0], zoomOut, offsetX);
            int y3 = yToPX(p3[1], zoomOut, offsetY);

            g.drawLine(x0, y0, x1, y1, 10, zoomOut, true, true);
            g.drawLine(x1, y1, x2, y2, 10, zoomOut, true, true);
            g.drawLine(x2, y2, x3, y3, 10, zoomOut, true, true);
            g.drawLine(x3, y3, x0, y0, 10, zoomOut, true, true);
        } else {
            int x0 = getX0();
            int y0 = getY0();
            int dx = l * Mathh.cos(angle) / 1000;
            int dy = l * Mathh.sin(angle) / 1000;

            g.drawLine(
                    xToPX(x0 - dx / 2, zoomOut, offsetX),
                    yToPX(y0 - dy / 2, zoomOut, offsetY),
                    xToPX(x0 + dx / 2, zoomOut, offsetX),
                    yToPX(y0 + dy / 2, zoomOut, offsetY),
                    thickness,
                    zoomOut,
                    true,
                    true,
                    false,
                    false
            );
        }

        int maxDim = Math.max(l, thickness);
        int radiusWorld = maxDim / 2;
        int vectorAngle = (l >= thickness) ? angle : angle + 90;
        drawPhysics(g, zoomOut, xToPX(getX0(), zoomOut, offsetX), yToPX(getY0(), zoomOut, offsetY), radiusWorld, vectorAngle);
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

    public PlacementStep[] getExtraEditingSteps() {
        return new PlacementStep[] {
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        short dx = (short) (pointX - getX0());
                        short dy = (short) (pointY - getY0());

                        cx = (short) ((dx * Mathh.cos(angle) + dy * Mathh.sin(angle)) / 1000);
                        cy = (short) ((dx * Mathh.cos(angle + 90) + dy * Mathh.sin(angle + 90)) / 1000);
                    }

                    public String getName() {
                        return "Set center of mass";
                    }

                    public String getCurrentStepInfo() {
                        return "cX=" + cx + " cY=" + cy;
                    }
                }
        };
    }

    public Element setArgs(short[] args) {
        l = args[2];
        thickness = args[3];
        angle = args[4];

        setX0(args[0]);
        setY0(args[1]);

        parseBodyArgs(args, 5);

        return this;
    }

    public short[] getArgs() {
        return concatArrays(new short[] {getX0(), getY0(), l, thickness, angle}, getBodyArgsValues());
    }

    public Property[] getProperties() {
        return concatArrays(concatArrays(super.getProperties(), new Property[] {
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
        }), getBodyProperties());
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

    private short getX0() {
        return (short) (getX() + l * Mathh.cos(angle) / 2000 + thickness * Mathh.cos(angle + 90) / 2000);
    }

    private short getY0() {
        return (short) (getY() + l * Mathh.sin(angle) / 2000 + thickness * Mathh.sin(angle + 90) / 2000);
    }

    private void setX0(short x0) {
        setX((short) (x0 - l * Mathh.cos(angle) / 2000 - thickness * Mathh.cos(angle + 90) / 2000));
    }

    private void setY0(short y0) {
        setY((short) (y0 - l * Mathh.sin(angle) / 2000 - thickness * Mathh.sin(angle + 90) / 2000));
    }

    public void recalcCalculatedArgs() { }
}
