package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.rendering.ParallaxRenderer;
import de.bsautermeister.jump.sprites.Snorer;
import de.bsautermeister.jump.utils.GdxUtils;

public class MenuBackgroundRenderer implements Disposable {

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final ShaderProgram waterShader;
    private final TextureRegion waterTexture;

    private final TmxMapLoader mapLoader;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final ParallaxRenderer parallaxRenderer;
    private final Array<Rectangle> waterList;
    private final Snorer snorer;

    private final Interpolation interpolation = new Interpolation.SwingOut(1.0f);

    private final static float IN_TRANSITION_TIME = 3f;
    private float gameTime;

    public MenuBackgroundRenderer(AssetManager assetManager, SpriteBatch batch, TextureAtlas atlas) {
        this.batch = batch;
        camera = new OrthographicCamera();
        viewport = new StretchViewport(Cfg.WORLD_WIDTH / Cfg.PPM, Cfg.WORLD_HEIGHT / Cfg.PPM, camera);

        waterShader = GdxUtils.loadCompiledShader("shader/default.vs","shader/water.fs");

        waterTexture = atlas.findRegion(RegionNames.WATER);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("maps/menu_background.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / Cfg.PPM, batch);
        parallaxRenderer = new ParallaxRenderer(camera, mapRenderer);
        parallaxRenderer.setMap(map);

        WorldCreator worldCreator = new WorldCreator(null, null, map, atlas);
        worldCreator.buildFromMap();
        waterList = worldCreator.getWaterRegions();

        snorer = new Snorer(assetManager, atlas, worldCreator.getSnorerRegion());
    }

    public void update(float delta) {
        gameTime += delta;

        camera.position.x = viewport.getWorldWidth() / 2 + Cfg.BLOCK_SIZE_PPM;
        camera.position.y = viewport.getWorldHeight() / 2 + interpolation.apply(
                32f, 2 * Cfg.BLOCK_SIZE_PPM, MathUtils.clamp(gameTime / IN_TRANSITION_TIME, 0f, 1f));

        snorer.update(delta);
    }

    public void render() {
        GdxUtils.clearScreen(Color.BLACK);
        viewport.apply();

        GdxUtils.clearScreen(Color.BLACK);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (gameTime < 0.5f) {
            float progress = Interpolation.smooth.apply(Math.min(2 * gameTime, 1.0f));
            batch.setColor(progress, progress, progress, 1f);
        }

        renderBackground(batch);
        renderForeground(batch);
        batch.end();
    }

    public void skipIntroTransition() {
        gameTime += IN_TRANSITION_TIME;
    }

    private void renderBackground(SpriteBatch batch) {
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_STATIC_KEY, 1.0f, 1.0f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_CLOUDS2_KEY, 0.1f, 0.05f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_MOUNTAINS_KEY, 0.2f, 0.1f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_CLOUDS1_KEY, 0.4f, 0.2f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_FORREST2_KEY, 0.65f, 0.325f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_FORREST1_KEY, 0.80f, 0.4f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_GRASS2_KEY, 0.85f, 0.425f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_GRASS1_KEY, 0.90f, 0.45f);

        mapRenderer.setView(camera);

        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BG_TILES_KEY));
    }

    private void renderForeground(SpriteBatch batch) {
        renderWater(batch, waterTexture, 1f);
        renderWater(batch, waterTexture, 0.5f);

        mapRenderer.setView(camera);
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.FG_TILES_KEY));

        snorer.draw(batch);
    }

    private void renderWater(SpriteBatch batch, TextureRegion waterTexture, float opacity) {
        ShaderProgram prevShader = batch.getShader();
        for (Rectangle waterRegion : waterList) {
            batch.setShader(waterShader);
            waterShader.setUniformf("u_time", gameTime);
            waterShader.setUniformf("u_opacity", opacity);
            waterShader.setUniformf("u_width", waterRegion.getWidth() * Cfg.PPM);
           int regionHeightInPixel = MathUtils.round(waterRegion.getHeight() * Cfg.PPM);
           batch.draw(waterTexture.getTexture(),
                   waterRegion.getX(), (waterRegion.getY()),
                   waterRegion.getWidth(), waterRegion.getHeight(),
                   waterTexture.getRegionX(), waterTexture.getRegionY(),
                   waterTexture.getRegionWidth(), regionHeightInPixel,
                   false, false);
        }
        batch.setShader(prevShader);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        waterShader.dispose();
        snorer.stop();
    }
}
