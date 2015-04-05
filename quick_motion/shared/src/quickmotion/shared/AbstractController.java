package quickmotion.shared;

/**
 *
 */
public interface AbstractController {
    Vector2f getPointerPosition();

    boolean isPressedThisFrame();

    boolean isPressed();

    boolean isSelectAll();

    boolean isAnimated();

    boolean isPartial();
}
