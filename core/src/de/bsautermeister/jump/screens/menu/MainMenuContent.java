package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.screens.menu.controls.AnimatedLabel;

public class MainMenuContent extends Table {

    private static float DELAY_OFFSET = 0.25f;

    private final Callbacks callbacks;

    public MainMenuContent(AssetManager assetManager, Callbacks callbacks) {
        this.callbacks = callbacks;
        initialize(assetManager);
    }

    private void initialize(AssetManager assetManager) {
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);

        center();
        setFillParent(true);

        defaults()
                .padLeft(Cfg.BUTTON_HORIZONTAL_PAD)
                .padRight(Cfg.BUTTON_HORIZONTAL_PAD)
                .padTop(Cfg.BUTTON_VERTICAL_PAD)
                .padBottom(Cfg.BUTTON_VERTICAL_PAD);

        AnimatedLabel title = new AnimatedLabel(skin, Styles.Label.TITLE, Float.MAX_VALUE, 11)
                .typeText("October Bro");
        add(title)
                .pad(Cfg.TITLE_PAD)
                .row();

        float delay = 1.0f;
        Button playButton = new TextButton("Play", skin);
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
        add(playButton)
                .row();

        if (JumpGame.hasSavedData()) {
            Button continueButton = new TextButton("Resume", skin);
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
            add(continueButton)
                    .row();
        }

        Button aboutButton = new TextButton("About", skin);
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
        add(aboutButton)
                .row();

        pack();
    }

    public interface Callbacks {
        void playClicked();
        void continueClicked();
        void aboutClicked();
    }
}
