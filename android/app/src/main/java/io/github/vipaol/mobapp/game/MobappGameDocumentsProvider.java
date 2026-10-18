package io.github.vipaol.mobapp.game;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.platform.MobappDocumentsProvider;
import mobileapplication3.platform.Platform;

import java.io.File;

public class MobappGameDocumentsProvider extends MobappDocumentsProvider {
    @Override
    protected File getRootDir() {
        return new File(EditorSettings.getGameFolderPath());
    }

    @Override
    protected String getRootTitle() {
        return "mobapp-game " + Platform.getAppVersion(getContext());
    }

    @Override
    protected String getSummary() {
        return "Custom game content";
    }

    @Override
    protected int getRootIconRes() {
        return R.mipmap.ic_launcher;
    }
}