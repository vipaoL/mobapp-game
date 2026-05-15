// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Resources;
import mobileapplication3.ui.*;

import java.io.IOException;

public class BulkResaverUI extends AbstractPopupPage {
    public static final String OUTPUT_FOLDER = "ResaverOutput/";
    private static final String INITIAL_STATUS = "Click 'Re-save' to re-save all built-in structures and levels to " + OUTPUT_FOLDER;
    private boolean isWorking = false;
    private TextComponent statusComponent;

    public BulkResaverUI(IPopupFeedback parent) {
        super("Bulk Re-saver", parent);
    }

    protected Button[] getActionButtons() {
        return new Button[] {
            new Button("Re-save") {
                public void buttonPressed() {
                    if (!isWorking) {
                        new Thread(new Runnable() {
                            public void run() {
                                doResave();
                            }
                        }).start();
                    }
                }
            },
            new BackButton(feedback)
        };
    }

    protected IUIComponent initAndGetPageContent() {
        statusComponent = new TextComponent(INITIAL_STATUS);
        statusComponent.setBgColor(COLOR_TRANSPARENT);
        return statusComponent;
    }

    private void doResave() {
        isWorking = true;

        String gamePath = EditorSettings.getGameFolderPath();
        String outputDir = gamePath + OUTPUT_FOLDER;

        try {
            FileUtils.createFolder(outputDir);
            FileUtils.checkFolder(outputDir);
        } catch (IOException e) {
            Logger.log("Folder error: " + e);
            updateStatus("Error: Could not create/access output folder.");
            isWorking = false;
            return;
        }

        // Re-save structures
        String structPrefix = "/s";
        String structExt = ".mgstruct";
        int structCount = Resources.countSequentialResources(structPrefix, structExt);

        for (int i = 0; i < structCount; i++) {
            String name = structPrefix.substring(1) + (i + 1) + structExt;
            updateStatus("Re-saving structure " + (i + 1) + "/" + structCount);
            resave(MGStructs.RESOURCE_PREFIX + FileUtils.SEP + name, outputDir + name, EditorUI.MODE_STRUCTURE);
        }

        // Re-save levels
        String levelPrefix = "/l";
        String levelExt = ".mglvl";
        int levelCount = Resources.countSequentialResources(levelPrefix, levelExt);

        for (int i = 0; i < levelCount; i++) {
            String name = levelPrefix.substring(1) + (i + 1) + levelExt;
            updateStatus("Re-saving level " + (i + 1) + "/" + levelCount);
            resave(MGStructs.RESOURCE_PREFIX + FileUtils.SEP + name, outputDir + name, EditorUI.MODE_LEVEL);
        }

        updateStatus("Re-saving finished! Check " + outputDir);
        isWorking = false;
    }

    private void resave(String resPath, String savePath, int mode) {
        try {
            Element[] elements = MGStructs.readMGStruct(resPath);
            if (elements != null) {
                StructureBuilder sb = new StructureBuilder(mode) {
                    public void onUpdate() {}
                    public void setSelectedInList(int i) {}
                };
                sb.setElements(elements);
                sb.saveToFile(savePath);
                Logger.log("Re-saved: " + resPath + " -> " + savePath);
            } else {
                Logger.log("Failed to load: " + resPath);
            }
        } catch (Exception e) {
            Logger.log("Error re-saving " + resPath + ": " + e);
        }
    }

    private void updateStatus(String status) {
        if (statusComponent != null) {
            statusComponent.setText(status);
        }
        Logger.log(status);
    }
}
