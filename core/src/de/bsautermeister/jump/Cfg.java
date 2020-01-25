package de.bsautermeister.jump;

import com.badlogic.gdx.utils.Logger;

public interface Cfg {
    int LOG_LEVEL = Logger.DEBUG;
    boolean DEBUG_MODE = false;

    float HUD_WIDTH = 1280; // still world units! (only loading screen)
    float HUD_HEIGHT = 720; // still world units! (only loading screen)

    int BLOCK_SIZE = 16;
    int BLOCKS_X = 25;
    int BLOCKS_Y = 13;

    int WORLD_WIDTH = BLOCKS_X * BLOCK_SIZE;
    int WORLD_HEIGHT = BLOCKS_Y * BLOCK_SIZE;

    int WINDOW_WIDTH = Cfg.WORLD_WIDTH * 2;
    int WINDOW_HEIGHT = Cfg.WORLD_HEIGHT * 2;

    float PPM = 100f;

    int LEVEL_PAGES = 3;
    int LEVEL_ROWS = 2;
    int LEVEL_COLUMNS = 3;
    int LEVELS_PER_STAGE = LEVEL_COLUMNS * LEVEL_ROWS;

    float GOAL_REACHED_FINISH_DELAY = 2.5f;

    int COIN_SCORE = 100;
    String COIN_SCORE_STRING = String.valueOf(COIN_SCORE);

    float ENEMY_WAKE_UP_DISTANCE2 = (float)Math.pow(Cfg.WORLD_WIDTH * 0.75f / Cfg.PPM, 2);
    float MAX_FALLING_SPEED = -4f;

    int BLANK_COIN_IDX = 49;
}
