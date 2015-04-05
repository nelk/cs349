package quickmotion.desktop;

import quickmotion.shared.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 *
 */
public class InputController implements AbstractController {

    // view abstract methods
    @Override
    public Vector2f getPointerPosition() {
        return mouse.getPos().to2f();
    }

    @Override
    public boolean isPressedThisFrame() {
        return mouse.isPressedThisFrame();
    }

    @Override
    public boolean isPressed() {
        return mouse.isPressed();
    }

    @Override
    public boolean isSelectAll() {
        return Key.CTRL.isPressed() && Key.A.isPressedThisFrame();
    }

    @Override
    public boolean isAnimated() {
        return Key.CTRL.isPressed();
    }

    @Override
    public boolean isPartial() {
        return !Key.CTRL.isPressed();
    }


    public static class Mouse {
        Vector2d pos = new Vector2d();
        Vector2d lastPos = new Vector2d();
        Vector2d startDrag = new Vector2d();
        Vector2d endDrag = new Vector2d();
        Delta held = new Delta(0);
        Delta released = new Delta(0);
        Delta last = new Delta(0);
        boolean pressed = false;
        boolean pressedThisFrame = false;
        boolean releasedThisFrame = false;

        public Vector2d getMoveVector() {
            if (lastPos == null) {
                return new Vector2d(0, 0);
            }
            return Vector2d.subtract(lastPos, pos);
        }

        // Immutable
        public Vector2d getPos() {
            return pos.clone();
        }

        // Immutable
        public Vector2d getStartDrag() {
            return startDrag.clone();
        }

        public boolean isPressed() {
            return pressed;
        }

        public boolean isPressedThisFrame() {
            return pressedThisFrame;
        }
    }

    public static enum Key {
        UP(KeyEvent.VK_UP),
        LEFT(KeyEvent.VK_LEFT),
        DOWN(KeyEvent.VK_DOWN),
        RIGHT(KeyEvent.VK_RIGHT),
        A(KeyEvent.VK_A),
        JUMP(KeyEvent.VK_SPACE),
        CANCEL(KeyEvent.VK_ESCAPE),
        QUIT(KeyEvent.VK_Q),
        ENTER(KeyEvent.VK_ENTER),
        CTRL(KeyEvent.VK_CONTROL);

        int keyboardKey;
        Delta held = new Delta(0);   //How long the key has been held for (0 means not being held)
        Delta released = new Delta(0); //How long the key has been released for since last release (0 means has never been released)
        Delta last = new Delta(0); //How long last hold lasted (0 means has never been released before)
        long maybeReleasedTime = -1;
        boolean pressed = false;
        boolean pressedThisFrame = false;

        Key(int keyboardKey) {
            this.keyboardKey = keyboardKey;
        }

        public boolean isPressed() {
            return pressed;
        }

        public boolean isPressedThisFrame() {
            return pressedThisFrame;
        }
    }


    private HashMap<String, Key> keyNameMap = new HashMap<String, Key>();
    private HashMap<Integer, Key> keyValueMap = new HashMap<Integer, Key>();
    private Mouse mouse;
    private AbstractView view;

    public InputController(AbstractView view) {
        for (Key key : Key.values()) {
            keyNameMap.put(key.name(), key);
            keyValueMap.put(key.keyboardKey, key);
        }
        mouse = new Mouse();
        this.view = view;
    }

    public Mouse getMouse() {
        return mouse;
    }

    private Key getKey(String name) {
        return keyNameMap.get(name);
    }

    private Key getKey(int keyboardValue) {
        return keyValueMap.get(keyboardValue);
    }


    public void setupInputComponent(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                Key key = getKey(e.getKeyCode());
                if (key == null) return;
                if (key.maybeReleasedTime >= 0 && key.maybeReleasedTime == e.getWhen()) {
                    key.maybeReleasedTime = -1;
                    return;
                } else {
                    key.last = key.held;
                }

                key.pressed = true;
                key.pressedThisFrame = true;
                key.held = new Delta(0);

            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                Key key = getKey(e.getKeyCode());
                if (key == null) return;
                key.maybeReleasedTime = e.getWhen();
            }
        });

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Vector2d m = new Vector2d(e.getX(), e.getY());
                mouse.startDrag = m.add(view.getCanvasOffset());
                mouse.pos = mouse.startDrag;
                mouse.pressed = true;
                mouse.pressedThisFrame = true;
                mouse.held = new Delta(0);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Vector2d m = new Vector2d(e.getX(), e.getY());
                mouse.endDrag = m.add(view.getCanvasOffset());
                mouse.pos = mouse.endDrag;
                mouse.pressed = false;
                mouse.releasedThisFrame = true;
                mouse.last = mouse.held;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
            }
        });
        component.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                Vector2d m = new Vector2d(e.getX(), e.getY());
                mouse.lastPos = mouse.pos;
                mouse.pos = m.add(view.getCanvasOffset());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                Vector2d m = new Vector2d(e.getX(), e.getY());
                mouse.lastPos = mouse.pos;
                mouse.pos = m.add(view.getCanvasOffset());
            }
        });
    }

    public void update (Delta delta) {
        for (Key key : Key.values()) {
            if (key.maybeReleasedTime >= 0 && System.currentTimeMillis() >= key.maybeReleasedTime + 10) { // HACK, not good!
                key.maybeReleasedTime = -1;
                key.pressed = false;
                key.last = key.held;
            }
            if (key.pressed) {
                if (key.pressedThisFrame && key.held.getMillis() > 0) {
                    key.pressedThisFrame = false;
                }
                key.held.add(delta);
            } else {
                key.released.add(delta);
            }
        }

        // Fix for bug where pressedThisFrame stays on
        if (mouse.pressedThisFrame && (mouse.held.getMillis() > 0 || !mouse.pressed)) {
            mouse.pressedThisFrame = false;
        }
        if (mouse.releasedThisFrame && (mouse.released.getMillis() > 0 || mouse.pressed)) {
            mouse.releasedThisFrame = false;
        }
        if (mouse.pressed) {
            mouse.held.add(delta);
        } else {
            mouse.released.add(delta);
        }
    }

    public void clearInput() {
        for (Key key : Key.values()) {
            key.pressed = false;
            key.pressedThisFrame = false;
            key.last = key.held;
            key.held = new Delta(0);
        }
        mouse.pressed = false;
        mouse.pressedThisFrame = false;
        mouse.releasedThisFrame = false;
        mouse.last = mouse.held;
        mouse.held = new Delta(0);

    }

    public void debugDraw(Graphics g) {
        int y = 200;
        for (Key key : Key.values()) {
            g.drawString("Key " + key.name() + ": pressed=" + key.pressed + ", held=" + key.held.getSecs(), 50, y);
            y += 20;
        }
        g.drawString("Mouse: pressed=" + mouse.pressed + ", pressedThisFrame=" + mouse.isPressedThisFrame() + ", releasedThisFrame=" + mouse.releasedThisFrame + ", pos=" + mouse.pos.toString() + ", dragVector=" + (mouse.getPos().subtract(mouse.getStartDrag())).toString(), 50, y);
    }
}
