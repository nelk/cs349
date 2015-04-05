package quickmotion.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import quickmotion.shared.DrawnLine;
import quickmotion.shared.Mat;

/**
 *
 */
public class DrawnLineRenderer {

    private static final Paint stroke = new Paint();
    private static final Paint hoveredStroke = new Paint();
    private static final Paint selectionStroke = new Paint();
    private static final Paint hoveredSelectionStroke = new Paint();

    static {
        stroke.setStrokeWidth(3f);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);

        hoveredStroke.setStrokeWidth(6f);
        hoveredStroke.setStrokeCap(Paint.Cap.ROUND);
        hoveredStroke.setStrokeJoin(Paint.Join.ROUND);

        selectionStroke.setStrokeWidth(5f);
        selectionStroke.setStrokeCap(Paint.Cap.ROUND);
        selectionStroke.setStrokeJoin(Paint.Join.ROUND);

        hoveredSelectionStroke.setStrokeWidth(8f);
        hoveredSelectionStroke.setStrokeCap(Paint.Cap.ROUND);
        hoveredSelectionStroke.setStrokeJoin(Paint.Join.ROUND);
    }

    public static void paint(DrawnLine line, Canvas canvas, float time) {
        if (line.existsAt(time)) {
            Mat mat = line.getTransformAtTime(time);
            canvas.save();
            if (!(mat instanceof MatMatrix)) {
                throw new RuntimeException("Given unknown Mat implementation");
            }
            canvas.concat(((MatMatrix) mat).toMatrix());

            Paint usingStroke;
            // Selection stroke
            if (line.isSelected()) {
                usingStroke = line.isHovered() ? hoveredSelectionStroke : selectionStroke;
                usingStroke.setColor(line.getSelectColour());
                canvas.drawLine(line.x1, line.y1, line.x2, line.y2, usingStroke);
            }

            // Main stroke
            usingStroke = line.isHovered() ? hoveredStroke : stroke;
            usingStroke.setColor(line.getColour());
            canvas.drawLine(line.x1, line.y1, line.x2, line.y2, usingStroke);

            canvas.restore();
        }
    }

}
