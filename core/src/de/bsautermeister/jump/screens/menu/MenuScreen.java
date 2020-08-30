package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.game.GameScreen;
import de.bsautermeister.jump.screens.transition.ScaleScreenTransition;
import de.bsautermeister.jump.utils.GdxUtils;

public class MenuScreen extends ScreenBase {
    private final Viewport uiViewport;
    private Stage stage;

    private TextureAtlas atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);

    private final MenuBackgroundRenderer backgroundRenderer;

    private Table content;

    private final String initialContentType;
    private final int lastLevel;

    public MenuScreen(GameApp game, boolean skipIntroTransition) {
        this(game, skipIntroTransition, MainMenuContent.TYPE, 1); // TODO: select highest unlocked level by default?
    }

    public MenuScreen(GameApp game, boolean skipIntroTransition, String contentType, int lastLevel) {
        super(game);
        this.uiViewport = new StretchViewport(Cfg.UI_WIDTH, Cfg.UI_HEIGHT);
        backgroundRenderer = new MenuBackgroundRenderer(getAssetManager(), getBatch(), atlas);
        if (skipIntroTransition) {
            backgroundRenderer.skipIntroTransition();
        }
        this.initialContentType = contentType;
        this.lastLevel = lastLevel;
    }

    @Override
    public void show() {
        stage = new Stage(uiViewport, getGame().getBatch());
        stage.setDebugAll(Cfg.DEBUG_MODE);

        setContent(createContent(initialContentType, lastLevel));

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        getGame().getBackgroundMusic().selectMusic(AssetPaths.Music.MENU_AUDIO);
        getGame().getBackgroundMusic().setVolume(MusicPlayer.MAX_VOLUME, true);
        getGame().getBackgroundMusic().playFromBeginning();
    }

    private void setContent(Table newContent) {
        if (content != null) {
            content.addAction(Actions.sequence(
                    Actions.removeActor()
            ));
        }
        content = newContent;
        stage.addActor(newContent);
    }

    private final Table createContent(String contentType, int lastLevel) {
        if (MainMenuContent.TYPE.equals(contentType)) {
            return createMainContent();
        } else if (SelectLevelMenuContent.TYPE.equals(contentType)) {
            int page = lastLevel / Cfg.LEVELS_PER_PAGE + 1;
            page = Math.min(page, Cfg.LEVEL_PAGES);
            return createSelectLevelContent(page);
        } else if (AboutContent.TYPE.equals(contentType)) {
            return createAboutContent();
        }

        throw new IllegalArgumentException("Unknown content type");
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

            @Override
            public void achievementsClicked() {
                JumpGame.getGameServiceManager().showAchievements();
            }

            @Override
            public void aboutClicked() {
                setContent(createAboutContent());
            }
        });
    }

    private Table createSelectLevelContent(final int page) {
        return new SelectLevelMenuContent(page, getAssetManager(), new SelectLevelMenuContent.Callbacks() {
            @Override
            public void leftClicked() {
                setContent(createSelectLevelContent(page - 1));
            }

            @Override
            public void rightClicked() {
                setContent(createSelectLevelContent(page + 1));
            }

            @Override
            public void levelSelected(int level, Vector2 clickScreenPosition) {
                setScreen(new GameScreen(getGame(), level), new ScaleScreenTransition(
                        Cfg.SCREEN_TRANSITION_TIME, Interpolation.smooth, true,
                        clickScreenPosition));
            }
        });
    }

    private Table createAboutContent() {
        return new AboutContent(getAssetManager());
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (content instanceof MainMenuContent) {
                Gdx.app.exit();
            } else {
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
