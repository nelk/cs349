package quickmotion.shared;

import java.io.Serializable;

/**
 *
 */
public class LineSegment implements Cloneable, Serializable {
    public float x1, x2, y1, y2;

    public LineSegment(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public LineSegment(Vector2f p1, Vector2f p2) {
        this.x1 = p1.getX();
        this.y1 = p1.getY();
        this.x2 = p2.getX();
        this.y2 = p2.getY();
    }

    public LineSegment translate(Vector2f offset) {
        this.x1 += offset.getX();
        this.x2 += offset.getX();
        this.y1 += offset.getY();
        this.y2 += offset.getY();
        return this;
    }

    public Vector2f getP1() {
        return new Vector2f(x1, y1);
    }

    public Vector2f getP2() {
        return new Vector2f(x2, y2);
    }

    public Vector2f intersection(LineSegment other) {
        if (this.getP1().equals(this.getP2()) || other.getP1().equals(other.getP2())) {
            return null;
        }
        // Line1 = a1 + ta
        Vector2f a1 = getP1(), a = this.toVector();
        // Line2 = b1 + sb
        Vector2f b1 = other.getP1(), b = other.toVector();

        //Solve for parameters s and t
        float s, t;
        if (a.getX() == 0) {
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


    public Vector2f closestPointToPoint(Vector2f p) {
        final Vector2f v = toVector();
        final float lengthSquared = v.x*v.x + v.y*v.y;
        if (lengthSquared == 0.0) return getP1(); // Line segment has 0 length
        final float t = Vector2f.dotProduct(Vector2f.subtract(p, getP1()), v) / lengthSquared;
        if (t < 0.0) return getP1();
        else if (t > 1.0) return getP2();
        return getP1().add(v.scalarMultiple(t));  // Projection onto line segment
    }

    public double distanceFromPoint(Vector2f p) {
        Vector2f closest = closestPointToPoint(p);
        return closest.subtract(p).magnitude();
    }

    /**
     * Directly scale the line according to its current relative position
     * (Multiply its absolute position by factor)
     * @param factor
     */
    public void scale(float factor) {
        x1 *= factor;
        x2 *= factor;
        y1 *= factor;
        y1 *= factor;
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
        return Vector2f.add(getP1(), Vector2f.scalarMultiple(Vector2f.subtract(getP2(), getP1()), 0.5f));
    }

    public Vector2f toVector() {
        return Vector2f.subtract(getP2(), getP1());
    }

    public Box getBoundingBox() {
        return Box.boxFromTwoPoints(getP1(), getP2());
    }

    @Override
    public LineSegment clone() {
        return new LineSegment(x1, y1, x2, y2);
    }

    public String serialize() {
        return getP1().serialize() + "," + getP2().serialize();
    }

    public boolean deserialize(String s) {
        // TODO - rollback if some fail
        try {
            String[] vals = s.split(",");
            if (vals.length != 4) {
                return false;
            }
            x1 = java.lang.Float.parseFloat(vals[0]);
            y1 = java.lang.Float.parseFloat(vals[1]);
            x2 = java.lang.Float.parseFloat(vals[2]);
            y2 = java.lang.Float.parseFloat(vals[3]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
