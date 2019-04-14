package de.bsautermeister.jump.scenes;

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
import de.bsautermeister.jump.sprites.Mario;

public class Hud implements Disposable {
    private final Stage stage;
    private final Viewport viewport;

    private Mario mario;

    private int currentTTL;
    private int currentScore;

    private Label countDownLabel;
    private Label scoreLabel;
    private Label timeLabel;
    private Label levelLabel;
    private Label worldLabel;
    private Label marioLabel;

    private Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

    public Hud(SpriteBatch batch, Mario mario) {
        this.mario = mario;

        this.currentTTL = 300;
        this.currentScore = 0;

        this.viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        this.stage = new Stage(viewport, batch);
        this.stage.addActor(buildUi());
    }

    private Actor buildUi() {
        Table table = new Table()
                .top();
        table.setFillParent(true);
        table.top();

        countDownLabel = new Label(getFormattedCountDown(currentTTL), labelStyle);
        scoreLabel = new Label(getFormattedScore(currentScore), labelStyle);
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
        updateScore();
        updateTimeToLive();
    }

    private void updateScore() {
        int score = mario.getScore();
        if (currentScore != score) {
            currentScore = score;
            scoreLabel.setText(getFormattedScore(currentScore));
        }
    }

    private void updateTimeToLive() {
        int ttl = (int)Math.ceil(mario.getTimeToLive());
        if (currentTTL != ttl) {
            currentTTL = ttl;
            countDownLabel.setText(getFormattedCountDown(currentTTL));
        }
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
