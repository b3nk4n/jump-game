package de.bsautermeister.jump.scenes;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.RegionNames;

public class Hud implements Disposable {
    private final static float PAD_TOP = 8f;
    private final static float PAD_SIDE = 24f;

    private final Stage stage;

    private int currentTTL;
    private int currentScore;
    private int currentBeers;
    private final int totalBeers;

    private Label timeValueLabel;
    private Label scoreValueLabel;
    private Label timeLabel;
    private Label scoreLabel;

    private final Animation<TextureRegion> beerometer;
    private final BitmapFont fontS;

    public Hud(SpriteBatch batch, Viewport uiViewport, AssetManager assetManager, int totalBeers) {
        this.stage = new Stage(uiViewport, batch);

        this.totalBeers = totalBeers;
        this.stage.addActor(buildUi(assetManager));
        this.stage.setDebugAll(Cfg.DEBUG_MODE);

        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.UI);

        fontS = assetManager.get(AssetDescriptors.Fonts.S);
        this.beerometer = new Animation<TextureRegion>(0,
                atlas.findRegions(RegionNames.fromTemplate(RegionNames.BEEROMETER_TPL, totalBeers)));
    }

    private Actor buildUi(AssetManager assetManager) {
        BitmapFont fontM = assetManager.get(AssetDescriptors.Fonts.M);
        Label.LabelStyle labelStyleM = new Label.LabelStyle(fontM, Color.WHITE);

        timeLabel = new Label("Time", labelStyleM);
        timeValueLabel = new Label(getFormattedCountDown(currentTTL), labelStyleM);
        scoreLabel = new Label("Score", labelStyleM);
        scoreValueLabel = new Label(getFormattedScore(currentScore), labelStyleM);

        Table table = new Table()
                .top();
        table.setFillParent(true);
        table.top();

        table.add(scoreLabel)
                .expandX()
                .left();
        table.add(timeLabel)
                .expandX()
                .right();
        table.row();
        table.add(scoreValueLabel)
                .expandX()
                .left();
        table.add(timeValueLabel)
                .expandX()
                .right();
        table.padTop(PAD_TOP).padLeft(PAD_SIDE).padRight(PAD_SIDE);
        table.pack();

        return table;
    }

    public void update(int beers, int score, float ttl) {
        updateBeers(beers);
        updateScore(score);
        updateTimeToLive(ttl);
    }

    private void updateBeers(int beers) {
        if (currentBeers != beers) {
            currentBeers = Math.min(beers, totalBeers);
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

    public void draw(SpriteBatch batch) {
        stage.draw();

        drawBeerometer(batch);
    }

    private void drawBeerometer(SpriteBatch batch) {
        TextureRegion frame = beerometer.getKeyFrames()[currentBeers];
        float x = Cfg.UI_WIDTH / 2f - frame.getRegionWidth() / 2f;
        float y = Cfg.UI_HEIGHT - frame.getRegionHeight() - 42f;

        batch.begin();
        batch.draw(frame, x, y);
        fontS.draw(batch, "Beerometer", x, Cfg.UI_HEIGHT - 12f);
        batch.end();
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

    public Camera getCamera() {
        return stage.getCamera();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
