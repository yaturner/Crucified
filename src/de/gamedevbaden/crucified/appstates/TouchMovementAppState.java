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
import de.gamedevbaden.crucified.appstates.game.GameSessionAppState;
import de.gamedevbaden.crucified.enums.InputCommand;
import de.gamedevbaden.crucified.game.GameSession;

/**
 * A minimal on-screen virtual joystick: a touch that starts on the left half
 * of the screen drives {@link InputCommand#Forward}/{@link InputCommand#Backward}/
 * {@link InputCommand#Left}/{@link InputCommand#Right} based on drag direction.
 * <p>
 * Movement isn't driven by polling {@link InputCommand#isPressed()} directly;
 * the actual gameplay pipeline is {@link com.jme3.input.controls.ActionListener}
 * callbacks routed through {@link GameSession#applyInput(String, boolean)} (see
 * {@link de.gamedevbaden.crucified.appstates.game.GameEventAppState}), so this
 * state calls that directly instead of relying on an InputManager mapping/trigger.
 */
public class TouchMovementAppState extends AbstractAppState implements RawInputListener {

    /**
     * Drag distance (pixels) before a direction is considered "pressed".
     */
    private static final float DEAD_ZONE = 24f;

    private InputManager inputManager;
    private GameSession gameSession;
    private TouchButtonsAppState touchButtonsAppState;
    private float screenWidth;
    private int activePointerId = -1;
    private float startX, startY;

    private boolean forward, backward, left, right;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.inputManager = app.getInputManager();
        this.gameSession = stateManager.getState(GameSessionAppState.class).getGameSession();
        this.touchButtonsAppState = stateManager.getState(TouchButtonsAppState.class);
        this.screenWidth = app.getContext().getSettings().getWidth();
        inputManager.addRawInputListener(this);
    }

    @Override
    public void cleanup() {
        inputManager.removeRawInputListener(this);
        releaseJoystick();
        super.cleanup();
    }

    private void releaseJoystick() {
        activePointerId = -1;
        setForward(false);
        setBackward(false);
        setLeft(false);
        setRight(false);
    }

    private void setForward(boolean pressed) {
        if (forward != pressed) {
            forward = pressed;
            InputCommand.Forward.setPressed(pressed);
            gameSession.applyInput(InputCommand.Forward.name(), pressed);
        }
    }

    private void setBackward(boolean pressed) {
        if (backward != pressed) {
            backward = pressed;
            InputCommand.Backward.setPressed(pressed);
            gameSession.applyInput(InputCommand.Backward.name(), pressed);
        }
    }

    private void setLeft(boolean pressed) {
        if (left != pressed) {
            left = pressed;
            InputCommand.Left.setPressed(pressed);
            gameSession.applyInput(InputCommand.Left.name(), pressed);
        }
    }

    private void setRight(boolean pressed) {
        if (right != pressed) {
            right = pressed;
            InputCommand.Right.setPressed(pressed);
            gameSession.applyInput(InputCommand.Right.name(), pressed);
        }
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {
        switch (evt.getType()) {
            case DOWN:
                if (activePointerId == -1 && evt.getX() < screenWidth / 2f
                        && !touchButtonsAppState.isInsideButton(evt.getX(), evt.getY())) {
                    activePointerId = evt.getPointerId();
                    startX = evt.getX();
                    startY = evt.getY();
                }
                break;
            case MOVE:
                if (evt.getPointerId() == activePointerId) {
                    float dx = evt.getX() - startX;
                    float dy = evt.getY() - startY;
                    setForward(dy > DEAD_ZONE);
                    setBackward(dy < -DEAD_ZONE);
                    setRight(dx > DEAD_ZONE);
                    setLeft(dx < -DEAD_ZONE);
                }
                break;
            case UP:
                if (evt.getPointerId() == activePointerId) {
                    releaseJoystick();
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
