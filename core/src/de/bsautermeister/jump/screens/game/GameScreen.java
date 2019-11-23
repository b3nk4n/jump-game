package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.commons.GameStats;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.menu.MenuScreen;

public class GameScreen extends ScreenBase {

    private GameController controller;
    private GameRenderer renderer;
    private GameSoundEffects soundEffects;

    private TextureAtlas atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);

    private GameStats gameStats = new GameStats();
    private int level;
    private final FileHandle gameToResume;

    private final GameScreenCallbacks callbacks = new GameScreenCallbacks() {
        @Override
        public void success(int level) {
            gameStats.setHighestFinishedLevel(level);
            setScreen(new GameScreen(getGame(), level + 1));
        }

        @Override
        public void fail() {
            setScreen(new GameOverScreen(getGame()));
        }

        @Override
        public void backToMenu() {
            setScreen(new MenuScreen(getGame()));
        }
    };

    /**
     * Start new game at given level.
     */
    public GameScreen(GameApp game, int level) {
        super(game);
        this.level = level;
        this.gameToResume = null;
    }

    /**
     * Resume game of saved level.
     */
    public GameScreen(GameApp game) {
        super(game);
        this.level = gameStats.getLastStartedLevel();
        this.gameToResume = JumpGame.getSavedDataHandle();
    }

    @Override
    public void show() {
        super.show();

        soundEffects = new GameSoundEffects(getAssetManager());
        controller = new GameController(callbacks, getGame().getMusicPlayer(), soundEffects,
                level, gameToResume);
        renderer = new GameRenderer(getBatch(), getAssetManager(), atlas, controller);

        JumpGame.deleteSavedData();

        // enable phones BACK button
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void pause() {
        controller.save();
    }

    @Override
    public void render(float delta) {
        controller.update(delta);
        renderer.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        controller.dispose();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return renderer.getInputProcessor();
    }
}
