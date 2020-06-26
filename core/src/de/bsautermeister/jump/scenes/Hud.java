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

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;

public class Hud implements Disposable {
    private final Stage stage;

    private int currentTTL;
    private int currentScore;
    private int currentBeers;
    private final int totalBeers;

    private Label timeValueLabel;
    private Label scoreValueLabel;
    private Label timeLabel;
    private Label beersValueLabel;
    private Label beersLabel;
    private Label scoreLabel;

    private Label.LabelStyle labelStyle;

    public Hud(SpriteBatch batch, Viewport uiViewport, AssetManager assetManager, int totalBeers) {
        this.stage = new Stage(uiViewport, batch);
        BitmapFont font = assetManager.get(AssetDescriptors.Fonts.M);
        labelStyle = new Label.LabelStyle(font, Color.WHITE);
        this.totalBeers = totalBeers;
        this.stage.addActor(buildUi());
        this.stage.setDebugAll(Cfg.DEBUG_MODE);
    }

    private Actor buildUi() {
        Table table = new Table()
                .top();
        table.setFillParent(true);
        table.top();

        timeLabel = new Label("Time", labelStyle);
        timeValueLabel = new Label(getFormattedCountDown(currentTTL), labelStyle);
        scoreLabel = new Label("Score", labelStyle);
        scoreValueLabel = new Label(getFormattedScore(currentScore), labelStyle);
        beersLabel = new Label("Beers", labelStyle);
        beersValueLabel = new Label(getFormattedBeers(currentBeers, totalBeers), labelStyle);

        final float padTop = 8f;
        final float padVertical = 16f;
        table.add(scoreLabel)
                .left();
        table.add(beersLabel)
                .expandX();
        table.add(timeLabel)
                .right();
        table.row();
        table.add(scoreValueLabel)
                .left();
        table.add(beersValueLabel)
                .expandX();
        table.add(timeValueLabel)
                .right();
        table.padTop(8f).padLeft(16f).padRight(16f);

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
            beersValueLabel.setText(getFormattedBeers(currentBeers, totalBeers));
        }
    }

    private void updateScore(int score) {
        if (currentScore != score) {
            currentScore = score;
            scoreValueLabel.setText(getFormattedScore(currentScore));
        }
    }

    private void updateTimeToLive(float timeToLive) {
        int ttl = (int)Math.ceil(timeToLive);
        if (currentTTL != ttl) {
            currentTTL = ttl;
            timeValueLabel.setText(getFormattedCountDown(currentTTL));
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
