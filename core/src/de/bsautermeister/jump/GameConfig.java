package de.bsautermeister.jump;

import com.badlogic.gdx.utils.Logger;

public interface GameConfig {
    int LOG_LEVEL = Logger.DEBUG;
    boolean DEBUG_MODE = false;

    float HUD_WIDTH = 720; // still world units!
    float HUD_HEIGHT = 1280; // still world units!

    int WORLD_WIDTH = 400;
    int WORLD_HEIGHT = 208;

    float PPM = 100f;
}
