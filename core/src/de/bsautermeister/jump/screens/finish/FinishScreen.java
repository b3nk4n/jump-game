package de.bsautermeister.jump.screens.finish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.menu.MenuScreen;
import de.bsautermeister.jump.screens.transition.ScaleScreenTransition;
import de.bsautermeister.jump.utils.GdxUtils;

public class FinishScreen extends ScreenBase {

    private InsideTentRenderer insideTentRenderer;

    private final SpriteBatch batch;

    public FinishScreen(GameApp game) {
        super(game);
        batch = game.getBatch();
    }

    @Override
    public void show() {
        insideTentRenderer = new InsideTentRenderer(getGame().getAssetManager());
    }

    @Override
    public void render(float delta) {
        insideTentRenderer.update(delta);

        if (Gdx.input.isTouched()) {
            getGame().getForegroundMusic().fadeOutStop();
            setScreen(new MenuScreen(getGame(), true), new ScaleScreenTransition(
                    Cfg.SCREEN_TRANSITION_TIME, Interpolation.smooth, true));
        }

        GdxUtils.clearScreen(Color.BLACK);

        insideTentRenderer.render(batch);
    }

    @Override
    public void resize(int width, int height) {
        insideTentRenderer.resize(width, height);
    }
}
