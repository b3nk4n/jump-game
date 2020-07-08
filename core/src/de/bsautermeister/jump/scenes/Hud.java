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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
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
    private int currentPretzels = -1;

    private Label timeValueLabel;
    private Label scoreValueLabel;
    private Label pretzelValueLabel;

    private Image pretzelImage;

    private Animation<TextureRegion> beerometer;
    private BitmapFont fontS;

    public Hud(SpriteBatch batch, Viewport uiViewport, AssetManager assetManager, int totalBeers) {
        this.stage = new Stage(uiViewport, batch);

        this.totalBeers = totalBeers;
        this.stage.addActor(buildUi(assetManager));
        this.stage.setDebugAll(Cfg.DEBUG_MODE);
    }

    private Actor buildUi(AssetManager assetManager) {
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.UI);

        fontS = assetManager.get(AssetDescriptors.Fonts.S);
        beerometer = new Animation<TextureRegion>(0,
                atlas.findRegions(RegionNames.fromTemplate(RegionNames.UI_BEEROMETER_TPL, totalBeers)));

        TextureRegion timeRegion = atlas.findRegion(RegionNames.UI_TIME);
        TextureRegion pretzelRegion = atlas.findRegion(RegionNames.UI_PRETZEL);

        BitmapFont fontM = assetManager.get(AssetDescriptors.Fonts.M);
        Label.LabelStyle labelStyleM = new Label.LabelStyle(fontM, Color.WHITE);

        timeValueLabel = new Label(getFormattedCountDown(currentTTL), labelStyleM);
        timeValueLabel.setAlignment(Align.right);
        scoreValueLabel = new Label(getFormattedScore(currentScore), labelStyleM);
        pretzelValueLabel = new Label(getFormattedPretzels(currentPretzels), labelStyleM);
        pretzelValueLabel.setAlignment(Align.right);

        Image timeImage = new Image(timeRegion);
        pretzelImage = new Image(pretzelRegion);

        Table table = new Table()
                .top();
        table.setFillParent(true);

        table.add(scoreValueLabel)
                .expandX()
                .left();
        table.add(timeImage)
                .padTop(12f)
                .padRight(8f);
        table.add(timeValueLabel)
                .width(80f)
                .right();
        table.padTop(PAD_TOP).padLeft(PAD_SIDE).padRight(PAD_SIDE);
        table.row();
        table.add();
        table.add(pretzelImage)
                .padTop(12f)
                .padRight(8f);
        table.add(pretzelValueLabel)
                .width(80f)
                .right();
        table.pack();

        return table;
    }

    public void update(int beers, int score, int pretzels, int ttl) {
        updateBeers(beers);
        updateScore(score);
        updatePretzels(pretzels);
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

    private void updatePretzels(int pretzels) {
        if (currentPretzels != pretzels) {
            currentPretzels = pretzels;
            pretzelValueLabel.setText(getFormattedPretzels(currentPretzels));

            boolean visible = currentPretzels > 0;
            pretzelImage.setVisible(visible);
            pretzelValueLabel.setVisible(visible);
        }
    }

    private void updateTimeToLive(int ttl) {
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

    private static String getFormattedPretzels(int pretzels) {
        return String.format("%02d", pretzels);
    }

    public Camera getCamera() {
        return stage.getCamera();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
