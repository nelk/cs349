package quickmotion.desktop;

import quickmotion.shared.Mat;
import quickmotion.shared.Vector2f;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

/**
 *
 */
public class MatAffineTransform extends Mat {
    private AffineTransform t;

    public static void injectFactory() {
        Mat.factory = new MatAffineTransform(); // This instance is just used to access overridable 'static' methods in the abstract Mat
    }

    private MatAffineTransform() {
        t = new AffineTransform();
    }

    @Override
    protected Mat concreteNewInstance() {
        return new MatAffineTransform();
    }

    @Override
    protected Mat concreteNewInstance(Mat mat) {
        MatAffineTransform newInstance = new MatAffineTransform();
        newInstance.t = new AffineTransform(castOrFail(mat).t);
        return newInstance;
    }

    private static MatAffineTransform castOrFail(Mat mat) {
        if (mat instanceof MatAffineTransform) {
            return (MatAffineTransform) mat;
        }
        throw new RuntimeException("Attempted to cast Mat to MatAffineTransform, but Mat was of unknown implementation.");
    }

    public AffineTransform toAffineTransform() {
        return t;
    }

    @Override
    public Mat rotate(float r) {
        t.rotate(r);
        return this;
    }

    @Override
    public Mat translate(Vector2f v) {
        t.translate(v.getX(), v.getY());
        return this;
    }

    @Override
    public Mat scale(Vector2f s) {
        t.scale(s.getX(), s.getY());
        return this;
    }

    @Override
    public Mat inverse() {
        try {
            t = t.createInverse();
        } catch (NoninvertibleTransformException e) {
            return null;
        }
        return this;
    }

    /**
     * Transforms vectors in place.
     * @param vectors Any number of vectors to transform
     */
    @Override
    public void transform(Vector2f... vectors) {
        int n = vectors.length;
        double[] originalV = new double[n*2];
        double[] finalV = new double[n*2];
        int i = 0;
        for (Vector2f v : vectors) {
            originalV[i++] = v.getX();
            originalV[i++] = v.getY();
        }

        t.transform(originalV, 0, finalV, 0, n);

        i = 0;
        for (Vector2f v : vectors) {
            v.setX((float)finalV[i++]);
            v.setY((float)finalV[i++]);
        }
    }

    /**
     * Transforms vectors in place.
     * @param vectors Any number of vectors to transform
     */
    @Override
    public boolean inverseTransform(Vector2f... vectors) {
        int n = vectors.length;
        double[] originalV = new double[n*2];
        double[] finalV = new double[n*2];
        int i = 0;
        for (Vector2f v : vectors) {
            originalV[i++] = v.getX();
            originalV[i++] = v.getY();
        }

        try {
            t.inverseTransform(originalV, 0, finalV, 0, n);

            i = 0;
            for (Vector2f v : vectors) {
                v.setX((float)finalV[i++]);
                v.setY((float)finalV[i++]);
            }
            return true;
        } catch (NoninvertibleTransformException e) {
            return false;
        }
    }

    @Override
    public Mat cat(Mat mat) {
        this.t.concatenate(castOrFail(mat).t);
        return this;
    }

    @Override
    public Mat preCat(Mat mat) {
        this.t.preConcatenate(castOrFail(mat).t);
        return this;
    }

    @Override
    public String serialize() {
        double[] flatMatrix = new double[6];
        t.getMatrix(flatMatrix);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(String.valueOf((float)flatMatrix[i]));
            if (i < 5) sb.append(',');
        }
        return sb.toString();
    }

    @Override
    public boolean deserialize(String s) {
        try {
            float[] flatMatrix = new float[6];
            String[] vals = s.split(",");
            for (int i = 0; i < 6; i++) {
                flatMatrix[i] = Float.valueOf(vals[i]);
            }
            this.t = new AffineTransform(flatMatrix);
            return true;
        } catch (NumberFormatException ignored) {
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }
}
