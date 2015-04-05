package quickmotion.shared;

/**
 * Abstract Matrix/AffineTransform
 * For compatibility with both desktop and android applications
 */
public abstract class Mat {
    // Mat is an abstract factory itself
    protected static Mat factory;

    /**
     * The only way to create new instances of the abstract Mat
     */
    public static Mat newInstance() {
        return factory.concreteNewInstance();
    }
    public static Mat newInstance(Mat mat) {
        return factory.concreteNewInstance(mat);
    }

    protected abstract Mat concreteNewInstance();
    protected abstract Mat concreteNewInstance(Mat mat);

    // Note that these methods return 'this' (builder)
    public abstract Mat rotate(float r);
    public abstract Mat translate(Vector2f t);
    public abstract Mat scale(Vector2f s);

    /**
     * Returns inverse matrix
     * @return inverse, or null if not invertible
     */
    public abstract Mat inverse();

    public abstract void transform(Vector2f... vectors);

    /**
     * Applies inverse of matrix to each vector
     * @param vectors Vectors to transform in place -> will remain unchanged if matrix is not invertible
     * @return True if invertible
     */
    public abstract boolean inverseTransform(Vector2f... vectors);


    /**
     * Concatenate matrix
     * @param mat
     * @return 'this'
     */
    public abstract Mat cat(Mat mat);
    public abstract Mat preCat(Mat mat);

    public abstract String serialize();
    public abstract boolean deserialize(String s);
}
