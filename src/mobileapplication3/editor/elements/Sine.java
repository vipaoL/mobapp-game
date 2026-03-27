// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.ui.Property;

/**
 *
 * @author vipaol
 */
public class Sine extends AbstractCurve {
    //        #
    //      #
    //  .  #    "." - (x0;y0) in the file
    //    #
    //  @       "@" - (x;y)

    private final static int STEP = 30;
    private short id = SINE;
    private short l, halfPeriods = 1, offset = 270, amp;

    public PlacementStep[] getPlacementSteps() {
        PlacementStep[] steps = {
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        setLength((short) (pointX - x));
                        int offset = Mathh.sin(-Sine.this.offset);
                        int amp = (y - pointY);
                        if (offset != 0) {
                            amp = amp * 1000 / offset;
                        }
                        setAmplitude((short) (amp / 2));
                    }

                    public String getName() {
                        return "Change length and amplitude";
                    }

                    public String getCurrentStepInfo() {
                        return "l=" + l;
                    }
                },
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        int dx = pointX - x;
                        if (dx * l > 0) {
                            setHalfPeriodsNumber((short) Math.max(1, l / dx));
                        }
                    }

                    public String getName() {
                        return "Change number of half-periods";
                    }

                    public String getCurrentStepInfo() {
                        return "half-periods=" + halfPeriods;
                    }
                }
        };
        return concatArrays(super.getPlacementSteps(), steps);
    }

    public PlacementStep[] getExtraEditingSteps() {
        return new PlacementStep[] {
                new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        setOffset((short) ((pointX - x) * halfPeriods * 180 / l + 90));
                    }

                    public String getName() {
                        return "Change phase shift";
                    }

                    public String getCurrentStepInfo() {
                        return "offset=" + offset;
                    }
                }
        };
    }

    private short getY0() {
        return (short) (y - amp * Mathh.sin(-offset) / 1000);
    }

    private void setY0(short y0) {
        setY((short) (y0 + amp * Mathh.sin(-offset) / 1000));
    }

    public void setLength(short l) {
        if (this.l == l) {
            return;
        }
        pointsCache = null;
        this.l = l;
    }

    public void setHalfPeriodsNumber(short n) {
        if (halfPeriods == n) {
            return;
        }
        pointsCache = null;
        halfPeriods = n;
    }

    public void setOffset(short offset) throws IllegalArgumentException {
        if (this.offset == offset) {
            return;
        }
        pointsCache = null;
        offset = (short) Mathh.normalizeAngle(offset);
        this.offset = offset;
    }

    public void setAmplitude(short a) {
        if (amp == a) {
            return;
        }
        pointsCache = null;
        amp = a;
    }

    public Element setArgs(short[] args) {
        setLength(args[2]);
        setHalfPeriodsNumber(args[3]);
        setOffset((short) -args[4]);
        setAmplitude(args[5]);
        setX(args[0]);
        setY0(args[1]);
        pointsCache = null;
        return this;
    }

    public short[] getArgs() {
        return new short[]{getX(), getY0(), l, halfPeriods, (short) Mathh.normalizeAngle(-offset), amp};
    }

    public Property[] getProperties() {
        return concatArrays(super.getProperties(), new Property[]{
                new Property("Length") {
                    public void setValue(int value) {
                        setLength((short) value);
                    }

                    public int getValue() {
                        return l;
                    }
                },
                new Property("Half-periods") {
                    public void setValue(int value) {
                        setHalfPeriodsNumber((short) value);
                    }

                    public int getValue() {
                        return halfPeriods;
                    }

                    public int getMinValue() {
                        return 1;
                    }

                    public int getMaxValue() {
                        return (short) (l / 64);
                    }
                },
                new Property("Phase shift") {
                    public void setValue(int value) {
                        setOffset((short) value);
                    }

                    public int getValue() {
                        return offset;
                    }

                    public int getMaxValue() {
                        return 360;
                    }

                    public int getMinValue() {
                        return 0;
                    }
                },
                new Property("Amplitude") {
                    public void setValue(int value) {
                        setAmplitude((short) value);
                    }

                    public int getValue() {
                        return amp;
                    }
                },
                new Property("Two-sided") {
                    public void setValue(int value) {
                        id = value == 1 ? SINE : SINE_FACE_UP;
                    }

                    public int getValue() {
                        return id == SINE ? 1 : 0;
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
                        id = value == 1 ? SINE_FACE_DOWN : SINE_FACE_UP;
                    }

                    public int getValue() {
                        return id == SINE_FACE_DOWN ? 1 : 0;
                    }

                    public int getMinValue() {
                        return 0;
                    }

                    public int getMaxValue() {
                        return 1;
                    }

                    public boolean isActive() {
                        return id != SINE;
                    }
                },
        });
    }

    public Sine setID(short id) {
        this.id = id;
        return this;
    }

    public short getID() {
        return id;
    }

    public int getStepsToPlace() {
        return 3;
    }

    public String getName() {
        return "Sine";
    }

    private short[][] getEnds() {
        return new short[][] {
                new short[]{x, y},
                new short[]{(short) (x + l), (short) (getY0() + amp * Mathh.sin(180 * halfPeriods - offset) / 1000)}
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

    protected void genPoints() {
        int y0 = getY0();
        if (amp == 0) {
            pointsCache = new PointsCache(2);
            pointsCache.writePointToCache(x, y0);
            pointsCache.writePointToCache(x + l, y0);
        } else {
            int startA = 360 - offset;
            int endA = 360 + halfPeriods * 180 - offset;
            int a = endA - startA;

            int nextPointX;
            int nextPointY;
            pointsCache = new PointsCache(1 + halfPeriods * 6);
            for (int i = startA; i <= endA; i += STEP) {
                nextPointX = x + (i - startA) * l / a;
                nextPointY = y0 + amp * Mathh.sin(i) / 1000;
                pointsCache.writePointToCache(nextPointX, nextPointY);
            }

            if (a % STEP != 0) {
                nextPointX = x + l;
                nextPointY = y0 + amp * Mathh.sin(endA) / 1000;
                pointsCache.writePointToCache(nextPointX, nextPointY);
            }
        }
    }

    protected int getArrowsDirection() {
        if (id == SINE_FACE_UP) {
            return ARROWS_NORMAL;
        } else if (id == SINE_FACE_DOWN) {
            return ARROWS_INVERTED;
        } else {
            return NO_ARROWS;
        }
    }

    public void recalcCalculatedArgs() { }
}
