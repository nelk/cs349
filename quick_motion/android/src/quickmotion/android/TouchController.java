package quickmotion.android;

import android.view.MotionEvent;
import quickmotion.shared.*;

/**
 *
 */
public class TouchController implements AbstractController {

    // view abstract methods
    @Override
    public Vector2f getPointerPosition() {
        return touch.getPos();
    }

    @Override
    public boolean isPressedThisFrame() {
        return touch.isPressedThisFrame();
    }

    @Override
    public boolean isPressed() {
        return touch.isPressed();
    }

    @Override
    public boolean isSelectAll() {
        return false;
    }

    @Override
    public boolean isAnimated() {
        return view.shouldAnimate();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPartial() {
        return true;
    }

    public static class Touch {
        Vector2f pos = new Vector2f();
        Vector2f lastPos = new Vector2f();
        Vector2f startDrag = new Vector2f();
        Vector2f endDrag = new Vector2f();
        Delta held = new Delta(0);
        Delta released = new Delta(0);
        Delta last = new Delta(0);
        boolean pressed = false;
        boolean pressedThisFrame = false;
        boolean processedPressedThisFrame = false;
        boolean releasedThisFrame = false;

        public Vector2f getMoveVector() {
            if (lastPos == null) {
                return new Vector2f(0, 0);
            }
            return Vector2f.subtract(lastPos, pos);
        }

        // Immutable
        public Vector2f getPos() {
            return pos.clone();
        }

        // Immutable
        public Vector2f getStartDrag() {
            return startDrag.clone();
        }

        public boolean isPressed() {
            return pressed;
        }

        public boolean isPressedThisFrame() {
            return pressedThisFrame;
        }
    }

    private Touch touch;
    private AbstractView view;

    public TouchController(AbstractView view) {
        touch = new Touch();
        this.view = view;
    }

    public Touch getTouch() {
        return touch;
    }

    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            int lastPoint = e.getPointerCount() - 1;
            Vector2f m = new Vector2f(e.getX(lastPoint), e.getY(lastPoint));
            touch.startDrag = m.add(view.getCanvasOffset().to2f());
            touch.pos = touch.startDrag;
            touch.pressed = true;
            touch.pressedThisFrame = true;
            touch.held = new Delta(0);
            return true;
        } else if (e.getAction() == MotionEvent.ACTION_UP) {
            int lastPoint = e.getPointerCount() - 1;
            Vector2f m = new Vector2f(e.getX(lastPoint), e.getY(lastPoint));
            touch.endDrag = m.add(view.getCanvasOffset().to2f());
            touch.pos = touch.endDrag;
            touch.pressed = false;
            touch.releasedThisFrame = true;
            touch.last = touch.held;
            return true;
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            int lastPoint = e.getPointerCount() - 1;
            Vector2f m = new Vector2f(e.getX(lastPoint), e.getY(lastPoint));
            touch.lastPos = touch.pos;
            touch.pos = m.add(view.getCanvasOffset().to2f());
            return true;
        }
        return false;
    }

    public void update (Delta delta) {
        // Fix for bug where pressedThisFrame stays on
        if (touch.pressedThisFrame && (touch.processedPressedThisFrame || !touch.pressed)) {
            touch.pressedThisFrame = false;
            touch.processedPressedThisFrame = false;
        } else if (touch.pressedThisFrame) {
            touch.processedPressedThisFrame = true;
        }
        if (touch.releasedThisFrame && (touch.released.getMillis() > 0 || touch.pressed)) {
            touch.releasedThisFrame = false;
        }
        if (touch.pressed) {
            touch.held.add(delta);
        } else {
            touch.released.add(delta);
        }
    }

    public void clearInput() {
        touch.pressed = false;
        touch.pressedThisFrame = false;
        touch.releasedThisFrame = false;
        touch.last = touch.held;
        touch.held = new Delta(0);
    }
}
