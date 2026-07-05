package de.gamedevbaden.crucified.appstates;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;

/**
 * A touch that starts on the right half of the screen drags the camera's
 * look direction, forwarding deltas to {@link CameraAppState}.
 */
public class TouchLookAppState extends AbstractAppState implements RawInputListener {

    private static final float SENSITIVITY = 0.008f;

    private AppStateManager stateManager;
    private InputManager inputManager;
    private TouchButtonsAppState touchButtonsAppState;
    private float screenWidth;
    private int activePointerId = -1;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.stateManager = stateManager;
        this.inputManager = app.getInputManager();
        this.touchButtonsAppState = stateManager.getState(TouchButtonsAppState.class);
        this.screenWidth = app.getContext().getSettings().getWidth();
        inputManager.addRawInputListener(this);
    }

    @Override
    public void cleanup() {
        inputManager.removeRawInputListener(this);
        activePointerId = -1;
        super.cleanup();
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {
        switch (evt.getType()) {
            case DOWN:
                if (activePointerId == -1 && evt.getX() >= screenWidth / 2f
                        && !touchButtonsAppState.isInsideButton(evt.getX(), evt.getY())) {
                    activePointerId = evt.getPointerId();
                }
                break;
            case MOVE:
                if (evt.getPointerId() == activePointerId) {
                    CameraAppState cameraAppState = stateManager.getState(CameraAppState.class);
                    if (cameraAppState != null) {
                        cameraAppState.applyLookDelta(evt.getDeltaX() * SENSITIVITY, evt.getDeltaY() * SENSITIVITY);
                    }
                }
                break;
            case UP:
                if (evt.getPointerId() == activePointerId) {
                    activePointerId = -1;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void beginInput() {
    }

    @Override
    public void endInput() {
    }

    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {
    }

    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {
    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {
    }
}
