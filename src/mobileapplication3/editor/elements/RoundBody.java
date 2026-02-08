package mobileapplication3.editor.elements;

import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public class RoundBody extends Body {
    private short r = 1;

    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        x = pointX;
                        y = pointY;
                    }

                    public String getName() {
                        return "Move";
                    }

                    public String getCurrentStepInfo() {
                        return "x=" + x + " y=" + y;
                    }
                },
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        short dx = (short) (pointX - x);
                        short dy = (short) (pointY - y);
                        r = calcDistance(dx, dy);
                    }

                    public String getName() {
                        return "Change radius";
                    }

                    public String getCurrentStepInfo() {
                        return "r=" + r;
                    }
                }
        };
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int r = this.r * 1000 / zoomOut;

        g.setColor(getColor(drawAsSelected));
        g.fillArc(xToPX(x, zoomOut, offsetX) - r, yToPX(y, zoomOut, offsetY) - r, r*2, r*2, 0, 360);
    }

    public Element setArgs(short[] args) {
        x = args[0];
        y = args[1];
        r = args[2];

        parseBodyArgs(args, 3);

        return this;
    }

    public short[] getArgsValues() {
        return concatArrays(new short[] {x, y, r}, getBodyArgsValues());
    }

    public Property[] getArgs() {
        Property[] roundBodyArgs = new Property[] {
                xProp,
                yProp,
                new Property("R") {
                    public void setValue(int value) { r = (short) value; }
                    public int getValue() { return r; }
                    public int getMinValue() { return 1; }
                }
        };

        return concatArrays(roundBodyArgs, getBodyArgs());
    }

    public short getID() {
        return ROUND_BODY;
    }

    public String getName() {
        return "Round body";
    }

    public short[] getStartPoint() {
        return new short[]{(short) (x - r), y};
    }

    public short[] getEndPoint() throws Exception {
        return new short[]{(short) (x + r), y};
    }

    public void recalcCalculatedArgs() { }
}