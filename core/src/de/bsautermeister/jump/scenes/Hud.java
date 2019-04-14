package de.bsautermeister.jump.scenes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.GameConfig;

public class Hud implements Disposable {
    private final Stage stage;
    private final Viewport viewport;

    private int worldTimer;
    private float timeCount;
    private static int score;

    private Label countDownLabel;
    private static Label scoreLabel;
    private Label timeLabel;
    private Label levelLabel;
    private Label worldLabel;
    private Label marioLabel;

    private Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

    public Hud(SpriteBatch batch) {
        this.worldTimer = 300;
        this.timeCount = 0;
        this.score = 0;

        this.viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        this.stage = new Stage(viewport, batch);
        this.stage.addActor(buildUi());
    }

    private Actor buildUi() {
        Table table = new Table()
                .top();
        table.setFillParent(true);
        table.top();

        countDownLabel = new Label(getFormattedCountDown(worldTimer), labelStyle);
        scoreLabel = new Label(getFormattedScore(score), labelStyle);
        timeLabel = new Label("TIME", labelStyle);
        levelLabel = new Label("1-1", labelStyle);
        worldLabel = new Label("WORLD", labelStyle);
        marioLabel = new Label("MARIO", labelStyle);

        table.add(marioLabel)
                .expandX()
                .padTop(10);
        table.add(worldLabel)
                .expandX()
                .padTop(10);
        table.add(timeLabel)
                .expandX()
                .padTop(10);
        table.row();
        table.add(scoreLabel)
                .expandX();
        table.add(levelLabel)
                .expandX();
        table.add(countDownLabel)
                .expandX();

        return table;
    }

    public void update(float delta) {
        timeCount += delta;

        if (timeCount > 1) {
            worldTimer--;
            countDownLabel.setText(getFormattedCountDown(worldTimer));
            timeCount = 0;
        }
    }

    public static void addScore(int value) { // TODO make it non-static, and actually store the score outside of the HUD (move score and time to Mario class?)
        score += value;
        scoreLabel.setText(getFormattedScore(score));
    }

    private static String getFormattedCountDown(int worldTimer) {
        return String.format("%03d", worldTimer);
    }

    private static String getFormattedScore(int score) {
        return String.format("%06d", score);
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
