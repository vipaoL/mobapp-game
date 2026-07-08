// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import at.emini.physics2D.World;
import at.emini.physics2D.util.PhysicsFileReader;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.IUIComponent;
import utils.GameFileUtils;
import utils.MgStruct;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author vipaol
 */
public class Levels extends GenericMenu {
    private static final String LEVELS_FOLDER_NAME = "MobappGame/Levels";

    private static final String BTN_BACK = "Back";
    private static final String BTN_NEXT = "Next >";
    private static final String BTN_PREV = "< Previous";
    private static final String BTN_LOAD_CUSTOM = "Load custom level";

    private int itemsPerPage = 7;
    private int currentPage = 0;

    private String[] allLevelNames = new String[0];
    private boolean isBuiltinMode = true;

    private String[] levelPaths = new String[0];
    private String[] buttons;

    private int builtinLevelsCount = 0;

    private boolean loadingLevel = false;

    public Levels() {
        Logger.log("Levels:constr");
        builtinLevelsCount = seekForLevelsInRes();
        if (builtinLevelsCount > 0) {
            isBuiltinMode = true;
            allLevelNames = new String[builtinLevelsCount];
            for (int i = 0; i < builtinLevelsCount; i++) {
                allLevelNames[i] = "Level " + (i + 1);
            }
            refreshButtons();
        } else {
            loadCustomLevels();
        }
    }

    protected void loadCanvasParams(int x0, int y0, int w, int h) {
        if (h > 0) {
            int fontHeight = Font.getDefaultFontHeight();
            int newItemsPerPage = Math.max(3, (h / fontHeight) / 3);

            if (newItemsPerPage != itemsPerPage) {
                itemsPerPage = newItemsPerPage;
                this.x0 = x0;
                this.y0 = y0;
                this.w = w;
                this.h = h;

                refreshButtons();
                return;
            }
        }

        super.loadCanvasParams(x0, y0, w, h);
    }

    private void refreshButtons() {
        int totalPages = (allLevelNames.length + itemsPerPage - 1) / itemsPerPage;
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        int startIdx = currentPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, allLevelNames.length);
        int itemsOnPage = endIdx - startIdx;

        boolean hasNext = currentPage < totalPages - 1;
        boolean hasPrev = currentPage > 0;

        int btnCount = 1 + itemsOnPage;
        if (hasNext) {
            btnCount++;
        }
        if (hasPrev) {
            btnCount++;
        }
        if (isBuiltinMode && currentPage == 0) {
            btnCount++;
        }
        if (currentPage == 0) {
            btnCount++;
        }

        buttons = new String[btnCount];
        int idx = 0;

        String str = isBuiltinMode ? "Levels" : "Custom levels / emini worlds";
        if (totalPages > 1) {
            str += " (" + (currentPage + 1) + "/" + totalPages + ")";
        }
        buttons[idx++] = str;

        for (int i = startIdx; i < endIdx; i++) {
            buttons[idx++] = allLevelNames[i];
        }

        if (hasNext) {
            buttons[idx++] = BTN_NEXT;
        }
        if (hasPrev) {
            buttons[idx++] = BTN_PREV;
        }
        if (isBuiltinMode && currentPage == 0) {
            buttons[idx++] = BTN_LOAD_CUSTOM;
        }
        if (currentPage == 0) {
            buttons[idx++] = BTN_BACK;
        }

        loadParams(buttons, 1);
        setFirstReachable(1);
        selected = Mathh.constrain(1, selected, btnCount - 1);

        if (w != 0 && h != 0) {
            super.loadCanvasParams(x0, y0, w, h);
        }
    }

    private void loadCustomLevels() {
        isBuiltinMode = false;
        builtinLevelsCount = 0;
        try {
            levelPaths = getLevels();
            if (levelPaths != null) {
                allLevelNames = new String[levelPaths.length];
                for (int i = 0; i < levelPaths.length; i++) {
                    allLevelNames[i] = getLevelName(levelPaths[i]);
                }
            } else {
                allLevelNames = new String[0];
            }
        } catch (SecurityException ex) {
            Platform.showError("No read permission", ex);
        } catch (Exception ex) {
            Platform.showError(ex);
        }
        currentPage = 0;
        refreshButtons();
    }

    private int seekForLevelsInRes() {
        int c = 0;
        for (int i = 1; tryRes(getLevelResPath(i)); i++) {
            Logger.log(getLevelResPath(i));
            c++;
        }
        return c;
    }

    private static String getLevelResPath(int i) {
        return "/l" + i + ".mglvl";
    }

    public boolean tryRes(String path) {
        InputStream is = null;
        try {
            is = Platform.getResource(path);
            if (is == null) {
                return false;
            }
            DataInputStream dis = new DataInputStream(is);
            dis.readShort();
            return true;
        } catch (Exception ex) {
            Logger.log(path + " " + ex);
            return false;
        } finally {
            try {
                is.close();
            } catch (Exception ignored) { }
        }
    }

    public static synchronized boolean openBuiltinLevel(int i, IUIComponent prevScreen) {
        InputStream is = null;
        try {
            is = Platform.getResource(getLevelResPath(i));
            if (is == null) {
                return false;
            }
            RootContainer.setRootUIComponent(openLevel(new DataInputStream(is), i, prevScreen));
            return true;
        } catch (Exception ex) {
            Platform.showError("Can't open level!", ex);
        } finally {
            try {
                is.close();
            } catch (Exception ignored) { }
        }
        return false;
    }

    public String[] getLevels() {
        Logger.log("Levels:getLevels()");
        return GameFileUtils.listFilesInAllPlaces(LEVELS_FOLDER_NAME);
    }

    private String getLevelName(String path) {
        if (path != null) {
            return path.substring(path.lastIndexOf('/') + 1);
        } else {
            return null;
        }
    }

    public synchronized void openFromFS(final String path) {
        if (loadingLevel) {
            return;
        }
        loadingLevel = true;

        (new Thread(new Runnable() {
            public void run() {
                try {
                    GameplayCanvas gameCanvas = null;
                    if (path.endsWith(".phy")) {
                        gameCanvas = new GameplayCanvas(Levels.this).loadLevel(readWorldFile(path));
                    } else if (path.endsWith(".mglvl")) {
                        gameCanvas = openLevel(path, Levels.this);
                    }
                    if (gameCanvas != null) {
                        RootContainer.setRootUIComponent(gameCanvas);
                    }
                } catch (Exception ex) {
                    Platform.showError(ex);
                } finally {
                    loadingLevel = false;
                }
            }
        })).start();
    }

    private static GameplayCanvas openLevel(String path, IUIComponent prevScreen) {
        return openLevel(FileUtils.fileToDataInputStream(path), -1, prevScreen);
    }

    private static GameplayCanvas openLevel(DataInputStream dis, int i, IUIComponent prevScreen) {
        try {
            short[][] level = MgStruct.readFromDataInputStream(dis);
            if (level != null) {
                return new GameplayCanvas(prevScreen).loadLevel(level, i);
            }
        } catch (IOException ex) {
            Platform.showError(ex);
        }
        return null;
    }

    public GraphicsWorld readWorldFile(String path) {
        PhysicsFileReader reader;
        InputStream is = FileUtils.fileToDataInputStream(path);
        reader = new PhysicsFileReader(is);
        GraphicsWorld w = new GraphicsWorld(World.loadWorld(reader));
        reader.close();
        return w;
    }

    public synchronized void selectPressed() {
        String btnText = buttons[selected];

        if (BTN_BACK.equals(btnText)) {
            RootContainer.setRootUIComponent(new MenuCanvas());
        } else if (BTN_NEXT.equals(btnText)) {
            currentPage++;
            refreshButtons();
            setFocusTo(BTN_NEXT);
        } else if (BTN_PREV.equals(btnText)) {
            currentPage--;
            refreshButtons();
            setFocusTo(BTN_PREV);
        } else if (BTN_LOAD_CUSTOM.equals(btnText)) {
            loadCustomLevels();
        } else {
            if (!loadingLevel) {
                int localIndex = selected - 1;
                int globalIndex = (currentPage * itemsPerPage) + localIndex;

                if (isBuiltinMode) {
                    loadingLevel = true;
                    boolean success = openBuiltinLevel(globalIndex + 1, this);
                    loadingLevel = false;
                    if (!success) {
                        init();
                    }
                } else {
                    if (globalIndex >= 0 && globalIndex < levelPaths.length) {
                        try {
                            openFromFS(levelPaths[globalIndex]);
                        } catch (Exception ex) {
                            Platform.showError(ex);
                        }
                    }
                }
            }
        }
    }

    private void setFocusTo(String targetBtn) {
        for (int i = 0; i < buttons.length; i++) {
            if (targetBtn.equals(buttons[i])) {
                selected = i;
                break;
            }
        }
    }

    public void focusOnBuiltinLevel(int levelId) {
        if (!isBuiltinMode || levelId <= 0 || levelId > builtinLevelsCount) {
            return;
        }
        int globalIndex = levelId - 1;
        if (itemsPerPage > 0) {
            currentPage = globalIndex / itemsPerPage;
            refreshButtons();
            setFocusTo(allLevelNames[globalIndex]);
        }
    }
}