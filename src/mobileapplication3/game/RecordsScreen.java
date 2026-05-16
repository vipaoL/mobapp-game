// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Records;
import mobileapplication3.platform.ui.RootContainer;

/**
 *
 * @author vipaol
 */
public class RecordsScreen extends GenericMenu {
    private final String[] buttons;

    public RecordsScreen() {
        int[] records = new int[0];
        try {
            records = Records.getRecords();
        } catch (Exception ex) {
            Platform.showError("Can't get records:", ex);
        }
        buttons = new String[records.length + 2];
        buttons[0] = "Best scores";
        buttons[buttons.length-1] = "Back";
        for (int i = 0; i < records.length; i++) {
            buttons[i + 1] = records[i] + "";
        }
        loadParams(buttons);
        setFirstReachable(buttons.length-1);
    }

    public void selectPressed() {
        if (selected == buttons.length - 1) {
            RootContainer.setRootUIComponent(new MenuCanvas());
        }
    }
}
