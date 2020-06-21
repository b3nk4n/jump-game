package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.Styles;

public class GameOverOverlay extends Table {

    public interface Callback {
        void quit(Vector2 clickScreenPosition);
        void restart();
    }

    private Callback callback;

    public GameOverOverlay(Skin skin, Callback callback) {
        super(skin);
        this.callback = callback;
        this.setVisible(false);

        init();
    }

    private void init() {
        defaults().pad(20);

        Label titleLabel = new Label("GAME OVER", getSkin(), Styles.Label.DEFAULT);

        Table buttonTable = new Table(getSkin());
        buttonTable.defaults().pad(20);
        buttonTable.center();

        Button quitButton = new Button(getSkin(), Styles.Button.PLAY);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Vector2 clickScreenPosition = event.getStage()
                        .getViewport()
                        .project(new Vector2(event.getStageX(), event.getStageY()));
                callback.quit(clickScreenPosition);
            }
        });
        buttonTable.add(quitButton);

        Button retryButton = new Button(getSkin(), Styles.Button.PLAY);
        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.restart();
            }
        });
        buttonTable.add(retryButton);

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
            callback.quit(new Vector2(Cfg.WINDOW_WIDTH / 2, Cfg.WINDOW_HEIGHT / 2));
        }
    }
}
