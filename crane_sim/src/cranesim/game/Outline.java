package cranesim.game;

import cranesim.tools.Box;
import cranesim.tools.Vector2f;

import java.awt.*;

/**
 *
 */
public class Outline  extends Entity {
    public Outline(Vector2f pos, Box boundingBox) {
        super(boundingBox);
        setNodeTranslation(pos);
        setPriority(0);
    }

    @Override
    public void paintImpl(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.drawRoundRect(0, 0, Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()), 4, 4);
    }
}
