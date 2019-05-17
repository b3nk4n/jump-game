package de.bsautermeister.jump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameOverScreen extends ScreenBase {
    private Viewport viewport;
    private Stage stage;

    public GameOverScreen(GameApp game) {
        super(game);
        this.viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        this.stage = new Stage(viewport, game.getBatch());

        Label.LabelStyle font = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        Label gameOverLabel = new Label("GAME OVER", font);
        table.add(gameOverLabel).expandX();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen(Color.BLACK);
        stage.draw();

        if (Gdx.input.isTouched()) {
            setScreen(new GameScreen(getGame()));
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
