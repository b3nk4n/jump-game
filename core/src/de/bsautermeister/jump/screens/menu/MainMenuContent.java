package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;

public class MainMenuContent extends Table {

    private final Callbacks callbacks;

    public MainMenuContent(AssetManager assetManager, Callbacks callbacks) {
        this.callbacks = callbacks;
        initialize(assetManager);
    }

    private void initialize(AssetManager assetManager) {
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);

        center();
        setFillParent(true);

        Label title = new Label("October Bro", skin, Styles.Label.TITLE);
        add(title).pad(8f).row();

        Button playButton = new Button(skin, Styles.Button.PLAY);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.playClicked();
            }
        });
        add(playButton).pad(8f);

        if (JumpGame.hasSavedData()) {
            Button continueButton = new Button(skin, Styles.Button.CONTINUE);
            continueButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    callbacks.continueClicked();
                }
            });
            add(continueButton).pad(8f);
        }

        row();

        Button aboutButton = new Button(skin, Styles.Button.ABOUT);
        aboutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.aboutClicked();
            }
        });
        add(aboutButton).pad(8f);

        pack();
    }

    public interface Callbacks {
        void playClicked();
        void continueClicked();
        void aboutClicked();
    }
}
