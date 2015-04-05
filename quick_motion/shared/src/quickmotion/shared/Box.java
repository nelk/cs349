package quickmotion.shared;

/**
 *
 */
public class Box implements Cloneable{
    private Vector2f topLeft;
    private Vector2f bottomRight;

    Box() {}

    /**
     * Create a box from its top left corner and its size
     * @param pos Top left corner of box
     * @param size Size of box as (width, height)
     */
    public static Box boxFromPositionAndSize(Vector2f pos, Vector2f size) {
        Box box = new Box();
        box.topLeft = pos.clone();
        box.bottomRight = Vector2f.add(pos, size);
        return box;
    }

    /**
     * Create a box from two opposite corners
     * @param p1 One corner
     * @param p2 Corner across from it (non-adjacent)
     */
    public static Box boxFromTwoPoints(Vector2f p1, Vector2f p2) {
        Box box = new Box();
        box.topLeft = new Vector2f(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
        box.bottomRight = new Vector2f(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
        return box;
    }
    
    /**
     * Find the intersecting rectangle between two boxes.
     * @param other
     * @return The intersecting box, if this box and the other box intersect, otherwise null
     */
    public Box intersection(Box other) {
        Vector2f tl = new Vector2f(Math.max(this.getLeft(), other.getLeft()), Math.max(this.getTop(), other.getTop())); //Position of top left corner of intersection rectangle
        Vector2f br = new Vector2f(Math.min(this.getRight(), other.getRight()),
                Math.min(this.getBottom(), other.getBottom()));
        if (tl.x <= br.x && tl.y <= br.y) {
            return boxFromTwoPoints(tl, br);
        } else {
            return null;
        }

    }

    /**
     * Directly scale the line according to its current relative position
     * (Multiply its absolute position by factor)
     * @param factor
     */
    public void scale(float factor) {
        topLeft.scalarMultiple(factor);
        bottomRight.scalarMultiple(factor);
    }

    /**
     * @return A point in the center of this box
     */
    public Vector2f getCenter() {
        return Vector2f.add(topLeft, Vector2f.scalarMultiple(Vector2f.subtract(bottomRight, topLeft), 0.5f));
    }
    
    public boolean isPointWithin(Vector2f point) {
        return point.x >= getLeft() && point.x <= getRight() && point.y >= getTop() && point.y <= getBottom();
    }
    
    public void translate(Vector2f offset) {
        this.topLeft.add(offset);
        this.bottomRight.add(offset);
    }

    public Vector2f getTopLeft() {
        return topLeft.clone();
    }
    public Vector2f getBottomRight() {
        return bottomRight.clone();
    }
    public Vector2f getTopRight() {
        return Vector2f.add(getTopLeft(), new Vector2f(getWidth(), 0));
    }
    public Vector2f getBottomLeft() {
        return Vector2f.subtract(getBottomRight(), new Vector2f(getWidth(), 0));
    }
    public float getLeft() {
        return topLeft.x;
    }
    public float getRight() {
        return bottomRight.x;
    }
    public float getTop() {
        return topLeft.y;
    }
    public float getBottom() {
        return bottomRight.y;
    }
    public float getWidth() {
        return bottomRight.x - topLeft.x;
    }
    public float getHeight() {
        return bottomRight.y - topLeft.y;
    }
    public Vector2f getSize() {
        return new Vector2f(getWidth(), getHeight());
    }

    @Override
    public Box clone() {
        try {
            Box box = (Box)super.clone();
            box.topLeft = box.topLeft.clone();
            box.bottomRight = box.bottomRight.clone();
            return box;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (bottomRight != null ? !bottomRight.equals(box.bottomRight) : box.bottomRight != null) return false;
        if (topLeft != null ? !topLeft.equals(box.topLeft) : box.topLeft != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = topLeft != null ? topLeft.hashCode() : 0;
        result = 31 * result + (bottomRight != null ? bottomRight.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Box{" + topLeft.toString() + ", " + bottomRight.toString() + "}";
    }
}
