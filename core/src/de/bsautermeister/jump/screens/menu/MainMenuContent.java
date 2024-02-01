package de.bsautermeister.jump.screens.menu;

import static de.bsautermeister.jump.assets.Styles.ImageButton.PRIVACY;
import static de.bsautermeister.jump.assets.Styles.ImageButton.STAR;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Language;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.screens.menu.controls.AnimatedLabel;

public class MainMenuContent extends Table {

    public static final String TYPE = MainMenuContent.class.getSimpleName();

    private static final float DELAY_OFFSET = 0.25f;

    private final Callbacks callbacks;

    private final boolean privacyOptionRequired;

    public MainMenuContent(AssetManager assetManager, Callbacks callbacks, boolean privacyOptionRequired) {
        this.callbacks = callbacks;
        this.privacyOptionRequired = privacyOptionRequired;
        initialize(assetManager);
    }

    private void initialize(AssetManager assetManager) {
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);
        I18NBundle i18n = assetManager.get(AssetDescriptors.I18n.LANGUAGE);

        center();
        setFillParent(true);

        defaults()
                .pad(8f);

        Table contentTable = new Table();
        contentTable.defaults()
                .padLeft(Cfg.BUTTON_HORIZONTAL_PAD)
                .padRight(Cfg.BUTTON_HORIZONTAL_PAD)
                .padTop(Cfg.BUTTON_VERTICAL_PAD)
                .padBottom(Cfg.BUTTON_VERTICAL_PAD);
        add(contentTable).expand().row();

        Table rightFooterTable = new Table();
        rightFooterTable.defaults().pad(8f);
        rightFooterTable.addAction(Actions.sequence(
                Actions.alpha(0f),
                Actions.delay(3.0f),
                Actions.alpha(1f, 0.5f)
        ));
        add(rightFooterTable).right();

        String gameTitle = i18n.get(Language.GAME);
        AnimatedLabel title = new AnimatedLabel(skin, Styles.Label.TITLE, Float.MAX_VALUE, gameTitle.length())
                .typeText(gameTitle);
        contentTable.add(title)
                .pad(Cfg.TITLE_PAD)
                .row();

        float delay = 1.0f;
        Button playButton = new TextButton(i18n.get(Language.PLAY), skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.playClicked();
            }
        });
        playButton.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(delay),
                Actions.show()
        ));
        delay += DELAY_OFFSET;
        contentTable.add(playButton)
                .row();

        if (JumpGame.hasSavedData()) {
            Button continueButton = new TextButton(i18n.get(Language.RESUME_GAME), skin);
            continueButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    callbacks.continueClicked();
                }
            });
            continueButton.addAction(Actions.sequence(
                    Actions.hide(),
                    Actions.delay(delay),
                    Actions.show()
            ));
            delay += DELAY_OFFSET;
            contentTable.add(continueButton)
                    .row();
        }

        if (JumpGame.getGameServiceManager().isSupported()) {
            Button aboutButton = new TextButton(i18n.get(Language.ACHIEVEMENTS), skin);
            aboutButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    callbacks.achievementsClicked();
                }
            });
            aboutButton.addAction(Actions.sequence(
                    Actions.hide(),
                    Actions.delay(delay),
                    Actions.show()
            ));
            delay += DELAY_OFFSET;
            contentTable.add(aboutButton)
                    .row();
        }

        Button aboutButton = new TextButton(i18n.get(Language.ABOUT), skin);
        aboutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.aboutClicked();
            }
        });
        aboutButton.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(delay),
                Actions.show()
        ));
        contentTable.add(aboutButton)
                .row();

        if (privacyOptionRequired) {
            ImageButton privacyOptionButton = new ImageButton(skin, PRIVACY);
            privacyOptionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    callbacks.privacyClicked();
                }
            });
            rightFooterTable.add(privacyOptionButton);
        }

        final ImageButton rateButton = new ImageButton(skin, STAR);
        rateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.rateClicked();
            }
        });
        rightFooterTable.add(rateButton);

        pack();
    }

    public interface Callbacks {
        void playClicked();
        void continueClicked();
        void achievementsClicked();
        void aboutClicked();
        void rateClicked();
        void privacyClicked();
    }
}
