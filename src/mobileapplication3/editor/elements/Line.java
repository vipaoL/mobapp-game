// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

/**
 *
 * @author vipaol
 */
public class Line extends Element {
    private short id = LINE;
    protected short x2, y2;

    PlacementStep movePlacementStep = new PlacementStep() {
        public void place(short pointX, short pointY) {
            setPos(pointX, pointY);
        }

        public String getName() {
            return "Move";
        }

        public String getCurrentStepInfo() {
            return "x1=" + x + " y1=" + y + "; x2=" + x2 + " y2=" + y2;
        }
    };

    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[]{
                movePlacementStep,
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        setEndPoint(pointX, pointY);
                    }

                    public String getName() {
                        return "Move end point";
                    }

                    public String getCurrentStepInfo() {
                        return "x2=" + x2 + " y2=" + y2;
                    }
                }
        };
    }

    public PlacementStep[] getExtraEditingSteps() {
        return new PlacementStep[] {
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        setStartPoint(pointX, pointY);
                    }

                    public String getName() {
                        return "Move start point";
                    }

                    public String getCurrentStepInfo() {
                        return "x1=" + x + " y1=" + y;
                    }
                }
        };
    }

    public int getStepsToPlace() {
        return 2;
    }

    public void setStartPoint(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public void setEndPoint(short x, short y) {
        x2 = x;
        y2 = y;
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int x1 = xToPX(this.x, zoomOut, offsetX);
        int y1 = yToPX(this.y, zoomOut, offsetY);
        int x2 = xToPX(this.x2, zoomOut, offsetX);
        int y2 = yToPX(this.y2, zoomOut, offsetY);

        // collision side indicator
        int arrowsSide;
        if (id == LINE_FACE_UP) {
            arrowsSide = ARROWS_NORMAL;
        } else if (id == LINE_FACE_DOWN) {
            arrowsSide = ARROWS_INVERTED;
        } else {
            arrowsSide = NO_ARROWS;
        }

        g.setColor(getColor(drawAsSelected));
        drawLineWithArrow(g, x1, y1, x2, y2, arrowsSide, zoomOut, drawThickness);
    }

    public Element setArgs(short[] args) {
        x = args[0];
        y = args[1];
        x2 = args[2];
        y2 = args[3];
        return this;
    }

    public short[] getArgs() {
        return new short[]{x, y, x2, y2};
    }

    public Property[] getProperties() {
        return concatArrays(super.getProperties(), new Property[]{
                new Property("X2") {
                    public void setValue(int value) {
                        x2 = (short) value;
                    }

                    public int getValue() {
                        return x2;
                    }
                },
                new Property("Y2") {
                    public void setValue(int value) {
                        y2 = (short) value;
                    }

                    public int getValue() {
                        return y2;
                    }
                },
                new Property("Two-sided") {
                    public void setValue(int value) {
                        id = value == 1 ? LINE : LINE_FACE_UP;
                    }

                    public int getValue() {
                        return id == LINE ? 1 : 0;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public int getMaxValue() {
                        return 1;
                    }
                },
                new Property("Flip collision side") {
                    public void setValue(int value) {
                        id = value == 1 ? LINE_FACE_DOWN : LINE_FACE_UP;
                    }

                    public int getValue() {
                        return id == LINE_FACE_DOWN ? 1 : 0;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public int getMaxValue() {
                        return 1;
                    }

                    public boolean isActive() {
                        return id != LINE;
                    }
                },
        });
    }

    public Line setID(short id) {
        this.id = id;
        return this;
    }

    public short getID() {
        return id;
    }

    public String getName() {
        return "Line";
    }

    public void setPos(short x, short y) {
        x2 += (short) (x - getX());
        y2 += (short) (y - getY());
        super.setPos(x, y);
    }

    private short[][] getEnds() {
        return new short[][] {
                new short[]{x, y},
                new short[]{x2, y2}
        };
    }

    public short[] getStartPoint() {
        short[][] ends = getEnds();
        return StartPointUtils.compareAsStartPoints(ends[0], ends[1]);
    }

    public short[] getEndPoint() {
        short[][] ends = getEnds();
        return EndPoint.compareAsEndPoints(ends[0], ends[1]);
    }

    public boolean isBody() {
        return false;
    }

    public void recalcCalculatedArgs() { }
}
