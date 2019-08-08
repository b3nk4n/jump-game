package de.bsautermeister.jump;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.LoadingScreen;

public class JumpGame extends GameApp {
    private SpriteBatch batch;

    public static final short NOTHING_BIT = 0;
    public static final short GROUND_BIT = 1;
    public static final short MARIO_BIT = 2;
    public static final short BRICK_BIT = 4;
    public static final short COIN_BIT = 8;
    public static final short DESTROYED_BIT = 16;
    public static final short ENEMY_BIT = 32;
    public static final short OBJECT_BIT = 64;
    public static final short ENEMY_HEAD_BIT = 128;
    public static final short ITEM_BIT = 256;
    public static final short MARIO_HEAD_BIT = 512;
    public static final short MARIO_FEET_BIT = 1024;
    public static final short ENEMY_SIDE_BIT = 2048;
    public static final short BLOCK_TOP_BIT = 4096;
    public static final short COLLIDER_BIT = 8192;
    public static final short PLATFORM_BIT = 16384;

    private final static String SAVE_DAT_FILENAME = "save_game.dat";

    @Override
    public void create() {
        super.create();
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        batch = new SpriteBatch();

        setScreen(new LoadingScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public static FileHandle getSavedDataHandle() {
        return Gdx.files.local(SAVE_DAT_FILENAME);
    }

    public static void deleteSavedData() {
        final FileHandle handle = getSavedDataHandle();
        if (handle.exists())
            handle.delete();
    }

    public static boolean hasSavedData() {
        return getSavedDataHandle().exists();
    }
}
