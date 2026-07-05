package de.gamedevbaden.crucified.appstates;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import de.gamedevbaden.crucified.appstates.game.GameSessionAppState;
import de.gamedevbaden.crucified.enums.InputCommand;
import de.gamedevbaden.crucified.game.GameSession;

/**
 * Two fixed on-screen touch buttons: "SPRINT" (bottom-left, hold to run,
 * mirrors {@link InputCommand#Shift}) and "USE" (bottom-right, tap to
 * interact, mirrors {@link InputCommand#Interaction}).
 * <p>
 * Sprint is a hold-based movement modifier, so it goes through
 * {@link GameSession#applyInput(String, boolean)} directly, same as
 * {@link TouchMovementAppState}. Interaction isn't part of that pipeline
 * (see {@link de.gamedevbaden.crucified.appstates.game.GameEventAppState}'s
 * comment "interaction is handled differently") - it's a raycast fired by
 * {@link PlayerInteractionState}, so this calls that state's onAction directly.
 */
public class TouchButtonsAppState extends AbstractAppState implements RawInputListener {

    private static final float BUTTON_WIDTH = 220f;
    private static final float BUTTON_HEIGHT = 140f;
    private static final float MARGIN = 30f;

    private InputManager inputManager;
    private AssetManager assetManager;
    private GameSession gameSession;
    private PlayerInteractionState playerInteractionState;
    private Node guiNode;

    private float sprintMinX, sprintMinY, sprintMaxX, sprintMaxY;
    private float useMinX, useMinY, useMaxX, useMaxY;

    private int sprintPointerId = -1;
    private boolean sprinting;

    private Geometry sprintButton;
    private Geometry useButton;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.inputManager = app.getInputManager();
        this.assetManager = app.getAssetManager();
        this.gameSession = stateManager.getState(GameSessionAppState.class).getGameSession();
        this.playerInteractionState = stateManager.getState(PlayerInteractionState.class);
        this.guiNode = ((SimpleApplication) app).getGuiNode();

        float screenWidth = app.getContext().getSettings().getWidth();

        sprintMinX = MARGIN;
        sprintMinY = MARGIN;
        sprintMaxX = MARGIN + BUTTON_WIDTH;
        sprintMaxY = MARGIN + BUTTON_HEIGHT;

        useMinX = screenWidth - MARGIN - BUTTON_WIDTH;
        useMinY = MARGIN;
        useMaxX = screenWidth - MARGIN;
        useMaxY = MARGIN + BUTTON_HEIGHT;

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        sprintButton = createButton("SprintButton", sprintMinX, sprintMinY, ColorRGBA.Blue);
        useButton = createButton("UseButton", useMinX, useMinY, ColorRGBA.Orange);
        guiNode.attachChild(sprintButton);
        guiNode.attachChild(useButton);

        guiNode.attachChild(createLabel(font, "SPRINT", sprintMinX, sprintMinY));
        guiNode.attachChild(createLabel(font, "USE", useMinX, useMinY));

        inputManager.addRawInputListener(this);
    }

    private Geometry createButton(String name, float x, float y, ColorRGBA tint) {
        Quad quad = new Quad(BUTTON_WIDTH, BUTTON_HEIGHT);
        Geometry geometry = new Geometry(name, quad);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        ColorRGBA color = tint.clone();
        color.a = 0.35f;
        material.setColor("Color", color);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geometry.setMaterial(material);
        geometry.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Gui);
        geometry.setLocalTranslation(x, y, 0);
        return geometry;
    }

    private BitmapText createLabel(BitmapFont font, String text, float buttonX, float buttonY) {
        BitmapText label = new BitmapText(font, false);
        label.setSize(font.getCharSet().getRenderedSize());
        label.setColor(ColorRGBA.White);
        label.setText(text);
        label.setLocalTranslation(
                buttonX + (BUTTON_WIDTH - label.getLineWidth()) / 2f,
                buttonY + (BUTTON_HEIGHT + label.getLineHeight()) / 2f,
                0);
        return label;
    }

    @Override
    public void cleanup() {
        inputManager.removeRawInputListener(this);
        guiNode.detachChild(sprintButton);
        guiNode.detachChild(useButton);
        setSprinting(false);
        super.cleanup();
    }

    /**
     * Whether the given touch coordinate (same bottom-left-origin space as
     * {@link TouchEvent#getX()}/{@link TouchEvent#getY()}) falls within
     * either button, so the movement/look touch states can avoid stealing it.
     */
    public boolean isInsideButton(float x, float y) {
        return (x >= sprintMinX && x <= sprintMaxX && y >= sprintMinY && y <= sprintMaxY)
                || (x >= useMinX && x <= useMaxX && y >= useMinY && y <= useMaxY);
    }

    private void setSprinting(boolean pressed) {
        if (sprinting != pressed) {
            sprinting = pressed;
            InputCommand.Shift.setPressed(pressed);
            gameSession.applyInput(InputCommand.Shift.name(), pressed);
        }
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {
        switch (evt.getType()) {
            case DOWN:
                if (evt.getX() >= sprintMinX && evt.getX() <= sprintMaxX
                        && evt.getY() >= sprintMinY && evt.getY() <= sprintMaxY) {
                    sprintPointerId = evt.getPointerId();
                    setSprinting(true);
                } else if (evt.getX() >= useMinX && evt.getX() <= useMaxX
                        && evt.getY() >= useMinY && evt.getY() <= useMaxY) {
                    playerInteractionState.onAction(InputCommand.Interaction.name(), true, 0f);
                }
                break;
            case UP:
                if (evt.getPointerId() == sprintPointerId) {
                    setSprinting(false);
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
