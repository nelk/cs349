package quickmotion.android;

import android.graphics.Matrix;
import quickmotion.shared.Mat;
import quickmotion.shared.Vector2f;

/**
 *
 */
public class MatMatrix extends Mat {
    private Matrix t;

    public static void injectFactory() {
        Mat.factory = new MatMatrix(); // This instance is just used to access overridable 'static' methods in the abstract Mat
    }

    private MatMatrix() {
        t = new Matrix();
    }

    @Override
    protected Mat concreteNewInstance() {
        return new MatMatrix();
    }

    @Override
    protected Mat concreteNewInstance(Mat mat) {
        MatMatrix newInstance = new MatMatrix();
        newInstance.t = new Matrix(castOrFail(mat).t);
        return newInstance;
    }

    private static MatMatrix castOrFail(Mat mat) {
        if (mat instanceof MatMatrix) {
            return (MatMatrix) mat;
        }
        throw new RuntimeException("Attempted to cast Mat to MatMatrix, but Mat was of unknown implementation.");
    }

    public Matrix toMatrix() {
        return t;
    }

    @Override
    public Mat rotate(float r) {
        t.preRotate((float) Math.toDegrees(r));
        return this;
    }

    @Override
    public Mat translate(Vector2f v) {
        t.preTranslate(v.getX(), v.getY());
        return this;
    }

    @Override
    public Mat scale(Vector2f s) {
        t.preScale(s.getX(), s.getY());
        return this;
    }

    @Override
    public Mat inverse() {
        Matrix inverse = new Matrix();
        if (t.invert(inverse)) {
            t = inverse;
            return this;
        }
        return null;
    }

    /**
     * Transforms vectors in place.
     * @param vectors Any number of vectors to transform
     */
    @Override
    public void transform(Vector2f... vectors) {
        int n = vectors.length;
        float[] originalV = new float[n*2];
        float[] finalV = new float[n*2];
        int i = 0;
        for (Vector2f v : vectors) {
            originalV[i++] = v.getX();
            originalV[i++] = v.getY();
        }

        t.mapPoints(finalV, originalV);

        i = 0;
        for (Vector2f v : vectors) {
            v.setX(finalV[i++]);
            v.setY(finalV[i++]);
        }
    }

    /**
     * Transforms vectors in place.
     * @param vectors Any number of vectors to transform
     */
    @Override
    public boolean inverseTransform(Vector2f... vectors) {
        int n = vectors.length;
        float[] originalV = new float[n*2];
        float[] finalV = new float[n*2];
        int i = 0;
        for (Vector2f v : vectors) {
            originalV[i++] = v.getX();
            originalV[i++] = v.getY();
        }

        Matrix inverse = new Matrix();
        if (t.invert(inverse)) {
            inverse.mapPoints(finalV, originalV);

            i = 0;
            for (Vector2f v : vectors) {
                v.setX(finalV[i++]);
                v.setY(finalV[i++]);
            }
            return true;
        }
        return false;
    }

    @Override
    public Mat cat(Mat mat) {
        this.t.preConcat(castOrFail(mat).t);
        return this;
    }

    @Override
    public Mat preCat(Mat mat) {
        this.t.postConcat(castOrFail(mat).t);
        return this;
    }

    @Override
    public String serialize() {
        float[] flatMatrix = new float[9];
        t.getValues(flatMatrix);
        StringBuilder sb = new StringBuilder();
        sb.append(flatMatrix[0]);
        sb.append(',');
        sb.append(flatMatrix[3]);
        sb.append(',');
        sb.append(flatMatrix[1]);
        sb.append(',');
        sb.append(flatMatrix[4]);
        sb.append(',');
        sb.append(flatMatrix[2]);
        sb.append(',');
        sb.append(flatMatrix[5]);
        return sb.toString();
    }

    @Override
    public boolean deserialize(String s) {
        try {
            float[] flatMatrix = new float[9];
            String[] vals = s.split(",");
            flatMatrix[0] = Float.valueOf(vals[0]);
            flatMatrix[1] = Float.valueOf(vals[2]);
            flatMatrix[2] = Float.valueOf(vals[4]);
            flatMatrix[3] = Float.valueOf(vals[1]);
            flatMatrix[4] = Float.valueOf(vals[3]);
            flatMatrix[5] = Float.valueOf(vals[5]);
            flatMatrix[6] = 0f;
            flatMatrix[7] = 0f;
            flatMatrix[8] = 1f;
            this.t = new Matrix();
            this.t.setValues(flatMatrix);
            return true;
        } catch (NumberFormatException ignored) {
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }
}
