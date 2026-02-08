// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import at.emini.physics2D.UserData;

/**
 *
 * @author vipaol
 */
public class MUserData implements UserData {
    public static final int TYPE_BODY = 0;
    public static final int TYPE_LEVEL_FINISH = 1;

    public static final int STATIC = -1;

    public static final int COLOR_DEFAULT = 0xffffff;
    public static final int COLOR_LAVA = 0xff5500;
    public static final int COLOR_LEVEL_FINISH = 0x00ff00;

    public String string;
    public int i = 1;
    public final int bodyType;
    private short[] effect;
    private int color = COLOR_DEFAULT;
    private int colorStroke = COLOR_DEFAULT;
    private int fallDelay = STATIC;
    private boolean isLava = false;

    public MUserData(int bodyType) {
        this.bodyType = bodyType;
        switch (bodyType) {
            case TYPE_LEVEL_FINISH:
                setColor(COLOR_LEVEL_FINISH);
                setColorStroke(COLOR_LEVEL_FINISH);
                break;
        }
    }

    public MUserData() {
            bodyType = TYPE_BODY;
    }

    public UserData copy() {
            MUserData newUserData = new MUserData(bodyType);
            if (effect != null) {
                short[] effect = new short[this.effect.length];
                System.arraycopy(this.effect, 0, effect, 0, this.effect.length);
                newUserData.setEffect(effect);
            }
            newUserData.setColor(color);
            newUserData.setFallDelay(fallDelay);
            return newUserData;
        }

    public UserData createNewUserData(String string, int i) {
        MUserData mUserData = new MUserData();
        mUserData.i = i;
        mUserData.string = string;
        return mUserData;
    }

    public int getFallDelay() {
        return fallDelay;
    }

    public MUserData setFallDelay(int fallDelay) {
        this.fallDelay = fallDelay;
        return this;
    }

    public int getColor() {
        return color;
    }

    public MUserData setColor(int color) {
        this.color = color;
        return this;
    }

    public MUserData setColor(int r, int g, int b) {
        this.color = (r << 16) | (g << 8) | b;
        return this;
    }

    public boolean isLava() {
        return isLava;
    }

    public MUserData setIsLava(boolean isLava) {
        this.isLava = isLava;
        if (isLava) {
            setColor(COLOR_LAVA);
            setColorStroke(COLOR_LAVA);
        }
        return this;
    }

    public short[] getEffect() {
        return effect;
    }

    public void setEffect(short[] effect) {
        this.effect = effect;
    }

    public int getColorStroke() {
        return colorStroke;
    }

    public void setColorStroke(int colorStroke) {
        this.colorStroke = colorStroke;
    }
}