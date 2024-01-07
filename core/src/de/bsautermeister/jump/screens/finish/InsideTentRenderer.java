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
import de.bsautermeister.jump.screens.finish.model.Person;
import de.bsautermeister.jump.screens.finish.model.PersonFormation;
import de.bsautermeister.jump.screens.finish.model.PersonFormationFactory;

public class InsideTentRenderer {

    private final OrthographicCamera camera;
    private final Viewport viewport;

    private TextureRegion tentInsideBackground;
    private TextureRegion tentInsideDecoration;
    private TextureRegion tableRow;


    private Animation<TextureRegion>[] personAnimations;

    private PersonFormation personFormation;

    public InsideTentRenderer(AssetManager assetManager) {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        viewport = new StretchViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT, camera);

        init(assetManager);
    }

    private void init(AssetManager assetManager) {
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.GAMEPLAY);
        tentInsideBackground = atlas.findRegion(RegionNames.TENT_INSIDE_BACKGROUND);
        tentInsideDecoration = atlas.findRegion(RegionNames.TENT_DECORATION_BACKGROUND);
        tableRow = atlas.findRegion(RegionNames.TABLE_ROW);
        personAnimations = new Animation[Person.VARIATIONS];
        personAnimations[0] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.BIG_PLAYER_BEER_VICTORY), Animation.PlayMode.NORMAL);
        personAnimations[1] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.GIRLFRIEND), Animation.PlayMode.NORMAL);
        personAnimations[2] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_1), Animation.PlayMode.NORMAL);
        personAnimations[3] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_2), Animation.PlayMode.NORMAL);
        personAnimations[4] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_3), Animation.PlayMode.NORMAL);
        personAnimations[5] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_4), Animation.PlayMode.NORMAL);
        personAnimations[6] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_5), Animation.PlayMode.NORMAL);
        personAnimations[7] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_6), Animation.PlayMode.NORMAL);
        personAnimations[8] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_7), Animation.PlayMode.NORMAL);
        personAnimations[9] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_8), Animation.PlayMode.NORMAL);
        personAnimations[10] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_9), Animation.PlayMode.NORMAL);
        personAnimations[11] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.PERSON_10), Animation.PlayMode.NORMAL);
        personAnimations[12] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.GIRL_1), Animation.PlayMode.NORMAL);
        personAnimations[13] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.GIRL_2), Animation.PlayMode.NORMAL);
        personAnimations[14] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.GIRL_3), Animation.PlayMode.NORMAL);
        personAnimations[15] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.GIRL_4), Animation.PlayMode.NORMAL);
        personAnimations[16] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.GIRL_5), Animation.PlayMode.NORMAL);
        personAnimations[17] = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.GIRL_6), Animation.PlayMode.NORMAL);

        personFormation = PersonFormationFactory.createRandomFormation();
    }

    public void update(float delta) {
        personFormation.update(delta);
        camera.update();

    }

    public void render(SpriteBatch batch, boolean usedInFbo) {
        if (!usedInFbo) {
            viewport.apply();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderBackground(batch);
        renderForeground(batch);

        batch.end();
    }

    private void renderBackground(SpriteBatch batch) {
        batch.draw(tentInsideBackground, 0, 0, Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        batch.draw(tentInsideDecoration, 0, 0, Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
    }

    private void renderForeground(SpriteBatch batch) {
        float width = viewport.getWorldWidth();

        drawPersonRow(batch, personFormation.getRow(0), width / 2, 36, -2.5f, 0.725f, 0.725f);
        drawTableRow(batch, width / 2, 33, 0.625f, 0.75f);
        drawPersonRow(batch, personFormation.getRow(1), width / 2, 28, -2.25f, 0.775f, 0.775f);
        drawPersonRow(batch, personFormation.getRow(2), width / 2, 24, -2.0f, 0.8f, 0.8f);
        drawTableRow(batch, width / 2, 20, 0.75f, 0.825f);
        drawPersonRow(batch, personFormation.getRow(3), width / 2, 16, -1.5f, 0.85f, 0.85f);
        drawPersonRow(batch, personFormation.getRow(4), width / 2, 12, -1.25f, 0.875f, 0.875f);
        drawTableRow(batch, width / 2, 7, 0.875f, 0.9f);
        drawPersonRow(batch, personFormation.getRow(5), width / 2, 4, -0.75f, 0.925f, 0.925f);
        drawPersonRow(batch, personFormation.getRow(6), width / 2, 0, -0.5f, 0.95f, 0.95f);
        drawTableRow(batch, width / 2, -6, 1.0f, 0.975f);
        drawPersonRow(batch, personFormation.getRow(7), width / 2, -8, 0, 1.0f, 1.0f);
    }

    private void drawTableRow(SpriteBatch batch, float centerX, float bottomY, float scale, float tint) {
        batch.setColor(tint, tint, tint, 1.0f);
        float posX = centerX - tableRow.getRegionWidth() / 2;
        batch.draw(tableRow, posX, bottomY,
                tableRow.getRegionWidth() / 2, 0.0f,
                tableRow.getRegionWidth(), tableRow.getRegionHeight(),
                scale, scale, 0.0f);
        batch.setColor(Color.WHITE);
    }

    private void drawPersonRow(SpriteBatch batch, Array<Person> formation, float centerX, float bottomY, float padding, float scale, float tint) {
        batch.setColor(tint, tint, tint, 1.0f);
        // right
        int startIdx = formation.size / 2;
        float startX = centerX + padding / 2;
        for (int x = 0; x < formation.size / 2; ++x) {
            Person person = formation.get(startIdx + x);

            if (person.isPlaceholder) continue;

            Animation<TextureRegion> animation = this.personAnimations[person.getCharacterIdx()];
            TextureRegion personFrame = animation.getKeyFrame(person.getAnimationValue());
            float posX = startX + x * (padding + personFrame.getRegionWidth() * scale);
            batch.draw(personFrame, posX, bottomY,
                    personFrame.getRegionWidth() / 2, 0.0f,
                    personFrame.getRegionWidth(), personFrame.getRegionHeight(),
                    scale, scale, person.getRotation());
        }

        // left
        startIdx = formation.size / 2 - 1;
        startX = centerX - padding / 2;
        for (int x = 0; x < formation.size / 2; ++x) {
            Person person = formation.get(startIdx - x);

            if (person.isPlaceholder) continue;

            Animation<TextureRegion> animation = this.personAnimations[person.getCharacterIdx()];
            TextureRegion personFrame = animation.getKeyFrame(person.getAnimationValue());
            float posX = startX - x * padding - (x + 1) *  (personFrame.getRegionWidth() * scale);
            batch.draw(personFrame, posX, bottomY,
                    personFrame.getRegionWidth() / 2, 0.0f,
                    personFrame.getRegionWidth(), personFrame.getRegionHeight(),
                    scale, scale, person.getRotation());
        }
        batch.setColor(Color.WHITE);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }
}
