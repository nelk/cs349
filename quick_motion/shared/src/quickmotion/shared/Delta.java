package quickmotion.shared;

/**
 *
 */
public class Delta {
    private long millis;
    private float secs;
    public Delta(long millis) {
        setMillis(millis);
    }
    public Delta(float secs) {
        setSecs(secs);
    }
    public void add(Delta d) {
        setMillis(this.millis + d.millis);
    }
    public long getMillis() {
        return millis;
    }
    public float getSecs() {
        return secs;
    }
    public void setMillis(long millis) {
        this.millis = millis;
        this.secs = millis / 1000f;
    }
    public void setSecs(float secs) {
        this.secs = secs;
        this.millis = Math.round(secs * 1000);
    }
}
