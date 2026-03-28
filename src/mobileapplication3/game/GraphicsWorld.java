// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.game;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import utils.MobappGameSettings;

import java.util.Random;
import java.util.Vector;

/**
 *
 * @author vipaol
 */
public class GraphicsWorld extends World {
    public static final int THICKNESS_BODIES = 10;
    public static final int THICKNESS_LANDSCAPE = 24;
    public static int DEFAULT_LANDSCAPE_COLOR = 0x4444ff;
    private static final int BIG_SCREEN_SIDE = 480;
    private static final int CAR_COLLISION_LAYER = 1;

    public int colBg = 0x000000;
    public int colLandscape = DEFAULT_LANDSCAPE_COLOR;
    int colBodies = 0xffffff;
    int currColBg;
    int currColWheel;
    int currColLandscape;
    int currColBodies;

    public static int scWidth = 200;
    private int halfScWidth = scWidth/2;
    public static int scHeight = 200;
    private int scMinSide = Math.min(scWidth, scHeight);

    public boolean removeBodies = true;
    private boolean betterGraphics, bg, legacyDrawingMethod;
    public static boolean bgOverride = false;
    private int bgLineStep = scMinSide / 3;
    private int bgLineThickness;
    public int bgXOffset = 0;

    public int cameraRotationMode = MobappGameSettings.CAMERA_ROTATION_DEFAULT_VALUE;
    private int camCos = 1000;
    private int camSin = 0;

    int zoomOutBase = 0;
    int zoomOut = 100;
    int offsetX = 0;
    int offsetY = 0;
    public int camOffsetX;
    public int camOffsetY;
    public int viewField;

    public int carX = 0;
    public int carY = 0;
    private GameplayCanvas game = null;
    public Body carbody;
    public Body leftWheel;
    public Body rightWheel;
    private Joint leftjoint;
    private Joint rightjoint;
    private final Random random = new Random();

    // list of all bodies car touched (for falling platforms)
    public Vector waitingForDynamic = new Vector();
    long prevBodyTickTime = System.currentTimeMillis();
    public int barrierX = Integer.MIN_VALUE;
    public int lowestY;

    public GraphicsWorld() {
        resetColors();
        readSettings();
    }

    public GraphicsWorld(World w) {
        super(w);
        resetColors();
        readSettings();
    }

    private void readSettings() {
        bg = bgOverride;
        try {
            currColLandscape = colLandscape = MobappGameSettings.getLandscapeColor();
            bg = bg || MobappGameSettings.isBGEnabled(false);
            legacyDrawingMethod = MobappGameSettings.isLegacyDrawingMethodEnabled(false);
            cameraRotationMode = MobappGameSettings.getCameraRotationMode();
        } catch (Throwable ex) {
            Logger.log(ex);
        }
        if (bg) {
            colBg = 0x150031;
        }
    }

    public void resetColors() {
        if (DebugMenu.whatTheGame) {
            currColWheel = 0x888888;
            colBg = 0x001155;
            colBodies = 0x555555;
        }
        currColWheel = colBg;
        currColBg = colBg;
        currColBodies = colBodies;
        currColLandscape = colLandscape;
    }

    public void setGame(GameplayCanvas game) {
        this.game = game;
    }

    public void removeBody(Body body) {
        if (body == leftWheel) {
            Logger.log("Deleting leftWheel...");
        } else if (body == carbody) {
            Logger.log("Deleting carbody...");
        } else if (body == rightWheel) {
            Logger.log("Deleting rightWheel...");
        }
        super.removeBody(body);
    }

    public void cleanWorld() {
        Constraint[] constraints = getConstraints();
        while (getConstraintCount() > 0) {
            removeConstraint(constraints[0]);
        }
        rmAllBodies();
        rmLandscapeSegments();
    }
    private void rmLandscapeSegments() {
        Landscape landscape = getLandscape();
        while (landscape.segmentCount() > 0) {
            landscape.removeSegment(0);
        }
    }

    private void rmAllBodies() {
        Body[] bodies = getBodies();
        while (getBodyCount() > 0) {
            removeBody(bodies[0]);
        }
        leftWheel = null;
        carbody = null;
        rightWheel = null;
    }

    public void addCar(int spawnX, int spawnY, int ang2FX) {
        carX = spawnX;
        carY = spawnY;
        int carBodyLength = 240;
        int carBodyHeight = 40;
        int wheelRadius = 40;
        Shape carbodyShape;
        Shape wheelShape;

        carbodyShape = Shape.createRectangle(carBodyLength, carBodyHeight);
        carbodyShape.setMass(1);
        carbodyShape.setFriction(0);
        carbodyShape.setElasticity(100);
        carbodyShape.correctCentroid();
        carbody = new Body(spawnX, spawnY, carbodyShape, true);
        carbody.setRotation2FX(ang2FX);

        int ang = (int) (ang2FX * 360L / FXUtil.TWO_PI_2FX) + 1;

        wheelShape = Shape.createCircle(wheelRadius);
        wheelShape.setElasticity(100);
        wheelShape.setFriction(0);
        wheelShape.setMass(2);
        wheelShape.correctCentroid();
        int lwX = spawnX - (carBodyLength / 2 - wheelRadius - 2) * Mathh.cos(ang) / 1000;
        int lwY = spawnY + wheelRadius / 2 - (carBodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        int rwX = spawnX + (carBodyLength / 2 - wheelRadius + 2) * Mathh.cos(ang) / 1000;
        int rwY = spawnY + wheelRadius / 2 + (carBodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        leftWheel = new Body(lwX, lwY, wheelShape, true);
        rightWheel = new Body(rwX, rwY, wheelShape, true);

        super.removeBody(carbody);
        super.removeBody(leftWheel);
        super.removeBody(rightWheel);

        addBody(carbody);
        carbody.addCollisionLayer(CAR_COLLISION_LAYER);
        addBody(leftWheel);
        leftWheel.addCollisionLayer(CAR_COLLISION_LAYER);
        addBody(rightWheel);
        rightWheel.addCollisionLayer(CAR_COLLISION_LAYER);

        leftjoint = new Joint(carbody, leftWheel, FXVector.newVector(-carBodyLength / 2 + wheelRadius - 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        rightjoint = new Joint(carbody, rightWheel, FXVector.newVector(carBodyLength / 2 - wheelRadius + 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        addConstraint(leftjoint);
        addConstraint(rightjoint);

        bgXOffset = spawnX;
    }

    public void destroyCar() {
        removeConstraint(leftjoint);
        removeConstraint(rightjoint);
        leftWheel.removeCollisionLayer(CAR_COLLISION_LAYER);
        carbody.removeCollisionLayer(CAR_COLLISION_LAYER);
        rightWheel.removeCollisionLayer(CAR_COLLISION_LAYER);
        int forceFX = -FXUtil.ONE_FX * 500;
        leftWheel.applyMomentum(new FXVector(-forceFX, forceFX));
        rightWheel.applyMomentum(new FXVector(forceFX, forceFX));
        leftWheel.shape().setElasticity(100);
        carbody.shape().setElasticity(100);
        getLandscape().getShape().setElasticity(200);
    }

    public void tickCustomBodies() {
        int diffTime = (int) (System.currentTimeMillis() - prevBodyTickTime);
        // ticking timers on each body car touched and set it as dynamic
        // for falling platforms
        for (int i = 0; i < waitingForDynamic.size(); i++) {
            try {
                Body body = (Body) waitingForDynamic.elementAt(i);
                MUserData userData = (MUserData) body.getUserData();
                userData.setFallDelay(userData.getFallDelay() - diffTime);
                if (userData.getFallDelay() <= 0) {
                    userData.setFallDelay(Integer.MIN_VALUE);
                    body.setDynamic(true);
                    waitingForDynamic.removeElementAt(i);
                }
            } catch (ArrayIndexOutOfBoundsException ignored) { }
        }
        // removing all that fell out the world or got too left
        if (removeBodies) {
            for (int i = 0; i < getBodyCount(); i++) {
                if (viewField < 100) {
                    break; // Hack to not remove bodies until the correct screen size is set. Needs a proper fix
                }
                Body[] bodies = getBodies();
                Body body = bodies[i];
                if (body.positionFX().xAsInt() < barrierX || body.positionFX().yAsInt() > lowestY + 2000) {
                    if (body != carbody && body != leftWheel && body != rightWheel) {
                        removeBody(body);
                    }
                }
            }
        }
        prevBodyTickTime = System.currentTimeMillis();
    }

    public void setWheelColor(int color) {
        currColWheel = color;
    }

    public void drawWorld(Graphics g, int[][] structuresData, int structureRingBufferOffset, int structureCount) {
        // fill background
        g.setColor(currColBg);
        g.fillRect(0, 0, scWidth, scHeight);
        try {
            carX = carbody.positionFX().xAsInt();
            carY = carbody.positionFX().yAsInt();

            // zooming and moving virtual camera
            calcZoomOut();
            calcOffset();

            drawBg(g);
            if (structuresData != null && !legacyDrawingMethod && cameraRotationMode == MobappGameSettings.CAMERA_ROTATION_STATIC) {
                try {
                    drawLandscape(g, structuresData, structureRingBufferOffset, structureCount);
                } catch (Exception ex) {
                    Logger.log(ex);
                }
            } else {
                drawLandscape(g);
            }
            drawBodies(g); // draw all bodies, excluding car wheels
            drawCar(g); // draw car wheels
            //drawConstraints(g); // don't draw constraints
        } catch (NullPointerException ex) {
            int l = scWidth * 2 / 3;
            int h = scHeight / 24;
            g.drawRect(scWidth / 2 - l / 2, scHeight * 2 / 3, l, h);
            g.fillRect(scWidth / 2 - l / 2, scHeight * 2 / 3, l/5, h);
            Logger.log(ex);
        }
        //g.fillTriangle(xToPX(carX+viewField/2-10), 0, xToPX(carX+viewField/2), scHeight, xToPX(carX+viewField/2+10), 0);
    }

    private void drawBg(Graphics g) {
        // some very boring code
        if (game.points == 292) {
            currColBg = 0x2f92ff;
            currColLandscape = 0xffffff;
        } else if (game.points == 293) {
            currColBg = colBg;
            currColLandscape = colLandscape;
        }

        if (bg) {
            int sunR = Math.min(scWidth, scHeight) / 4;
            int sunCenterY = scHeight - scHeight * 3 / 5;

            g.setColor(191, 0, 127);
            int offset = (carX - bgXOffset) / 32;
            int l = (scWidth * 4);
            int y1 = sunCenterY + sunR;
            int y2 = scHeight;
            int ii = 0;
            int n = l/bgLineStep;
            // vertical lines
            for (int i = 0; i < n; i++) {
                int x2 = -(ii + (offset) % bgLineStep - l/2)/*  *64/8  */;
                ii += bgLineStep;
                int x1 = x2 / 4;
                int thickness = bgLineThickness;
                if (Math.abs(i*8 - n*4) > n) {
                    thickness -= 1;
                }
                drawLine(g, x1 + halfScWidth, y1, x2 + halfScWidth, y2, thickness, false);
            }
            // horizontal lines
            n = scHeight*2/bgLineStep;
            for (int i = 0; i < n; i++) {
                if (i == 1) {
                    continue;
                }
                int y = y1 + (y2 - y1) * i * i / n / n;
                drawLine(g, 0, y, scWidth, y, 1, false);
            }
            g.setColor(255, 170, 0);

            // sun
            int lines = 6;
            g.fillArc(halfScWidth - sunR, sunCenterY - sunR, sunR * 2, sunR * 2, 0, 360);
            g.setColor(currColBg);
            for (int i = 0; i < lines; i++) {
                int y = i * sunR / lines + sunCenterY - sunR / 12;
                drawLine(g, 0, y, scWidth, y, bgLineThickness*2*(i+1)/lines, false);
            }
        }
    }

    private void drawBodies(Graphics g) {
        Body[] bodies = getBodies();
        int bodyCount = getBodyCount();
        for (int i = 0; i < bodyCount; i++) {
            if (bodies[i] != leftWheel && bodies[i] != rightWheel) {
                drawBody(g, bodies[i]);
            }
        }
    }

    private void drawBody(Graphics g, Body b) {
        FXVector[] vertices = b.getVertices();

        int colorFill = currColBodies;
        int colorStroke = currColBodies;
        int thickness = THICKNESS_BODIES * 500 / zoomOut * 2;

        UserData userData = b.getUserData();
        if (userData instanceof MUserData) {
            MUserData mUserData = (MUserData) userData;
            colorFill = mUserData.getColor();
            colorStroke = mUserData.getColorStroke();
            if (colorFill == MUserData.COLOR_DEFAULT) {
                colorFill = currColBodies;
            }
            if (colorStroke == MUserData.COLOR_DEFAULT) {
                colorStroke = colorFill;
            }
        }

        if (vertices.length == 1) { // if shape of the body is circle
            int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
            g.setColor(colorStroke);
            int x = b.positionFX().xAsInt();
            int y = b.positionFX().yAsInt();
            int zoomedRadius = radius * 1000 / zoomOut;
            drawArc(g,
                    xToPX(x, y) - zoomedRadius,
                    yToPX(x, y) - zoomedRadius,
                    radius * 2000 / zoomOut,
                    radius * 2000 / zoomOut,
                    0, 360, thickness, colorFill
            );
        }
        else { // if not a circle, then a polygon
            // fill
            g.setColor(colorFill);
            int p0X = vertices[0].xAsInt();
            int p0Y = vertices[0].yAsInt();
            for (int i = 0; i < vertices.length - 1; i++) {
                if (b != carbody) {
                    int piX = vertices[i].xAsInt();
                    int piY = vertices[i].yAsInt();
                    int pi1X = vertices[i + 1].xAsInt();
                    int pi1Y = vertices[i + 1].yAsInt();
                    g.fillTriangle(
                            xToPX(p0X, p0Y),
                            yToPX(p0X, p0Y),
                            xToPX(piX, piY),
                            yToPX(piX, piY),
                            xToPX(pi1X, pi1Y),
                            yToPX(pi1X, pi1Y)
                    );
                }
            }

            // stroke
            g.setColor(colorStroke);
            for (int i = 0; i < vertices.length - 1; i++) {
                int piX = vertices[i].xAsInt();
                int piY = vertices[i].yAsInt();
                int pi1X = vertices[i + 1].xAsInt();
                int pi1Y = vertices[i + 1].yAsInt();
                drawLine(g,
                        xToPX(piX, piY),
                        yToPX(piX, piY),
                        xToPX(pi1X, pi1Y),
                        yToPX(pi1X, pi1Y),
                        THICKNESS_BODIES
                );
            }
            int pnX = vertices[vertices.length - 1].xAsInt();
            int pnY = vertices[vertices.length - 1].yAsInt();
            drawLine(g,
                    xToPX(pnX, pnY),
                    yToPX(pnX, pnY),
                    xToPX(p0X, p0Y),
                    yToPX(p0X, p0Y),
                    THICKNESS_BODIES
            );
        }
    }

    private void drawCar(Graphics g) {
        drawWheel(g, leftWheel);
        drawWheel(g, rightWheel);
    }

    private void drawLandscape(Graphics g) {
        Landscape landscape = getLandscape();
        for (int i = 0; i < landscape.segmentCount(); i++) {
            int stPointX = xToPX(landscape.startPoint(i).xAsInt(), landscape.startPoint(i).yAsInt());
            int stPointY = yToPX(landscape.startPoint(i).xAsInt(), landscape.startPoint(i).yAsInt());
            int endPointX = xToPX(landscape.endPoint(i).xAsInt(), landscape.endPoint(i).yAsInt());
            int endPointY = yToPX(landscape.endPoint(i).xAsInt(), landscape.endPoint(i).yAsInt());
            if (stPointX < scWidth | endPointX > 0) {
                if (!DebugMenu.isDebugEnabled) {
                    g.setColor(currColLandscape);
                } else {
                    g.setColor(255, 255, 255);
                }
                if (DebugMenu.whatTheGame) {
                    drawGroundLine(
                            g,
                            stPointX,
                            stPointY,
                            endPointX,
                            endPointY,
                            THICKNESS_LANDSCAPE
                    );
                } else {
                    drawLine(
                            g,
                            stPointX,
                            stPointY,
                            endPointX,
                            endPointY,
                            THICKNESS_LANDSCAPE
                    );
                }
                g.setColor(0xff0000);
                if (DebugMenu.showLinePoints) {
                    g.fillArc(stPointX-1, stPointY-1, 2, 2, 0, 360);
                    g.fillArc(endPointX-1, endPointY-1, 2, 2, 0, 360);
                }
            }
        }
    }

    private void drawLandscape(Graphics g, int[][] structuresData, int structureRingBufferOffset, int structureCount) {
        int prevStructureEndX = 0;
        int prevStructureEndY = 0;
        for (int i = structureRingBufferOffset; i < structureRingBufferOffset + structureCount; i++) {
            int[] structureData = structuresData[i % structuresData.length];
            int c = 0;
            int endX = structureData[c++];
            int endY = structureData[c++];
            int lineCount = structureData[c++];
            int structureID = structureData[c++];
            int color = currColLandscape;
            if (DebugMenu.structureDebug) {
                Random random = new Random(structureID);
                g.setColor(64 + random.nextInt(192), 64 + random.nextInt(192), 64 + random.nextInt(192));
                color = g.getColor();
            } else {
                if (DebugMenu.isDebugEnabled) {
                    g.setColor(0xffffff);
                } else {
                    g.setColor(currColLandscape);
                }
            }

            if (xToPX(endX, endY) < 0) {
                prevStructureEndX = endX;
                prevStructureEndY = endY;
                continue;
            }

            while (c < structureData.length - 1) {
                int id = structureData[c++];
                switch (id) {
                    case ElementPlacer.DRAWING_DATA_ID_LINE:
                        int x1 = structureData[c++];
                        int y1 = structureData[c++];
                        int x2 = structureData[c++];
                        int y2 = structureData[c++];
                        drawLine(g, xToPX(x1, y1), yToPX(x1, y1), xToPX(x2, y2), yToPX(x2, y2), THICKNESS_LANDSCAPE);
                        break;
                    case ElementPlacer.DRAWING_DATA_ID_PATH: {
                        int pointsCount = structureData[c++];
                        int x = structureData[c++];
                        int y = structureData[c++];
                        int prevX = xToPX(x, y);
                        int prevY = yToPX(x, y);
                        for (int j = 1; j < pointsCount; j++) {
                            int newX = structureData[c++];
                            int newY = structureData[c++];
                            drawLine(g, prevX, prevY, prevX = xToPX(newX, newY), prevY = yToPX(newX, newY), THICKNESS_LANDSCAPE);
                        }
                        break;
                    }
                    case ElementPlacer.DRAWING_DATA_ID_CIRCLE: {
                        int x = structureData[c++];
                        int y = structureData[c++];
                        int r = structureData[c++];
                        g.drawArc(xToPX(x - r, y - r), yToPX(x - r, y - r), r * 2 * 1000 / zoomOut, r * 2 * 1000 / zoomOut, 0, 360, THICKNESS_LANDSCAPE, zoomOut, betterGraphics, true, true);
                        break;
                    }
                    case ElementPlacer.DRAWING_DATA_ID_ARC: {
                        int x = structureData[c++];
                        int y = structureData[c++];
                        int r = structureData[c++];
                        int startAngle = structureData[c++];
                        int arcAngle = structureData[c++];
                        if (arcAngle == 0) {
                            arcAngle = 360;
                        }
                        int kx = structureData[c++];
                        int ky = structureData[c++];
                        if (DebugMenu.structureDebug) {
                            if (!DebugMenu.simulationMode) {
                                g.drawString("startAngle=" + startAngle, xToPX(x, y), yToPX(x, y), Graphics.BOTTOM | Graphics.HCENTER);
                                g.drawString("arcAngle=" + arcAngle, xToPX(x, y), yToPX(x, y), Graphics.TOP | Graphics.HCENTER);
                            }
                        }
                        int cX = x - r * kx / 10;
                        int cY = y - r * ky / 10;
                        g.drawArc(xToPX(cX, cY), yToPX(cX, cY), r*2 * kx * 100 / zoomOut, r*2 * ky * 100 / zoomOut, startAngle, arcAngle, THICKNESS_LANDSCAPE, zoomOut, betterGraphics, true, true);
                        break;
                    }
                }
            }

            if (DebugMenu.structureDebug) {
                if (prevStructureEndX == 0) {
                    prevStructureEndX = endX - 1000;
                    prevStructureEndY = endY - 100;
                }
                g.setColor(0x000033);
                String str = String.valueOf(lineCount);
                int textX = (endX + prevStructureEndX) / 2;
                int textY = (endY + prevStructureEndY) / 2;
                int x = xToPX(textX, textY);
                int y = yToPX(textX, textY);
                int w = g.stringWidth(str);
                int h = g.getFontHeight();
                g.fillRect(x - w/2, y - h/2, w, h);
                g.setColor(color);
                g.drawLine(xToPX(endX, endY), 0, xToPX(endX, endY), scHeight);
                g.drawString(str, x, y, Graphics.VCENTER | Graphics.HCENTER);
            }

            prevStructureEndX = endX;
            prevStructureEndY = endY;

            if (xToPX(endX, endY) >= scWidth) {
                break;
            }
        }
    }

//    private void drawConstraints(Graphics g) {
//        int constraintCount = getConstraintCount();
//        Constraint[] constraints = getConstraints();
//        for (int i = 0; i < constraintCount; i++) {
//            if (constraints[i] instanceof Spring) {
//                Spring spring = (Spring) constraints[i];
//                g.drawLine(xToPX(spring.getPoint1().xAsInt()),
//                        yToPX(spring.getPoint1().yAsInt()),
//                        xToPX(spring.getPoint2().xAsInt()),
//                        yToPX(spring.getPoint2().yAsInt()));
//            }
//        }
//    }

    private void drawWheel(Graphics g, Body b) {
        int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
        if (game.currentEffects[GameplayCanvas.EFFECT_SPEED] == null) {
            currColWheel = currColBg;
            if (DebugMenu.discoMode) {
                currColWheel = random.nextInt(16777216);
                currColBodies = random.nextInt(16777216);
            }
        }

        g.setColor(currColBodies);
        int x = b.positionFX().xAsInt();
        int y = b.positionFX().yAsInt();
        int zoomedRadius = radius * 1000 / zoomOut;
        drawArc(g,
                xToPX(x, y) - zoomedRadius,
                yToPX(x, y) - zoomedRadius,
                radius * 2000 / zoomOut,
                radius * 2000 / zoomOut,
                0, 360, THICKNESS_BODIES * 500 / zoomOut * 2, currColWheel
        );
    }

    private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
        drawLine(g, x1, y1, x2, y2, thickness, true);
    }

    private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, boolean zoomThickness) {
        if (DebugMenu.discoMode) {
            g.setColor(random.nextInt(16777216));
        }
        g.drawLine(x1, y1, x2, y2, thickness, zoomOut, betterGraphics, zoomThickness);
    }

    private void drawGroundLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
        g.setColor(0x333300);
        if (DebugMenu.discoMode) {
            g.setColor(random.nextInt(16777216));
        }
        int y3 = Math.max(y1, y2);
        int x3 = x1;
        if (y3 == y1) {
            x3 = x2;
        }
        g.fillTriangle(x1, y1, x2, y2, x3, y3);
        g.fillRect(x1, y3, x2 - x1, scHeight - y3);

        g.setColor(0x00ff00);

        g.drawLine(x1, y1, x2, y2, thickness, zoomOut, betterGraphics);
    }

    private void drawArc(Graphics g, int x, int y, int w, int h, int startAngle, int arcAngle, int thickness, int fillColor) {
        int prevColor = g.getColor();

        if (thickness > 1 && betterGraphics) {
            g.fillArc(x - thickness / 2, y - thickness / 2, w + thickness, h + thickness, startAngle, arcAngle);
            g.setColor(fillColor);
            g.fillArc(x + thickness / 2, y + thickness / 2, w - thickness, h - thickness, startAngle, arcAngle);
            g.setColor(prevColor);
        } else {
            g.setColor(fillColor);
            g.fillArc(x + thickness / 2, y + thickness / 2, w - thickness, h - thickness, startAngle, arcAngle);
            g.setColor(prevColor);
            g.drawArc(x, y, w, h, startAngle, arcAngle);
        }
    }

    public int xToPX(int x, int y) {
        if (cameraRotationMode == MobappGameSettings.CAMERA_ROTATION_STATIC) {
            return x * 1000 / zoomOut + offsetX + camOffsetX;
        } else {
            long dx = x - carX;
            long dy = y - carY;

            long rotatedX = (dx * camCos - dy * camSin) / 1000;

            return (int)(rotatedX * 1000 / zoomOut) + offsetX + camOffsetX;
        }
    }

    public int yToPX(int x, int y) {
        if (cameraRotationMode == MobappGameSettings.CAMERA_ROTATION_STATIC) {
            return y * 1000 / zoomOut + offsetY + camOffsetY;
        } else {
            long dx = x - carX;
            long dy = y - carY;

            long rotatedY = (dx * camSin + dy * camCos) / 1000;

            return (int)(rotatedY * 1000 / zoomOut) + offsetY + camOffsetY;
        }
    }

    public void refreshCarPos() {
        if (carbody != null) {
            FXVector posFX = carbody.positionFX();
            carX = posFX.xAsInt();
            carY = posFX.yAsInt();
        } else {
            carX = -8000;
            carY = 0;
        }
    }

    public void refreshScreenParameters(int w, int h) {
        Logger.log("world:refreshing screen params:");
        Logger.log(w + " " + h);
        if (w <= 0 || h <= 0) {
            return;
        }

        scWidth = w;
        halfScWidth = scWidth / 2;
        scHeight = h;
        scMinSide = Math.min(scWidth, scHeight);
        bgLineStep = scMinSide / 3;
        zoomOutBase = 2000000 / scMinSide;
        calcZoomOut();
        bgLineThickness = Math.max(w, h)/250;

        try {
            betterGraphics = MobappGameSettings.isBetterGraphicsEnabled(Math.max(scWidth, scHeight) >= BIG_SCREEN_SIDE);
        } catch (Throwable ex) {
            Logger.log(ex);
        }
    }

    private void calcZoomOut() {
        if (DebugMenu.simulationMode) {
            zoomOut = 50000;
        } else {
            zoomOut = (1000 * (carY - 1000) / scMinSide);
            int zoomOutBase = this.zoomOutBase;
            if (game.currentEffects[GameplayCanvas.EFFECT_SPEED] != null) {
                if (game.currentEffects[GameplayCanvas.EFFECT_SPEED][0] > 0) {
                    zoomOut = zoomOut * game.currentEffects[GameplayCanvas.EFFECT_SPEED][2] / 100;
                    zoomOutBase = zoomOutBase * game.currentEffects[GameplayCanvas.EFFECT_SPEED][2] / 100;
                }
            }
            if (zoomOut <= 0) {
                zoomOut = -zoomOut;
                zoomOut += 1;
            }
            zoomOut += zoomOutBase;
            if (Math.abs(zoomOut) < 500000 / scMinSide) {
                zoomOut = 500000 / scMinSide * Mathh.sign(zoomOut);
            }
        }

        // for timely track generation and deleting waste objects
        viewField = scWidth * zoomOut / 1000;
        if (DebugMenu.isDebugEnabled && DebugMenu.closerWorldgen || DebugMenu.simulationMode) {
            viewField /= 4;
        }
    }

    private void calcOffset() {
        if (cameraRotationMode == MobappGameSettings.CAMERA_ROTATION_STATIC || carbody == null) {
            camCos = 1000;
            camSin = 0;

            offsetX = -carX * 1000 / zoomOut + scWidth / 3;
            offsetY = -carY * 1000 / zoomOut + scHeight * 2 / 3;
            offsetY += carY * scMinSide / 20000;
            offsetY = Mathh.constrain(-carY * 1000 / zoomOut + scHeight / 16, offsetY, -carY * 1000 / zoomOut + scHeight * 4 / 5);
        } else {
            double camRotation = -carbody.rotation2FX() * 1d / FXUtil.ONE_2FX;

            if (cameraRotationMode == MobappGameSettings.CAMERA_ROTATION_DONT_FLIP) {
                camRotation = Math.PI / 4 * Math.sin(camRotation);
                if (Math.PI / 2 < camRotation && camRotation < Math.PI * 3 / 2) {
                    camRotation = camRotation * camRotation / Math.PI * 4;
                }
            }

            camCos = (int) (Math.cos(camRotation) * 1000);
            camSin = (int) (Math.sin(camRotation) * 1000);

            offsetX = scWidth / 2;

            offsetY = scHeight * 2 / 3;
        }
    }

    public void moveBg(int dx) {
        bgXOffset = bgXOffset + dx;
    }
}
