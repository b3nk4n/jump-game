package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Language;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.commons.JumpGameStats;
import de.bsautermeister.jump.screens.game.GameSoundEffects;
import de.bsautermeister.jump.screens.game.level.LevelInfo;
import de.bsautermeister.jump.screens.game.level.LevelMetadata;

public class SelectLevelMenuContent extends Table {
    private final Callbacks callbacks;
    private final GameSoundEffects gameSoundEffects;
    private final I18NBundle i18n;

    private Label infoLabel;

    private final int page;

    public SelectLevelMenuContent(int page, AssetManager assetManager, Callbacks callbacks) {
        this.page = page;
        this.callbacks = callbacks;
        this.gameSoundEffects = new GameSoundEffects(assetManager);
        i18n = assetManager.get(AssetDescriptors.I18n.LANGUAGE);
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

        Label title = new Label(i18n.get(Language.SELECT_LEVEL), skin, Styles.Label.XXLARGE);
        Table container = new Table();
        container.center();
        container.add(title).row();

        Table levelTable = new Table();
        for (int r = 0; r < Cfg.LEVEL_ROWS; ++r) {
            for (int c = 1; c <= Cfg.LEVEL_COLUMNS; ++c) {
                levelTable.add(createLevelButton(skin, (page - 1) * Cfg.LEVELS_PER_STAGE + r * Cfg.LEVEL_COLUMNS + c)).pad(8f);
            }
            levelTable.row();
        }

        infoLabel = new Label("", skin, Styles.Label.DEFAULT);
        infoLabel.setVisible(false);
        levelTable.add(infoLabel).colspan(3);

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

    private Button createLevelButton(Skin skin, final int level) {
        int highestUnlockedLevel = JumpGameStats.INSTANCE.getHighestFinishedLevel() + 1;
        final boolean locked = level > highestUnlockedLevel;
        final int totalStars = JumpGameStats.INSTANCE.getTotalStars();
        LevelInfo levelInfo = LevelMetadata.getLevelInfo(level);
        final int requiredStarsToUnlock = levelInfo.getRequiredStarsToUnlock();
        boolean stillLocked = level == highestUnlockedLevel && totalStars < requiredStarsToUnlock;
        String styleName = locked ? Styles.TextButton.LOCKED : getUnlockedLevelButtonStyleName(level, stillLocked);
        final TextButton levelButton = new TextButton(
                locked || stillLocked ? "" : String.valueOf(level), skin, styleName);
        levelButton.getLabel().setAlignment(Align.top);
        levelButton.getLabelCell().padTop(30f);
        if (locked) {
            levelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    gameSoundEffects.playRandomBurpSound(1.0f);
                }
            });
        } else {
            if (stillLocked) {
                levelButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        gameSoundEffects.playRandomBurpSound(1.0f);
                        int missingStars = requiredStarsToUnlock - totalStars;
                        infoLabel.setText(i18n.format(Language.MISSING_FOR_UNLOCK, missingStars));
                        infoLabel.clearActions();
                        infoLabel.addAction(Actions.sequence(
                                Actions.show(),
                                Actions.delay(3f),
                                Actions.hide()
                        ));
                    }
                });

            } else {
                levelButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Vector2 clickScreenPosition = event.getStage()
                                .getViewport()
                                .project(new Vector2(event.getStageX(), event.getStageY()));
                        callbacks.levelSelected(level, clickScreenPosition);
                    }
                });
            }
        }

        return levelButton;
    }

    private String getUnlockedLevelButtonStyleName(int level, boolean stillLocked) {
        if (stillLocked) {
            return Styles.TextButton.STILL_LOCKED;
        }

        int stars = JumpGameStats.INSTANCE.getLevelStars(level);
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
        void levelSelected(int level, Vector2 clickScreenPosition);
    }
}
