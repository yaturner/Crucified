package de.gamedevbaden.crucified.android;

import com.jme3.app.AndroidHarness;

/**
 * Android entry point. Launches {@link de.gamedevbaden.crucified.tests.MobileGame}.
 */
public class MainActivity extends AndroidHarness {

    public MainActivity() {
        appClass = "de.gamedevbaden.crucified.tests.MobileGame";

        eglBitsPerPixel = 24;
        eglDepthBits = 16;
        eglSamples = 0;

        // Touch input is handled ourselves (virtual joystick + look-drag);
        // don't also emulate mouse/keyboard/joystick events from it.
        mouseEventsEnabled = false;
        keyEventsEnabled = false;
        joystickEventsEnabled = false;

        finishOnAppStop = true;
        handleExitHook = true;
        exitDialogTitle = "Exit?";
        exitDialogMessage = "Do you want to quit?";

        screenFullScreen = true;
        screenShowTitle = false;
    }
}
