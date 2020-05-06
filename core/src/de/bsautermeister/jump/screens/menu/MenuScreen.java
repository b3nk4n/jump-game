package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.game.GameScreen;
import de.bsautermeister.jump.utils.GdxUtils;

public class MenuScreen extends ScreenBase {
    private final Viewport viewport;
    private MenuStage stage;

    private TextureAtlas atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);

    private final MenuBackgroundRenderer backgroundRenderer;

    public MenuScreen(GameApp game) {
        super(game);
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        backgroundRenderer = new MenuBackgroundRenderer(getAssetManager(), getBatch(), atlas);
    }

    @Override
    public void show() {
        this.stage = new MenuStage(viewport, getGame().getBatch(), getAssetManager(),
                new MenuStage.Callbacks() {
                    @Override
                    public void playClicked() {
                        playNewGame();
                    }

                    @Override
                    public void continueClicked() {
                        continueGame();
                    }
                }
        );
        stage.initialize();

        // use default BACK button handling (exit game)
        Gdx.input.setCatchKey(Input.Keys.BACK, false);

        getGame().getMusicPlayer().selectMusic(AssetPaths.Music.MENU_AUDIO);
        getGame().getMusicPlayer().setVolume(MusicPlayer.MAX_VOLUME, true);
        getGame().getMusicPlayer().playFromBeginning();
    }

    private void playNewGame() {
        setScreen(new SelectLevelScreen(getGame(), 1));
    }

    private void continueGame() {
        setScreen(new GameScreen(getGame()));
    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen(Color.BLACK);

        backgroundRenderer.update(delta);
        backgroundRenderer.render();

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        backgroundRenderer.resize(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundRenderer.dispose();
        atlas.dispose();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return stage;
    }
}
