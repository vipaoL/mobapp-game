// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.editor;

import mobileapplication3.editor.elements.*;
import mobileapplication3.editor.elements.StartPointUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.UIComponent;

public class StructureViewerComponent extends UIComponent {

    protected static final int MIN_ZOOM_OUT = 8, MAX_ZOOM_OUT = 200000;

    protected int offsetX, offsetY, zoomOut;
    protected short start, end;

    protected Element[] elements = new Element[0];

    public StructureViewerComponent() {
        setBgColor(COLOR_ACCENT_MUTED);
        setZoomOut(8192);
    }

    public StructureViewerComponent(Element[] elements) {
        this();
        setElements(elements);
    }

    public void postInit() {
        setOptimalZoomAndOffset(w, h);
    }

    public void setElements(Element[] elements) {
        this.elements = elements;
        start = StartPointUtils.findStartPoint(elements)[0];
        end = EndPoint.findEndPoint(elements)[0];
    }

    public boolean canBeFocused() {
        return false;
    }

    protected boolean handlePointerClicked(int x, int y) {
        return false;
    }

    protected boolean handleKeyPressed(int keyCode, int count) {
        return false;
    }

    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        try {
            drawElements(g, x0, y0, elements);
        } catch (Exception ex) {
            g.drawString(ex.toString(), x0, y0, TOP | LEFT);
        }
    }

    protected void drawElements(Graphics g, int x0, int y0, Element[] elements) {
        for (int i = 0; i < elements.length; i++) {
            try {
                elements[i].paint(g, zoomOut, x0 + offsetX, y0 + offsetY, true, false);
            } catch (Exception ex) {
                Logger.log(ex);
            }
        }
    }

    protected void onSetBounds(int x0, int y0, int w, int h) {
        setOptimalZoomAndOffset(w, h);
    }

    protected void setOptimalZoomAndOffset(int w, int h) {
        int zoomOut = Mathh.constrain(MIN_ZOOM_OUT, 4000000 / Math.min(w, h), MAX_ZOOM_OUT);
        zoomOut = Math.max(zoomOut, 1000*(end - start)*3/w/2);
        setZoomOut(zoomOut);
        offsetX = w/2;
        if (zoomOut != 0) {
            offsetX -= (end + start) / 2 * 1000 / zoomOut;
        }
        offsetY = h/2;
    }

    protected void setZoomOut(int zoomOut) {
        this.zoomOut = zoomOut;
    }

    public void centerView(int zoom) {
        this.zoomOut = zoom;

        if (elements == null || elements.length == 0) {
            this.offsetX = w / 2;
            this.offsetY = h / 2;
            return;
        }

        short[] startPoint = StartPointUtils.findStartPoint(elements);
        short[] endPoint = EndPoint.findEndPoint(elements);

        int levelW = Math.abs(endPoint[0] - startPoint[0]);
        int levelH = Math.abs(endPoint[1] - startPoint[1]);

        int viewW = w * this.zoomOut / 1000;
        int viewH = h * this.zoomOut / 1000;

        int focusX;
        int focusY;

        if (levelW <= viewW && levelH <= viewH) {
            focusX = (startPoint[0] + endPoint[0]) / 2;
            focusY = (startPoint[1] + endPoint[1]) / 2;
        } else {
            int count = elements.length;
            long sumX = 0;
            long sumY = 0;
            int validCount = 0;

            int[] elemCX = new int[count];
            int[] elemCY = new int[count];
            boolean[] valid = new boolean[count];

            for (int i = 0; i < count; i++) {
                Element element = elements[i];
                if (element instanceof LevelStart || element instanceof EndPoint) {
                    continue;
                }

                short[] aabb = element.getAABB();
                elemCX[i] = (aabb[0] + aabb[2]) / 2;
                elemCY[i] = (aabb[1] + aabb[3]) / 2;
                valid[i] = true;

                sumX += aabb[0] + aabb[2];
                sumY += aabb[1] + aabb[3];
                validCount++;
            }

            if (validCount == 0) {
                focusX = (startPoint[0] + endPoint[0]) / 2;
                focusY = (startPoint[1] + endPoint[1]) / 2;
            } else {
                int avgX = (int) (sumX / (validCount * 2));
                int avgY = (int) (sumY / (validCount * 2));

                int[] neighbors = new int[count];
                int maxNeighbors = 0;

                for (int i = 0; i < count; i++) {
                    if (!valid[i]) {
                        continue;
                    }
                    int n = 0;
                    for (int j = 0; j < count; j++) {
                        if (!valid[j]) {
                            continue;
                        }
                        if (Math.abs(elemCX[i] - elemCX[j]) <= viewW && Math.abs(elemCY[i] - elemCY[j]) <= viewH) {
                            n++;
                        }
                    }
                    neighbors[i] = n;
                    if (n > maxNeighbors) {
                        maxNeighbors = n;
                    }
                }

                int densityThreshold = maxNeighbors / 4;

                focusX = avgX;
                focusY = avgY;
                long minScore = Long.MAX_VALUE;
                boolean foundValidAnchor = false;

                for (int i = 0; i < count; i++) {
                    if (!valid[i]) {
                        continue;
                    }

                    if (neighbors[i] < densityThreshold) {
                        continue;
                    }

                    long dx = elemCX[i] - avgX;
                    long dy = elemCY[i] - avgY;
                    long distSq = dx * dx + dy * dy;

                    short[] aabb = elements[i].getAABB();
                    long elemW = aabb[2] - aabb[0];
                    long elemH = aabb[3] - aabb[1];
                    if (elemW < 50 && elemH < 50) {
                        continue;
                    }

                    long sizeSq = elemW * elemW + elemH * elemH;

                    long score = distSq + sizeSq;

                    if (score < minScore) {
                        minScore = score;
                        focusX = elemCX[i];
                        focusY = elemCY[i];
                        foundValidAnchor = true;
                    }
                }

                if (!foundValidAnchor) {
                    focusX = avgX;
                    focusY = avgY;
                }
            }
        }

        this.offsetX = w / 2;
        if (this.zoomOut != 0) {
            this.offsetX -= focusX * 1000 / this.zoomOut;
        }

        this.offsetY = h / 2;
        if (this.zoomOut != 0) {
            this.offsetY -= focusY * 1000 / this.zoomOut;
        }
    }
}
