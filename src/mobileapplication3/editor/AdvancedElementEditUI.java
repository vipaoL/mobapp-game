// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.ui.*;

public class AdvancedElementEditUI extends AbstractPopupPage {

    private final Element element;
    private List list;
    private final StructureBuilder sb;

    public AdvancedElementEditUI(Element element, StructureBuilder sb, IPopupFeedback parent) {
        super("Advanced edit: " + element.getName(), parent);
        this.element = element;
        this.sb = sb;
    }

    protected Button[] getActionButtons() {
        final short[] argsUnmodified = element.getArgsValues();
        return new Button[] {
            new Button("OK") {
                public void buttonPressed() {
                    if (element.getID() != Element.END_POINT) {
                        sb.recalculateEndPoint();
                    }
                    sb.onUpdate();
                    close();
                }
            },
            new Button("Cancel") {
                public void buttonPressed() {
                    element.setArgs(argsUnmodified);
                    close();
                }
            }.setBindedKeyCode(Keys.KEY_NUM0)
        };
    }

    protected IUIComponent initAndGetPageContent() {
        list = new List() {
            public final void onSetBounds(int x0, int y0, int w, int h) {
                setElementsPadding(getElemH()/16);
                super.onSetBounds(x0, y0, w, h);
            }
        };

        refreshList();

        return list;
    }

    private void refreshList() {
        Property[] properties = element.getArgs();
        IUIComponent[] rows = new IUIComponent[properties.length + 1];
        for (int i = 0; i < properties.length; i++) {
            final Property property = properties[i];
            if (property.getMinValue() != 0 || property.getMaxValue() != 1) {
                rows[i] = new Slider(property);
            } else {
                rows[i] = new ButtonComponent(new Switch(property.getName()) {
                    public boolean getValue() {
                        return property.getValue() == 1;
                    }

                    public void setValue(boolean value) {
                        property.setValue(value ? 1 : 0);
                        refreshList();
                    }

                    public boolean isActive() {
                        return property.isActive();
                    }
                });
            }
        }
        rows[properties.length] = new ButtonComponent(new Button ("Refresh and recalculate values") {
            public void buttonPressed() {
                element.recalcCalculatedArgs();
                refreshList();
            }
        });
        list.setElements(rows);
    }
}
