package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;

public class GameScreen extends ScreenBase {

    private GameController controller;
    private GameRenderer renderer;
    private GameSoundEffects soundEffects;

    private TextureAtlas atlas;

    private FileHandle gameToLoad;
    private int level;

    private final GameScreenCallbacks callbacks = new GameScreenCallbacks() {
        @Override
        public void success(int level) {
            setScreen(new GameScreen(getGame(), level + 1));
        }

        @Override
        public void fail() {
            setScreen(new GameOverScreen(getGame()));
        }
    };

    public GameScreen(GameApp game, int level) {
        super(game);
        this.level = level;
        this.atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);
    }

    public GameScreen(GameApp game, FileHandle fileHandle) {
        //super(game);
        this(game, -1); // TODO whatever
        this.gameToLoad = fileHandle;
    }

    @Override
    public void show() {
        super.show();

        if (JumpGame.hasSavedData()) {
            //load(gameToLoad); // TODO duplicated?
            // ensure to not load this saved game later anymore
            //JumpGame.deleteSavedData();
        }

        soundEffects = new GameSoundEffects(getAssetManager());
        controller = new GameController(callbacks, getGame().getMusicPlayer(), soundEffects, level);
        renderer = new GameRenderer(getBatch(), getAssetManager(), atlas, controller);
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
}
