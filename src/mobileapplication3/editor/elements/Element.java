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

    public static final int LINE_THICKNESS = 24;

    public static final int COLOR_LANDSCAPE = 0x4444ff;
    public static final int COLOR_BODY = 0xffffff;
    public static final int COLOR_SELECTED = 0xaaffff;

    protected int color;
    protected int colorSelected;

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
                return new Line();
            case Element.CIRCLE:
                return new Circle();
            case Element.BROKEN_LINE:
                return new BrokenLine();
            case Element.BROKEN_CIRCLE:
                return new BrokenCircle();
            case Element.SINE:
                return new Sine();
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
        short[] args = getArgsValues();
        StringBuffer sb = new StringBuffer();
        sb.append("id="+getID());
        for (int i = 0; i < args.length; i++) {
            sb.append(" " + args[i]);
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
        return getArgsValues().length;
    }

    public int getDataLengthInShorts() {
        return 1 + getArgsCount(); // id + args
    }

    public short[] getAsShortArray() {
        short[] args = getArgsValues();
        short[] arr = new short[args.length + 1];
        arr[0] = getID();
        System.arraycopy(args, 0, arr, 1, args.length);
        return arr;
    }

    public final PlacementStep[] getAllSteps() {
        PlacementStep[] placementSteps = getPlacementSteps();
        PlacementStep[] editSteps = getExtraEditingSteps();
        PlacementStep[] allSteps = new PlacementStep[placementSteps.length + editSteps.length];
        System.arraycopy(placementSteps, 0, allSteps, 0, placementSteps.length);
        System.arraycopy(editSteps, 0, allSteps, placementSteps.length, editSteps.length);
        return allSteps;
    }

    public Element clone() {
        if (getID() == END_POINT || getID() == LEVEL_START) {
            return null;
        }

        Element clone = Element.createTypedInstance(getID());
        clone.setArgs(getArgsValues());
        return clone;
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

    public static void move(Element[] elements, int dx, int dy) {
        for (int i = 0; i < elements.length; i++) {
            elements[i].move((short) dx, (short) dy);
        }
    }

    public abstract PlacementStep[] getPlacementSteps();

    public abstract PlacementStep[] getExtraEditingSteps();

    public abstract void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected);

    public abstract Element setArgs(short[] args);

    public abstract short[] getArgsValues();

    public abstract Property[] getArgs();

    public abstract short getID();

    public abstract int getStepsToPlace();

    public abstract String getName();

    public abstract void move(short dx, short dy);

    public abstract short[] getStartPoint();

    public abstract short[] getEndPoint() throws Exception;

    public abstract boolean isBody();

    public abstract void recalcCalculatedArgs();

    public abstract class PlacementStep {
        public abstract void place(short pointX, short pointY);
        public abstract String getName();
        public abstract String getCurrentStepInfo();
    }

}
