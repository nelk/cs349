package cranesim.game;

import cranesim.tools.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

/**
 *
 */
public class CraneMagnet extends CraneArm {
    private static final float PICKUP_ANGLE_THRESHOLD = 0.3f; // rads
    protected static final int MAGNET_WIDTH = 20;
    protected static final int MAGNET_HEIGHT = 50;
    protected static final Color ON_C = new Color(128, 10, 17);
    protected static final Color ON_HIGH_C = new Color(128, 46, 78);
    protected static final Color OFF_C = new Color(128, 62, 0);
    protected static final Color OFF_HIGH_C = new Color(128, 89, 43);

    protected boolean magnetOn = false;
    protected CandyBox stuckBox = null;

    public CraneMagnet(int seq) {
        super(seq);
        boundingBox = Box.boxFromPositionAndSize(new Vector2f((WIDTH - USE_WIDTH)/2, -MAGNET_HEIGHT/2), new Vector2f(MAGNET_WIDTH, MAGNET_HEIGHT));
    }

    @Override
    public void preUpdateImpl(Delta delta) {
        if (totalTransform != null) {
            // Mouse stuff
            Vector2f mPos = InputController.Mouse.getPos().to2f();
            Vector2f.transformVectorsWithAffineTransform(totalTransform, true, mPos);
            if (boundingBox.isPointWithin(mPos)) {
                if (InputController.Mouse.isPressedThisFrame()) {
                    DirectManipManager.getInstance().requestControl(this);
                } else if (DirectManipManager.getInstance().isControlled(this) && !InputController.Mouse.isPressed()) {
                    if (useMagnet()) {// Click and release
                        DirectManipManager.getInstance().releaseControl(this);
                        DirectManipManager.getInstance().requestHighlight(this);
                    }
                } else {
                    DirectManipManager.getInstance().requestHighlight(this);
                }
            } else {
                if (DirectManipManager.getInstance().isControlled(this) && !InputController.Mouse.isPressed()) { // Click, drag, release
                    DirectManipManager.getInstance().releaseControl(this);
                } else {
                    DirectManipManager.getInstance().releaseHighlight(this);
                }
            }
        }
    }

    @Override
    public void postUpdateImpl(Delta delta) {
        // Check for off screen
        Box transformedBound = findTransformedBounds();
        if (transformedBound.getLeft() < 0 || transformedBound.getRight() > CraneSim.SCREEN_WIDTH || transformedBound.getTop() < 0 || transformedBound.getBottom() > CraneSim.SCREEN_HEIGHT) {
            DirectManipManager.getInstance().setGoodState(false);
        } else {

            // Check collision with boxes
            CraneSim.clearDebugCollideBoxes();
            for (Entity entity : CraneSim.getEntities()) {
                if (entity.getTotalTransform() == null || entity == this || entity == getParent() || (entity instanceof CandyBox && ((CandyBox)entity).isStuck()) || entity instanceof Outline) continue;
                if (CraneSim.transformedBoxCollision(getBoundingBox(), getTotalTransform(), entity.getBoundingBox(), entity.getTotalTransform())) {
                    // ROLLBACK
                    /*
                    if (stuckBox != null) {
                        stuckBox.rollBack();
                    }
                    SceneNode node = this;
                    while (node != null && node instanceof DirectlyManip) {
                        node.rollBack();
                        if (DirectManipManager.getInstance().isControlled((DirectlyManip) node)) {
                            break;
                        }
                        node = node.getParent();
                    }
                    */
                    DirectManipManager.getInstance().setGoodState(false);
                }
            }
            CraneSim.setDebugCollideMagnet(getBoundingBox(), false);
        }
    }

    /**
     * Note: This does NOT release blocks
     */
    public void turnOffMagnet() {
        magnetOn = false;
    }

    private boolean useMagnet() {
        if (totalTransform == null) return false;
        if (!DirectManipManager.getInstance().isGoodState()) return false; // Only use magnet in good state
        magnetOn = !magnetOn;
        if (magnetOn) {
            Box magnetField = Box.boxFromPositionAndSize(boundingBox.getTopRight(), boundingBox.getSize());
            for (Entity entity : CraneSim.getEntities()) {
                if (!(entity instanceof CandyBox)) continue;
                if (CraneSim.transformedBoxCollision(magnetField, getTotalTransform(), entity.getBoundingBox(), entity.getTotalTransform())) {
                    // Check for block to be at reasonable angle
                    float[] angleFix = {0};
                    Vector2f translateFixPoint = new Vector2f();
                    if (CraneSim.closestRightAngleWithingThresholdWithFixes(this, entity, PICKUP_ANGLE_THRESHOLD, angleFix, translateFixPoint) != CraneSim.ANGLE_OUTSIDE_THRESHOLD) {
                        Vector2f newPosition = entity.getBoundingBox().getTopLeft().clone();
                        stuckBox = (CandyBox)entity;
                        stuckBox.setStuck(this); // Note: This stores current translation/rotation
                        stuckBox.removeFromParent();
                        addChildNode(stuckBox);

                        try {
                            // Set box transform for sticking
                            AffineTransform convertTransform = new AffineTransform(entity.getTotalTransform());
                            convertTransform.rotate(angleFix[0]);
                            convertTransform.preConcatenate(getTotalTransform().createInverse());
                            Vector2f.transformVectorsWithAffineTransform(convertTransform, false, translateFixPoint, newPosition);
                            newPosition.setX(newPosition.getX() - (translateFixPoint.getX() - getBoundingBox().getRight() - 1)); // - 1 is so that when unsticking box doesn't intersect with dropped block
                            stuckBox.setNodeTranslation(newPosition);
                            stuckBox.rotateNode(angleFix[0] - CraneSim.findTransformAngle(getTotalTransform()));
                        } catch (NoninvertibleTransformException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                }
            }
            CraneSim.setDebugCollideMagnet(magnetField, true);
        } else if (stuckBox != null) {
            // Set box transform for letting go
            AffineTransform transform = getTotalTransform();
            Vector2f newPosition = stuckBox.getNodeTranslation();
            Vector2f.transformVectorsWithAffineTransform(transform, false, newPosition);
            stuckBox.setNodeTranslation(newPosition);
            stuckBox.rotateNode(CraneSim.findTransformAngle(getTotalTransform()));

            stuckBox.removeFromParent();
            CraneSim.getSceneGraph().addChildNode(stuckBox);
            stuckBox.setStuck(null);
            stuckBox = null;

            CraneSim.movedBox();
        }
        CraneSim.finishedMove();

        return true;
    }

    @Override
    public void paintImpl(Graphics2D g2d) {
        if (!DirectManipManager.getInstance().isGoodState()) {
            g2d.setColor(CraneSim.BAD_MANIP_COLOUR);
        } else if (DirectManipManager.getInstance().isHighlighted(this)) {
            if (magnetOn) {
                g2d.setColor(ON_HIGH_C);
            } else {
                g2d.setColor(OFF_HIGH_C);
            }
        } else {
            if (magnetOn) {
                g2d.setColor(ON_C);
            } else {
                g2d.setColor(OFF_C);
            }
        }
        g2d.fillRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));
        g2d.setColor(Color.black);
        g2d.drawRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));
    }
}
