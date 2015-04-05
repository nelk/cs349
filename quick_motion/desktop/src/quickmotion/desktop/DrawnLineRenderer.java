package quickmotion.desktop;

import quickmotion.shared.DrawnLine;
import quickmotion.shared.Mat;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 *
 */
public class DrawnLineRenderer {

    private static final Stroke stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke hoveredStroke = new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke selectionStroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke hoveredSelectionStroke = new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public static void paint(DrawnLine line, Graphics2D g2d, float time) {
        if (line.existsAt(time)) {
            AffineTransform saveTransform = g2d.getTransform();
            Mat mat = line.getTransformAtTime(time);
            if (!(mat instanceof MatAffineTransform)) {
                throw new RuntimeException("Given unknown Mat implementation");
            }
            g2d.transform(((MatAffineTransform)mat).toAffineTransform());

            Stroke saveStroke = g2d.getStroke();

            // Selection stroke
            if (line.isSelected()) {
                g2d.setStroke(line.isHovered() ? hoveredSelectionStroke : selectionStroke);
                g2d.setColor(new Color(line.getSelectColour()));
                g2d.drawLine(Math.round(line.x1), Math.round(line.y1), Math.round(line.x2), Math.round(line.y2));
            }

            // Main stroke
            g2d.setStroke(line.isHovered() ? hoveredStroke : stroke);
            g2d.setColor(new Color(line.getColour()));
            g2d.drawLine(Math.round(line.x1), Math.round(line.y1), Math.round(line.x2), Math.round(line.y2));

            g2d.setStroke(saveStroke);

            g2d.setTransform(saveTransform);
        }
    }

}

