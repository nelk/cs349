package quickmotion.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import quickmotion.shared.SyncTools;
import quickmotion.shared.Tool;
import quickmotion.shared.Vector2f;

import java.util.ArrayList;

/**
 *
 */
public class ToolRenderer {
    public static void paint(Tool tool, Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        if (tool == Tool.LASSO) {
            float phase = tool.getLassoAntsTime()*50;
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.STROKE);

            ArrayList<Vector2f> lassoPath = (ArrayList<Vector2f>) SyncTools.synchronizedCopy(tool.getLassoPath());
            if (lassoPath != null && lassoPath.size() >= 2) {
                Vector2f last = null;
                lassoPath.add(lassoPath.get(0));
                for (Vector2f p : lassoPath) {
                    if (last != null) {
                        paint.setPathEffect(new DashPathEffect(new float[]{5f, 5f}, phase));
                        phase += Vector2f.subtract(p, last).magnitude();
                        canvas.drawLine(last.x, last.y, p.x, p.y, paint);
                    }
                    last = p;
                }
            }
        } else if (tool == Tool.ROTATE || tool == Tool.SCALE) {
            Vector2f selectionCenter = tool.getSelectionCenter();
            if (selectionCenter == null) return;

            final int W = 4;

            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(selectionCenter.x - W/2, selectionCenter.y - W/2, selectionCenter.x + W/2, selectionCenter.y + W/2, paint);
        }
    }
}
