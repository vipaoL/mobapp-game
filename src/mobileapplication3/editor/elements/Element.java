// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

/**
 *
 * @author vipaol
 */
public abstract class Element {
    public static final short EOF = 0;
    public static final short END_POINT = 1;
    public static final short LINE = 2;
    public static final short CIRCLE = 3;
    public static final short BROKEN_LINE = 4;
    public static final short BROKEN_CIRCLE = 5;
    public static final short SINE = 6;
    public static final short ACCELERATOR = 7;
    public static final short TRAMPOLINE = 8;
    public static final short LEVEL_START = 9;
    public static final short LEVEL_FINISH = 10;
    public static final short LAVA = 11;
    public static final short SQUARE_BODY = 12;
    public static final short ROUND_BODY = 13;
    public static final short SINE_FACE_UP = 14;
    public static final short SINE_FACE_DOWN = 15;
    public static final short LINE_FACE_UP = 16;
    public static final short LINE_FACE_DOWN = 17;
    public static final short CIRCLE_FACE_OUTSIDE = 18;
    public static final short CIRCLE_FACE_INSIDE = 19;

    public static final int LINE_THICKNESS = 24;

    public static final int COLOR_LANDSCAPE = 0x4444ff;
    public static final int COLOR_BODY = 0xffffff;
    public static final int COLOR_SELECTED = 0xaaffff;

    protected static final int NO_ARROWS = 0;
    protected static final int ARROWS_NORMAL = 1;
    protected static final int ARROWS_INVERTED = -1;

    protected short x, y;
    protected int color;
    protected int colorSelected;

    protected Property xProp = new Property("X") {
        public void setValue(int value) {
            setX((short) value);
        }

        public int getValue() {
            return getX();
        }
    };

    protected Property yProp = new Property("Y") {
        public void setValue(int value) {
            setY((short) value);
        }

        public int getValue() {
            return getY();
        }
    };

    protected Element() {
        color = isBody() ? COLOR_BODY : COLOR_LANDSCAPE;
        colorSelected = COLOR_SELECTED;
    }

    public static Element createTypedInstance(short id) throws IllegalArgumentException {
        if (id < 1) {
            throw new IllegalArgumentException("Element id can't be < 1");
        }

        switch (id) {
            case Element.END_POINT:
                return new EndPoint();
            case Element.LINE:
            case Element.LINE_FACE_UP:
            case Element.LINE_FACE_DOWN:
                return new Line().setID(id);
            case Element.CIRCLE:
            case Element.CIRCLE_FACE_OUTSIDE:
            case Element.CIRCLE_FACE_INSIDE:
                return new Circle().setID(id);
            case Element.BROKEN_LINE:
                return new BrokenLine();
//            case Element.BROKEN_CIRCLE: // Not implemented yet
//                return new BrokenCircle();
            case Element.SINE:
            case Element.SINE_FACE_UP:
            case Element.SINE_FACE_DOWN:
                return new Sine().setID(id);
            case Element.ACCELERATOR:
                return new Accelerator();
            case Element.TRAMPOLINE:
                return new Trampoline();
            case Element.LEVEL_START:
                return new LevelStart();
            case Element.LEVEL_FINISH:
                return new LevelFinish();
            case Element.LAVA:
                return new Lava();
            case Element.SQUARE_BODY:
                return new SquareBody();
            case Element.ROUND_BODY:
                return new RoundBody();
            default:
                Logger.log("Unknown id: " + id);
                return null;
        }
    }

    public void printDebug() {
        short[] args = getArgs();
        StringBuffer sb = new StringBuffer();
        sb.append("id=").append(getID());
        for (int i = 0; i < args.length; i++) {
            sb.append(" ").append(args[i]);
        }
        Logger.log(sb.toString());
    }

    public int xToPX(int c, int zoomOut, int offsetX) {
        return c * 1000 / zoomOut + offsetX;
    }

    public int yToPX(int c, int zoomOut, int offsetY) {
        return c * 1000 / zoomOut + offsetY;
    }

    public static short calcDistance(short dx, short dy) {
        return Mathh.calcDistance(dx, dy);
    }

    public int getArgsCount() {
        return getArgs().length;
    }

    public int getDataLengthInShorts() {
        return 1 + getArgsCount(); // id + args
    }

    public short[] getAsShortArray() {
        short[] args = getArgs();
        short[] arr = new short[args.length + 1];
        arr[0] = getID();
        System.arraycopy(args, 0, arr, 1, args.length);
        return arr;
    }

    public int getStepsToPlace() {
        return 1;
    }

    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        setPos(pointX, pointY);
                    }

                    public String getName() {
                        return "Move";
                    }

                    public String getCurrentStepInfo() {
                        return "x=" + x + " y=" + y;
                    }
                }
        };
    }

    public PlacementStep[] getExtraEditingSteps() {
        return new PlacementStep[0];
    }

    public final PlacementStep[] getAllSteps() {
        PlacementStep[] placementSteps = getPlacementSteps();
        PlacementStep[] editSteps = getExtraEditingSteps();
        PlacementStep[] allSteps = new PlacementStep[placementSteps.length + editSteps.length];
        System.arraycopy(placementSteps, 0, allSteps, 0, placementSteps.length);
        System.arraycopy(editSteps, 0, allSteps, placementSteps.length, editSteps.length);
        return allSteps;
    }

    public Property[] getProperties() {
        return new Property[] {
                xProp,
                yProp,
        };
    }

    public Element clone() {
        if (getID() == END_POINT || getID() == LEVEL_START) {
            return null;
        }

        Element clone = Element.createTypedInstance(getID());
        clone.setArgs(getArgs());
        return clone;
    }

    public String toString() {
        return getName();
    }

    protected int getColor(boolean isSelected) {
        return isSelected ? colorSelected : color;
    }

    public static short[] concatArrays(short[] arr1, short[] arr2) {
        short[] result = new short[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static Property[] concatArrays(Property[] arr1, Property[] arr2) {
        Property[] result = new Property[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static PlacementStep[] concatArrays(PlacementStep[] arr1, PlacementStep[] arr2) {
        PlacementStep[] result = new PlacementStep[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public final void setX(short x) {
        setPos(x, getY());
    }

    public final void setY(short y) {
        setPos(getX(), y);
    }

    public void setPos(short x, short y) {
        this.x = x;
        this.y = y;
        recalcCalculatedArgs();
    }

    public final void move(short dx, short dy) {
        setPos((short) (x + dx), (short) (y + dy));
    }

    public static void move(Element[] elements, int dx, int dy) {
        for (int i = 0; i < elements.length; i++) {
            elements[i].move((short) dx, (short) dy);
        }
    }

    protected static void drawLineWithArrow(Graphics g, int x1, int y1, int x2, int y2, int arrowsDirection, int zoomOut, boolean drawThickness) {
        g.drawLine(x1, y1, x2, y2, LINE_THICKNESS, zoomOut, drawThickness, true, true, true);
        if (arrowsDirection != NO_ARROWS) {
            int dx = x2 - x1;
            int dy = y2 - y1;
            if (arrowsDirection == ARROWS_INVERTED) {
                dx = -dx;
                dy = -dy;
            }
            int l = Mathh.calcDistance(dx, dy);
            int centerX = (x1 + x2) / 2;
            int centerY = (y1 + y2) / 2;
            int lzoomout = l * zoomOut;
            g.drawArrow(centerX, centerY, centerX + dy * 100000 / lzoomout, centerY - dx * 100000 / lzoomout, LINE_THICKNESS/6, zoomOut, drawThickness);
        }
    }

    public abstract void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected);

    public abstract Element setArgs(short[] args);

    public abstract short[] getArgs();

    public abstract short getID();

    public abstract String getName();

    public abstract short[] getStartPoint();

    public abstract short[] getEndPoint() throws Exception;

    public abstract boolean isBody();

    public abstract void recalcCalculatedArgs();

    public abstract static class PlacementStep {
        public abstract void place(short pointX, short pointY);
        public abstract String getName();
        public abstract String getCurrentStepInfo();
    }
}
