package de.bsautermeister.jump.screens.finish;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.utils.GdxUtils;

public class FinishScreen extends ScreenBase {

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final AssetManager assetManager;

    private TextureRegion tentInsideBackground;
    private TextureRegion tableRow;

    private float gameTime;

    private AnimationPath personAnimationPath;
    private Animation<TextureRegion> person;

    private static final Array<PersonMeta> FORMATION = Array.with(
            new PersonMeta(0f),
            new PersonMeta(0f),
            new PersonMeta(0f),
            new PersonMeta(0f),
            new PersonMeta(0f),
            new PersonMeta(0f),
            new PersonMeta(0f),
            new PersonMeta(0f),
            PersonMeta.empty(),
            new PersonMeta(-1f),
            new PersonMeta(-1f),
            new PersonMeta(-1f),
            new PersonMeta(-1f),
            new PersonMeta(-1f),
            new PersonMeta(-1f),
            new PersonMeta(-1f),
            new PersonMeta(-1f),
            PersonMeta.empty(),
            new PersonMeta(-2f),
            new PersonMeta(-2f),
            new PersonMeta(-2f),
            new PersonMeta(-2f),
            new PersonMeta(-2f),
            new PersonMeta(-2f),
            new PersonMeta(-2f),
            new PersonMeta(-2f)
    );

    public FinishScreen(GameApp game) {
        super(game);

        batch = game.getBatch();
        assetManager = game.getAssetManager();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        viewport = new StretchViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT, camera);
    }

    @Override
    public void show() {
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.GAMEPLAY);
        tentInsideBackground = atlas.findRegion(RegionNames.TENT_INSIDE_BACKGROUND);
        tableRow = atlas.findRegion(RegionNames.TABLE_ROW);
        person = new Animation<TextureRegion>(0.066f,
                atlas.findRegions(RegionNames.BIG_PLAYER_BEER_VICTORY), Animation.PlayMode.NORMAL);
        personAnimationPath = new AnimationPath(0f, true, Array.with(
                new AnimationPathItem(1.0f, 1f),
                new AnimationPathItem(2.0f, 1f),
                new AnimationPathItem(1.0f, 0f),
                new AnimationPathItem(2.0f, 0f),
                new AnimationPathItem(1.0f, 1f),
                new AnimationPathItem(2.0f, 1f),
                new AnimationPathItem(1.0f, 0f),
                new AnimationPathItem(2.0f, 0f)
        ));
    }

    @Override
    public void render(float delta) {
        // update
        gameTime += delta;
        personAnimationPath.update(delta);

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
    }

    private void renderForeground(float delta) {
        float width = viewport.getWorldWidth();

        float personStateTime = personAnimationPath.getValue();

        drawRow(FORMATION, personStateTime, width / 2, 36, -2.5f, 0.725f);
        drawTableRow(width / 2, 33, 0.625f);
        drawRow(FORMATION, personStateTime, width / 2, 28, -2.25f, 0.775f);
        drawRow(FORMATION, personStateTime, width / 2, 24, -2.0f, 0.8f);
        drawTableRow(width / 2, 20, 0.75f);
        drawRow(FORMATION, personStateTime, width / 2, 16, -1.5f, 0.85f);
        drawRow(FORMATION, personStateTime, width / 2, 12, -1.25f, 0.875f);
        drawTableRow(width / 2, 7, 0.875f);
        drawRow(FORMATION, personStateTime, width / 2, 4, -0.75f, 0.925f);
        drawRow(FORMATION, personStateTime, width / 2, 0, -0.5f, 0.95f);
        drawTableRow(width / 2, -6, 1.0f);
        drawRow(FORMATION, personStateTime, width / 2, -8, 0, 1.0f);
    }

    private void drawTableRow(float centerX, float bottomY, float scale) {
        float posX = centerX - tableRow.getRegionWidth() / 2;
        batch.draw(tableRow, posX, bottomY,
                tableRow.getRegionWidth() / 2, 0.0f,
                tableRow.getRegionWidth(), tableRow.getRegionHeight(),
                scale, scale, 0.0f);
    }

    private void drawRow(Array<PersonMeta> formation, float stateTime, float centerX, float bottomY, float padding, float scale) {
        // right
        int startIdx = formation.size / 2;
        float startX = centerX + padding / 2;
        for (int x = 0; x < formation.size / 2; ++x) {
            PersonMeta personMeta = formation.get(startIdx + x);

            if (personMeta.isPlaceholder) continue;

            TextureRegion personFrame = person.getKeyFrame(stateTime);
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
            PersonMeta personMeta = formation.get(startIdx - x);

            if (personMeta.isPlaceholder) continue;

            TextureRegion personFrame = person.getKeyFrame(stateTime);
            float posX = startX - x * padding - (x + 1) *  (personFrame.getRegionWidth() * scale);
            batch.draw(personFrame, posX, bottomY,
                    personFrame.getRegionWidth() / 2, 0.0f,
                    personFrame.getRegionWidth(), personFrame.getRegionHeight(),
                    scale, scale, 0.0f);
        }
    }

    private static class PersonMeta {
        public final boolean isPlaceholder;
        public final float timeOffset;
        // TODO character bit, color, action (wipping, jumping, cheering)? Or even sub-typing?

        private PersonMeta(boolean isPlaceholder) {
            this.timeOffset = 0f;
            this.isPlaceholder = isPlaceholder;
        }

        public PersonMeta() {
            this(0f);
        }

        public PersonMeta(float timeOffset) {
            this.timeOffset = timeOffset;
            isPlaceholder = false;
        }

        static PersonMeta empty() {
            return new PersonMeta(true);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    private static class AnimationPath {
        private float pathItemTimeElapsed;

        private final boolean loop;
        private final float initialValue;
        private float value;
        private final Array<AnimationPathItem> path;
        private int currentPathIndex;

        private final Interpolation interpolation = Interpolation.linear;
        private float previousValue;

        public AnimationPath(float initialValue, boolean loop, Array<AnimationPathItem> path) {
            this.initialValue = initialValue;
            this.loop = loop;
            this.path = path;
            reset();
        }

        private void reset() {
            pathItemTimeElapsed = 0;
            currentPathIndex = 0;
            value = initialValue;
            previousValue = initialValue;
        }

        public void update(float delta) {
            if (currentPathIndex >= path.size) {
                if (loop) {
                    currentPathIndex = 0;
                } else {
                    return;
                }
            }

            pathItemTimeElapsed += delta;

            AnimationPathItem currentItem = path.get(currentPathIndex);
            float progress = Math.min(pathItemTimeElapsed / currentItem.duration, 1.0f);

            value = interpolation.apply(previousValue, currentItem.value, progress);

            if (progress >= 1.0f) {
                currentPathIndex++;
                previousValue = currentItem.value;
                pathItemTimeElapsed = 0f;
            }
        }

        public float getValue() {
            return value;
        }
    }

    private static class AnimationPathItem {
        public final float duration;
        public final float value;

        public AnimationPathItem(float duration, float value) {
            this.duration = duration;
            this.value = value;
        }
    }
}
