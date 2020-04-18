package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
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
import de.bsautermeister.jump.utils.GdxUtils;

public class MenuBackgroundRenderer implements Disposable {

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final OrthographicCamera backgroundParallaxCamera;
    private final Viewport viewport;

    private final ShaderProgram waterShader;
    private final TextureRegion waterTexture;

    private final TmxMapLoader mapLoader;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private Array<Rectangle> waterList;

    private final Interpolation interpolation = new Interpolation.SwingOut(1.0f);

    private float gameTime;

    public MenuBackgroundRenderer(SpriteBatch batch, TextureAtlas atlas) {
        this.batch = batch;
        camera = new OrthographicCamera();
        backgroundParallaxCamera = new OrthographicCamera();
        viewport = new StretchViewport((Cfg.WORLD_WIDTH + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, (Cfg.WORLD_HEIGHT + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, camera);

        waterShader = GdxUtils.loadCompiledShader("shader/default.vs","shader/water.fs");

        waterTexture = atlas.findRegion(RegionNames.WATER);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("maps/menu_background.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / Cfg.PPM, batch);

        WorldCreator worldCreator = new WorldCreator(null, null, map, atlas);
        worldCreator.buildFromMap();
        waterList = worldCreator.getWaterRegions();
    }

    public void update(float delta) {
        gameTime += delta;

        camera.position.x = viewport.getWorldWidth() / 2 + Cfg.BLOCK_SIZE_PPM;
        camera.position.y = viewport.getWorldHeight() / 2 + interpolation.apply(
                3.66f, 2 * Cfg.BLOCK_SIZE_PPM, MathUtils.clamp(gameTime / 3, 0f, 1f));
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
        batch.setShader(null);
        batch.end();
    }

    private void renderBackground(SpriteBatch batch) {
        renderParallaxLayer(backgroundParallaxCamera, WorldCreator.BG_IMG_STATIC_KEY, 1.0f);
        renderParallaxLayer(backgroundParallaxCamera, WorldCreator.BG_IMG_PARALLAX5_KEY, 0.1f);
        renderParallaxLayer(backgroundParallaxCamera, WorldCreator.BG_IMG_PARALLAX4_KEY, 0.2f);
        renderParallaxLayer(backgroundParallaxCamera, WorldCreator.BG_IMG_PARALLAX3_KEY, 0.4f);
        renderParallaxLayer(backgroundParallaxCamera, WorldCreator.BG_IMG_PARALLAX2_KEY, 0.6f);
        renderParallaxLayer(backgroundParallaxCamera, WorldCreator.BG_IMG_PARALLAX1_KEY, 0.80f);

        mapRenderer.setView(camera);

        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BG_TILES_KEY));
    }

    private void renderParallaxLayer(OrthographicCamera parallaxCamera, String layer, float factor) {
        parallaxCamera.setToOrtho(false, camera.viewportWidth, camera.viewportHeight);
        parallaxCamera.position.set(
                camera.viewportWidth * (1 - factor) + camera.position.x * factor,
                camera.viewportHeight * (1 - factor)  + camera.position.y * factor,
                0);
        parallaxCamera.update();
        mapRenderer.setView(parallaxCamera);
        mapRenderer.renderImageLayer((TiledMapImageLayer) map.getLayers().get(layer));
    }

    private void renderForeground(SpriteBatch batch) {
        renderWater(batch, waterTexture, 1f);
        renderWater(batch, waterTexture, 0.5f);

        mapRenderer.setView(camera);
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.FG_TILES_KEY));
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
    }
}
