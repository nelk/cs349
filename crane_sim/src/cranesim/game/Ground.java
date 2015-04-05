package cranesim.game;

import cranesim.tools.Box;
import cranesim.tools.Vector2f;

import java.awt.*;

/**
 *
 */
public class Ground extends Entity {
    public Ground(Vector2f pos, Box boundingBox) {
        super(boundingBox);
        setNodeTranslation(pos);
    }

    @Override
    public void paintImpl(Graphics2D g2d) {
        g2d.setColor(Color.green);
        g2d.fillRect(0, 0, Math.round(boundingBox.getWidth()), Math.round(boundingBox.getHeight()));
    }
}
