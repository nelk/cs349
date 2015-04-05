package quickmotion.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import quickmotion.shared.AbstractView;
import quickmotion.shared.DrawnLine;

/**
 *
 */
public class QuickMotionView extends SurfaceView implements SurfaceHolder.Callback {
    private AbstractView view;
    private boolean surfaceReady;

    public QuickMotionView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public QuickMotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public QuickMotionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    public void setView(AbstractView view) {
        this.view = view;
    }

    protected void doDraw(Canvas canvas) {
        if (view == null) return;

        float time = view.getTimeLine().getTime();

        canvas.drawColor(view.getBackgroundColour());

        for (DrawnLine l : view.getLines()) {
            DrawnLineRenderer.paint(l, canvas, time);
        }
        ToolRenderer.paint(view.getTool(), canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.surfaceReady = false;
    }

    public boolean isSurfaceReady() {
        return this.surfaceReady;
    }
}
