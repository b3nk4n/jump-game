package de.bsautermeister.jump.scenes;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.assets.AssetDescriptors;

public class Hud implements Disposable {
    private final Stage stage;

    private int currentTTL;
    private int currentScore;
    private int currentBeers;
    private final int totalBeers;

    private Label countDownLabel;
    private Label scoreLabel;
    private Label timeLabel;
    private Label beersLabel;
    private Label worldLabel;
    private Label playerLabel;

    private Label.LabelStyle labelStyle;

    public Hud(SpriteBatch batch, Viewport hudViewport, AssetManager assetManager, int totalBeers) {
        this.stage = new Stage(hudViewport, batch);
        BitmapFont font = assetManager.get(AssetDescriptors.Fonts.S);
        labelStyle = new Label.LabelStyle(font, Color.WHITE);
        this.totalBeers = totalBeers;
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
        beersLabel = new Label(getFormattedBeers(currentBeers, totalBeers), labelStyle);
        worldLabel = new Label("BEERS", labelStyle);
        playerLabel = new Label("PLAYER", labelStyle);

        table.add(playerLabel)
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
        table.add(beersLabel)
                .expandX();
        table.add(countDownLabel)
                .expandX();

        return table;
    }

    public void update(int beers, int score, float ttl) {
        updateBeers(beers);
        updateScore(score);
        updateTimeToLive(ttl);
    }

    private void updateBeers(int level) {
        if (currentBeers != level) {
            currentBeers = level;
            beersLabel.setText(getFormattedBeers(currentBeers, totalBeers));
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

    private static String getFormattedBeers(int beers, int totalBeers) {
        return String.format("%d / %d", beers, totalBeers);
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
