package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.Language;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.screens.menu.controls.AnimatedLabel;

public class PauseOverlay extends Table {

    public interface Callback {
        void quit(Vector2 clickScreenPosition);
        void resume();
        void restart();
    }

    private AnimatedLabel titleLabel;
    private Table buttonTable;

    private Callback callback;

    public PauseOverlay(Skin skin, I18NBundle i18n, Callback callback) {
        super(skin);
        this.callback = callback;
        init(i18n);
    }

    private void init(I18NBundle i18n) {
        String title = i18n.get(Language.PAUSED);
        titleLabel = new AnimatedLabel(getSkin(), Styles.Label.TITLE, Float.MAX_VALUE, title.length())
                .typeText(title);
        add(titleLabel)
                .pad(Cfg.TITLE_PAD)
                .row();

        buttonTable = new Table(getSkin());
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

        Button resumeButton = new TextButton(i18n.get(Language.RESUME), getSkin());
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.resume();
            }
        });
        buttonTable.add(resumeButton).row();

        Button restartButton = new TextButton(i18n.get(Language.RESTART), getSkin());
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.restart();
            }
        });
        buttonTable.add(restartButton).row();

        Button quitButton = new TextButton(i18n.get(Language.QUIT), getSkin());
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Vector2 clickScreenPosition = event.getStage()
                        .getViewport()
                        .project(new Vector2(event.getStageX(), event.getStageY()));
                callback.quit(clickScreenPosition);
            }
        });
        buttonTable.add(quitButton).row();

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
