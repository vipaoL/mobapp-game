// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
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
        Logger.log("getting grid content: " + files.length + " files");
        try {
            for (int i = 0; i < files.length; i++) {
                final String filePath = getPath() + files[i];
                try {
                    gridContentVector.addElement(new EditorFileListCell(filePath) {
                        public void openInEditor() {
                            StructuresMenu.this.openInEditor(filePath);
                        }
                    });
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
                final String path = MGStructs.RESOURCE_PREFIX + PREFIX + (i + 1) + EXTENSION;
                Logger.log("Loading " + path);
                try {
                    EditorFileListCell cell = new EditorFileListCell(path) {
                        public void openInEditor() {
                            StructuresMenu.this.openInEditor(path);
                        }
                    };
                    cell.shiftBgHue(COLOR_OFFSET_FOR_BUILT_IN);
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
            buttons[i] = new Button(name) {
                public void buttonPressed() {
                    openInEditor(getPath() + name);
                }
            };
        }
        for (int i = 0; i < builtinStructuresCount; i++) {
            String name = PREFIX + (i + 1) + EXTENSION;
            final String path = MGStructs.RESOURCE_PREFIX + name;
            Button button = new Button(name.substring(1) + " (built-in)") {
                public void buttonPressed() {
                    openInEditor(path);
                }
            };
            button.setBgColor(GraphicsUtils.shiftHue(button.getBgColor(), COLOR_OFFSET_FOR_BUILT_IN));
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
}
