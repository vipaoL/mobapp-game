// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Resources;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.GraphicsUtils;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;

import java.io.IOException;
import java.util.Vector;

public class StructuresMenu extends AbstractEditorMenu {
    public static final String PREFIX = "/s";
    public static final String EXTENSION = ".mgstruct";

    private String path = null;

    private final int builtinStructuresCount;

    public StructuresMenu(final IPopupFeedback parent) {
        super(parent, "Structures");
        builtinStructuresCount = Resources.countSequentialResources(PREFIX, EXTENSION);
        Logger.log(builtinStructuresCount + " built-in structures found");
    }

    protected String getPath() {
        if (path == null) {
            path = EditorSettings.getStructsFolderPath();
        }
        return path;
    }

    public IUIComponent[] getGridContent() {
        Vector gridContentVector = new Vector();
        String[] files = { };
        try {
            files = listFiles(getPath());
        } catch (IOException e) {
            Platform.showError(e);
        }
        if (files == null) {
            files = new String[0];
        }
        Logger.log("getting grid content: " + files.length + " files");
        try {
            for (int i = 0; i < files.length; i++) {
                final String name = files[i];
                final String filePath = getPath() + name;
                try {
                    EditorFileListCell cell = new EditorFileListCell(filePath) {
                        public void openInEditor() {
                            StructuresMenu.this.openInEditor(filePath);
                        }
                    };

                    if (isBuiltinName(name)) {
                        String builtinPath = MGStructs.RESOURCE_PREFIX + FileUtils.SEP + name;
                        if (MGStructs.areIdentical(filePath, builtinPath)) {
                            cell.shiftBgHue(COLOR_OFFSET_FOR_BUILT_IN);
                        } else {
                            cell.shiftBgHue(COLOR_OFFSET_FOR_BUILT_IN / 2);
                        }
                    }

                    gridContentVector.addElement(cell);
                } catch (Exception ex) {
                    Logger.log("Can't create StructureViewer:");
                    Logger.log(ex);
                }
            }
        } catch (Exception e) {
            Platform.showError(e);
        }

        try {
            for (int i = 0; i < builtinStructuresCount; i++) {
                String name = PREFIX.substring(1) + (i + 1) + EXTENSION;

                final String path = MGStructs.RESOURCE_PREFIX + FileUtils.SEP + name;
                Logger.log("Loading " + path);
                try {
                    EditorFileListCell cell = new EditorFileListCell(path) {
                        public void openInEditor() {
                            StructuresMenu.this.openInEditor(path);
                        }
                    };

                    boolean overridden = isOverriddenByFiles(name, files);
                    if (overridden) {
                        cell.shiftBgHue(COLOR_OFFSET_FOR_BUILT_IN / 2);
                    } else {
                        cell.shiftBgHue(COLOR_OFFSET_FOR_BUILT_IN);
                    }

                    gridContentVector.addElement(cell);
                    Logger.log("Loaded " + path);
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            Platform.showError(e);
        }

        IUIComponent[] gridContent = new IUIComponent[gridContentVector.size()];
        for (int i = 0; i < gridContentVector.size(); i++) {
            gridContent[i] = (IUIComponent) gridContentVector.elementAt(i);
        }
        Logger.log("Grid: " + gridContent.length + " cells");
        return gridContent;
    }

    public Button[] getList() {
        String[] files = null;
        try {
            files = listFiles(getPath());
        } catch (IOException e) {
            Platform.showError(e);
        }
        if (files == null) {
            files = new String[0];
        }
        Button[] buttons = new Button[files.length + builtinStructuresCount];
        for (int i = 0; i < files.length; i++) {
            final String name = files[i];
            boolean overrides = isBuiltinName(name);

            String buttonText = name;
            int hueShift = 0;
            if (overrides) {
                String builtinPath = MGStructs.RESOURCE_PREFIX + FileUtils.SEP + name;
                if (MGStructs.areIdentical(getPath() + name, builtinPath)) {
                    buttonText += " (overrides, identical)";
                    hueShift = COLOR_OFFSET_FOR_BUILT_IN;
                } else {
                    buttonText += " (overrides)";
                    hueShift = COLOR_OFFSET_FOR_BUILT_IN / 2;
                }
            }

            Button button = new Button(buttonText) {
                public void buttonPressed() {
                    openInEditor(getPath() + name);
                }
            };
            button.setBgColor(GraphicsUtils.shiftHue(button.getBgColor(), hueShift));
            buttons[i] = button;
        }
        for (int i = 0; i < builtinStructuresCount; i++) {
            String name = PREFIX.substring(1) + (i + 1) + EXTENSION;
            final String path = MGStructs.RESOURCE_PREFIX + FileUtils.SEP + name;
            boolean overridden = isOverriddenByFiles(name, files);

            String buttonText = name;
            int hueShift;
            if (overridden) {
                buttonText += " (overridden)";
                hueShift = COLOR_OFFSET_FOR_BUILT_IN / 2;
            } else {
                buttonText += " (built-in)";
                hueShift = COLOR_OFFSET_FOR_BUILT_IN;
            }

            Button button = new Button(buttonText) {
                public void buttonPressed() {
                    openInEditor(path);
                }
            };

            button.setBgColor(GraphicsUtils.shiftHue(button.getBgColor(), hueShift));
            buttons[files.length + i] = button;
        }
        return buttons;
    }

    public void openInEditor(String path) {
        openInEditor(MGStructs.readMGStruct(path), path);
    }

    public void openInEditor(Element[] elements, String path) {
        RootContainer.setRootUIComponent(new EditorUI(EditorUI.MODE_STRUCTURE, elements, path));
    }

    protected void createNew() {
        RootContainer.setRootUIComponent(new EditorUI(EditorUI.MODE_STRUCTURE));
    }

    private boolean isOverriddenByFiles(String name, String[] files) {
        for (int i = 0; i < files.length; i++) {
            if (name.equals(files[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isBuiltinName(String name) {
        if (name == null) return false;
        String prefix = PREFIX.substring(1);
        if (name.startsWith(prefix) && name.endsWith(EXTENSION)) {
            try {
                String numStr = name.substring(prefix.length(), name.length() - EXTENSION.length());
                int id = Integer.parseInt(numStr);
                return id > 0 && id <= builtinStructuresCount;
            } catch (NumberFormatException ignored) { }
        }
        return false;
    }
}