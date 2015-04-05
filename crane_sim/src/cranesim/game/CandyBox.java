package cranesim.game;

import cranesim.tools.*;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 *
 */
public class CandyBox extends Entity {
    private static final float SAFE_COLLISION_SPEED = 1.2f;
    private static final float DROP_ANGLE_THRESHOLD = 0.5f;
    private CraneMagnet stuckMagnet;
    private Vector2f velocity;
    private static float GRAVITY = 10f;
    private Vector2f preMoveTranslation;
    private float preMoveRotation = 0;
    private AffineTransform preMoveTransform;
    private static final Stroke lineStroke = new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

    public CandyBox(Vector2f pos, Box boundingBox) {
        super(boundingBox);
        setNodeTranslation(pos);
        velocity = new Vector2f();
    }

    public boolean isStuck() {
        return stuckMagnet != null;
    }

    /**
     * Set the magnet this is stuck to
     * NOTE: Side effect is that it will store the current translation and rotation as the pre-move values. You can use <code>rollBackToPreMove()</code> to reset the block to these values.
     * @param stuck
     */
    public void setStuck(CraneMagnet stuck) {
        this.stuckMagnet = stuck;
        if (stuck != null) {
            preMoveRotation = getNodeRotation();
            preMoveTranslation = getNodeTranslation();
            preMoveTransform = getTotalTransform();
        }
    }

    public void rollBackToPreMove() {
        if (preMoveTransform != null) {
            totalTransform = preMoveTransform;
            setNodeTranslation(preMoveTranslation);
            setNodeRotation(preMoveRotation);
        }
    }

    @Override
    public void preUpdateImpl(Delta delta) {
        if (!isStuck()) {
            // Gravity
            velocity.setY(velocity.getY() + GRAVITY*delta.getSecs());
            translateNode(velocity);
        } else {
            velocity.setY(0f);
        }
    }

    @Override
    public void postUpdateImpl(Delta delta) {
        for (Entity entity : CraneSim.getEntities()) {
            if (entity == this || !(entity instanceof CandyBox || entity instanceof Ground)  || (entity instanceof CandyBox && ((CandyBox)entity).isStuck())) continue;
            // Check to ensure this is higher than entity
            Box thisTransformedBound = findTransformedBounds();
            Box entityTransformedBound = entity.findTransformedBounds();
            if (thisTransformedBound.getTop() >= entityTransformedBound.getTop()) continue;

            if (CraneSim.transformedBoxCollision(getBoundingBox(), getTotalTransform(), entity.getBoundingBox(), entity.getTotalTransform())) {
                if (isStuck()) {
                    // ROLLBACK
                    /*
                    rollBack();
                    SceneNode node = getParent();
                    while (node != null && node instanceof DirectlyManip) {
                        node.rollBack();
                        if (DirectManipManager.getInstance().isControlled((DirectlyManip) node)) {
                            break;
                        }
                        node = node.getParent();
                    }
                    */
                    DirectManipManager.getInstance().setGoodState(false);
                } else {
                    // Check if safe collision
                    float[] angleFix = {0};
                    Vector2f translateFixPoint = new Vector2f();
                    int closestAngleAnswer = CraneSim.closestRightAngleWithingThresholdWithFixes(entity, this, DROP_ANGLE_THRESHOLD, angleFix, translateFixPoint);
                    if (closestAngleAnswer == CraneSim.ANGLE_OUTSIDE_THRESHOLD || velocity.magnitude() > SAFE_COLLISION_SPEED) {
                        rollBackToPreMove();
                        CraneMagnet theMagnet = CraneSim.getTheMagnet();
                        if (theMagnet != null && theMagnet.stuckBox != null) {
                            theMagnet.stuckBox.removeFromParent();
                            CraneSim.getSceneGraph().addChildNode(theMagnet.stuckBox);
                            theMagnet.stuckBox.setStuck(null);
                            theMagnet.stuckBox.rollBackToPreMove();
                            theMagnet.stuckBox = null;
                            theMagnet.turnOffMagnet();
                        }
                        CraneSim.brokeBox();
                    } else {
                        Vector2f newPosition = new Vector2f(getNodeTranslation().getX(), getNodeTranslation().getY() + (entityTransformedBound.getTop() - thisTransformedBound.getBottom()));
                        adjustTotalTransform(newPosition, getNodeRotation() + angleFix[0]); // No need to factor in total transform rotation because these are always on root node
                    }
                    velocity.setY(0f);
                    break;
                }
            }
        }
    }

    @Override
    public void paintImpl(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.fillRoundRect(0, 0, Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()), 4, 4);

        g2d.setColor(Color.red);

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


        g2d.setColor(Color.black);
        g2d.drawRoundRect(0, 0, Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()), 4, 4);

        if (CraneSim.DEBUG_CANDYBOX_CORNERS) {
            g2d.setColor(Color.red);
            g2d.fillRoundRect(0, 0, 5, 5, 4, 4);
        }
    }
}

