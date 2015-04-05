package cranesim.tools;

/**
 *
 */
public class DirectManipManager {
    private static DirectManipManager singleton;
    DirectlyManip underMouse;
    DirectlyManip underMouseControl;
    private boolean goodState = true;
    private boolean goodStateChanged = false;

    public static DirectManipManager getInstance() {
        if (singleton == null) {
            singleton = new DirectManipManager();
        }
        return singleton;
    }

    public void update(Delta delta) {
        if (!goodStateChanged) {
            goodState = true;
        }
        goodStateChanged = false;
    }

    public boolean isAnyControlled() {
        return underMouseControl != null;
    }

    public boolean isGoodState() {
        return goodState;
    }

    public void setGoodState(boolean goodState) {
        this.goodState = goodState;
        this.goodStateChanged = true;
    }

    public boolean requestHighlight(DirectlyManip me) {
        if (underMouseControl == null && (underMouse == null || underMouse.getPriority() < me.getPriority())) {
            underMouse = me;
            return true;
        }
        return false;
    }

    public boolean requestControl(DirectlyManip me) {
        if (underMouseControl == null || underMouseControl.getPriority() < me.getPriority()) {
            underMouse = null;
            underMouseControl = me;
            return true;
        }
        return false;
    }

    public void releaseHighlight(DirectlyManip me) {
        if (underMouse == me) underMouse = null;
    }

    public void releaseControl(DirectlyManip me) {
        if (underMouseControl == me) underMouseControl = null;
    }

    public boolean isHighlighted(DirectlyManip me) {
        return me == underMouse;
    }

    public boolean isControlled(DirectlyManip me) {
        return me == underMouseControl;
    }

}
