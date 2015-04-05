package quickmotion.shared;

import java.util.List;

/**
 *
 */
public interface AbstractView {
    Tool getTool();

    TimeLine getTimeLine();

    AbstractController getController();

    void notifyTimelineUpdated();

    Vector2d getCanvasOffset();

    boolean addAnimation(Animation currentAnimation);

    List<DrawnLine> getLines();

    boolean addLine(DrawnLine l);

    boolean shouldAnimate();

    int getBackgroundColour();
}
