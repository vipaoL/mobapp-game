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
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    int dx = pointX - x1;
                    int dy = pointY - y1;
                    setStartPoint(pointX, pointY);
                    setEndPoint((short) (x2 + dx), (short) (y2 + dy));
                }

                public String getName() {
                    return "Move";
                }

                public String getCurrentStepInfo() {
                    return "x1=" + x1 + " y1=" + y1;
                }
            },
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    setEndPoint(pointX, pointY);
                    recalcCalculatedArgs();
                }

                public String getName() {
                    return "Move end point";
                }

                public String getCurrentStepInfo() {
                    return "plL=" + platformLength + " ang=" + ang + "; x1=" + x1 + " y1=" + y1 + "; x2=" + x2 + " y2=" + y2;
                }
            }
        };
    }

    public void recalcCalculatedArgs() {
        short dx = (short) (x2 - x1);
        short dy = (short) (y2 - y1);
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

    public PlacementStep[] getExtraEditingSteps() {
        return super.getExtraEditingSteps();
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int n = (l + spacing) / (platformLength + spacing);

        int spX = spacing * dx / l;
        int spY = spacing * dy / l;

        int platfDx = (dx+spX) / n;
        int platfDy = (dy+spY) / n;

        g.setColor(getColor(drawAsSelected));

        for (int i = 0; i < n; i++) {
            g.drawLine(
                    xToPX(x1 + i * platfDx, zoomOut, offsetX),
                    yToPX(y1 + i * platfDy, zoomOut, offsetY),
                    xToPX(x1 + (i + 1) * platfDx - spX, zoomOut, offsetX),
                    yToPX(y1 + (i + 1) * platfDy - spY, zoomOut, offsetY),
                    thickness,
                    zoomOut,
                    true,
                    true,
                    false,
                    false);
        }
    }

    public Element setArgs(short[] args) {
        x1 = args[0];
        y1 = args[1];
        x2 = args[2];
        y2 = args[3];
        thickness = args[4];
        platformLength = args[5];
        spacing = args[6];
        l = args[7];
        ang = args[8];
        return this;
    }

    public short[] getArgsValues() {
        return new short[]{x1, y1, x2, y2, thickness, platformLength, spacing, l, ang};
    }

    public Property[] getArgs() {
        Property[] superArgs = super.getArgs();
        Property[] thisArgs = {
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
        };
        Property[] args = new Property[superArgs.length + thisArgs.length];
        System.arraycopy(superArgs, 0, args, 0, superArgs.length);
        System.arraycopy(thisArgs, 0, args, superArgs.length, thisArgs.length);
        return args;
    }

    public short getID() {
        return Element.BROKEN_LINE;
    }

    public int getStepsToPlace() {
        return 2;
    }

    public String getName() {
        return "Broken Line";
    }

    public boolean isBody() {
        return true;
    }
}
