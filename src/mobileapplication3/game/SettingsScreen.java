// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import mobileapplication3.platform.Battery;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.GraphicsUtils;
import mobileapplication3.ui.IUIComponent;
import utils.MobappGameSettings;

public class SettingsScreen extends GenericMenu {
    private static final int
            LANDSCAPE_COLOR = 0,
            SHOW_FPS = 1,
            BATTERY = 2,
            GRAPHICS_SETTINGS = 3,
            OTHER_SETTINGS = 4,
            PLATFORM_SETTINGS = 5,
            DEBUG = 6,
            ABOUT = 7,
            BACK = 8;

    private static final int[] LANDSCAPE_COLORS = {
            GraphicsWorld.DEFAULT_LANDSCAPE_COLOR,
            0x44aaff, // light blue
            0xaaaaff, // pale blue
            0xffffff, // white
            0xffe100, // yellow
            0x44ffaa, // light green
            0xaaff44, // lime
            0xff44aa, // pink
            0xffaa44, // orange

            MobappGameSettings.LANDSCAPE_COLOR_RGB,
            MobappGameSettings.LANDSCAPE_COLOR_RGB_WITH_BG,
    };

    private static final String[] LANDSCAPE_COLOR_NAMES = {
            "blue",
            "light blue",
            "pale blue",
            "white",
            "yellow",
            "light green",
            "lime",
            "pink",
            "orange",

            "RGB",
            "RGB+background"
    };

    private static final String[] BATTERY_POS_NAMES = {
            "Off",
            "Left",
            "Center",
            "Right"
    };

    private static final String[] menuOpts = new String[BACK + 1];

    // array with states of all buttons (active/inactive/enabled)
    private final int[] statemap = new int[menuOpts.length];
    private boolean batFailed = false;

    public SettingsScreen() {
        loadParams(menuOpts);
        loadStatemap(statemap);
    }

    public void init() {
        setSpecialOption(DEBUG); // highlight "Debug settings" if enabled
        setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);

        refreshStates();

        MobappGameSettings.setAutoSaveEnabled(false);
    }

    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        drawBg(g, x0, y0, w, h, forceInactive);

        // show landscape color
        int color = MobappGameSettings.getLandscapeColor();
        float[] hsv = new float[3];
        GraphicsUtils.RGBToHSV(color, hsv);
        float m = hsv[0];

        int minScreenSide = Math.min(w, h);

        // diameter
        int minD = minScreenSide / 16;
        int rangeD = minScreenSide / 8;
        int d = calculatePosition(m, 4, minD, rangeD);

        // X
        int minX = d / 2;
        int rangeX = w - d;
        int x = calculatePosition(m, 3, minX, rangeX);

        // Y
        int minY = d / 2;
        int rangeY = h - d;
        int y = calculatePosition(m, 5, minY, rangeY);

        g.setColor(color);
        g.fillArc(x - d / 2, y - d / 2, d, d, 0, 360);

        int bgColor = this.bgColor;
        this.bgColor = COLOR_TRANSPARENT;
        super.onPaint(g, x0, y0, w, h, forceInactive);
        this.bgColor = bgColor;
    }

    private static int calculatePosition(float m, int k, int min, int range) {
        int maxM = 360;

        int M = (int) ((m * k + k * k * k) % maxM);
        boolean sign = (M * 2 / maxM) % 2 == 0;

        return min + (range * 2 * ((sign ? M : (maxM - M)) % maxM) / maxM);
    }

    void selectPressed() {
        int selected = this.selected;
        switch (selected) {
            case LANDSCAPE_COLOR:
                nextLandscapeColor();
                break;
            case SHOW_FPS:
                MobappGameSettings.toggleFPSShown();
                break;
            case BATTERY:
                int currentPos = MobappGameSettings.getBatteryPosition();
                if (currentPos == MobappGameSettings.BATTERY_OFF) {
                    if (!Battery.checkAndInit()) {
                        batFailed = true;
                        Logger.log("Battery init failed");
                        break;
                    } else {
                        int batLevel = Battery.getBatteryLevel();
                        if (batLevel == Battery.ERROR) {
                            String err = "Can't get battery level";
                            menuOpts[selected] = err;
                            Logger.log(err);
                            break;
                        } else {
                            Logger.log("bat method: " + Battery.getMethod());
                        }
                    }
                }
                MobappGameSettings.nextBatteryPosition();
                break;
            case GRAPHICS_SETTINGS:
                RootContainer.setRootUIComponent(new GraphicsSettingsScreen());
                return;
            case OTHER_SETTINGS:
                RootContainer.setRootUIComponent(new OtherSettingsScreen());
                return;
            case PLATFORM_SETTINGS:
                stop();
                RootContainer.setRootUIComponent(getPlatformSettings());
                return;
            case DEBUG:
                stop();
                RootContainer.setRootUIComponent(new DebugMenu());
                return;
            case ABOUT:
                stop();
                RootContainer.setRootUIComponent(new AboutScreen());
                return;
            case BACK:
                stop();
                RootContainer.setRootUIComponent(new MenuCanvas());
                return;
            default:
                break;
        }
        refreshStates();
    }

    private void stop() {
        new Thread(new Runnable() {
            public void run() {
                MobappGameSettings.save();
                MobappGameSettings.setAutoSaveEnabled(true);
            }
        }, "save settings").start();
    }

    private void nextLandscapeColor() {
        do {
            int landscapeColor = MobappGameSettings.getLandscapeColorSetting();
            int i = findArrayIndex(LANDSCAPE_COLORS, landscapeColor);
            int newLandscapeColor = LANDSCAPE_COLORS[(i + 1) % LANDSCAPE_COLORS.length];
            MobappGameSettings.setLandscapeColor(newLandscapeColor);
        } while (!DebugMenu.gamingMode && MobappGameSettings.getLandscapeColorSetting() > 0xffffff);
    }

    void refreshStates() {
        menuOpts[LANDSCAPE_COLOR] = "Landscape color: " + LANDSCAPE_COLOR_NAMES[findArrayIndex(LANDSCAPE_COLORS, MobappGameSettings.getLandscapeColorSetting())];
        menuOpts[SHOW_FPS] = "Show FPS";
        menuOpts[GRAPHICS_SETTINGS] = "Graphics settings";
        menuOpts[OTHER_SETTINGS] = "Other settings";
        menuOpts[PLATFORM_SETTINGS] = "Platform settings";
        menuOpts[DEBUG] = "Debug settings";
        menuOpts[ABOUT] = "About";
        menuOpts[BACK] = "Back";

        setEnabledFor(findArrayIndex(LANDSCAPE_COLORS, MobappGameSettings.getLandscapeColor()) != 0, LANDSCAPE_COLOR);
        setEnabledFor(MobappGameSettings.isFPSShown(), SHOW_FPS);

        int batPos = MobappGameSettings.getBatteryPosition();
        menuOpts[BATTERY] = "Battery: " + BATTERY_POS_NAMES[batPos];

        if (!batFailed) {
            setEnabledFor(batPos != MobappGameSettings.BATTERY_OFF, BATTERY);
        } else {
            setStateFor(STATE_INACTIVE, BATTERY);
        }

        try {
            Class.forName("PlatformSettingsScreen");
            menuOpts[PLATFORM_SETTINGS] = String.valueOf(getPlatformSettings());
        } catch (ClassNotFoundException ex) {
            setStateFor(STATE_INACTIVE, PLATFORM_SETTINGS);
        }
    }

    static int findArrayIndex(int[] arr, int a) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == a) {
                return i;
            }
        }
        return 0;
    }

    private static IUIComponent getPlatformSettings() {
        try {
            return (IUIComponent) Class.forName("PlatformSettingsScreen").newInstance();
        } catch (Exception ex) {
            Platform.showError(ex);
            return null;
        }
    }
}