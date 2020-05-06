package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.game.GameScreen;
import de.bsautermeister.jump.screens.transition.ScreenTransitions;
import de.bsautermeister.jump.utils.GdxUtils;

public class SelectLevelScreen extends ScreenBase {
    private final Viewport viewport;
    private SelectLevelStage stage;
    private final int page;

    public SelectLevelScreen(GameApp game, final int page) {
        super(game);
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        this.page = page;
    }

    @Override
    public void show() {
        this.stage = new SelectLevelStage(viewport, getGame().getBatch(), getAssetManager(), page,
                new SelectLevelStage.Callbacks() {
                    @Override
                    public void leftClicked() {
                        setScreen(new SelectLevelScreen(getGame(), page - 1), ScreenTransitions.SLIDE_RIGHT);
                    }

                    @Override
                    public void rightClicked() {
                        setScreen(new SelectLevelScreen(getGame(), page + 1), ScreenTransitions.SLIDE_LEFT);
                    }

                    @Override
                    public void levelSelected(int absoluteLevel) {
                        setScreen(new GameScreen(getGame(), absoluteLevel));
                    }
                }
        );
        stage.initialize();

        // enable phones BACK button
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen(Color.BLACK);

        handleInput();

        stage.act();
        stage.draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            setScreen(new MenuScreen(getGame()));
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return stage;
    }
}
