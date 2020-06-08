package de.bsautermeister.jump.screens.finish;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.finish.model.Person;
import de.bsautermeister.jump.screens.finish.model.PersonFormation;
import de.bsautermeister.jump.screens.finish.model.PersonFormationFactory;
import de.bsautermeister.jump.utils.GdxUtils;

public class FinishScreen extends ScreenBase {

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final AssetManager assetManager;

    private TextureRegion tentInsideBackground;
    private TextureRegion tentInsideDecoration;
    private TextureRegion tableRow;

    private Animation<TextureRegion> person;

    private PersonFormation personFormation;

    public FinishScreen(GameApp game) {
        super(game);

        batch = game.getBatch();
        assetManager = game.getAssetManager();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        viewport = new StretchViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT, camera);

        personFormation = PersonFormationFactory.createRandomFormation();
    }

    @Override
    public void show() {
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.GAMEPLAY);
        tentInsideBackground = atlas.findRegion(RegionNames.TENT_INSIDE_BACKGROUND);
        tentInsideDecoration = atlas.findRegion(RegionNames.TENT_DECORATION_BACKGROUND);
        tableRow = atlas.findRegion(RegionNames.TABLE_ROW);
        person = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.BIG_PLAYER_BEER_VICTORY), Animation.PlayMode.NORMAL);
    }

    @Override
    public void render(float delta) {
        // update
        personFormation.update(delta);

        camera.update();

        // draw
        viewport.apply();

        GdxUtils.clearScreen(Color.BLACK);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderBackground();
        renderForeground(delta);

        batch.end();
    }

    private void renderBackground() {
        batch.draw(tentInsideBackground, 0, 0);
        batch.draw(tentInsideDecoration, 0, 0);
    }

    private void renderForeground(float delta) {
        float width = viewport.getWorldWidth();

        drawRow(personFormation.getRow(0), width / 2, 36, -2.5f, 0.725f, 0.725f);
        drawTableRow(width / 2, 33, 0.625f, 0.75f);
        drawRow(personFormation.getRow(1), width / 2, 28, -2.25f, 0.775f, 0.775f);
        drawRow(personFormation.getRow(2), width / 2, 24, -2.0f, 0.8f, 0.8f);
        drawTableRow(width / 2, 20, 0.75f, 0.825f);
        drawRow(personFormation.getRow(3), width / 2, 16, -1.5f, 0.85f, 0.85f);
        drawRow(personFormation.getRow(4), width / 2, 12, -1.25f, 0.875f, 0.875f);
        drawTableRow(width / 2, 7, 0.875f, 0.9f);
        drawRow(personFormation.getRow(5), width / 2, 4, -0.75f, 0.925f, 0.925f);
        drawRow(personFormation.getRow(6), width / 2, 0, -0.5f, 0.95f, 0.95f);
        drawTableRow(width / 2, -6, 1.0f, 0.975f);
        drawRow(personFormation.getRow(7), width / 2, -8, 0, 1.0f, 1.0f);
    }

    private void drawTableRow(float centerX, float bottomY, float scale, float tint) {
        batch.setColor(tint, tint, tint, 1.0f);
        float posX = centerX - tableRow.getRegionWidth() / 2;
        batch.draw(tableRow, posX, bottomY,
                tableRow.getRegionWidth() / 2, 0.0f,
                tableRow.getRegionWidth(), tableRow.getRegionHeight(),
                scale, scale, 0.0f);
        batch.setColor(Color.WHITE);
    }

    private void drawRow(Array<Person> formation, float centerX, float bottomY, float padding, float scale, float tint) {
        batch.setColor(tint, tint, tint, 1.0f);
        // right
        int startIdx = formation.size / 2;
        float startX = centerX + padding / 2;
        for (int x = 0; x < formation.size / 2; ++x) {
            Person person = formation.get(startIdx + x);

            if (person.isPlaceholder) continue;

            TextureRegion personFrame = this.person.getKeyFrame(person.getAnimationValue());
            float posX = startX + x * (padding + personFrame.getRegionWidth() * scale);
            batch.draw(personFrame, posX, bottomY,
                    personFrame.getRegionWidth() / 2, 0.0f,
                    personFrame.getRegionWidth(), personFrame.getRegionHeight(),
                    scale, scale, 0.0f);
        }

        // left
        startIdx = formation.size / 2 - 1;
        startX = centerX - padding / 2;
        for (int x = 0; x < formation.size / 2; ++x) {
            Person person = formation.get(startIdx - x);

            if (person.isPlaceholder) continue;

            TextureRegion personFrame = this.person.getKeyFrame(person.getAnimationValue());
            float posX = startX - x * padding - (x + 1) *  (personFrame.getRegionWidth() * scale);
            batch.draw(personFrame, posX, bottomY,
                    personFrame.getRegionWidth() / 2, 0.0f,
                    personFrame.getRegionWidth(), personFrame.getRegionHeight(),
                    scale, scale, 0.0f);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }
}
