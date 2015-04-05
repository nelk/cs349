package cranesim.tools;

import cranesim.game.CraneSim;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 *
 */
public class InputController {

    public static class Mouse {
        static Vector2d pos = new Vector2d();
        static Vector2d lastPos = new Vector2d();
        static Vector2d startDrag = new Vector2d();
        static Vector2d endDrag = new Vector2d();
        static Delta held = new Delta(0);
        static Delta released = new Delta(0);
        static Delta last = new Delta(0);
        static boolean pressed = false;
        static boolean pressedThisFrame = false;
        static boolean releasedThisFrame = false;

        public static Vector2d getMoveVector() {
            if (lastPos == null) {
                return new Vector2d(0, 0);
            }
            return Vector2d.subtract(lastPos, pos);
        }

        // Immutable
        public static Vector2d getPos() {
            return pos.clone();
        }

        // Immutable
        public static Vector2d getStartDrag() {
            return startDrag.clone();
        }

        public static boolean isPressed() {
            return pressed;
        }

        public static boolean isPressedThisFrame() {
            return pressedThisFrame;
        }
    }

    public static enum Key {
        UP(KeyEvent.VK_UP),
        LEFT(KeyEvent.VK_LEFT),
        DOWN(KeyEvent.VK_DOWN),
        RIGHT(KeyEvent.VK_RIGHT),
        JUMP(KeyEvent.VK_SPACE),
        CANCEL(KeyEvent.VK_ESCAPE),
        QUIT(KeyEvent.VK_Q),
        ENTER(KeyEvent.VK_ENTER);

        int keyboardKey;
        Delta held = new Delta(0);   //How long the key has been held for (0 means not being held)
        Delta released = new Delta(0); //How long the key has been released for since last release (0 means has never been released)
        Delta last = new Delta(0); //How long last hold lasted (0 means has never been released before)
        long maybeReleasedTime = -1;
        boolean pressed = false;
        boolean pressedThisFrame = false;
        static HashMap<String, Key> keyNameMap = new HashMap<String, Key>();
        static HashMap<Integer, Key> keyValueMap = new HashMap<Integer, Key>();
        static {
            for (Key key : Key.values()) {
                keyNameMap.put(key.name(), key);
                keyValueMap.put(key.keyboardKey, key);
            }
        }

        Key(int keyboardKey) {
            this.keyboardKey = keyboardKey;
        }

        static Key get(String name) {
            return keyNameMap.get(name);
        }

        static Key get(int keyboardValue) {
            return keyValueMap.get(keyboardValue);
        }

        public boolean isPressed() {
            return pressed;
        }

        public boolean isPressedThisFrame() {
            return pressedThisFrame;
        }
    }

    public static void setupInputComponent(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                Key key = Key.get(e.getKeyCode());
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
                Key key = Key.get(e.getKeyCode());
                if (key == null) return;
                key.maybeReleasedTime = e.getWhen();
            }
        });

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Vector2d m = new Vector2d(e.getX(), e.getY());
                Mouse.startDrag = m.add(CraneSim.getCanvasOffset());
                Mouse.pressed = true;
                Mouse.pressedThisFrame = true;
                Mouse.held = new Delta(0);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Vector2d m = new Vector2d(e.getX(), e.getY());
                Mouse.endDrag = m.add(CraneSim.getCanvasOffset());
                Mouse.pressed = false;
                Mouse.releasedThisFrame = true;
                Mouse.last = Mouse.held;
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
                Mouse.lastPos = Mouse.pos;
                Mouse.pos = m.add(CraneSim.getCanvasOffset());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                Vector2d m = new Vector2d(e.getX(), e.getY());
                Mouse.lastPos = Mouse.pos;
                Mouse.pos = m.add(CraneSim.getCanvasOffset());
            }
        });
    }

    public static void update (Delta delta) {
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
        if (Mouse.pressedThisFrame && (Mouse.held.getMillis() > 0 || !Mouse.pressed)) {
            Mouse.pressedThisFrame = false;
        }
        if (Mouse.releasedThisFrame && (Mouse.released.getMillis() > 0 || Mouse.pressed)) {
            Mouse.releasedThisFrame = false;
        }
        if (Mouse.pressed) {
            Mouse.held.add(delta);
        } else {
            Mouse.released.add(delta);
        }
    }

    public static void clearInput() {
        for (Key key : Key.values()) {
            key.pressed = false;
            key.pressedThisFrame = false;
            key.last = key.held;
            key.held = new Delta(0);
        }
        Mouse.pressed = false;
        Mouse.pressedThisFrame = false;
        Mouse.releasedThisFrame = false;
        Mouse.last = Mouse.held;
        Mouse.held = new Delta(0);

    }

    public static void debugDraw(Graphics g) {
        int y = 200;
        for (Key key : Key.values()) {
            g.drawString("Key " + key.name() + ": pressed=" + key.pressed + ", held=" + key.held.getSecs(), 50, y);
            y += 20;
        }
        g.drawString("Mouse: pressed=" + Mouse.pressed + ", pressedThisFrame=" + Mouse.isPressedThisFrame() + ", releasedThisFrame=" + Mouse.releasedThisFrame + ", pos=" + Mouse.pos.toString() + ", dragVector=" + (Mouse.getPos().subtract(Mouse.getStartDrag())).toString(), 50, y);
    }
}
