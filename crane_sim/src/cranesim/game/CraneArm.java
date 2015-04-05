package cranesim.game;

import cranesim.tools.*;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 *
 */
public class CraneArm extends Entity implements DirectlyManip {
    protected static final int WIDTH = 120;
    protected static final int HEIGHT = 20;
    protected static final int USE_WIDTH = 100;
    protected Float originalRotation;
    protected int seq = 0;
    private static final Stroke lineStroke = new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

    public CraneArm(int seq) {
        super(Box.boxFromPositionAndSize(new Vector2f(- (WIDTH - USE_WIDTH) / 2, - HEIGHT/2), new Vector2f(WIDTH, HEIGHT)));
        translateNode(new Vector2f(USE_WIDTH, 0));
        this.seq = seq;
        setPriority(seq + 2);
    }

    @Override
    public void preUpdateImpl(Delta delta) {
        if (originalRotation == null) {
            originalRotation = getNodeRotation();
        }

        Vector2f mPos = InputController.Mouse.getPos().to2f();
        Vector2f startDrag = InputController.Mouse.getStartDrag().to2f();
        if (totalTransform != null) {
            Vector2f.transformVectorsWithAffineTransform(totalTransform, true, mPos, startDrag);
            if (boundingBox.isPointWithin(mPos)) {
                if (InputController.Mouse.isPressedThisFrame()) {
                    DirectManipManager.getInstance().requestControl(this);
                } else {
                    DirectManipManager.getInstance().requestHighlight(this);
                }
            } else {
                DirectManipManager.getInstance().releaseHighlight(this);
            }
        }

        if (DirectManipManager.getInstance().isControlled(this)) {
            if (mPos != null && startDrag != null) {
                // NOTE: totalTransform depends on addTempRotation! We are in reference of current total rotation!
                float addedRotation = mPos.angle() - startDrag.angle();
                setNodeRotation(originalRotation + addedRotation); // Set new addedTempRotation
            }
            if (!InputController.Mouse.isPressed()) {
                DirectManipManager.getInstance().releaseControl(this);
                if (DirectManipManager.getInstance().isGoodState()) {
                    originalRotation = getNodeRotation();
                    CraneSim.finishedMove();
                } else {
                    setNodeRotation(originalRotation);
                    DirectManipManager.getInstance().setGoodState(true);
                }
            }
        }
    }

    @Override
    public void paintImpl(Graphics2D g2d) {
        if (CraneSim.DEBUG_CRANEARM_MOUSE_TEST && totalTransform != null) {
            AffineTransform saveTransform = g2d.getTransform();
            Vector2f mPos = InputController.Mouse.getPos().to2f();
            Vector2f.transformVectorsWithAffineTransform(totalTransform, true, mPos);
            // RESET TRANSFORM TO BASE
            g2d.setTransform(new AffineTransform());
            // MOVE A BIT SO WE CAN SEE EASIER
            g2d.translate(WIDTH, HEIGHT);
            g2d.setColor(Color.yellow);
            g2d.drawRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));
            g2d.setColor(Color.red);
            g2d.fillOval(Math.round(mPos.getX()) - 3, Math.round(mPos.getY()) - 3, 6, 6);
            // RESET TRANSFORM TO WHAT IT WAS
            g2d.setTransform(saveTransform);
        }

        // Draw ghost guide
        if (DirectManipManager.getInstance().isControlled(this)) {
            g2d.rotate(originalRotation - getNodeRotation());
            g2d.setColor(Color.blue);
            g2d.drawRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));
            g2d.rotate(getNodeRotation() - originalRotation);
        }

        if (!DirectManipManager.getInstance().isGoodState()) {
            g2d.setColor(CraneSim.BAD_MANIP_COLOUR);
        } else if (DirectManipManager.getInstance().isHighlighted(this) || DirectManipManager.getInstance().isControlled(this)) {
            g2d.setColor(Color.CYAN);
        } else {
            g2d.setColor(Color.yellow);
        }
        g2d.fillRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));
        g2d.setColor(Color.black);
        g2d.drawRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));

        // Draw lines
        Stroke saveStroke = g2d.getStroke();
        g2d.setStroke(lineStroke);
        final float SLOPE = 2f;
        final int SPACING = 12;
        for (int i = Math.round(getBoundingBox().getLeft()) + SPACING/2; i < getBoundingBox().getRight() + getBoundingBox().getHeight()/SLOPE - SPACING/2; i += SPACING) {
            int x1 = i - Math.round(getBoundingBox().getHeight()/SLOPE);
            int y1 = Math.round(getBoundingBox().getBottom());
            if (x1 < getBoundingBox().getLeft()) {
                y1 += Math.round((x1-getBoundingBox().getLeft())*SLOPE);
                x1 = Math.round(getBoundingBox().getLeft());// + 2;
            }
            int x2 = i;
            int y2 = Math.round(getBoundingBox().getTop());
            if (x2 > getBoundingBox().getRight()) {
                y2 += Math.round((x2 - getBoundingBox().getRight())*SLOPE);
                x2 = Math.round(getBoundingBox().getRight());// - 2;
            }
            g2d.drawLine(x1, y1, x2, y2);
        }
        g2d.setStroke(saveStroke);

        final int BOLT_RADIUS = 4;
        g2d.setColor(Color.RED);
        g2d.fillOval(-BOLT_RADIUS/2, -BOLT_RADIUS/2, BOLT_RADIUS, BOLT_RADIUS); // Bolt holding arm pieces together
        g2d.setColor(Color.BLACK);
        g2d.drawOval(-BOLT_RADIUS/2, -BOLT_RADIUS/2, BOLT_RADIUS, BOLT_RADIUS); // Bolt holding arm pieces together

        // Draw arrow guide
        if (DirectManipManager.getInstance().isControlled(this)) {
            g2d.setColor(Color.red);
            final int ARROW = 5;
            g2d.drawLine(USE_WIDTH*2/3, 0, USE_WIDTH*2/3-5, getNodeRotation() - originalRotation > 0 ? -ARROW : ARROW);
            g2d.drawLine(USE_WIDTH*2/3, 0, USE_WIDTH*2/3+5, getNodeRotation() - originalRotation > 0 ? -ARROW : ARROW);
            g2d.drawArc(-USE_WIDTH*2/3, -USE_WIDTH*2/3, USE_WIDTH*4/3, USE_WIDTH*4/3, 0, (int)Math.round(Math.toDegrees(getNodeRotation() - originalRotation)));
        }
    }
}
