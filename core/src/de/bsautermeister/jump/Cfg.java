package de.bsautermeister.jump;

import com.badlogic.gdx.utils.Logger;

public interface Cfg {
    int LOG_LEVEL = Logger.ERROR;
    boolean DEBUG_MODE = false;
    boolean DEBUG_ADS = false;

    /**
     * Enable consent form debugging using a local device.
     * To get the hased ID, start the app with {@link Cfg#DEBUG_ADS} enabled and check for the log:
     * Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("XXXXXX") to set this as a debug device.
     */
    String DEBUG_ADS_TEST_DEVICE_HASHED_ID = "6AF04AC20756A978832ACC7053531D1B"; // Pixel 4

    boolean SCREENSHOT_MODE = false;

    float UI_WIDTH = 1280;
    float UI_HEIGHT = 720;

    float PPM = 16f;

    int BLOCK_SIZE = 16;
    int BLOCKS_X = 26;
    int BLOCKS_Y = 14;

    int BLOCKS_PAD = 2;

    float BLOCK_SIZE_PPM = BLOCK_SIZE / PPM;

    int WORLD_WIDTH = BLOCKS_X * BLOCK_SIZE;
    int WORLD_HEIGHT = BLOCKS_Y * BLOCK_SIZE;

    int WINDOW_WIDTH = Cfg.WORLD_WIDTH * 3;
    int WINDOW_HEIGHT = Cfg.WORLD_HEIGHT * 3;

    int LEVEL_PAGES = 2;
    int LEVEL_ROWS = 2;
    int LEVEL_COLUMNS = 3;
    int LEVELS_PER_PAGE = LEVEL_COLUMNS * LEVEL_ROWS;

    float GOAL_REACHED_FINISH_DELAY = 5f;

    int SCORE_PER_SEC = 25;
    int COIN_SCORE = 100;
    int BOX_COIN_SCORE = 100;
    String BOX_COIN_SCORE_STRING = String.valueOf(BOX_COIN_SCORE);

    float MAX_FALLING_SPEED = -20f;
    float MAX_HORIZONTAL_SPEED = 8f;

    float HURRY_WARNING_TIME = 30f;

    float MIN_LANDING_HEIGHT = (BLOCK_SIZE / 2) / PPM;

    float MUNICH_START_THRESHOLD_X = 10f * 6.25f;
    float MUNICH_FULL_THRESHOLD_X = 3.33f * 6.25f;

    float GROUND_FRICTION = 0.1f;

    float GRAVITY = -4 * 9.81f;

    float SCREEN_TRANSITION_TIME = 0.5f;

    float BUTTON_VERTICAL_PAD = 16f;
    float BUTTON_HORIZONTAL_PAD = 64f;
    float TITLE_PAD = 32f;
}
