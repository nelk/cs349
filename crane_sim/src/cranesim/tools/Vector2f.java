package cranesim.tools;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

/**
 * @author Alex
 */
public class Vector2f implements Cloneable {
    public static final Vector2f ZERO_VECTOR = new Vector2f() {
        @Override
        public Vector2f add(Vector2f a) {
            return this;
        }
        @Override
        public Vector2f subtract(Vector2f a) {
            return this;
        }
        @Override
        public Vector2f scalarMultiple(float m) {
            return this;
        }
    };
    public float x, y;

    public Vector2f() {
        x = 0;
        y = 0;
    }

    public Vector2f(float ix, float iy) {
        x = ix;
        y = iy;
    }

    public void set(float ix, float iy) {
        x = ix;
        y = iy;
    }

    public void set(Vector2f v) {
        x = v.x;
        y = v.y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Vector2f add(Vector2f a) {
        this.x += a.x;
        this.y += a.y;
        return this;
    }

    public Vector2f subtract(Vector2f a) {
        this.x -= a.x;
        this.y -= a.y;
        return this;
    }

    public Vector2f scalarMultiple(float m) {
        this.x *= m;
        this.y *= m;
        return this;
    }

    public Vector2f reverse() {
        return scalarMultiple(-1f);
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }
    
    public float angle() {
        return (float) Math.atan2(y, x);
    }

    public Vector2f unit() {
        return Vector2f.scalarMultiple(this, 1 / this.magnitude());
    }

    public static Vector2f add(Vector2f a, Vector2f b) {
        return new Vector2f(a.x + b.x, a.y + b.y);
    }

    public static Vector2f subtract(Vector2f a, Vector2f b) {
        return new Vector2f(a.x - b.x, a.y - b.y);
    }

    public static Vector2f scalarMultiple(Vector2f a, float m) {
        return new Vector2f(a.x * m, a.y * m);
    }

    public static float dotProduct(Vector2f a, Vector2f b) {
        return a.x * b.x + a.y * b.y;
    }

    //Project a onto b
    public static Vector2f project(Vector2f a, Vector2f b) {
        return scalarMultiple(b, dotProduct(a, b) / (b.x * b.x + b.y * b.y));
    }

    //Returns the angle between a and b (in radians)
    public static float angle(Vector2f a, Vector2f b) {
        return (float) Math.acos(Vector2f.dotProduct(a, b) / (a.magnitude() * b.magnitude()));
    }

    //Flips vector around
    public static Vector2f negate(Vector2f v) {
        return Vector2f.scalarMultiple(v, -1f);
    }

    public static Vector2f perpendicular(Vector2f v) {
        return new Vector2f(-v.y, v.x);
    }

    public Vector2d to2d() {
        return new Vector2d(Math.round(x), Math.round(y));
    }

    @Override
    public final Vector2f clone() {
        try {
            return (Vector2f)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector2f vector2f = (Vector2f) o;

        if (Float.compare(vector2f.x, x) != 0) return false;
        if (Float.compare(vector2f.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Transforms vectors in place.
     * @param transform The AffineTransform
     * @param useInverse True to use the inverse transform; false for the forward transform
     * @param vectors Any number of vectors to transform
     */
    public static void transformVectorsWithAffineTransform(AffineTransform transform, boolean useInverse, Vector2f... vectors) {
        int n = vectors.length;
        double[] originalV = new double[n*2];
        double[] finalV = new double[n*2];
        int i = 0;
        for (Vector2f v : vectors) {
            originalV[i++] = v.getX();
            originalV[i++] = v.getY();
        }

        try {
            if (useInverse) {
                transform.inverseTransform(originalV, 0, finalV, 0, n);
            } else {
                transform.transform(originalV, 0, finalV, 0, n);
            }

            i = 0;
            for (Vector2f v : vectors) {
                v.setX((float)finalV[i++]);
                v.setY((float)finalV[i++]);
            }
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return;
        }
    }
}
