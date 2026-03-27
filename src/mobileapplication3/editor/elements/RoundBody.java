package mobileapplication3.editor.elements;

import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public class RoundBody extends Body {
    private short r = 1;

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int r = this.r * 1000 / zoomOut;
        g.setColor(getColor(drawAsSelected));
        g.fillArc(xToPX(x, zoomOut, offsetX) - r, yToPX(y, zoomOut, offsetY) - r, r*2, r*2, 0, 360);
    }

    public PlacementStep[] getPlacementSteps() {
        return concatArrays(super.getPlacementSteps(), new PlacementStep[]{
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
        });
    }

    public Element setArgs(short[] args) {
        setPos(args[0], args[1]);
        r = args[2];

        parseBodyArgs(args, 3);

        return this;
    }

    public short[] getArgs() {
        return concatArrays(new short[] {x, y, r}, getBodyArgsValues());
    }

    public Property[] getProperties() {
        return concatArrays(concatArrays(super.getProperties(), new Property[] {
                new Property("R") {
                    public void setValue(int value) { r = (short) value; }
                    public int getValue() { return r; }
                    public int getMinValue() { return 1; }
                }
        }), getBodyProperties());
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
