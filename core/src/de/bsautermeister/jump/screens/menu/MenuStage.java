package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;

public class MenuStage extends Stage {

    private final AssetManager assetManager;
    private final Callbacks callbacks;

    public MenuStage(Viewport viewport, Batch batch, AssetManager assetManager, Callbacks callbacks) {
        super(viewport, batch);
        this.assetManager = assetManager;
        this.callbacks = callbacks;
        setDebugAll(Cfg.DEBUG_MODE);
    }

    void initialize() {
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.UI); // TODO load a background image or a title image and dispose it
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        Button playButton = new Button(skin, Styles.Button.PLAY);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.playClicked();
            }
        });
        table.add(playButton).pad(8f);

        if (JumpGame.hasSavedData()) {
            Button continueButton = new Button(skin, Styles.Button.CONTINUE);
            continueButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    callbacks.continueClicked();
                }
            });
            table.add(continueButton).pad(8f);
        }

        table.pack();
        addActor(table);
    }

    public interface Callbacks {
        void playClicked();
        void continueClicked();
    }
}
