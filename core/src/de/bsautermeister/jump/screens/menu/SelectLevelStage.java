package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.commons.GameStats;

public class SelectLevelStage extends Stage {
    private final AssetManager assetManager;
    private final Callbacks callbacks;

    private final int page;

    public SelectLevelStage(Viewport viewport, Batch batch, AssetManager assetManager, int page,
                            Callbacks callbacks) {
        super(viewport, batch);
        this.assetManager = assetManager;
        this.page = page;
        this.callbacks = callbacks;
        setDebugAll(Cfg.DEBUG_MODE);
    }

    public void initialize() {
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.UI); // TODO load a background image
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        Button leftButton = new Button(skin, Styles.Button.ARROW_LEFT);
        leftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.leftClicked();;
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
                callbacks.rightClicked();
            }
        });
        rightButton.setVisible(page < Cfg.LEVEL_PAGES);
        table.add(rightButton).center();

        table.pack();
        addActor(table);
    }

    private Button createLevelButton(Skin skin, final int stage, final int level) {
        int highestUnlockedLevel = GameStats.INSTANCE.getHighestFinishedLevel() + 1;
        final int absoluteLevel = (stage - 1) * Cfg.LEVELS_PER_STAGE + level;
        String styleName = getLevelButtonStyleName(stage, level);
        TextButton levelButton = new TextButton(stage + "-" + level, skin, styleName);
        levelButton.getLabel().setAlignment(Align.top);
        levelButton.getLabelCell().pad(6);
        levelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.levelSelected(absoluteLevel);
            }
        });
        levelButton.setDisabled(absoluteLevel > highestUnlockedLevel);
        levelButton.setTouchable(absoluteLevel > highestUnlockedLevel ?
                Touchable.disabled : Touchable.enabled);
        return levelButton;
    }

    private String getLevelButtonStyleName(int stage, int level) {
        int absoluteLevel = (stage - 1) * Cfg.LEVELS_PER_STAGE + level;
        int stars = GameStats.INSTANCE.getLevelStars(absoluteLevel);
        switch (stars) {
            case 0:
                return Styles.TextButton.LEVEL_STARS0;
            case 1:
                return Styles.TextButton.LEVEL_STARS1;
            case 2:
                return Styles.TextButton.LEVEL_STARS2;
            case 3:
                return Styles.TextButton.LEVEL_STARS3;
            default:
                throw new IllegalArgumentException("Unsupported number of stars: " + stars);
        }
    }

    public interface Callbacks {
        void leftClicked();
        void rightClicked();
        void levelSelected(int absoluteLevel);
    }
}
