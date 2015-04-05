package cranesim.tools;

/**
 *
 */
public class LineSegment implements Cloneable {
    protected Vector2f p1, p2;
    
    public LineSegment(Vector2f p1, Vector2f p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Vector2f getP1() {
        return p1;
    }

    public Vector2f getP2() {
        return p2;
    }

    public void translate(Vector2f offset) {
        p1.add(offset);
        p2.add(offset);
    }
    
    public Vector2f intersection(LineSegment other) {
        // Line1 = a1 + ta
        Vector2f a1 = this.p1, a = this.getDirection();
        // Line2 = b1 + sb
        Vector2f b1 = other.p1, b = other.getDirection();

        //Solve for parameters s and t
        float s, t;
        if (a.x == 0) {
            // Special case for Line1 being vertical
            s = (a1.x - b1.x)/b.x;
            t = (b1.y + s*b.y - a1.y)/a.y;
        } else {
            float ayOverax = a.y / a.x;
            s = ((b1.y - a1.y) - (b1.x - a1.x) * ayOverax) / (b.x * ayOverax - b.y);
            t = (b1.x - a1.x + s * b.x) / a.x;
        }

        //If 0 <= s,t <= 1 then the intersection is within the line segments
        if (0 <= s && s <= 1 && 0 <= t && t <= 1) {
            return Vector2f.add(b1, Vector2f.scalarMultiple(b, s));
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
        p1.scalarMultiple(factor);
        p2.scalarMultiple(factor);
    }

    /**
     * Scale with positions being stretched/compressed from a relative point.
     * This will scale the line proportions themselves, but also move the line.
     * Useful when scaling something composed of many lines where they have offsets
     *  from a single point in the larger object (but they all have absolute position).
     * @param relativePoint The point line should be scaled with respect to
     * @param factor
     */
    public void scaleRelativeTo(Vector2f relativePoint, float factor) {
        translate(Vector2f.negate(relativePoint));
        scale(factor);
        translate(relativePoint);
    }

    /**
     * Scale relative to line's center
     * @param factor
     */
    public void scaleInPlace(float factor) {
        scaleRelativeTo(getCenter(), factor);
    }

    /**
     * @return A point that bisects this line.
     */
    public Vector2f getCenter() {
        return Vector2f.add(p1, Vector2f.scalarMultiple(Vector2f.subtract(p2, p1), 0.5f));
    }

    public Vector2f getDirection() {
        return Vector2f.subtract(p2, p1);
    }

    public Box getBoundingBox() {
        return Box.boxFromTwoPoints(p1, p2);
    }

    @Override
    public LineSegment clone() {
        try {
            LineSegment lineSegment = (LineSegment)super.clone();
            lineSegment.p1 = lineSegment.p1.clone();
            lineSegment.p2 = lineSegment.p2.clone();
            return lineSegment;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
