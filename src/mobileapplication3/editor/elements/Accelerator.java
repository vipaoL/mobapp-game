// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public class Accelerator extends AbstractRectBodyElement {
    private short directionOffset, m = 150, effectDuration = 30;

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
                true);
        int vectorX = m * Mathh.cos(angle + 15 + directionOffset) / 1000;
        int vectorY = m * Mathh.sin(angle + 15 + directionOffset) / 1000;
        g.drawArrow(
                xToPX(x, zoomOut, offsetX),
                yToPX(y, zoomOut, offsetY),
                xToPX(x + vectorX, zoomOut, offsetX),
                yToPX(y + vectorY, zoomOut, offsetY),
                thickness / 4,
                zoomOut,
                drawThickness);
    }

    private short[] getZeros() {
        short x = (short) (this.x - l * Mathh.cos(angle) / 2000);
        short y = (short) (this.y - l * Mathh.sin(angle) / 2000);
        return new short[] {x, y};
    }

    private void setZeros(int x, int y, int l, int angle) {
        this.x = (short) (x + l * Mathh.cos(angle) / 2000);
        this.y = (short) (y + l * Mathh.sin(angle) / 2000);
    }

    public void setModifierValue(short value) {
        m = value;

        int colorModifier = (m - 100) * 3;
        int red = Math.min(255, Math.max(0, colorModifier));
        int blue = Math.min(255, Math.max(0, -colorModifier));
        if (red < 50 & blue < 50) {
            red = 50;
            blue = 50;
        }
        color = (red << 16) + blue;
    }

    public Element setArgs(short[] args) {
//      x = args[0]; // will be in the next mgstruct file format
//      y = args[1];
        setZeros(args[0], args[1], args[2], args[4]);
        l = args[2];
        thickness = args[3];
        angle = args[4];
        directionOffset = args[5];
        setModifierValue(args[6]);
        effectDuration = args[7];
        recalcCalculatedArgs();
        return this;
    }

    public short[] getArgsValues() {
        short[] zeros = getZeros();
        short x = zeros[0];
        short y = zeros[1];
        return new short[] {x, y, l, thickness, angle, directionOffset, m, effectDuration};
    }

    public Property[] getArgs() {
        return concatArrays(super.getArgs(), new Property[] {
                new Property("Speed direction offset") {
                    public void setValue(int value) {
                        directionOffset = (short) value;
                    }

                    public int getValue() {
                        return directionOffset;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public int getMaxValue() {
                        return 360;
                    }
                },
                new Property("Speed multiplier (percents)") {
                    public void setValue(int value) {
                        setModifierValue((short) value);
                    }

                    public int getValue() {
                        return m;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public int getMaxValue() {
                        return 1000;
                    }
                },
                new Property("Effect duration (ticks)") {
                    public void setValue(int value) {
                        effectDuration = (short) value;
                    }

                    public int getValue() {
                        return effectDuration;
                    }

                    public int getMinValue() {
                        return 0;
                    };

                    public int getMaxValue() {
                        return 1200;
                    }
                }
        });
    }

    public short getID() {
        return ACCELERATOR;
    }

    public String getName() {
        return "Accelerator";
    }

}
