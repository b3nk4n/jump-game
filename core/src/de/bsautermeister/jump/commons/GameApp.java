package de.bsautermeister.jump.commons;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.transition.ScreenTransition;

public abstract class GameApp implements ApplicationListener {
    private AssetManager assetManager;
    private SpriteBatch batch;

    private TransitionContext transitionContext;

    private MusicPlayer backgroundMusic;
    private MusicPlayer foregroundMusic;

    @Override
    public void create() {
        assetManager = new AssetManager();
        batch = new SpriteBatch();

        transitionContext = new TransitionContext(batch);

        backgroundMusic = new MusicPlayer();
        foregroundMusic = new MusicPlayer();
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
    }

    @Override
    public void resume() {
        transitionContext.resume();
        backgroundMusic.resumeOrPlay();
        foregroundMusic.resumeOrPlay();
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

    public MusicPlayer getBackgroundMusic() {
        return backgroundMusic;
    }

    public MusicPlayer getForegroundMusic() {
        return foregroundMusic;
    }
}
