package de.bsautermeister.jump;

import com.badlogic.gdx.utils.Logger;

public interface Cfg {
    int LOG_LEVEL = Logger.DEBUG;
    boolean DEBUG_MODE = true;

    float HUD_WIDTH = 1280; // still world units! (only loading screen)
    float HUD_HEIGHT = 720; // still world units! (only loading screen)

    int WORLD_WIDTH = 400;
    int WORLD_HEIGHT = 208;

    int BLOCK_SIZE = 16;

    float PPM = 100f;

    int LEVEL_PAGES = 3;
    int LEVEL_ROWS = 2;
    int LEVEL_COLUMNS = 3;
    int LEVELS_PER_STAGE = LEVEL_COLUMNS * LEVEL_ROWS;

    float GOAL_REACHED_FINISH_DELAY = 2f;

    int COIN_SCORE = 100;
    String COIN_SCORE_STRING = String.valueOf(COIN_SCORE);
}
