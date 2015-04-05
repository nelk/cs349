package cranesim.game;

import cranesim.tools.*;

import java.awt.*;

/**
 *
 */
public class Crane extends Entity implements DirectlyManip {
    public static final int WIDTH = 125;
    public static final int HEIGHT = 125;
    public static final int TRACK_WIDTH = 10;
    public static final int TRACK_FRAME_HEIGHT = 25;
    public static final int TRACK_FRAME_ROUNDEDNESS = 15;
    private static final int NUM_ARMS = 4;

    private Vector2f originalTranslation;

    public Crane(Vector2f pos) {
        super(Box.boxFromPositionAndSize(new Vector2f(), new Vector2f(WIDTH, HEIGHT)));
        setNodeTranslation(pos);
        int seq = 1;
        CraneArm arm1, arm2;
        arm1 = new CraneArm(seq++);
        arm1.setNodeTranslation(new Vector2f(WIDTH/2, 0));
        arm1.rotateNode(-(float)Math.PI/2f);
        addChildNode(arm1);
        CraneSim.addEntity(arm1);
        for (int i = 0; i < NUM_ARMS - 1; ++i) {
            arm2 = new CraneArm(seq++);
            arm2.rotateNode((float)Math.PI/3f);
            arm1.addChildNode(arm2);
            CraneSim.addEntity(arm2);
            arm1 = arm2;
        }

        CraneMagnet magnet = new CraneMagnet(seq++);
        arm1.addChildNode(magnet);
        CraneSim.addEntity(magnet);
        setPriority(2);
    }

    @Override
    public void preUpdateImpl(Delta delta) {
        if (originalTranslation == null) {
            originalTranslation = getNodeTranslation().clone();
        }

        // Direct Manipulation movement
        Vector2f mPos = InputController.Mouse.getPos().to2f();
        Vector2f.transformVectorsWithAffineTransform(totalTransform, true, mPos);
        if (boundingBox.isPointWithin(mPos)) {
            if (InputController.Mouse.isPressedThisFrame()) {
                DirectManipManager.getInstance().requestControl(this);
            } else {
                DirectManipManager.getInstance().requestHighlight(this);
            }
        } else {
            DirectManipManager.getInstance().releaseHighlight(this);
        }

        if (DirectManipManager.getInstance().isControlled(this)) {
            Vector2f offset = InputController.Mouse.getPos().subtract(InputController.Mouse.getStartDrag()).to2f();
            offset.setY(0f);
            setNodeTranslation(Vector2f.add(originalTranslation, offset)); // Reset to new

            if (!InputController.Mouse.isPressed()) {
                DirectManipManager.getInstance().releaseControl(this);
                if (DirectManipManager.getInstance().isGoodState()) {
                    originalTranslation = getNodeTranslation();
                    CraneSim.finishedMove();
                } else {
                    setNodeTranslation(originalTranslation);
                    DirectManipManager.getInstance().setGoodState(true);
                }
            }
        }

    }

    @Override
    public void paintImpl(Graphics2D g2d) {
        // Draw ghost guide
        if (DirectManipManager.getInstance().isControlled(this)) {
            g2d.translate(originalTranslation.getX() - getNodeTranslation().getX(), 0);
            g2d.setColor(Color.blue);
            g2d.drawRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));
            g2d.translate(getNodeTranslation().getX() - originalTranslation.getX(), 0);
        }

        if (!DirectManipManager.getInstance().isGoodState()) {
            g2d.setColor(CraneSim.BAD_MANIP_COLOUR);
        } else if (DirectManipManager.getInstance().isHighlighted(this) || DirectManipManager.getInstance().isControlled(this)) {
            g2d.setColor(Color.GRAY);
        } else {
            g2d.setColor(Color.DARK_GRAY);
        }

        // Draw crane body
        g2d.fillRoundRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()), 60, 25);
        g2d.setColor(Color.black);
        g2d.drawRoundRect(Math.round(boundingBox.getLeft()), Math.round(boundingBox.getTop()), Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()), 60, 25);

        // Draw tracks
        Stroke saveStroke = g2d.getStroke();
        Stroke trackStroke = new BasicStroke(TRACK_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[]{12f}, Math.max(0, getNodeTranslation().getX()));
        g2d.setStroke(trackStroke);
        g2d.setColor(Color.black);
        g2d.drawRoundRect(Math.round(getBoundingBox().getLeft()) - 20, Math.round(getBoundingBox().getBottom()) - TRACK_FRAME_HEIGHT, Math.round(getBoundingBox().getWidth()) + 40, TRACK_FRAME_HEIGHT, TRACK_FRAME_ROUNDEDNESS, TRACK_FRAME_ROUNDEDNESS);
        g2d.setStroke(saveStroke);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRoundRect(Math.round(getBoundingBox().getLeft()) - 20, Math.round(getBoundingBox().getBottom()) - TRACK_FRAME_HEIGHT, Math.round(getBoundingBox().getWidth()) + 40, TRACK_FRAME_HEIGHT, TRACK_FRAME_ROUNDEDNESS, TRACK_FRAME_ROUNDEDNESS);
    }

}
