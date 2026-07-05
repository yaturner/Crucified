package de.gamedevbaden.crucified.tests;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.simsilica.es.EntityData;
import de.gamedevbaden.crucified.appstates.EntityDataState;
import de.gamedevbaden.crucified.appstates.GameCommanderHolder;
import de.gamedevbaden.crucified.appstates.PlayerInteractionState;
import de.gamedevbaden.crucified.appstates.SceneEntityLoader;
import de.gamedevbaden.crucified.appstates.TouchButtonsAppState;
import de.gamedevbaden.crucified.appstates.TouchLookAppState;
import de.gamedevbaden.crucified.appstates.TouchMovementAppState;
import de.gamedevbaden.crucified.appstates.game.GameCommanderAppState;
import de.gamedevbaden.crucified.appstates.game.GameEventAppState;
import de.gamedevbaden.crucified.appstates.game.GameEventHandler;
import de.gamedevbaden.crucified.appstates.game.GameSessionManager;
import de.gamedevbaden.crucified.enums.Scene;
import de.gamedevbaden.crucified.es.utils.EntityFactory;
import de.gamedevbaden.crucified.game.GameSession;
import de.gamedevbaden.crucified.utils.GameInitializer;
import de.gamedevbaden.crucified.utils.GameOptions;

/**
 * Android entry point: an offline single-player scene, adapted from
 * {@link SingleplayerTest} but without Nifty GUI or networking, and with
 * touch controls (virtual joystick + look-drag) instead of keyboard/mouse.
 */
public class MobileGame extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        setPauseOnLostFocus(false);
        flyCam.setEnabled(false);

        GameOptions.ENABLE_PHYSICS_DEBUG = false;

        // FinalIslandScene's terrain is a ~25MB asset that reliably exceeds
        // Android's per-app heap ceiling (even with largeHeap) while
        // deserializing; TestScene is a small scene better suited to a
        // memory-constrained device.
        SceneEntityLoader.sceneToLoad = Scene.TestScene;

        // create entity data state
        EntityDataState entityDataState = new EntityDataState();
        stateManager.attach(entityDataState);
        EntityData entityData = entityDataState.getEntityData();

        // create game commander handler
        GameCommanderAppState commanderAppState = new GameCommanderAppState(this);
        stateManager.attach(commanderAppState);

        // load test scene
        stateManager.attach(new SceneEntityLoader());

        // create session manager to create a session for a single player
        GameSessionManager sessionManager = new GameSessionManager();
        stateManager.attach(sessionManager);

        stateManager.attach(new GameEventHandler(sessionManager));

        // create GameSession for our player
        GameSession gameSession = sessionManager.createSession(EntityFactory.createPlayer(entityData));

        // create GameCommanderHolder
        GameCommanderHolder commanderHolder = new GameCommanderHolder();
        stateManager.attach(commanderHolder);
        commanderHolder.add(gameSession.getPlayer(), commanderAppState);

        // create our game session app states
        stateManager.attach(new PlayerInteractionState());
        stateManager.attach(new GameEventAppState());

        // init game logic states
        GameInitializer.initEssentialAppStates(stateManager);
        GameInitializer.initGameSessionRelatedAppStates(stateManager, gameSession);
        GameInitializer.initGameLogicAppStates(stateManager);
        GameInitializer.initViewAppStates(stateManager);
        GameInitializer.initSoundAppStates(stateManager);
        GameInitializer.initInputAppStates(stateManager);
        GameInitializer.initPlayerStates(stateManager);
        GameInitializer.initFirstPersonCameraView(stateManager);

        // touch controls: left half of the screen is a virtual joystick for
        // movement, right half is drag-to-look for the camera, plus fixed
        // SPRINT/USE buttons in the bottom corners.
        stateManager.attach(new TouchButtonsAppState());
        stateManager.attach(new TouchMovementAppState());
        stateManager.attach(new TouchLookAppState());

        stateManager.attach(new Loader()); // load scene
    }

    private static class Loader extends AbstractAppState {

        @Override
        public void initialize(AppStateManager stateManager, Application app) {
            stateManager.getState(SceneEntityLoader.class).createEntitiesFromScene(SceneEntityLoader.sceneToLoad);
            stateManager.getState(GameCommanderAppState.class).loadScene(SceneEntityLoader.sceneToLoad);

            EntityFactory.createDemon(stateManager.getState(EntityDataState.class).getEntityData());
            super.initialize(stateManager, app);
        }
    }
}
