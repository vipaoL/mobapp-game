// SPDX-License-Identifier: LGPL-2.1-only

package io.github.vipaol.mobapp.game;

import org.robovm.apple.foundation.NSURL;
import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.EditorUI;
import mobileapplication3.editor.MGStructs;
import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.ui.IUIComponent;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileOpenUtil {
    public static IUIComponent handleFileOpenURL(NSURL url) throws FileNotFoundException {
        String fullPath = url.getPath();
        Logger.log("Loading external file: \"" + fullPath + "\"...");

        DataInputStream dis = new DataInputStream(new FileInputStream(fullPath));
        Element[] elements = MGStructs.readMGStruct(dis);
        if (elements == null) {
            elements = new Element[0];
        }

        String name = getFileName(fullPath);

        int mode = name.endsWith(".mgstruct") ? EditorUI.MODE_STRUCTURE : EditorUI.MODE_LEVEL;

        String path = (mode == EditorUI.MODE_STRUCTURE ?
                EditorSettings.getStructsFolderPath() :
                EditorSettings.getLevelsFolderPath())
                + FileUtils.SEP + name;

        return new EditorUI(mode, elements, path).setViewMode(true);
    }

    private static String getFileName(String path) {
        if (path == null) {
            return "null";
        }
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            return path.substring(cut + 1);
        }
        return path;
    }
}
