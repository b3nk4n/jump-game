package de.bsautermeister.jump.screens;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.commons.GameStats;
import de.bsautermeister.jump.screens.transition.ScreenTransitions;
import de.bsautermeister.jump.utils.GdxUtils;

public class SelectLevelScreen extends ScreenBase {
    private final GameStats gameStats;

    private final Viewport viewport;
    private final Stage stage;
    private final int page;

    public SelectLevelScreen(GameApp game, int page) {
        super(game);
        this.gameStats = new GameStats();
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        this.stage = new Stage(viewport, game.getBatch());
        this.stage.setDebugAll(Cfg.DEBUG_MODE);
        this.page = page;
    }

    @Override
    public void show() {
        initialize();
    }

    private void initialize() {
        TextureAtlas atlas = getAsset(AssetDescriptors.Atlas.UI); // TODO load a background image
        Skin skin = getAsset(AssetDescriptors.Skins.UI);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        Button leftButton = new Button(skin, Styles.Button.ARROW_LEFT);
        leftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setScreen(new SelectLevelScreen(getGame(), page - 1), ScreenTransitions.SLIDE_RIGHT);
            }
        });
        leftButton.setVisible(page > 1);
        table.add(leftButton).center();

        Table levelTable = new Table();
        for (int r = 0; r < Cfg.LEVEL_ROWS; ++r) {
            for (int c = 1; c <= Cfg.LEVEL_COLUMNS; ++c) {
                levelTable.add(createLevelButton(skin, page,r * Cfg.LEVEL_COLUMNS + c)).pad(8f);
            }
            levelTable.row();
        }
        levelTable.pack();
        table.add(levelTable).expandX();

        Button rightButton = new Button(skin, Styles.Button.ARROW_RIGHT);
        rightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setScreen(new SelectLevelScreen(getGame(), page + 1), ScreenTransitions.SLIDE_LEFT);
            }
        });
        rightButton.setVisible(page < Cfg.LEVEL_PAGES);
        table.add(rightButton).center();

        table.pack();
        stage.addActor(table);
    }

    private Button createLevelButton(Skin skin, final int stage, final int level) {
        int highestUnlockedLevel = gameStats.getHighestFinishedLevel() + 1;
        final int absoluteLevel = (stage - 1) * Cfg.LEVELS_PER_PAGE + level;
        Button playButton = new Button(skin, Styles.Button.PLAY);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playLevel(absoluteLevel);
            }
        });
        playButton.setDisabled(absoluteLevel > highestUnlockedLevel);
        playButton.setTouchable(absoluteLevel > highestUnlockedLevel ?
                Touchable.disabled : Touchable.enabled);
        return playButton;
    }

    private void playLevel(int level) {
        setScreen(new GameScreen(getGame(), level));
    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen(Color.BLACK);

        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return stage;
    }
}
