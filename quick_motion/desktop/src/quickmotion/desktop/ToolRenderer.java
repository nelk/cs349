package quickmotion.desktop;

import quickmotion.shared.SyncTools;
import quickmotion.shared.Tool;
import quickmotion.shared.Vector2f;

import java.awt.*;
import java.util.ArrayList;

/**
 *
 */
public class ToolRenderer {
    public static void paint(Tool tool, Graphics2D g2d) {
        if (tool == Tool.LASSO) {
            g2d.setColor(Color.gray);
            Stroke saveStroke = g2d.getStroke();
            float phase = tool.getLassoAntsTime()*50;
            ArrayList<Vector2f> lassoPath = (ArrayList<Vector2f>) SyncTools.synchronizedCopy(tool.getLassoPath());
            if (lassoPath != null && lassoPath.size() >= 2) {
                Vector2f last = null;
                lassoPath.add(lassoPath.get(0));
                for (Vector2f p : lassoPath) {
                    if (last != null) {
                        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{5f}, phase));
                        phase += Vector2f.subtract(p, last).magnitude();
                        g2d.drawLine(Math.round(last.x), Math.round(last.y), Math.round(p.x), Math.round(p.y));
                    }
                    last = p;
                }
            }
            g2d.setStroke(saveStroke);
        } else if (tool == Tool.ROTATE || tool == Tool.SCALE) {
            Vector2f selectionCenter = tool.getSelectionCenter();
            if (selectionCenter == null) return;

            final int W = 4;

            g2d.setColor(Color.red);
            g2d.fillRect(Math.round(selectionCenter.getX() - W/2), Math.round(selectionCenter.getY() - W/2), W, W);
        }
    }
}

