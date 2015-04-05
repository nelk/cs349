package cranesim.tools;

/**
 * @author Alex
 */
public class Vector2d implements Cloneable {

    public int x, y;

    public Vector2d() {
        x = 0;
        y = 0;
    }

    public Vector2d(int ix, int iy) {
        x = ix;
        y = iy;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Vector2d add(Vector2d a) {
        this.x += a.x;
        this.y += a.y;
        return this;
    }

    public Vector2d subtract(Vector2d a) {
        this.x -= a.x;
        this.y -= a.y;
        return this;
    }

    public Vector2d scalarMultiple(int m) {
        this.x *= m;
        this.y *= m;
        return this;
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float angle() {
        return (float) Math.atan2(y, x);
    }

    public Vector2f unit() {
        return Vector2d.scalarMultiple(this, 1 / this.magnitude());
    }

    public static Vector2d add(Vector2d a, Vector2d b) {
        return new Vector2d(a.x + b.x, a.y + b.y);
    }

    public static Vector2d subtract(Vector2d a, Vector2d b) {
        return new Vector2d(a.x - b.x, a.y - b.y);
    }

    public static Vector2d scalarMultiple(Vector2d a, int m) {
        return new Vector2d(a.x * m, a.y * m);
    }

    public static Vector2f scalarMultiple(Vector2d a, float m) {
        return new Vector2f(a.x * m, a.y * m);
    }

    public static int dotProduct(Vector2d a, Vector2d b) {
        return a.x * b.x + a.y * b.y;
    }

    //Project a onto b
    public static Vector2d project(Vector2d a, Vector2d b) {
        return scalarMultiple(b, dotProduct(a, b) / (b.x * b.x + b.y * b.y));
    }

    //Returns the angle between a and b
    public static float angle(Vector2d a, Vector2d b) {
        return (float) Math.acos(Vector2d.dotProduct(a, b) / (a.magnitude() * b.magnitude()));
    }

    public Vector2f to2f() {
        return new Vector2f(x, y);
    }

    @Override
    public Vector2d clone() {
        try {
            return (Vector2d)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
