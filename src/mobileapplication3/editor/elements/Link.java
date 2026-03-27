package mobileapplication3.editor.elements;

import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public class Link extends Element {
    private short refOffset;
    private Element reference;

    public short getRefOffset() {
        return refOffset;
    }

    public void setRefOffset(short refOffset) {
        this.refOffset = refOffset;
    }

    public Element getReference() {
        return reference;
    }

    public Link setReference(Element reference) {
        if (reference == this) {
            reference = null;
        }

        short dX = getDX();
        short dY = getDY();
        this.reference = reference;
        setDX(dX);
        setDY(dY);

        return this;
    }

    public short getDX() {
        if (reference != null) {
            return (short) (x - reference.getX());
        } else {
            return x;
        }
    }

    public short getDY() {
        if (reference != null) {
            return (short) (y - reference.getY());
        } else {
            return y;
        }
    }

    public void setDX(short dX) {
        if (reference != null) {
            x = (short) (reference.getX() + dX);
        } else {
            x = dX;
        }
    }

    public void setDY(short dY) {
        if (reference != null) {
            y = (short) (reference.getY() + dY);
        } else {
            y = dY;
        }
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        g.setColor(getColor(drawAsSelected));
        if (reference != null) {
            reference.paint(g, zoomOut, offsetX + getDX() * 1000 / zoomOut, offsetY + getDY() * 1000 / zoomOut, drawThickness, drawAsSelected);
        } else {
            int r = 100;
            int scaledD = r * 2000 / zoomOut;
            int leftX = xToPX(x - r, zoomOut, offsetX);
            int rightX = xToPX(x + r, zoomOut, offsetX);
            int topY = yToPX(y - r, zoomOut, offsetY);
            int bottomY = yToPX(y + r, zoomOut, offsetY);
            g.drawLine(leftX, topY, rightX, bottomY);
            g.drawLine(rightX, topY, leftX, bottomY);
            g.drawRect(leftX, topY, scaledD, scaledD);
        }
    }

    protected int getColor(boolean isSelected) {
        if (reference == null && !isSelected) {
            return 0xff2222;
        } else {
            return super.getColor(isSelected);
        }
    }

    public Element setArgs(short[] args) {
        setDX(args[0]);
        setDY(args[1]);
        refOffset = args[2];
        return this;
    }

    public short[] getArgs() {
        return new short[] {
                getDX(),
                getDY(),
                refOffset,
        };
    }

    public Property[] getProperties() {
        return new Property[] {
                new Property("dX") {
                    public int getValue() {
                        return getDX();
                    }

                    public void setValue(int value) {
                        setDX((short) value);
                    }

                    public boolean isActive() {
                        return reference != null;
                    }
                },
                new Property("dY") {
                    public int getValue() {
                        return getDY();
                    }

                    public void setValue(int value) {
                        setDY((short) value);
                    }

                    public boolean isActive() {
                        return reference != null;
                    }
                },
        };
    }

    public short getID() {
        return LINK;
    }

    public String getName() {
        String name = "Link";
        return name + " to " + ((reference instanceof Link) ? name : String.valueOf(reference));
    }

    public short[] getStartPoint() {
        short[] d = {getDX(), getDY()};
        if (reference == null) {
            return d;
        }
        short[] refStartPoint = reference.getStartPoint();
        return new short[] {(short) (refStartPoint[0] + d[0]), (short) (refStartPoint[1] + d[1])};
    }

    public short[] getEndPoint() throws Exception {
        short[] d = {getDX(), getDY()};
        if (reference == null) {
            return d;
        }
        short[] refEndPoint = reference.getEndPoint();
        return new short[] {(short) (refEndPoint[0] + d[0]), (short) (refEndPoint[1] + d[1])};
    }

    public Element clone() {
        Link link = new Link();
        link.setArgs(getArgs());
        link.setReference(reference);
        return link;
    }

    public Element createLink() {
        Link link = new Link();
        link.setArgs(getArgs());
        link.setReference(this);
        return link;
    }

    public boolean isBody() {
        return reference != null && reference.isBody();
    }

    public void recalcCalculatedArgs() { }
}
