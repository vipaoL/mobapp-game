// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor;

import mobileapplication3.platform.*;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.*;

import java.io.IOException;

public abstract class AbstractEditorMenu extends AbstractPopupWindow {
    public static final int COLOR_OFFSET_FOR_BUILT_IN = -90;

    private final static int LAYOUT_MINIMIZED = 1, LAYOUT_LIST_OF_NAMES = 2, LAYOUT_GRID = 3;

    private final TextComponent title;
    private final ButtonCol buttons;
    private Grid grid = null;
    private ButtonRow backButtonComponent;
    private String[] files = null;
    private final IPopupFeedback parent;

    private int layout = LAYOUT_MINIMIZED;

    public AbstractEditorMenu(final IPopupFeedback parent, String titleStr) {
        super(parent);
        this.parent = parent;

        title = new TextComponent(titleStr);
        buttons = new ButtonCol();
    }

    public void init() {
        super.init();
        switch (EditorSettings.getWhatToLoadAutomatically()) {
            case EditorSettings.OPTION_ALWAYS_LOAD_NONE:
                setLayout(LAYOUT_MINIMIZED);
                break;
            case EditorSettings.OPTION_ALWAYS_LOAD_LIST:
                setLayout(LAYOUT_LIST_OF_NAMES);
                break;
            case EditorSettings.OPTION_ALWAYS_LOAD_THUMBNAILS:
                setLayout(LAYOUT_GRID);
                break;
        }
    }

    public void setLayout(int layout) {
        this.layout = layout;

        final Button createButton = new Button("Create new") {
            public void buttonPressed() {
                createNew();
            }
        }.setBindedKeyCode(Keys.KEY_NUM1);

        final Button alwaysShowListSwitch = new Switch("Always show list") {
            public void setValue(boolean value) {
                EditorSettings.setWhatToLoadAutomatically(value ? EditorSettings.OPTION_ALWAYS_LOAD_LIST : EditorSettings.OPTION_ALWAYS_LOAD_NONE);
            }

            public boolean getValue() {
                return EditorSettings.getWhatToLoadAutomatically() >= EditorSettings.OPTION_ALWAYS_LOAD_LIST;
            }
        }.setBindedKeyCode(Keys.KEY_NUM2);

        final Button alwaysShowGridSwitch = new Switch("Always show thumbnails") {
            public void setValue(boolean value) {
                EditorSettings.setWhatToLoadAutomatically(value ? EditorSettings.OPTION_ALWAYS_LOAD_THUMBNAILS : EditorSettings.OPTION_ALWAYS_LOAD_LIST);
            }

            public boolean getValue() {
                return EditorSettings.getWhatToLoadAutomatically() >= EditorSettings.OPTION_ALWAYS_LOAD_THUMBNAILS;
            }
        }.setBindedKeyCode(Keys.KEY_NUM2);

        final Button showGridButton = new Button("Show thumbnails") {
            public void buttonPressed() {
                setLayout(LAYOUT_GRID);
            }
        }.setBindedKeyCode(Keys.KEY_NUM3);

        final Button openInFileManager = new Button("Open in system file manager") {
            public void buttonPressed() {
                Platform.platformRequest("file://" + getPath());
            }
        };

        final BackButton backButton = new BackButton(parent);
        backButtonComponent = new ButtonRow(new Button[]{backButton}).bindToSoftButtons();

        switch (layout) {
            case LAYOUT_MINIMIZED:
                buttons.setButtons(new Button[] {
                        createButton,
                        new Button("Open") {
                            public void buttonPressed() {
                                setLayout(LAYOUT_LIST_OF_NAMES);
                            }
                        }.setBindedKeyCode(Keys.KEY_NUM2),
                        new ButtonStub(),
                        new BackButton(parent).setBindedKeyCodes(new int[] {Keys.KEY_SOFT_LEFT, Keys.KEY_SOFT_RIGHT, Keys.KEY_NUM0})
                });
                setComponents(new IUIComponent[]{title, buttons});
                break;
            case LAYOUT_LIST_OF_NAMES:
                Button[] fileButtons = getList();
                int topExtraButtons = 4;
                int bottomExtraButtons = 0;
                Button[] btns = new Button[topExtraButtons + fileButtons.length + bottomExtraButtons];
                btns[0] = createButton;
                btns[1] = alwaysShowListSwitch;
                btns[2] = showGridButton;
                btns[3] = openInFileManager.setBindedKeyCode(Keys.KEY_NUM4);
                System.arraycopy(fileButtons, 0, btns, topExtraButtons, fileButtons.length);
                buttons.setButtons(btns);
                setComponents(new IUIComponent[]{title, buttons, backButtonComponent});
                break;
            case LAYOUT_GRID:
                IUIComponent[] thumbnails = getGridContent();
                int topExtraCells = 3;
                IUIComponent[] cells = new IUIComponent[topExtraCells + thumbnails.length];
                cells[0] = new ButtonComponent(createButton);
                cells[1] = new ButtonComponent(alwaysShowGridSwitch);
                cells[2] = new ButtonComponent(openInFileManager).setBindedKeyCode(Keys.KEY_NUM3);
                for (int i = 0; i < thumbnails.length; i++) {
                    cells[topExtraCells + i] = thumbnails[i];
                }
                grid = new Grid(cells);
                setComponents(new IUIComponent[]{title, grid, backButtonComponent});
                break;
        }
    }

    protected void onSetBounds(int x0, int y0, int w, int h) {
        title
                .setSize(w, TextComponent.HEIGHT_AUTO)
                .setPos(x0, y0, TOP | LEFT);

        switch (layout) {
            case LAYOUT_MINIMIZED:
                buttons
                        .setButtonsBgPadding(w/512)
                        .setSize(w/2, (y0 + h - title.getBottomY()))
                        .setPos(x0 + w/2, y0 + h, BOTTOM | HCENTER);
                break;
            case LAYOUT_LIST_OF_NAMES:
                backButtonComponent
                        .setSize(w, ButtonRow.H_AUTO)
                        .setPos(x0 + w/2, y0 + h, HCENTER | BOTTOM);
                buttons
                        .setButtonsBgPadding(w/512)
                        .setSize(w, backButtonComponent.getTopY() - title.getBottomY())
                        .setPos(x0 + w/2, backButtonComponent.getTopY(), BOTTOM | HCENTER);
                break;
            case LAYOUT_GRID:
                backButtonComponent
                        .setSize(w, ButtonRow.H_AUTO)
                        .setPos(x0 + w/2, y0 + h, HCENTER | BOTTOM);
                grid
                        .setCols(Mathh.constrain(1, w/Font.getDefaultFontHeight()/6, 5))
                        .setElementsPadding(w/128)
                        .setSize(w, backButtonComponent.getTopY() - title.getBottomY())
                        .setPos(x0 + w/2, backButtonComponent.getTopY(), BOTTOM | HCENTER);
                break;
        }
    }

    protected String[] listFiles(String path) throws IOException {
        if (files == null) {
            files = FileUtils.list(path);
        }
        return files;
    }

    // TODO: move to Utils (after moving Utils to framework)
    protected void sortByName(String[] files) {
        if (files == null || files.length <= 1) {
            return;
        }
        for (int i = 1; i < files.length; i++) {
            String current = files[i];
            int j = i - 1;
            while (j >= 0 && compareStringsNatural(files[j], current) > 0) {
                files[j + 1] = files[j];
                j--;
            }
            files[j + 1] = current;
        }
    }

    private int compareStringsNatural(String s1, String s2) {
        int i1 = 0, i2 = 0;
        int len1 = s1.length();
        int len2 = s2.length();

        while (i1 < len1 && i2 < len2) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);

            boolean isDigit1 = (c1 >= '0' && c1 <= '9');
            boolean isDigit2 = (c2 >= '0' && c2 <= '9');

            if (isDigit1 && isDigit2) {
                int start1 = i1;
                while (i1 < len1 && (s1.charAt(i1) >= '0' && s1.charAt(i1) <= '9')) i1++;
                int end1 = i1;

                int start2 = i2;
                while (i2 < len2 && (s2.charAt(i2) >= '0' && s2.charAt(i2) <= '9')) i2++;
                int end2 = i2;

                while (start1 < end1 - 1 && s1.charAt(start1) == '0') start1++;
                while (start2 < end2 - 1 && s2.charAt(start2) == '0') start2++;

                int numLen1 = end1 - start1;
                int numLen2 = end2 - start2;

                if (numLen1 != numLen2) {
                    return numLen1 - numLen2;
                }

                for (int k = 0; k < numLen1; k++) {
                    char d1 = s1.charAt(start1 + k);
                    char d2 = s2.charAt(start2 + k);
                    if (d1 != d2) {
                        return d1 - d2;
                    }
                }
            } else {
                if (c1 != c2) {
                    c1 = Character.toLowerCase(c1);
                    c2 = Character.toLowerCase(c2);
                    if (c1 != c2) {
                        return c1 - c2;
                    }
                }
                i1++;
                i2++;
            }
        }
        return len1 - len2;
    }

    protected abstract Button[] getList();
    protected abstract IUIComponent[] getGridContent();
    protected abstract void createNew();
    protected abstract String getPath();

    protected abstract static class EditorFileListCell extends Container {
        protected String path;
        private final StructureViewerComponent structureViewer;
        private final TextComponent fileNameLabel;

        public EditorFileListCell(String path) {
            this.path = path;
            structureViewer = new StructureViewerComponent(MGStructs.readMGStruct(path));
            structureViewer.setBgColor(COLOR_TRANSPARENT);
            String[] tmp = Utils.split(path, String.valueOf(FileUtils.SEP));
            fileNameLabel = new TextComponent(tmp[tmp.length - 1]);
            setBgColor(COLOR_ACCENT_MUTED);
            roundBg(true);
            setComponents(new IUIComponent[]{structureViewer, fileNameLabel});
        }

        protected void shiftBgHue(int offset) {
            setBgColor(GraphicsUtils.shiftHue(getBgColor(), offset));
            fileNameLabel.setBgColor(GraphicsUtils.shiftHue(fileNameLabel.getBgColor(), offset));
        }

        protected void onSetBounds(int x0, int y0, int w, int h) {
            fileNameLabel.setSize(w, TextComponent.HEIGHT_AUTO).setPos(x0, y0 + h, TextComponent.BOTTOM | TextComponent.LEFT);
            structureViewer.setSize(w, fileNameLabel.getTopY() - y0).setPos(x0, y0, TextComponent.TOP | TextComponent.LEFT);
        }

        public boolean canBeFocused() {
            return true;
        }

        public boolean pointerClicked(int x, int y) {
            openInEditor();
            return true;
        }

        public boolean keyPressed(int keyCode, int count) {
            if (RootContainer.getAction(keyCode) == Keys.FIRE) {
                openInEditor();
            }
            return true;
        }

        protected abstract void openInEditor();
    }
}
