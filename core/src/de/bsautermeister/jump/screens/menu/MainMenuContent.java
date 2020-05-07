package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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

    void initialize(AssetManager assetManager) {
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.UI); // TODO load a background image or a title image and dispose it
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);

        center();
        setFillParent(true);

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

        pack();
    }

    public interface Callbacks {
        void playClicked();
        void continueClicked();
    }
}
