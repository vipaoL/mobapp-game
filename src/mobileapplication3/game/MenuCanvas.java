// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.Keys;
import utils.MgStruct;

/**
 *
 * @author vipaol
 */
public class MenuCanvas extends GenericMenu {

    private final String[] menuOptions = {
            "",
            "Play",
            "Load Structures",
            "Levels",
            "Editor",
            "Records",
            "Settings",
            "Exit",
            ""
    };

    private static int defaultSelected = 1; // currently selected option in menu

    // states
    private boolean isInited = false;
    private boolean isGameStarted = false;
    private int c = 0;

    private GameplayCanvas bg = null;

    private static boolean areExtStructsLoaded = false;

    public MenuCanvas(GameplayCanvas bg) {
        this();
        this.bg = bg;
        targetFPS = 1000 / GameplayCanvas.TICK_DURATION;
    }

    public MenuCanvas() {
        Logger.log("menu:constr");
        // menu initialization
        loadParams(menuOptions, defaultSelected);
        // placeholders
        setFirstReachable(1);
        setLastReachable(menuOptions.length - 2);
    }

    public void init() {
        Logger.log("menu:init");

        if (areExtStructsLoaded) { // highlight and change label of "Ext Structs" btn if it already loaded
            setStateFor(1, 2);
            menuOptions[2] = "Reload";
        }
        try {
            Class.forName("mobileapplication3.editor.Editor");
        } catch (ClassNotFoundException ex) {
            setStateFor(STATE_INACTIVE, 4);
        }
        isInited = true;
    }

    public void tick() {
        super.tick();
        if (c == 1) {
            if (bg != null) {
                bg.startAgain();
                RootContainer.setRootUIComponent(bg);
                bg = null;
            }
        }
    }

    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        try {
            if (bg != null) {
                if (!bg.drawAsBG(g)) {
                    bg = null;
                    setTargetFPS(DEFAULT_FPS);
                }
            }
            if (isInited) {
                int bgColor = this.bgColor;
                if (bg != null) {
                    this.bgColor = COLOR_TRANSPARENT;
                }
                super.onPaint(g, x0, y0, w, h, forceInactive);
                this.bgColor = bgColor;
                tick();
            }
        } catch (Exception ignored) { }
    }

    protected void onSetBounds(int x0, int y0, int w, int h) {
        super.onSetBounds(x0, y0, w, h);
        if (bg != null) {
            bg.setSize(w, h);
        }
    }

    private synchronized void startGame() {
        if (isGameStarted) {
            return;
        }
        isGameStarted = true;
        Logger.log("menu:startGame()");
        repaint();
        try {
            log("menu:new gCanvas");
            GameplayCanvas gameCanvas = new GameplayCanvas();
            log("menu:setting gCanvas displayable");
            RootContainer.setRootUIComponent(gameCanvas);
        } catch (Exception ex) {
            Platform.showError(ex);
            Logger.enableOnScreenLog(h);
            Logger.log("ex in startGame():");
            Logger.log(ex);
            repaint();
            isGameStarted = false;
        }
    }

    public boolean handleKeyPressed(int keyCode, int count) {         // Keyboard
        if (keyCode == Keys.KEY_STAR | keyCode == -10) {
            if (!Logger.isOnScreenLogEnabled()) {
                Logger.enableOnScreenLog(h);
                Logger.log(w + "x" + h);
            } else {
                Logger.disableOnScreenLog();
            }
        } else if (keyCode == Keys.KEY_NUM9) {
            c++;
        } else {
            c = 0;
        }
        return super.handleKeyPressed(keyCode, count);
    }

    public boolean handlePointerClicked(int x, int y) {
        if (x < w / 16 && y < h / 16 && bg != null) {
            c++;
            return true;
        } else {
            c = 0;
            return super.handlePointerClicked(x, y);
        }
    }

    void selectPressed() { // Do something when pressed an option in the menu
        defaultSelected = selected;
        if (selected == 1) { // Play
            startGame();
        }
        if (selected == 2) { // Ext Structs / Reload
            loadMG();
        }
        if (selected == 3) { // Levels
            RootContainer.setRootUIComponent(new Levels());
        }
        if (selected == 4) { // Editor
            try {
                Class.forName("mobileapplication3.editor.Editor").newInstance();
            } catch (Exception ex) {
                Logger.log("Can't open editor: " + ex);
            }
            Logger.log("opened editor");
        }
        if (selected == 5) { // Records
            RootContainer.setRootUIComponent(new RecordsScreen());
        }
        if (selected == 6) { // Settings
            RootContainer.setRootUIComponent(new SettingsScreen());
        }
        if (selected == 7) { // Exit
            Platform.exit();
        }
    }

    private void log(String s) {
        Logger.log(s);
        repaint();
    }

    private void loadMG() {
        (new Thread(new Runnable() {
            public void run() {
                menuOptions[2] = "Loading...";
                setStateFor(1, 2);
                boolean success = false;
                try {
                    success = MgStruct.loadFromFiles();
                } catch (Exception ex) {
                    Logger.log(ex);
                }
                if (success) {
                    areExtStructsLoaded = true;
                    menuOptions[2] = (MgStruct.loadedTotal - MgStruct.loadedFromRes) + " loaded";
                    setColorEnabledOption(0x0099ff00);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) { }
                    menuOptions[2] = "Reload";
                } else {
                    areExtStructsLoaded = false;
                    if (!MgStruct.loadCancelled) {
                        menuOptions[2] = "Nothing loaded";
                    } else {
                        menuOptions[2] = "Cancelled";
                    }
                    setColorEnabledOption(0x00880000);
                }
            }
        })).start();
    }

}
