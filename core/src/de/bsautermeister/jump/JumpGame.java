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
