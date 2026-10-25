// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import mobileapplication3.platform.ui.RootContainer;
import utils.MobappGameSettings;

public class GraphicsSettingsScreen extends GenericMenu {
    private static final int
            HI_RES_GRAPHICS = 0,
            LEGACY_DRAWING_METHOD = 1,
            CAMERA_ROTATION_MODE = 2,
            TARGET_FPS = 3,
            BACK = 4;

    public static final int[] FPS_PRESETS = {15, 20, 24, 30, 40, 45, 48, 50, 60, 72, 75, 80, 90, 100, 120, 144, 165, 240, 360, 500, 1000};

    private static final String[] menuOpts = new String[BACK + 1];

    // array with states of all buttons (active/inactive/enabled)
    private final int[] statemap = new int[menuOpts.length];

    public GraphicsSettingsScreen() {
        loadParams(menuOpts);
        loadStatemap(statemap);
    }

    public void init() {
        refreshStates();
    }

    void selectPressed() {
        int selected = this.selected;
        switch (selected) {
            case HI_RES_GRAPHICS:
                MobappGameSettings.toggleBetterGraphics();
                break;
            case LEGACY_DRAWING_METHOD:
                MobappGameSettings.toggleLegacyDrawingMethod();
                break;
            case CAMERA_ROTATION_MODE:
                MobappGameSettings.toggleCameraRotationMode();
                break;
            case TARGET_FPS:
                int i = SettingsScreen.findArrayIndex(FPS_PRESETS, MobappGameSettings.getTargetFPS());
                int nextFPS = FPS_PRESETS[(i + 1) % FPS_PRESETS.length];
                MobappGameSettings.setTargetFPS(nextFPS);
                setTargetFPS(nextFPS);
                break;
            case BACK:
                RootContainer.setRootUIComponent(new SettingsScreen());
                return;
            default:
                break;
        }
        refreshStates();
    }

    void refreshStates() {
        int currentFPS = MobappGameSettings.getTargetFPS();
        int cameraRotationMode = MobappGameSettings.getCameraRotationMode();
        String cameraRotationModeString = "?";
        switch (cameraRotationMode) {
            case MobappGameSettings.CAMERA_ROTATION_STATIC:
                cameraRotationModeString = "Static";
                break;
            case MobappGameSettings.CAMERA_ROTATION_DONT_FLIP:
                cameraRotationModeString = "Stay horizontal";
                break;
            case MobappGameSettings.CAMERA_ROTATION_FULL:
                cameraRotationModeString = "Full";
                break;
        }

        menuOpts[HI_RES_GRAPHICS] = "Graphics for hi-res screens";
        menuOpts[LEGACY_DRAWING_METHOD] = "Legacy drawing method";
        menuOpts[CAMERA_ROTATION_MODE] = "Camera rotation: " + cameraRotationModeString;
        menuOpts[TARGET_FPS] = "Target FPS: " + currentFPS;
        menuOpts[BACK] = "Back";

        setEnabledFor(MobappGameSettings.isBetterGraphicsEnabled(), HI_RES_GRAPHICS);
        setEnabledFor(cameraRotationMode != MobappGameSettings.CAMERA_ROTATION_DEFAULT_VALUE, CAMERA_ROTATION_MODE);

        if (cameraRotationMode == MobappGameSettings.CAMERA_ROTATION_STATIC) {
            setEnabledFor(MobappGameSettings.isLegacyDrawingMethodEnabled(), LEGACY_DRAWING_METHOD);
        } else {
            setStateFor(STATE_INACTIVE, LEGACY_DRAWING_METHOD);
        }

        setEnabledFor(currentFPS != MobappGameSettings.DEFAULT_TARGET_FPS, TARGET_FPS);
    }
}