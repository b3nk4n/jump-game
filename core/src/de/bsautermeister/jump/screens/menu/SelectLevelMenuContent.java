package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.commons.GameStats;
import de.bsautermeister.jump.screens.game.GameSoundEffects;

public class SelectLevelMenuContent extends Table {
    private final Callbacks callbacks;
    private final GameSoundEffects gameSoundEffects;

    private final int page;

    public SelectLevelMenuContent(int page, AssetManager assetManager, Callbacks callbacks) {
        this.page = page;
        this.callbacks = callbacks;
        this.gameSoundEffects = new GameSoundEffects(assetManager);
        initialize(assetManager);
    }

    private void initialize(AssetManager assetManager) {
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);

        center();
        setFillParent(true);

        Button leftButton = new Button(skin, Styles.Button.ARROW_LEFT);
        leftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.leftClicked();
            }
        });
        leftButton.setVisible(page > 1);
        add(leftButton).pad(16f).center();

        Label title = new Label("Select Level", skin, Styles.Label.XXLARGE);
        Table container = new Table();
        container.center();
        container.add(title).row();

        Table levelTable = new Table();
        for (int r = 0; r < Cfg.LEVEL_ROWS; ++r) {
            for (int c = 1; c <= Cfg.LEVEL_COLUMNS; ++c) {
                levelTable.add(createLevelButton(skin, page,r * Cfg.LEVEL_COLUMNS + c)).pad(8f);
            }
            levelTable.row();
        }
        levelTable.pack();
        container.add(levelTable).pad(Cfg.TITLE_PAD);
        add(container).expandX();

        Button rightButton = new Button(skin, Styles.Button.ARROW_RIGHT);
        rightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.rightClicked();
            }
        });
        rightButton.setVisible(page < Cfg.LEVEL_PAGES);
        add(rightButton).pad(16f).center();

        pack();
    }

    private Button createLevelButton(Skin skin, final int stage, final int level) {
        int highestUnlockedLevel = GameStats.INSTANCE.getHighestFinishedLevel() + 1;
        final int absoluteLevel = (stage - 1) * Cfg.LEVELS_PER_STAGE + level;
        final boolean disabled = absoluteLevel > highestUnlockedLevel;
        String styleName = getLevelButtonStyleName(stage, level, disabled);
        final TextButton levelButton = new TextButton(
                disabled ? "" : String.valueOf(absoluteLevel), skin, styleName);
        levelButton.getLabel().setAlignment(Align.top);
        levelButton.getLabelCell().padTop(30f);
        if (disabled) {
            levelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    gameSoundEffects.playRandomBurpSound();
                }
            });
        } else {
            levelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Vector2 clickScreenPosition = event.getStage()
                            .getViewport()
                            .project(new Vector2(event.getStageX(), event.getStageY()));
                    callbacks.levelSelected(absoluteLevel, clickScreenPosition);
                }
            });
        }

        return levelButton;
    }

    private String getLevelButtonStyleName(int stage, int level, boolean disabled) {
        if (disabled) {
            return Styles.TextButton.DISABLED;
        }

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
        void levelSelected(int absoluteLevel, Vector2 clickScreenPosition);
    }
}
