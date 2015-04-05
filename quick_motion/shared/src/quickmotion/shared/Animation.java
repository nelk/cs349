package quickmotion.shared;


import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class Animation implements Comparable<Animation> {
    public class Segment implements Comparable<Segment> {
        float time;
        Mat transform;
        public Segment(float time, Mat mat) {
            this.time = time;
            this.transform = mat;
        }

        @Override
        public int compareTo(Segment segment) {
            return Math.round(Math.signum(this.time - segment.time));
        }
    }

    public static enum Type {
        TRANSLATION, ROTATION, SCALE
    }

    public static class IncorrectAnimationType extends RuntimeException {
    }

    private static long id_counter = 0;
    private long id;
    private float startTime;
    private Type type;
    private ArrayList<Segment> segments;
    private Vector2f rotationReferencePoint;
    private int cachedSegIndex = -1;

    /**
     * Only use if deserializing.
     */
    public Animation() {
        this.startTime = 0;
        this.segments = new ArrayList<Segment>();
        this.type = null;
        this.rotationReferencePoint = null;
        this.id = 0;
    }

    public Animation(float startTime, Type type) {
        this.startTime = startTime;
        this.segments = new ArrayList<Segment>();
        this.type = type;
        this.rotationReferencePoint = new Vector2f();
        this.id = id_counter++;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    private int animationSegmentIndexAtTime(float t) {
        ArrayList<Segment> safeSegments = (ArrayList)SyncTools.synchronizedCopy(segments);
        int safeCachedSegIndex = cachedSegIndex;
        try {
            try {
                if (safeCachedSegIndex >= 0 && safeCachedSegIndex < safeSegments.size()) {
                    if (safeSegments.get(safeCachedSegIndex).time <= t && (safeCachedSegIndex >= safeSegments.size() - 1 || safeSegments.get(safeCachedSegIndex + 1).time > t)) {
                        return safeCachedSegIndex;
                    } else if (safeCachedSegIndex > 0 && safeSegments.get(safeCachedSegIndex - 1).time <= t && safeSegments.get(safeCachedSegIndex).time > t) {
                        return --safeCachedSegIndex;
                    } else if (safeSegments.get(safeCachedSegIndex).time <= t && safeSegments.get(safeCachedSegIndex + 1).time > t) {
                        return ++safeCachedSegIndex;
                    }
                }
            } catch (Exception e) {
            }

            int n = safeSegments.size();
            if (n == 0) {
                return -1;
            }
            int i = 0;
            int j = n;
            while (i != j && i + 1 != j) {
                if (t <= safeSegments.get((i + j)/2).time) {
                    j = (i + j)/2;
                } else {
                    i = (i + j)/2;
                }
            }
            cachedSegIndex = i;
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return safeSegments.size() - 1;
        }
    }

    public Segment animationSegmentAtTime(float t) {
        int idx = animationSegmentIndexAtTime(t);
        if (idx >= 0) {
            return segments.get(idx);
        }
        return null;
    }

    public void translate(float time, Vector2f delta, boolean newSegment) {
        if (type != Type.TRANSLATION) throw new IncorrectAnimationType();
        Mat newTransform;
        if (segments.size() == 0) newTransform = Mat.newInstance();
        else newTransform = Mat.newInstance(segments.get(segments.size() - 1).transform);
        newTransform.translate(delta);
        if (newSegment || segments.size() == 0) {
            segments.add(new Segment(time, newTransform));
        } else {
            segments.get(segments.size() - 1).transform = newTransform;
        }
    }

    public void rotate(float time, float delta, boolean newSegment) {
        if (type != Type.ROTATION) throw new IncorrectAnimationType();
        Mat newTransform;
        if (segments.size() == 0) newTransform = Mat.newInstance();
        else newTransform = Mat.newInstance(segments.get(segments.size() - 1).transform);
//        newTransform.translate(rotationReferencePoint.getX(), rotationReferencePoint.getY());
        newTransform.rotate(delta);
//        newTransform.translate(-rotationReferencePoint.getX(), -rotationReferencePoint.getY());
        if (newSegment || segments.size() == 0) {
            segments.add(new Segment(time, newTransform));
        } else {
            segments.get(segments.size() - 1).transform = newTransform;
        }
    }

    public void scale(float time, Vector2f delta, boolean newSegment) {
        if (type != Type.SCALE) throw new IncorrectAnimationType();
        Mat newTransform;
        if (segments.size() == 0) newTransform = Mat.newInstance();
        else newTransform = Mat.newInstance(segments.get(segments.size() - 1).transform);
//        newTransform.translate(rotationReferencePoint.getX(), rotationReferencePoint.getY());
        newTransform.scale(delta);
//        newTransform.translate(-rotationReferencePoint.getX(), -rotationReferencePoint.getY());
        if (newSegment || segments.size() == 0) {
            segments.add(new Segment(time, newTransform));
        } else {
            segments.get(segments.size() - 1).transform = newTransform;
        }
    }

    public void animateLine(DrawnLine line) {
        line.animate(this);
    }

    public void animateLines(Collection<DrawnLine> lines) {
        for (DrawnLine l : lines) {
            animateLine(l);
        }
    }

    public void insertTime(float time, float d) {
        int segIdx = animationSegmentIndexAtTime(time);
        for (int i = segIdx + 1; i < segments.size(); i++) {
            segments.get(i).time += d;
        }
    }

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Vector2f getRotationReferencePoint() {
        return rotationReferencePoint;
    }

    public void setRotationReferencePoint(Vector2f rotationReferencePoint) {
        this.rotationReferencePoint = rotationReferencePoint;
    }

    @Override
    public int compareTo(Animation animation) {
        return Math.round(Math.signum(this.startTime - animation.startTime));
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRotationReferencePoint().serialize());
        sb.append(',');
        sb.append(startTime);
        sb.append(',');
        sb.append(type.name());
        sb.append(',');
        for (Segment seg : segments) {
            sb.append(seg.time);
            sb.append(',');
            sb.append(seg.transform.serialize());
            sb.append(',');
        }
        // Remove last comma
        return sb.substring(0, sb.length() - 1);
    }

    public boolean deserialize(String s) {
        try {
            String[] vals = s.split(",");
            boolean worked = true;
            rotationReferencePoint = new Vector2f();
            worked = worked && rotationReferencePoint.deserialize(vals[0] + "," + vals[1]);
            if (!worked) return false;
            startTime = Float.parseFloat(vals[2]);
            type = Type.valueOf(vals[3]);
            for (int i = 4; i < vals.length; i+=7) {
                float time = Float.parseFloat(vals[i]);
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j < 7; j++) {
                    sb.append(vals[i + j]);
                    if (j != 6) sb.append(',');
                }
                Mat mat = Mat.newInstance();
                worked = worked && mat.deserialize(sb.toString());
                if (!worked) return false;
                segments.add(new Segment(time, mat));

            }
//            Collections.sort(segments); // Not necessary, always saved in order
            return true;
        } catch (NumberFormatException ignored) {
        } catch (IndexOutOfBoundsException ignored) {
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }
}
