package quickmotion.shared;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;

/**
 *
 */
public class DrawnLine extends LineSegment {
    private ArrayList<Animation> animations;
    float createTime;
    float deleteTime;
    private int colour;
    private int selectColour;
    private boolean selected;
    private boolean hovered;
    private Figure figure;

    public DrawnLine() {
        super(new Vector2f(), new Vector2f());
        animations = new ArrayList<Animation>();
    }

    public DrawnLine(Vector2f p1, Vector2f p2, int colour) {
        this(p1, p2, colour, new ArrayList<Animation>());
    }

    public DrawnLine(Vector2f p1, Vector2f p2, int colour, ArrayList<Animation> anims) {
        super(p1, p2);
        createTime = 0f;
        deleteTime = TimeLine.MAX_TIME;
        animations = anims;

        this.selected = false;
        this.colour = colour;

        float[] hsb = new float[3];
//        Color.RGBtoHSB(this.colour.getRed(), this.colour.getBlue(), this.colour.getGreen(), hsb);
//        hsb[0] = (hsb[0] + 0.5f);
//        if (hsb[0] > 1f) hsb[0] -= 1f;
//        this.selectColour = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        this.selectColour = 0x00FFFFFF ^ colour;
    }

    public boolean existsAt(float time) {
        return time >= getCreateTime() && time < getDeleteTime();
    }


    private void applyTransformHelper(HashMap<Animation.Type, Animation> anims, HashMap<Animation.Type, Mat> lastTransform, HashMap<Animation, Vector2f> baseAnimReferencePosition, Animation.Segment seg, Animation.Type type, Mat transform) {
        if (seg != null) {
            Mat newTransform = Mat.newInstance(seg.transform);
            Mat savingTransform = Mat.newInstance(newTransform);
            // Pad rotation and scale with movement to point
            Vector2f center = null;
            if (type == Animation.Type.ROTATION || type == Animation.Type.SCALE) {
                center = anims.get(type).getRotationReferencePoint().clone();
                transform.transform(center);
                center.subtract(baseAnimReferencePosition.get(anims.get(type)));
                newTransform.cat(Mat.newInstance().translate(Vector2f.reverse(center)));
                newTransform.preCat(Mat.newInstance().translate(center));
            }

            Mat existingTransform = Mat.newInstance();
            if (lastTransform.containsKey(type)) {
                existingTransform = lastTransform.get(type).inverse();
                if (existingTransform != null && (type == Animation.Type.ROTATION || type == Animation.Type.SCALE)) {
                    savingTransform.cat(existingTransform);
                    existingTransform.cat(Mat.newInstance().translate(Vector2f.reverse(center)));
                    existingTransform.preCat(Mat.newInstance().translate(center));
                }
            }
            newTransform.cat(existingTransform);
            lastTransform.put(type, savingTransform);
            transform.preCat(newTransform);
        }
    }

    private static Animation.Type[] transformOrder = new Animation.Type[]{Animation.Type.ROTATION, Animation.Type.SCALE, Animation.Type.TRANSLATION};
    public Mat getTransformAtTime(float time) {
        HashMap<Animation.Type, Animation> anims = new HashMap<Animation.Type, Animation>(3);
        HashMap<Animation.Type, Mat> lastTransform = new HashMap<Animation.Type, Mat>(3);
        HashMap<Animation, Vector2f> baseAnimReferencePosition = new HashMap<Animation, Vector2f>();
        Mat transform = Mat.newInstance();

        for (Animation a : SyncTools.synchronizedCopy(animations)) {
            if (a.getStartTime() <= time) {
                for (Animation.Type type : transformOrder) {
                    if (anims.containsKey(type)) {
                        Animation.Segment seg = anims.get(type).animationSegmentAtTime(a.getStartTime());
                        applyTransformHelper(anims, lastTransform, baseAnimReferencePosition, seg, type, transform);
                    }
                }
                anims.put(a.getType(), a);
                lastTransform.remove(a.getType());

                Vector2f baseRotationReference = a.getRotationReferencePoint().clone();
                transform.transform(baseRotationReference);
                baseRotationReference.subtract(a.getRotationReferencePoint());
                baseAnimReferencePosition.put(a, baseRotationReference);


            } else {
                break;
            }
        }

        for (Animation.Type type : transformOrder) {
            Animation a = anims.get(type);
            if (a == null) continue;
            Animation.Segment seg = a.animationSegmentAtTime(time);
            applyTransformHelper(anims, lastTransform, baseAnimReferencePosition, seg, type, transform);
        }
        return transform;
    }

    public LineSegment getTransformedLineAtTime(float time) {
        Mat transform = getTransformAtTime(time);
        Vector2f[] transformed = new Vector2f[]{ getP1(), getP2() };
        transform.transform(transformed);
        return new LineSegment(new Vector2f(transformed[0]), new Vector2f(transformed[1]));
    }

    public float getCreateTime() {
        return createTime;
    }

    public void setCreateTime(float createTime) {
        this.createTime = createTime;
    }

    public float getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(float deleteTime) {
        this.deleteTime = deleteTime;
    }

    public void animate(Animation animation) {
        synchronized (animations) {
            // Remove all animations of the same type as this one that occur after this one
            ListIterator<Animation> it = animations.listIterator();
            while (it.hasNext()) {
                Animation a = it.next();
                if (a.getType() == animation.getType() && a.getStartTime() > animation.getStartTime()) it.remove();
            }
            animations.add(animation);
            if (animations.size() > 1 && animations.get(animations.size() - 2).compareTo(animation) > 0) {
                Collections.sort(animations);
            }
        }
    }

    /**
     * Shouldn't need to use these normally - only for saving to disk
     */
    public ArrayList<Animation> getAnimations() {
        return animations;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

    public int getSelectColour() {
        return selectColour;
    }

    @Override
    public DrawnLine clone() {
        DrawnLine l = new DrawnLine(new Vector2f(x1, y1), new Vector2f(x2, y2), this.colour);
        l.createTime = this.createTime;
        l.deleteTime = this.deleteTime;
        return l;
    }

    public String serialize() {
        return String.valueOf(createTime) + "," + String.valueOf(deleteTime) + "," + super.serialize();
    }

    public boolean deserialize(String s) {
        try {
            String[] vals = s.split(",");
            if (vals.length != 6) {
                return false;
            }
            createTime = java.lang.Float.parseFloat(vals[0]);
            deleteTime = java.lang.Float.parseFloat(vals[1]);
            super.deserialize(vals[2] + "," + vals[3] + "," + vals[4] + "," + vals[5]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
