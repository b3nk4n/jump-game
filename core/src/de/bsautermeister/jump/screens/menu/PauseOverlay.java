package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.bsautermeister.jump.assets.Styles;

public class PauseOverlay extends Table {

    public interface Callback {
        void quit();
        void resume();
    }

    private Callback callback;

    public PauseOverlay(Skin skin, Callback callback) {
        super(skin);
        this.callback = callback;
        this.setVisible(false);

        init();
    }

    private void init() {
        defaults().pad(20);

        Label titleLabel = new Label("PAUSED", getSkin(), Styles.Label.DEFAULT);

        Table buttonTable = new Table(getSkin());
        buttonTable.defaults().pad(20);
        buttonTable.center();

        Button quitButton = new Button(getSkin(), Styles.Button.PLAY);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.quit();
            }
        });
        buttonTable.add(quitButton);

        Button resumeButton = new Button(getSkin(), Styles.Button.PLAY);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.resume();
            }
        });
        buttonTable.add(resumeButton);

        add(titleLabel).row();
        add(buttonTable);

        center();
        setFillParent(true);
        pack();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            callback.resume();
        }
    }
}
