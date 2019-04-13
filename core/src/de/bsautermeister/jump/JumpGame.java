package de.bsautermeister.jump;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.screens.GameScreen;

public class JumpGame extends Game {
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

    // TODO use no static context of AssetManager, but pass it around, especially in Android
    public static AssetManager assetManager;

    private MusicPlayer musicPlayer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        assetManager.load("audio/sounds/coin.wav", Sound.class);
        assetManager.load("audio/sounds/bump.wav", Sound.class);
        assetManager.load("audio/sounds/breakblock.wav", Sound.class);
        assetManager.load("audio/sounds/powerup_spawn.wav", Sound.class);
        assetManager.load("audio/sounds/powerup.wav", Sound.class);
        assetManager.load("audio/sounds/stomp.wav", Sound.class);
        assetManager.load("audio/sounds/powerdown.wav", Sound.class);
        assetManager.load("audio/sounds/mariodie.wav", Sound.class);
        assetManager.finishLoading();

        musicPlayer = new MusicPlayer();
        musicPlayer.setup("audio/music/mario_music.ogg", 1.0f);

        setScreen(new GameScreen(this));
    }

    @Override
    public void render() {
        super.render();

        float delta = Gdx.graphics.getDeltaTime();
        musicPlayer.update(delta);
    }

    @Override
    public void dispose() {
        batch.dispose();
        musicPlayer.dispose();
    }

    @Override
    public void pause() {
        super.pause();
        musicPlayer.pause();
    }

    @Override
    public void resume() {
        super.resume();
        musicPlayer.play();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
