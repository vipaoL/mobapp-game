// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import mobileapplication3.platform.ui.RootContainer;
import utils.MobappGameSettings;

public class OtherSettingsScreen extends GenericMenu {
    private static final int
            BOTTOM_BUTTONS = 0,
            RESTART_BUTTON = 1,
            STRUCTURES_AUTOLOAD = 2,
            INSTANT_RESTART = 3,
            SINGLE_KEY_NAV = 4,
            BACK = 5;

    private static final String[] menuOpts = new String[BACK + 1];

    // array with states of all buttons (active/inactive/enabled)
    private final int[] statemap = new int[menuOpts.length];

    public OtherSettingsScreen() {
        loadParams(menuOpts);
        loadStatemap(statemap);
    }

    public void init() {
        refreshStates();
    }

    void selectPressed() {
        int selected = this.selected;
        switch (selected) {
            case BOTTOM_BUTTONS:
                MobappGameSettings.toggleButtonsAtTheBottom();
                break;
            case RESTART_BUTTON:
                MobappGameSettings.toggleRestartButton();
                break;
            case STRUCTURES_AUTOLOAD:
                MobappGameSettings.toggleStructuresAutoload();
                break;
            case INSTANT_RESTART:
                MobappGameSettings.toggleInstantRestart();
                break;
            case SINGLE_KEY_NAV:
                singleKeyNav = MobappGameSettings.toggleSingleKeyNav();
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
        menuOpts[BOTTOM_BUTTONS] = "Buttons at the bottom";
        menuOpts[RESTART_BUTTON] = "Restart button";
        menuOpts[STRUCTURES_AUTOLOAD] = "Autoload structures";
        menuOpts[INSTANT_RESTART] = "Instant level restart";
        menuOpts[SINGLE_KEY_NAV] = "Single key navigation";
        menuOpts[BACK] = "Back";

        setEnabledFor(MobappGameSettings.buttonsAtTheBottom(), BOTTOM_BUTTONS);
        setEnabledFor(MobappGameSettings.isRestartButtonEnabled(), RESTART_BUTTON);
        setEnabledFor(MobappGameSettings.structuresAutoload(), STRUCTURES_AUTOLOAD);
        setEnabledFor(MobappGameSettings.isInstantRestartEnabled(), INSTANT_RESTART);
        setEnabledFor(MobappGameSettings.isSingleKeyNavEnabled(), SINGLE_KEY_NAV);
    }
}