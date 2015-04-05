package cranesim.tools;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 */
public class SceneNode implements Comparable<SceneNode> {
    // TODO - change to lists with head being current and configurable history size
    private Vector2f translation;
    private Vector2f lastTranslation;
    private float rotation;
    private float lastRotation;
    private boolean firstTime; // hack
    protected AffineTransform totalTransform;
    protected AffineTransform lastTotalTransform;
    protected ArrayList<SceneNode> children;
    protected SceneNode parent;
    protected int priority = 0;


    public SceneNode() {
        translation = new Vector2f();
        rotation = 0f;
        totalTransform = new AffineTransform();
        children = new ArrayList<SceneNode>();
        firstTime = true;
    }

    public void rollBack() {
        if (lastTotalTransform != null) {
            totalTransform = lastTotalTransform;
            translation = lastTranslation;
            rotation = lastRotation;
        }
    }

    public boolean addChildNode(SceneNode child) {
        synchronized (children) {
            if (children.add(child)) {
                child.parent = this;
                Collections.sort(children);
                return true;
            }
        }
        return false;
    }

    public boolean removeChildNode(SceneNode child) {
        synchronized (children) {
            if (children.remove(child)) {
                child.parent = null;
                Collections.sort(children);
                return true;
            }
        }
        return false;
    }

    public SceneNode getParent() {
        return parent;
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.removeChildNode(this);
            parent = null;
        }
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        if (parent != null) {
            synchronized (parent.children) {
                Collections.sort(parent.children);
            }
        }
    }

    public void rotateNode(float r) {
        rotation += r;
    }

    public void setNodeRotation(float r) {
        rotation = r;
    }

    public float getNodeRotation() {
        return rotation;
    }

    public void translateNode(Vector2f t) {
        translation.add(t);
    }

    public void setNodeTranslation(Vector2f t) {
        translation = t.clone();
    }

    public Vector2f getNodeTranslation() {
        return translation.clone();
    }

    public AffineTransform getTotalTransform() {
        return totalTransform;
    }

    // Override this!
    public void preUpdateImpl(Delta delta) {}

    // Override this!
    public void postUpdateImpl(Delta delta) {}

    public final void update(Delta delta) {
        update(delta, new AffineTransform());
    }

    protected final void adjustTotalTransform(Vector2f newTranslation, float newRotation) {
//        if (Math.abs(newTranslation.getY() - translation.getY()) > 30) {
//            System.out.println("what");
//        }
        totalTransform.rotate(-rotation);
        totalTransform.translate(newTranslation.getX() - translation.getX(), newTranslation.getY() - translation.getY());
        totalTransform.rotate(newRotation);
        translation = newTranslation.clone();
        rotation = newRotation;
    }

    public void update(Delta delta, AffineTransform accumulatedTransform) {
        // Update history
        lastTranslation = translation.clone();
        lastRotation = rotation;
        lastTotalTransform = totalTransform; // Clone here?

        preUpdateImpl(delta);

        // Update transform
        totalTransform = new AffineTransform(accumulatedTransform);
        totalTransform.translate(translation.getX(), translation.getY());
        totalTransform.rotate(rotation);
        if (firstTime) {
            firstTime = false;
            lastTotalTransform = null;
        }

        postUpdateImpl(delta);

        ArrayList<SceneNode> childrenCopy;
        synchronized (children) {
            childrenCopy = new ArrayList<SceneNode>(children);
        }
        for (SceneNode node : childrenCopy) {
            node.update(delta, new AffineTransform(totalTransform));
        }
    }

    // Override this!
    public void paintImpl(Graphics2D g2d) {}

    public final void paint(Graphics g) {
        paint((Graphics2D)g);
    }

    public final void paint(Graphics2D g2d) {
        AffineTransform saveTransform = g2d.getTransform();
        g2d.setTransform(totalTransform);
        paintImpl(g2d);

        ArrayList<SceneNode> childrenCopy;
        synchronized (children) {
            childrenCopy = new ArrayList<SceneNode>(children);
        }
        for (SceneNode node : childrenCopy) {
            node.paint(g2d);
        }
        g2d.setTransform(saveTransform);
    }

    @Override
    public int compareTo(SceneNode sceneNode) {
        return getPriority() - sceneNode.getPriority();
    }
}
