package de.bsautermeister.jump.commons;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Logger;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameEnv;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.transition.ScreenTransition;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.IGameServiceListener;

public abstract class GameApp implements ApplicationListener {

    private static final Logger LOG = new Logger(GameApp.class.getSimpleName(), Cfg.LOG_LEVEL);

    private AssetManager assetManager;
    private SpriteBatch batch;

    private final GameEnv gameEnv;
    private final IGameServiceClient gameServiceClient;

    private TransitionContext transitionContext;

    private MusicPlayer backgroundMusic;
    private MusicPlayer foregroundMusic;

    private FrameBufferManager frameBufferManager;

    public GameApp(GameEnv gameEnv, IGameServiceClient gameServiceClient) {
        this.gameEnv = gameEnv;
        this.gameServiceClient = gameServiceClient;
    }

    @Override
    public void create() {
        assetManager = new AssetManager();
        batch = new SpriteBatch();
        frameBufferManager = new FrameBufferManager();

        transitionContext = new TransitionContext(batch, frameBufferManager);

        backgroundMusic = new MusicPlayer();
        foregroundMusic = new MusicPlayer();

        gameServiceClient.setListener(new IGameServiceListener() {
            @Override
            public void gsOnSessionActive() {
                LOG.info("Game service session active");
            }

            @Override
            public void gsOnSessionInactive() {
                LOG.info("Game service session inactive");
            }

            @Override
            public void gsShowErrorToUser(GsErrorType et, String msg, Throwable t) {
                LOG.error("Game service error: " + msg, t);
            }
        });

        gameServiceClient.resumeSession();
    }

    public void setScreen(ScreenBase screen) {
        setScreen(screen, null);
    }

    public void setScreen(ScreenBase screen, ScreenTransition transition) {
        transitionContext.setScreen(screen, transition);
    }

    public ScreenBase getScreen() {
        return transitionContext.getScreen();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        transitionContext.render(delta);
        backgroundMusic.update(delta);
        foregroundMusic.update(delta);
    }

    @Override
    public void resize(int width, int height) {
        transitionContext.resize(width, height);
    }

    @Override
    public void pause() {
        transitionContext.pause();
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
        if (foregroundMusic.isPlaying()) {
            foregroundMusic.pause();
        }
        gameServiceClient.pauseSession();
    }

    @Override
    public void resume() {
        transitionContext.resume();
        backgroundMusic.resumeOrPlay();
        foregroundMusic.resumeOrPlay();
        gameServiceClient.resumeSession();
    }

    @Override
    public void dispose() {
        transitionContext.dispose();
        assetManager.dispose();
        batch.dispose();
        backgroundMusic.dispose();
        foregroundMusic.dispose();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public GameEnv getEnv() {
        return gameEnv;
    }

    public MusicPlayer getBackgroundMusic() {
        return backgroundMusic;
    }

    public MusicPlayer getForegroundMusic() {
        return foregroundMusic;
    }

    public FrameBufferManager getFrameBufferManager() {
        return frameBufferManager;
    }

    public IGameServiceClient getGameServiceClient() {
        return gameServiceClient;
    }
}
