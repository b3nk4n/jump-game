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

    private MusicPlayer musicPlayer;

    @Override
    public void create() {
        assetManager = new AssetManager();
        batch = new SpriteBatch();

        transitionContext = new TransitionContext(batch);

        musicPlayer = new MusicPlayer();
    }

    public void setScreen(ScreenBase screen) {
        setScreen(screen, null);
    }

    public void setScreen(ScreenBase screen, ScreenTransition transtion) {
        transitionContext.setScreen(screen, transtion);
    }

    public ScreenBase getScreen() {
        return transitionContext.getScreen();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        transitionContext.render(delta);
        musicPlayer.update(delta);
    }

    @Override
    public void resize(int width, int height) {
        transitionContext.resize(width, height);
    }

    @Override
    public void pause() {
        transitionContext.pause();
        musicPlayer.pause();
    }

    @Override
    public void resume() {
        transitionContext.resume();
        musicPlayer.play();
    }

    @Override
    public void dispose() {
        transitionContext.dispose();
        assetManager.dispose();
        batch.dispose();
        musicPlayer.dispose();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
