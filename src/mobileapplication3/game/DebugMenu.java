// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Sound;
import mobileapplication3.platform.ui.RootContainer;
import utils.MobappGameSettings;

/**
 *
 * @author vipaol
 */
public class DebugMenu extends GenericMenu implements Runnable {
    public static final String GAMING_MODE_SETTING_STR = "GAMING MODE";
    private static final String[] MENU_OPTS = {
        "Enable debug",
        "Show log",
        "Structure debug",
        "Simulation mode",
        GAMING_MODE_SETTING_STR,
        "Physics precision",
        "Music",
        "Back"
    };

    public static boolean isDebugEnabled = false;
    public static boolean closerWorldgen = false;
    public static boolean coordinates = false;
    public static boolean gamingMode = false;
    public static boolean speedo = false;
    public static boolean cheat = false;
    public static boolean music = false;
    public static boolean showFontSize = false;
    public static boolean mgstructOnly = false;
    public static boolean dontCountFlips = false;
    public static boolean showAngle = false;
    public static boolean showLinePoints = false;
    public static boolean simulationMode = false;
    public static final boolean whatTheGame = false;
    public static boolean showContacts = false;
    public static boolean structureDebug = false;

    private Thread thread;

    public DebugMenu() {
        loadParams(MENU_OPTS);
        loadStatemap(new int[MENU_OPTS.length]);

        repaintOnlyOnFlushGraphics = true;
    }

    public void postInit() {
        setSpecialOption(4);
        refreshStates();
        thread = new Thread(this, "debug menu");
        thread.start();
    }

    public void run() {
        long sleep;
        long start;

        while (!isStopped) {
            if (!isPaused) {
                start = System.currentTimeMillis();

                if (gamingMode) {
                    setSpecialOptnActColor(MobappGameSettings.getLandscapeColor());
                }

                onPaint(getUGraphics(), x0, y0, w, h, false);
                flushGraphics();
                tick();

                sleep = MIN_FRAME_TIME - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                sleep = 200;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void selectPressed() {
        int selected = this.selected;
        switch (selected) {
            case 0:
                isDebugEnabled = !isDebugEnabled;
                coordinates = isDebugEnabled;
                Logger.logToStdout(isDebugEnabled);
                break;
            case 1:
                RootContainer.enableOnScreenLog = !RootContainer.enableOnScreenLog;
                if (RootContainer.enableOnScreenLog) {
                    Logger.enableOnScreenLog(h);
                    Logger.log(w + "x" + h);
                } else {
                    Logger.disableOnScreenLog();
                }
                break;
            case 2:
                structureDebug = !structureDebug;
                break;
            case 3:
                simulationMode = !simulationMode;
                break;
            case 4:
                gamingMode = !gamingMode;
                MENU_OPTS[4] = gamingMode ? "<- Check landscape colors" : GAMING_MODE_SETTING_STR;
                break;
            case 5:
                int value = MobappGameSettings.getPhysicsPrecision();
                if (value == MobappGameSettings.AUTO_PHYSICS_PRECISION) {/*
                    value = MobappGameSettings.DYNAMIC_PHYSICS_PRECISION;
                } else if (value == MobappGameSettings.DYNAMIC_PHYSICS_PRECISION) {*/
                    value = 1;
                } else {
                    value *= 2;
                    if (value > MobappGameSettings.MAX_PHYSICS_PRECISION) {
                        value = MobappGameSettings.AUTO_PHYSICS_PRECISION;
                    }
                }
                MobappGameSettings.setPhysicsPrecision(value);
                break;
            case 6:
                music = !music;
                if (music) {
                    Sound sound = new Sound();
                    sound.start();
                }   break;
            default:
                break;
        }
        if (selected == MENU_OPTS.length - 1) {
            stop();
            RootContainer.setRootUIComponent(new SettingsScreen());
        } else {
            refreshStates();
        }
    }

    private void stop() {
        isStopped = true;
        try {
            thread.join();
        } catch (InterruptedException ignored) { }
    }

    void refreshStates() {
        int physicsPrecision = MobappGameSettings.getPhysicsPrecision();
        MENU_OPTS[5] = "Physics precision: ";
        if (physicsPrecision == MobappGameSettings.AUTO_PHYSICS_PRECISION) {
            MENU_OPTS[5] += "Auto";
        } else if (physicsPrecision == MobappGameSettings.DYNAMIC_PHYSICS_PRECISION) {
            MENU_OPTS[5] += "Dynamic";
        } else {
            MENU_OPTS[5] += String.valueOf(physicsPrecision);
        }
        setEnabledFor(DebugMenu.isDebugEnabled, 0);
        setEnabledFor(RootContainer.enableOnScreenLog, 1);
        setEnabledFor(structureDebug, 2);
        setEnabledFor(simulationMode, 3);
        setIsSpecialOptnActivated(gamingMode);
        setEnabledFor(physicsPrecision != MobappGameSettings.DEFAULT_PHYSICS_PRECISION, 5);
        setStateFor(/*music*/GenericMenu.STATE_INACTIVE, 6); // disable this option. it's not ready yet
    }
}
