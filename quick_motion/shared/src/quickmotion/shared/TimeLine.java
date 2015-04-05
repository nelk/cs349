package quickmotion.shared;

/**
 *
 */
public class TimeLine {
    public static final float STEP_SIZE = 0.1f;
    public static final float STARTING_MAX_TIME = 10.0f;
    public static final float MAX_TIME = Float.MAX_VALUE;

    private float stepFactor = 1f;
    private float maxObservedTime = STARTING_MAX_TIME;
    private float time;
    private boolean playing = false;

    public TimeLine() {
        this.time = 0f;
    }

    public float getTime() {
        return time;
    }

    public void increment() {
        setTime(getTime() + STEP_SIZE*stepFactor);
    }

    public void decrement() {
        setTime(getTime() - STEP_SIZE*stepFactor);
    }

    public void setTime(float time) {
        this.time = Math.max(Math.min(time, MAX_TIME), 0);
        this.maxObservedTime = Math.max(this.maxObservedTime, this.time);
    }

    public float getMaxObservedTime() {
        return maxObservedTime;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public float doTimeStep(Delta delta) {
        setTime(this.time + delta.getSecs()*stepFactor);
        return this.time;
    }

    public void update(Delta delta) {
        if (isPlaying()) {
            if (getTime() >= getMaxObservedTime()) {
                setPlaying(false);
            } else {
                doTimeStep(delta);
            }
        }
    }

    public void insertTime(float t) {
        maxObservedTime += t;
    }

    public void reset() {
        maxObservedTime = STARTING_MAX_TIME;
        time = 0f;
    }

    public float getStepFactor() {
        return stepFactor;
    }

    public void setStepFactor(float stepFactor) {
        this.stepFactor = stepFactor;
    }
}
