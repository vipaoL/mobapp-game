// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

public class Accelerator extends AbstractRectBodyElement {
    private short directionOffset, m = 150, effectDuration = 30;

    public Accelerator() {
        setModifierValue(m);
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        super.paint(g, zoomOut, offsetX, offsetY, drawThickness, drawAsSelected);
        int vectorX = m * Mathh.cos(angle + 15 + directionOffset) / 1000;
        int vectorY = m * Mathh.sin(angle + 15 + directionOffset) / 1000;
        g.drawArrow(
                xToPX(getX0(), zoomOut, offsetX),
                yToPX(getY0(), zoomOut, offsetY),
                xToPX(getX0() + vectorX, zoomOut, offsetX),
                yToPX(getY0() + vectorY, zoomOut, offsetY),
                thickness / 4,
                zoomOut,
                drawThickness
        );
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
        super.setArgs(args);
        x = (short) (args[0] + thickness / 2 * Mathh.sin(angle) / 1000);
        y = (short) (args[1] - thickness / 2 * Mathh.cos(angle) / 1000);
        directionOffset = args[5];
        setModifierValue(args[6]);
        effectDuration = args[7];
        recalcCalculatedArgs();
        return this;
    }

    public short[] getArgs() {
        int offsetX = -thickness / 2 * Mathh.sin(angle) / 1000;
        int offsetY = thickness / 2 * Mathh.cos(angle) / 1000;
        return new short[] {(short) (x + offsetX), (short) (y + offsetY), l, thickness, angle, directionOffset, m, effectDuration};
    }

    public Property[] getProperties() {
        return concatArrays(super.getProperties(), new Property[] {
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
