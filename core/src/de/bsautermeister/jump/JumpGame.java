package de.bsautermeister.jump;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.loading.LoadingScreen;
import de.bsautermeister.jump.services.GameServiceManager;
import de.bsautermeister.jump.services.GameServices;

public class JumpGame extends GameApp {
    private SpriteBatch batch;

    private final static String SAVE_DAT_FILENAME = "save_game.dat";

    private final GameServices gameServices;
    private static GameServiceManager gameServiceManager;

    public JumpGame(GameServices gameServices) {
        this.gameServices = gameServices;
    }

    @Override
    public void create() {
        super.create();
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        batch = new SpriteBatch(); // TODO second instance of SpriteBatch?!

        gameServiceManager = new GameServiceManager(gameServices);

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

    public static GameServiceManager getGameServiceManager() {
        return gameServiceManager;
    }
}
