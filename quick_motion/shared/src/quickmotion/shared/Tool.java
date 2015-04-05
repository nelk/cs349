package quickmotion.shared;

import java.util.*;
import java.util.List;

/**
 *
 */
public enum Tool {
    MARKER ("marker.png", new Vector2d(0, 32)) {
        @Override
        public void dragLine(Delta delta, float time, LineSegment line) {
            boolean timeVariant = view.getController().isAnimated();
            DrawnLine l = new DrawnLine(line.getP1(), line.getP2(), markerColour);
            l.setCreateTime(time);
            currentFigure.addLine(l);
            l.setFigure(currentFigure);
            view.addLine(l);
            if (timeVariant) {
                view.getTimeLine().doTimeStep(delta);
                view.notifyTimelineUpdated();
            }
        }

        @Override
        public void dragRelease(float time) {
        }

        @Override
        public void dragStart(float time, Vector2f p) {
            currentFigure = new Figure();
        }

    }, ERASER ("eraser2.png", new Vector2d(0, 32)) {
        @Override
        public void dragLine(Delta delta, float time, LineSegment line) {
            boolean partial = view.getController().isPartial();
            for (DrawnLine l : view.getLines()) {
                if (!l.existsAt(time)) continue;
                double d = l.getTransformedLineAtTime(time).distanceFromPoint(line.getP2());
//                Vector2f intersection = l.getTransformedLineAtTime(time).intersection(line);
//                if (intersection != null) {
                 if (d <= SELECT_DISTANCE) {
                    l.setDeleteTime(time - 0.001f);
                    // TODO delete line

                    if (!partial) {
                        // Delete rest of figure
                        for (DrawnLine l2 : l.getFigure().getLines()) {
                            l2.setDeleteTime(time - 0.001f);
                        }
                    }
                }
            }
        }

        @Override
        public void dragRelease(float time) {
        }

        @Override
        public void dragStart(float time, Vector2f p) {
        }

    }, LASSO("lasso.png", new Vector2d(0, 32)) {
        @Override
        public void move(float time, Vector2f p) {
            // Check if any selected lines are hovered over
            boolean found = false;
            for (DrawnLine l : selectedLines) {
                double d = l.getTransformedLineAtTime(time).distanceFromPoint(p);
                if (d <= SELECT_DISTANCE) {
                   found = true;
                    break;
                }
            }
            for (DrawnLine l : selectedLines) {
                l.setHovered(l.isSelected() && found);
            }
        }

        @Override
        public void dragLine(Delta delta, float time, LineSegment line) {
            boolean timeVariant = view.getController().isAnimated();
            if (moving) {
                Vector2f lineVector = line.toVector();
                if (timeVariant) {
                    currentAnimation.translate(time, lineVector, true);
                    view.getTimeLine().doTimeStep(delta);
                    view.notifyTimelineUpdated();
                } else {
                    currentAnimation.translate(time, lineVector, false);
                }
            } else {
                lassoPath.add(line.getP1());
                lassoPath.add(line.getP2());
            }
        }

        @Override
        public void dragStart(float time, Vector2f p) {
            // Check if any lines in any selected figures are close to point
            moving = false;
            for (DrawnLine l : selectedLines) {
                double d = l.getTransformedLineAtTime(time).distanceFromPoint(p);
                if (d <= SELECT_DISTANCE) {
                    moving = true;
                    break;
                }

            }
            if (moving) {
                currentAnimation = new Animation(time, Animation.Type.TRANSLATION);
                view.addAnimation(currentAnimation);
                for (DrawnLine l : selectedLines) {
                    l.animate(currentAnimation);
                }
            } else {
                lassoPath.clear();
                for (DrawnLine l : selectedLines) {
                    l.setSelected(false);
                    l.setHovered(false);
                }
                selectedLines.clear();
            }
        }

        @Override
        public void dragRelease(float time) {
            if (moving) {
                moving = false;
            } else {
                if (lassoPath.size() > 0) {
                    boolean partial = view.getController().isPartial();
                    HashMap<Figure, List<DrawnLine>> inLinesByFigure = new HashMap<Figure, List<DrawnLine>>();
                    for (DrawnLine l : view.getLines()) {
                        LineSegment line = l.getTransformedLineAtTime(time);
                        if (!l.existsAt(time)) continue;
                        if (contains(lassoPath, line.getP1()) && contains(lassoPath, line.getP2())) {
                            if (!inLinesByFigure.containsKey(l.getFigure())) {
                                inLinesByFigure.put(l.getFigure(), new ArrayList<DrawnLine>());
                            }
                            inLinesByFigure.get(l.getFigure()).add(l);
                        }
                    }
                    for (Map.Entry<Figure, List<DrawnLine>> entry : inLinesByFigure.entrySet()) {
                        // Note, case for !partial and missing lines does nothing
                        if (entry.getKey().size() == entry.getValue().size()) {
                            // If all lines in figure contained
                            for (DrawnLine l : entry.getValue()) {
                                selectedLines.add(l);
                                l.setSelected(true);
                            }
                        } else if (partial) {
                            Figure splitFigure = new Figure();
                            LinkedList<DrawnLine> splitLines = new LinkedList<DrawnLine>();
                            for (DrawnLine l : entry.getValue()) {
                                selectedLines.add(l);
                                l.setSelected(true);
                                l.setFigure(splitFigure);
                                splitFigure.addLine(l);
                                splitLines.add(l);
                                entry.getKey().removeLine(l);
                            }
                        }
                    }
                }
                lassoPath.clear();
            }

        }



    }, ROTATE("rotate.png", new Vector2d(16, 16)) {
        @Override
        public void dragStart(float time, Vector2f p) {
            if (selectedLines.size() > 0) {
                Vector2f centerInModelCoords = new Vector2f();
                Vector2f centerInGlobalCoords = new Vector2f();
                getSelectedLinesCenter(time, centerInModelCoords, centerInGlobalCoords);
                selectionCenter = centerInGlobalCoords;
                currentAnimation = new Animation(time, Animation.Type.ROTATION);
                currentAnimation.setRotationReferencePoint(centerInGlobalCoords);
                view.addAnimation(currentAnimation);
                for (DrawnLine l : selectedLines) {
                    l.animate(currentAnimation);
                }
            }
        }

        @Override
        public void dragLine(Delta delta, float time, LineSegment line) {
            if (selectionCenter == null) return;
            boolean timeVariant = view.getController().isAnimated();
            float angle1 = Vector2f.subtract(line.getP1(), selectionCenter).angle();
            float angle2 = Vector2f.subtract(line.getP2(), selectionCenter).angle();
            float angle = angle2 - angle1;
            if (!Float.isNaN(angle) && Float.compare(angle, 0f) != 0) {
                currentAnimation.rotate(time, angle, timeVariant);
            }
            if (timeVariant) {
                view.getTimeLine().doTimeStep(delta);
                view.notifyTimelineUpdated();
            }
        }

        @Override
        public void dragRelease(float time) {
            selectionCenter = null;
        }




    }, SCALE("scale.png", new Vector2d(0, 32)) {
        @Override
        public void dragStart(float time, Vector2f p) {
            if (selectedLines.size() > 0) {
                Vector2f centerInModelCoords = new Vector2f();
                Vector2f centerInGlobalCoords = new Vector2f();
                getSelectedLinesCenter(time, centerInModelCoords, centerInGlobalCoords);
                selectionCenter = centerInGlobalCoords;
                currentAnimation = new Animation(time, Animation.Type.SCALE);
                currentAnimation.setRotationReferencePoint(centerInGlobalCoords);
                view.addAnimation(currentAnimation);
                for (DrawnLine l : selectedLines) {
                    l.animate(currentAnimation);
                }
            }
        }

        @Override
        public void dragLine(Delta delta, float time, LineSegment line) {
            if (selectionCenter == null) return;
            boolean timeVariant = view.getController().isAnimated();

            Vector2f deltaScale = line.toVector();
            if (deltaScale.magnitude() != 0) {
                deltaScale.setY(-deltaScale.getY());
                Vector2f scale = Vector2f.scalarMultiple(deltaScale, 0.01f).add(new Vector2f(1, 1));
                currentAnimation.scale(time, scale, timeVariant);
            }

            if (timeVariant) {
                view.getTimeLine().doTimeStep(delta);
                view.notifyTimelineUpdated();
            }
        }

        @Override
        public void dragRelease(float time) {
            selectionCenter = null;
        }

    };


    private static float SELECT_DISTANCE = 3f;

    private static Vector2f lastPos;
    private static Animation currentAnimation;
    private static Figure currentFigure;
    private static boolean moving;
    private static ArrayList<Vector2f> lassoPath = new ArrayList<Vector2f>();
    private static List<DrawnLine> selectedLines = new ArrayList<DrawnLine>();
    private static Vector2f selectionCenter;
    private static boolean dragging = false;
    private static int markerColour = 0xFF0000FF;
    private static float lassoAntsTime = 0f;
    private static boolean resetLassoRequested = false;
    private static AbstractView view;

    private String image;
    private Vector2d cursorCenter;

    Tool(String img, Vector2d p) {
        image = img;
        cursorCenter = p;
    }

    public static void setView(AbstractView view) {
        Tool.view = view;
    }

    public static void setColour(int c) {
        markerColour = c;
    }

    public static void resetLasso() {
        resetLassoRequested = true;
    }

    public static void notifyToolChanged() {
        if (view.getTool() == Tool.MARKER || view.getTool() == Tool.ERASER) {
            resetLasso();
        }
    }

    public static int getColour() {
        return markerColour;
    }

    public float getLassoAntsTime() {
        return lassoAntsTime;
    }

    public Vector2f getSelectionCenter() {
        return selectionCenter;
    }

    public ArrayList<Vector2f> getLassoPath() {
        return lassoPath;
    }

    public void update(Delta delta, float time) {
        if (resetLassoRequested) {
            resetLassoRequested = false;
            lassoPath.clear();

            for (DrawnLine l : selectedLines) {
                l.setSelected(false);
                l.setHovered(false);
            }
            selectedLines.clear();

        }
        lassoAntsTime += delta.getSecs();
        Vector2f mousePos = view.getController().getPointerPosition().subtract(view.getCanvasOffset().to2f());
        if (view.getController().isPressedThisFrame()) {
            lastPos = mousePos.clone();
            dragging = true;
            dragStart(time, lastPos);
        } else if (view.getController().isPressed()) {
            Vector2f thisPos = view.getController().getPointerPosition().subtract(view.getCanvasOffset().to2f());
            if (!thisPos.equals(lastPos)) {
                dragLine(delta, time, new LineSegment(lastPos, thisPos));
            }
            lastPos = thisPos;
        } else if (dragging) {
            dragging = false;
            dragRelease(time);
        } else {
            // Hotkey -> ctrl+a selects all lines
            if (view.getController().isSelectAll()) {
                if (view.getTool() != Tool.MARKER) {
                    selectedLines.clear();
                    for (DrawnLine l : view.getLines()) {
                        selectedLines.add(l);
                        l.setSelected(true);
                    }
                }
            }
        }
        move(time, mousePos);
    }

    public abstract void dragLine(Delta delta, float time, LineSegment line);
    public abstract void dragRelease(float time);
    public abstract void dragStart(float time, Vector2f p);
    public void move(float time, Vector2f p) {}

    /**
     * @param time
     * @param centerInModelCoords Out param
     * @param centerInGlobalCoords Out param
     */
    private static void getSelectedLinesCenter(float time, Vector2f centerInModelCoords, Vector2f centerInGlobalCoords) {
        // Find selection center
        Vector2f modelMins = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
        Vector2f modelMaxes = new Vector2f(-Float.MAX_VALUE, -Float.MAX_VALUE);
        Vector2f globalMins = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
        Vector2f globalMaxes = new Vector2f(-Float.MAX_VALUE, -Float.MAX_VALUE);
        for (DrawnLine l : selectedLines) {
            LineSegment line = l;
            if (line.x1 < modelMins.getX()) modelMins.setX(line.x1);
            if (line.x2 < modelMins.getX()) modelMins.setX(line.x2);
            if (line.x1 > modelMaxes.getX()) modelMaxes.setX(line.x1);
            if (line.x2 > modelMaxes.getX()) modelMaxes.setX(line.x2);
            if (line.y1 < modelMins.getY()) modelMins.setY(line.y1);
            if (line.y2 < modelMins.getY()) modelMins.setY(line.y2);
            if (line.y1 > modelMaxes.getY()) modelMaxes.setY(line.y1);
            if (line.y2 > modelMaxes.getY()) modelMaxes.setY(line.y2);

            line = l.getTransformedLineAtTime(time);
            if (line.x1 < globalMins.getX()) globalMins.setX(line.x1);
            if (line.x2 < globalMins.getX()) globalMins.setX(line.x2);
            if (line.x1 > globalMaxes.getX()) globalMaxes.setX(line.x1);
            if (line.x2 > globalMaxes.getX()) globalMaxes.setX(line.x2);
            if (line.y1 < globalMins.getY()) globalMins.setY(line.y1);
            if (line.y2 < globalMins.getY()) globalMins.setY(line.y2);
            if (line.y1 > globalMaxes.getY()) globalMaxes.setY(line.y1);
            if (line.y2 > globalMaxes.getY()) globalMaxes.setY(line.y2);
        }
        centerInModelCoords.set((modelMins.getX() + modelMaxes.getX())/2, (modelMins.getY() + modelMaxes.getY())/2);
        centerInGlobalCoords.set( (globalMins.getX() + globalMaxes.getX()) / 2,  (globalMins.getY() + globalMaxes.getY()) / 2);
    }


    /**
     * Checks if the Polygon contains a point.
     * @see "http://alienryderflex.com/polygon/"
     * @param polygon The polygon
     * @param p Point checking containment.
     * @return Point is in Poly flag.
     */
    public boolean contains(ArrayList<Vector2f> polygon, Vector2f p) {
        int polySides = polygon.size();
        boolean oddTransitions = false;
        for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
            if( ( polygon.get(i).y < p.y && polygon.get(j).y >= p.y ) || ( polygon.get(j).y < p.y && polygon.get(i).y >= p.y ) ) {
                if( polygon.get(i).x + ( p.y - polygon.get(i).y ) / ( polygon.get(j).y - polygon.get(i).y) * ( polygon.get(j).x - polygon.get(i).x) < p.x ) {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }

    public String getImage() {
        return image;
    }

    public Vector2d getCursorCenter() {
        return cursorCenter;
    }

    public static void setSelectDistance(float d) {
        SELECT_DISTANCE = d;
    }
}
