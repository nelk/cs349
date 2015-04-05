package cranesim.game;

import cranesim.tools.Box;
import cranesim.tools.SceneNode;
import cranesim.tools.Vector2f;


/**
 *
 */
public abstract class Entity extends SceneNode {
    protected Box boundingBox;

    public Entity(Box boundingBox) {
        super();
        this.boundingBox = boundingBox;
    }

    protected Box findTransformedBounds() {
        Vector2f points[] = {getBoundingBox().getTopLeft(),
                getBoundingBox().getTopRight(),
                getBoundingBox().getBottomLeft(),
                getBoundingBox().getBottomRight()};
        Vector2f.transformVectorsWithAffineTransform(getTotalTransform(), false, points);
        float minY = 0, maxY = 0, minX = 0, maxX = 0, x, y;
        boolean firstPoint = true;
        for (Vector2f point : points) {
            x = point.getX();
            y = point.getY();
            if (firstPoint || x < minX) minX = x;
            if (firstPoint || x > maxX) maxX = x;
            if (firstPoint || y < minY) minY = y;
            if (firstPoint || y > maxY) maxY = y;
            firstPoint = false;
        }
        return Box.boxFromTwoPoints(new Vector2f(minX, minY), new Vector2f(maxX, maxY));
    }

    // MUTABLE
    public Box getBoundingBox() {
        return boundingBox;
    }

    public boolean collide(Entity other) {
        return CraneSim.transformedBoxCollision(this.getBoundingBox(), this.getTotalTransform(), other.getBoundingBox(), other.getTotalTransform());
    }
}
