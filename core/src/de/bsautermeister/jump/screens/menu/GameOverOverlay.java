package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.screens.menu.controls.AnimatedLabel;

public class GameOverOverlay extends Table {

    public interface Callback {
        void quit(Vector2 clickScreenPosition);
        void restart();
    }

    private Callback callback;

    private AnimatedLabel titleLabel;

    public GameOverOverlay(Skin skin, Callback callback) {
        super(skin);
        this.callback = callback;

        init();
    }

    private void init() {
        titleLabel = new AnimatedLabel(getSkin(), Styles.Label.TITLE, Float.MAX_VALUE, 9)
                .typeText("Game Over");
        add(titleLabel)
                .pad(Cfg.TITLE_PAD)
                .row();

        Table buttonTable = new Table(getSkin());
        buttonTable.defaults()
                .padLeft(Cfg.BUTTON_HORIZONTAL_PAD)
                .padRight(Cfg.BUTTON_HORIZONTAL_PAD)
                .padTop(Cfg.BUTTON_VERTICAL_PAD)
                .padBottom(Cfg.BUTTON_VERTICAL_PAD);
        buttonTable.center();
        buttonTable.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(1f),
                Actions.show()
        ));

        Button quitButton = new TextButton("Quit", getSkin());
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

        Button retryButton = new TextButton("Retry", getSkin());
        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.restart();
            }
        });
        buttonTable.add(retryButton);
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
