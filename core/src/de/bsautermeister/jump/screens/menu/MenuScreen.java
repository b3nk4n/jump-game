package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
    private Stage stage;

    private TextureAtlas atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);

    private final MenuBackgroundRenderer backgroundRenderer;

    private Table content;

    public MenuScreen(GameApp game) {
        super(game);
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        backgroundRenderer = new MenuBackgroundRenderer(getAssetManager(), getBatch(), atlas);
    }

    @Override
    public void show() {
        stage = new Stage(viewport, getGame().getBatch());
        stage.setDebugAll(Cfg.DEBUG_MODE);
        setContent(createMainContent());

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        getGame().getMusicPlayer().selectMusic(AssetPaths.Music.MENU_AUDIO);
        getGame().getMusicPlayer().setVolume(MusicPlayer.MAX_VOLUME, true);
        getGame().getMusicPlayer().playFromBeginning();
    }

    private void setContent(Table newContent) {
        if (content != null) {
            content.addAction(Actions.sequence(
                    Actions.alpha(0f, 0.5f, Interpolation.smooth),
                    Actions.removeActor()
            ));
        }
        newContent.addAction(Actions.sequence(
                Actions.alpha(0f),
                Actions.delay(0.5f),
                Actions.alpha(1f, 0.5f, Interpolation.smooth)
        ));
        content = newContent;
        stage.addActor(newContent);
    }

    private Table createMainContent() {
        return new MainMenuContent(getAssetManager(), new MainMenuContent.Callbacks() {
            @Override
            public void playClicked() {
                setContent(createSelectLevelContent(1));
            }

            @Override
            public void continueClicked() {
                setScreen(new GameScreen(getGame()));
            }
        });
    }

    private Table createSelectLevelContent(int page) {
        return new SelectLevelMenuContent(page, getAssetManager(), new SelectLevelMenuContent.Callbacks() {
            @Override
            public void leftClicked() {
                setContent(createSelectLevelContent(1));
            }

            @Override
            public void rightClicked() {
                setContent(createSelectLevelContent(2));
            }

            @Override
            public void levelSelected(int absoluteLevel) {
                setScreen(new GameScreen(getGame(), absoluteLevel));
            }
        });
    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen(Color.BLACK);

        // ensure background tint color is not affected by actor actions
        getBatch().setColor(Color.WHITE);

        backgroundRenderer.update(delta);
        backgroundRenderer.render();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if (content instanceof MainMenuContent) {
                Gdx.app.exit();
            } else if (content instanceof SelectLevelMenuContent) {
                setContent(createMainContent());
            }
        }
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
