// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.Image;
import mobileapplication3.ui.CanvasComponent;
import utils.MobappGameSettings;

import java.util.Random;

public class LevelCompletedScreen extends CanvasComponent {
    private final GameplayCanvas game;
    private Image fadingLine;
    private final int accentColor;

    public LevelCompletedScreen(GameplayCanvas game) {
        this.game = game;
        accentColor = MobappGameSettings.getLandscapeColor();
        setBgColor(COLOR_TRANSPARENT);
    }

    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        int textX = x0 + w / 2;
        int textY = y0 + h / 3;

        Image fadingLine = getFadingLine(w);

        g.setFontSize(Font.SIZE_LARGE);

        for (int y = 0; y < g.getFontHeight(); y++) {
            int dy = y / 4;
            if (dy > 0) {
                y += dy;
            }
            g.drawImage(fadingLine, x0, textY - y, TOP | LEFT);
            g.drawImage(fadingLine, x0, textY + y, TOP | LEFT);
        }
        g.setColor(accentColor);
        g.drawString("Level completed", textX, textY, VCENTER | HCENTER);
    }

    private Image getFadingLine(int w) {
        if (fadingLine != null && fadingLine.getWidth() == w) {
            return fadingLine;
        }
        int[] rgb = new int[w];
        int accentColor = GameplayCanvas.dimColor(this.accentColor, 70);
        for (int x = 0; x < w / 2; x++) {
            int color = accentColor + (((1000 - Mathh.cos(x * 180 / w)) * 255 / 1000) << 24);
            rgb[x] = color;
            rgb[w - 1 - x] = color;
        }
        return fadingLine = Image.createRGBImage(rgb, w, 1, true);
    }

    public boolean canBeFocused() {
        return true;
    }

    protected void onSetBounds(int x0, int y0, int w, int h) { }

    public boolean handlePointerClicked(int x, int y) {
        closePopup();
        game.tryLoadNextLevel();
        return true;
    }

    public boolean handleKeyPressed(int keyCode, int count) {
        closePopup();
        game.tryLoadNextLevel();
        return true;
    }

    public void init() { }
}
