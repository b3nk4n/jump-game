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

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.sprites.Mario;

public class Hud implements Disposable {
    private final Stage stage;
    private final Viewport viewport;

    private int currentTTL;
    private int currentScore;
    private int currentLevel;

    private Label countDownLabel;
    private Label scoreLabel;
    private Label timeLabel;
    private Label levelLabel;
    private Label worldLabel;
    private Label marioLabel;

    private Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

    public Hud(SpriteBatch batch) {
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
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
        levelLabel = new Label(getFormattedLevel(currentLevel), labelStyle);
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

    public void update(int level, int score, float ttl) {
        updateLevel(level);
        updateScore(score);
        updateTimeToLive(ttl);
    }

    private void updateLevel(int level) {
        if (currentLevel != level) {
            currentLevel = level;
            levelLabel.setText(getFormattedLevel(currentLevel));
        }
    }

    private void updateScore(int score) {
        if (currentScore != score) {
            currentScore = score;
            scoreLabel.setText(getFormattedScore(currentScore));
        }
    }

    private void updateTimeToLive(float timeToLive) {
        int ttl = (int)Math.ceil(timeToLive);
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

    private static String getFormattedLevel(int level) {
        int stage = (level - 1) / Cfg.LEVELS_PER_STAGE + 1;
        int stageLevel = (level - 1) % Cfg.LEVELS_PER_STAGE + 1;

        return String.format("%d-%d", stage, stageLevel);
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
