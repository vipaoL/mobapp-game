// SPDX-License-Identifier: GPL-3.0-or-later

package utils;

import mobileapplication3.game.GraphicsWorld;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Settings;
import mobileapplication3.ui.GraphicsUtils;
import mobileapplication3.ui.IUIComponent;

public class MobappGameSettings {
    private static final String
            RECORD_STORE_SETTINGS = "gamesettings",
            GRAPHICS_FOR_HIRES = "btrGr",
            LANDSCAPE_COLOR = "landscapeCol",
            LEGACY_DRAWING_METHOD = "oldDrawing",
            CAMERA_ROTATION = "camRotation",
            PHYSICS_PRECISION = "PhyPrecision",
            DETAIL_LEVEL = "DetailLvl",
            FRAME_TIME = "FrameTime",
            SHOW_FPS = "showFPS",
            SHOW_BG = "enBG",
            BOTTOM_BUTTONS = "bottBtns",
            BATTERY_INDICATOR = "Batt";

    public static final int DYNAMIC_PHYSICS_PRECISION = -1, AUTO_PHYSICS_PRECISION = 0;
    public static final int
            DEFAULT_PHYSICS_PRECISION = AUTO_PHYSICS_PRECISION,
            DEFAULT_DETAIL_LEVEL = 1,
            DEFAULT_FRAME_TIME = 16;
    public static final int MAX_PHYSICS_PRECISION = 16, MAX_DETAIL_LEVEL = 3, MAX_FRAME_TIME = 100;

    public static final int
            CAMERA_ROTATION_STATIC = 0,
            CAMERA_ROTATION_DONT_FLIP = 1,
            CAMERA_ROTATION_FULL = 2,
            CAMERA_ROTATION_MIN_VALUE = CAMERA_ROTATION_STATIC,
            CAMERA_ROTATION_DEFAULT_VALUE = CAMERA_ROTATION_STATIC,
            CAMERA_ROTATION_MAX_VALUE = CAMERA_ROTATION_FULL;

    public static final int LANDSCAPE_COLOR_RGB = Integer.MAX_VALUE - 1;
    public static final int LANDSCAPE_COLOR_RGB_WITH_BG = Integer.MAX_VALUE;

    private static String mgstructsFolderPath = null;
    private static String detailLevel = Settings.UNDEF;
    private static int landscapeColor = IUIComponent.NOT_SET;
    public static boolean RGBMode = false;

    private static Settings settingsInst = null;

    private MobappGameSettings() { }

    private static Settings getSettingsInst() {
        if (settingsInst == null) {
            settingsInst = new Settings(new String[]{
                    GRAPHICS_FOR_HIRES,
                    LANDSCAPE_COLOR,
                    LEGACY_DRAWING_METHOD,
                    CAMERA_ROTATION,
                    PHYSICS_PRECISION,
                    DETAIL_LEVEL,
                    FRAME_TIME,
                    SHOW_FPS,
                    SHOW_BG,
                    BOTTOM_BUTTONS,
                    BATTERY_INDICATOR
                }, RECORD_STORE_SETTINGS);
        }
        return settingsInst;
    }

    public static void setAutoSaveEnabled(boolean autoSaveEnabled) {
        getSettingsInst().setAutoSaveEnabled(autoSaveEnabled);
    }

    public static void save() {
        getSettingsInst().saveToDisk();
    }

    public static boolean isBattIndicatorEnabled() {
        return getSettingsInst().getBool(BATTERY_INDICATOR);
    }

    public static boolean isBattIndicatorEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(BATTERY_INDICATOR, defaultValue);
    }

    public static void setBattIndicatorEnabled(boolean b) {
        getSettingsInst().set(BATTERY_INDICATOR, b);
    }

    public static boolean toggleBattIndicator() {
        return getSettingsInst().toggleBool(BATTERY_INDICATOR);
    }

    ///

    public static boolean isBGEnabled() {
        return getSettingsInst().getBool(SHOW_BG);
    }

    public static boolean isBGEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(SHOW_BG, defaultValue);
    }

    public static void setBGEnabled(boolean b) {
        getSettingsInst().set(SHOW_BG, b);
    }

    public static boolean toggleBG() {
        return getSettingsInst().toggleBool(SHOW_BG);
    }

    ///

    public static boolean isFPSShown() {
        return getSettingsInst().getBool(SHOW_FPS);
    }

    public static boolean isFPSShown(boolean defaultValue) {
        return getSettingsInst().getBool(SHOW_FPS, defaultValue);
    }

    public static void setFPSShown(boolean b) {
        getSettingsInst().set(SHOW_FPS, b);
    }

    public static boolean toggleFPSShown() {
        return getSettingsInst().toggleBool(SHOW_FPS);
    }

    ///

    public static boolean isBetterGraphicsEnabled() {
        return getSettingsInst().getBool(GRAPHICS_FOR_HIRES);
    }

    public static boolean isBetterGraphicsEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(GRAPHICS_FOR_HIRES, defaultValue);
    }

    public static void setBetterGraphicsEnabled(boolean b) {
        getSettingsInst().set(GRAPHICS_FOR_HIRES, b);
    }

    public static boolean toggleBetterGraphics() {
        return getSettingsInst().toggleBool(GRAPHICS_FOR_HIRES);
    }

    ///

    public static int getLandscapeColorSetting() {
        if (landscapeColor == IUIComponent.NOT_SET) {
            landscapeColor = getSettingsInst().getInt(LANDSCAPE_COLOR, GraphicsWorld.DEFAULT_LANDSCAPE_COLOR);
        }

        return landscapeColor;
    }

    public static int getLandscapeColor() {
        int landscapeColor = getLandscapeColorSetting();
        if (landscapeColor <= 0xffffff) {
            RGBMode = false;
            return landscapeColor;
        } else {
            RGBMode = true;
            GraphicsWorld.bgOverride = landscapeColor == LANDSCAPE_COLOR_RGB_WITH_BG;

            double maxSpeed = 360.0 / 1000.0;
            double minSpeed = 360.0 / 10000.0;

            double baseSpeed = (maxSpeed + minSpeed) / 2.0;
            double speedAmp = (maxSpeed - minSpeed) / 2.0;

            int cycleDuration = 120000;
            double n = 2 * Math.PI / cycleDuration;

            long millis = System.currentTimeMillis();
            int hue = (int) ((baseSpeed * millis + (speedAmp / n) * Math.sin(n * millis)) % 360);

            if (hue < 0) {
                hue += 360;
            }

            return GraphicsUtils.HSVToRGB(hue, 1, 1);
        }
    }

    public static void setLandscapeColor(int value) {
        landscapeColor = value;
        getSettingsInst().set(LANDSCAPE_COLOR, String.valueOf(value));
    }

    ///

    public static int getPhysicsPrecision() {
        return getSettingsInst().getInt(PHYSICS_PRECISION, DEFAULT_PHYSICS_PRECISION);
    }

    public static void setPhysicsPrecision(int value) {
        getSettingsInst().set(PHYSICS_PRECISION, String.valueOf(value));
    }

    ///

    public static int getDetailLevel() {
        int value;
        if (detailLevel.equals(Settings.UNDEF)) {
            value = getSettingsInst().getInt(DETAIL_LEVEL, DEFAULT_DETAIL_LEVEL);
            detailLevel = String.valueOf(value);
        } else {
            value = Integer.valueOf(detailLevel).intValue();
        }
        return value;
    }

    public static void setDetailLevel(int value) {
        detailLevel = String.valueOf(value);
        getSettingsInst().set(DETAIL_LEVEL, String.valueOf(value));
    }

    ///

    public static int getFrameTime() {
        return Mathh.constrain(1, getSettingsInst().getInt(FRAME_TIME, DEFAULT_FRAME_TIME), MAX_FRAME_TIME);
    }

    public static void setFrameTime(int valueMs) {
        getSettingsInst().set(FRAME_TIME, String.valueOf(Mathh.constrain(1, valueMs, MAX_FRAME_TIME)));
    }

    ///

    public static boolean isLegacyDrawingMethodEnabled() {
        return getSettingsInst().getBool(LEGACY_DRAWING_METHOD);
    }

    public static boolean isLegacyDrawingMethodEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(LEGACY_DRAWING_METHOD, defaultValue);
    }

    public static void setLegacyDrawingMethodEnabled(boolean b) {
        getSettingsInst().set(LEGACY_DRAWING_METHOD, b);
    }

    public static boolean toggleLegacyDrawingMethod() {
        return getSettingsInst().toggleBool(LEGACY_DRAWING_METHOD);
    }

    ///

    public static boolean buttonsAtTheBottom() {
        return getSettingsInst().getBool(BOTTOM_BUTTONS);
    }

    public static boolean buttonsAtTheBottom(boolean defaultValue) {
        return getSettingsInst().getBool(BOTTOM_BUTTONS, defaultValue);
    }

    public static void setButtonsAtTheBottom(boolean b) {
        getSettingsInst().set(BOTTOM_BUTTONS, b);
    }

    public static boolean toggleButtonsAtTheBottom() {
        return getSettingsInst().toggleBool(BOTTOM_BUTTONS);
    }

    ///

    public static int getCameraRotationMode() {
        return Mathh.constrain(
                CAMERA_ROTATION_MIN_VALUE,
                getSettingsInst().getInt(CAMERA_ROTATION, CAMERA_ROTATION_DEFAULT_VALUE),
                CAMERA_ROTATION_MAX_VALUE
        );
    }

    public static void setCameraRotationMode(int mode) {
        mode = Mathh.constrain(CAMERA_ROTATION_MIN_VALUE, mode, CAMERA_ROTATION_MAX_VALUE);
        getSettingsInst().set(CAMERA_ROTATION, String.valueOf(mode));
    }

    public static void toggleCameraRotationMode() {
        setCameraRotationMode((getCameraRotationMode() + 1) % (CAMERA_ROTATION_MAX_VALUE + 1));
    }

    ///
}
