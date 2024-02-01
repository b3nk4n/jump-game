package de.bsautermeister.jump;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.loading.LoadingScreen;
import de.bsautermeister.jump.serializer.BinarySerializer;
import de.bsautermeister.jump.services.AdService;
import de.bsautermeister.jump.services.GameServiceManager;
import de.bsautermeister.jump.services.RateService;
import de.golfgl.gdxgamesvcs.IGameServiceClient;

public class JumpGame extends GameApp {
    private final static String SAVE_DAT_FILENAME = "save_game.dat";

    private static GameServiceManager gameServiceManager;

    private final RateService rateService;
    private final AdService adService;

    public JumpGame(GameEnv gameEnv, IGameServiceClient gameServiceClient, RateService rateService, AdService adService) {
        super(gameEnv, gameServiceClient);
        this.rateService = rateService;
        this.adService = adService;
    }

    @Override
    public void create() {
        super.create();
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        gameServiceManager = new GameServiceManager(getGameServiceClient());

        setScreen(new LoadingScreen(this));
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
        FileHandle handle = getSavedDataHandle();

        if (!handle.exists()) {
            return false;
        }

        return BinarySerializer.isCompatibleVersion(handle.read());
    }

    public static GameServiceManager getGameServiceManager() {
        return gameServiceManager;
    }

    public RateService getRateService() {
        return rateService;
    }

    public AdService getAdService() {
        return adService;
    }
}
