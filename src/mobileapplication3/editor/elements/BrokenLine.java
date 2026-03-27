// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

/**
 *
 * @author vipaol
 */
public class BrokenLine extends Line {
    protected short thickness = 20, platformLength, spacing = 10, l, ang;

    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
            movePlacementStep,
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    setEndPoint(pointX, pointY);
                    recalcCalculatedArgs();
                }

                public String getName() {
                    return "Move end point";
                }

                public String getCurrentStepInfo() {
                    return "plL=" + platformLength + " ang=" + ang + "; x1=" + x + " y1=" + y + "; x2=" + x2 + " y2=" + y2;
                }
            }
        };
    }

    public void recalcCalculatedArgs() {
        short dx = (short) (x2 - x);
        short dy = (short) (y2 - y);
        if (dy == 0) {
            l = dx;
        } else if (dx == 0) {
            l = dy;
        } else {
            l = calcDistance(dx, dy);
        }
        if (l <= 0) {
            l = 1;
        }
        short optimalPlatfL = 260;
        platformLength = optimalPlatfL;
        if (platformLength > l) {
            platformLength = l;
        } else {
            short platfL1 = platformLength;
            while ((l + spacing) % (platformLength + spacing) != 0 & platformLength < l & (l + spacing) % (platfL1 + spacing) != 0) {
                platformLength++;
                if (platfL1 > 5)
                    platfL1--;
            }
            if ((l + spacing) % (platformLength + spacing) == 0) {
                platfL1 = platformLength;
            }
            platformLength = platfL1;
        }
        if (platformLength <= 0)
            platformLength = l;
        ang = (short) Mathh.arctg(dx, dy);
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int dx = x2 - x;
        int dy = y2 - y;

        int n = (l + spacing) / (platformLength + spacing);

        int spX = spacing * dx / l;
        int spY = spacing * dy / l;

        int platfDx = (dx+spX) / n;
        int platfDy = (dy+spY) / n;

        g.setColor(getColor(drawAsSelected));

        for (int i = 0; i < n; i++) {
            g.drawLine(
                    xToPX(x + i * platfDx, zoomOut, offsetX),
                    yToPX(y + i * platfDy, zoomOut, offsetY),
                    xToPX(x + (i + 1) * platfDx - spX, zoomOut, offsetX),
                    yToPX(y + (i + 1) * platfDy - spY, zoomOut, offsetY),
                    thickness,
                    zoomOut,
                    true,
                    true,
                    false,
                    false);
        }
    }

    public Element setArgs(short[] args) {
        x = args[0];
        y = args[1];
        x2 = args[2];
        y2 = args[3];
        thickness = args[4];
        platformLength = args[5];
        spacing = args[6];
        l = args[7];
        ang = args[8];
        return this;
    }

    public short[] getArgs() {
        return new short[]{x, y, x2, y2, thickness, platformLength, spacing, l, ang};
    }

    public Property[] getProperties() {
        return concatArrays(super.getProperties(), new Property[]{
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
                        return (short) (platformLength * 2);
                    }
                },
                new Property("Platform length") {
                    public void setValue(int value) {
                        platformLength = (short) value;
                    }

                    public int getValue() {
                        return platformLength;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public boolean isActive() {
                        return false;
                    }
                },
                new Property("Spacing") {
                    public void setValue(int value) {
                        spacing = (short) value;
                    }

                    public int getValue() {
                        return spacing;
                    }

                    public int getMinValue() {
                        return 0;
                    }
                },
                new Property("Length") {
                    public void setValue(int value) {
                        l = (short) value;
                    }

                    public int getValue() {
                        return l;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public boolean isActive() {
                        return false;
                    }
                },
                new Property("Angle") {
                    public void setValue(int value) {
                        ang = (short) value;
                    }

                    public int getValue() {
                        return ang;
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

    public short getID() {
        return Element.BROKEN_LINE;
    }

    public String getName() {
        return "Broken Line";
    }

    public boolean isBody() {
        return true;
    }
}
