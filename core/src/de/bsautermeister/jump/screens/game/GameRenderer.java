package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.LinkedBlockingQueue;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.scenes.Hud;
import de.bsautermeister.jump.sprites.BoxCoin;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.Flower;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Mario;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.text.TextMessage;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameRenderer implements Disposable {

    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final TextureAtlas atlas;
    private final GameController controller;
    private final World world;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Viewport hudViewport;
    private final Hud hud;

    private FrameBuffer frameBuffer;

    private final ShaderProgram waterShader;
    private final TextureRegion waterTexture;
    private final TextureRegion beerLiquidTexture;
    private final ShaderProgram drunkShader;
    private final ShaderProgram stonedShader;

    private final BitmapFont font;

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Box2DDebugRenderer box2DDebugRenderer;

    public GameRenderer(SpriteBatch batch, AssetManager assetManager, TextureAtlas atlas,
                        GameController controller) {
        this.batch = batch;
        this.assetManager = assetManager;
        this.atlas = atlas;
        this.map = controller.getMap();
        this.controller = controller;
        this.camera = controller.getCamera();
        this.viewport = controller.getViewport();
        this.world = controller.getWorld();

        float screenPixelPerTile = Gdx.graphics.getWidth() / Cfg.BLOCKS_X;
        frameBuffer = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                (int)(screenPixelPerTile * (Cfg.BLOCKS_X + 4)), // 2 extra block for each left and right
                (int)(screenPixelPerTile * (Cfg.BLOCKS_Y + 4)), // 2 extra block for each top and bottom
                false);

        hudViewport = new StretchViewport((Cfg.WORLD_WIDTH + 4 * Cfg.BLOCK_SIZE), (Cfg.WORLD_HEIGHT + 4 * Cfg.BLOCK_SIZE));
        hud = new Hud(batch, hudViewport, assetManager, controller.getTotalBeers());

        waterShader = GdxUtils.loadCompiledShader("shader/default.vs","shader/water.fs");
        drunkShader = GdxUtils.loadCompiledShader("shader/default.vs", "shader/wave_distortion.fs");
        stonedShader = GdxUtils.loadCompiledShader("shader/default.vs", "shader/invert_colors.fs");

        waterTexture = atlas.findRegion(RegionNames.WATER);
        beerLiquidTexture = atlas.findRegion(RegionNames.BEER_LIQUID);

        font = assetManager.get(AssetDescriptors.Fonts.MARIO12);

        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / Cfg.PPM, batch);
        this.box2DDebugRenderer = new Box2DDebugRenderer(true, true, false, true, true, true);
    }

    public void render(float delta) {
        float gameTime = controller.getGameTime();
        Mario mario = controller.getMario();
        int score = controller.getScore();
        int collectedBeers = controller.getCollectedBeers();

        mapRenderer.setView(camera);

        GdxUtils.clearScreen(Color.BLACK);
        viewport.apply();

        frameBuffer.begin();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (mario.isStoned()) {
            batch.setShader(stonedShader);
            stonedShader.setUniformf("u_effectRatio", mario.getStonedRatio());
        }
        renderBackground(batch);
        renderForeground(batch);
        renderEffects(batch);
        batch.setShader(null);
        batch.end();
        frameBuffer.end();

        batch.begin();
        if (mario.isDrunk()) {
            batch.setShader(drunkShader);
            drunkShader.setUniformf("u_time", gameTime);
            drunkShader.setUniformf("u_imageSize", Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
            drunkShader.setUniformf("u_amplitude", 7.1f * mario.getDrunkRatio(), 9.1f * mario.getDrunkRatio());
            drunkShader.setUniformf("u_waveLength", 111f, 311f);
            drunkShader.setUniformf("u_velocity", 71f, 111f);
        }
        float screenPixelPerTile = Gdx.graphics.getWidth() / Cfg.BLOCKS_X;
        batch.draw(frameBuffer.getColorBufferTexture(),
                camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight,
                (int)screenPixelPerTile * 2, (int)screenPixelPerTile * 2, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);

        if (mario.isStoned()) {
            Color c = batch.getColor();
            batch.setColor(c.r, c.g, c.b, 0.5f);
            float offset1 =  screenPixelPerTile * 0.66f * (float)Math.sin(-gameTime) * mario.getStonedRatio();
            float offset2 =  screenPixelPerTile * 0.66f * (float)Math.cos(gameTime * 0.8f) * mario.getStonedRatio();
            float offset3 =  screenPixelPerTile * 0.66f * (float)Math.sin(gameTime * 0.9f) * mario.getStonedRatio();
            float offset4 =  screenPixelPerTile * 0.66f * (float)Math.cos(gameTime * 0.7f) * mario.getStonedRatio();
            batch.draw(frameBuffer.getColorBufferTexture(),
                    camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight,
                    (int)(screenPixelPerTile * 2 + offset1), (int)(screenPixelPerTile * 2 - offset2), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
            batch.draw(frameBuffer.getColorBufferTexture(),
                    camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight,
                    (int)(screenPixelPerTile * 2 - offset3), (int)(screenPixelPerTile * 2 + offset4), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
            batch.setColor(c);
        }

        batch.setShader(null);
        batch.end();

        if (Cfg.DEBUG_MODE) {
            float zoomX = Cfg.BLOCKS_X / (4f + Cfg.BLOCKS_X);
            float zoomY = Cfg.BLOCKS_Y / (4f + Cfg.BLOCKS_Y);
            camera.viewportWidth = camera.viewportWidth * zoomX;
            camera.viewportHeight = camera.viewportHeight * zoomY;
            camera.update();

            box2DDebugRenderer.render(world, camera.combined);

            camera.viewportWidth = camera.viewportWidth / zoomX;
            camera.viewportHeight = camera.viewportHeight / zoomY;
            camera.update();
        }

        batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.update(collectedBeers, score, mario.getTimeToLive());
        renderHud(batch);
    }

    private void renderBackground(SpriteBatch batch) {
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BACKGROUND_KEY));
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BACKGROUND_GRAPHICS_KEY));

        Array<BoxCoin> activeBoxCoins = controller.getActiveBoxCoins();
        for (BoxCoin boxCoin : activeBoxCoins) {
            boxCoin.draw(batch);
        }
    }

    private void renderForeground(SpriteBatch batch) {
        float gameTime = controller.getGameTime();

        Array<Coin> coins = controller.getCoins();
        for (Coin coin : coins) {
            coin.draw(batch);
        }

        ObjectMap<String, Item> items = controller.getItems();
        for (Item item : items.values()) {
            item.draw(batch);
        }

        ObjectMap<String, Enemy> enemies = controller.getEnemies();
        for (Enemy enemy : enemies.values()) {
            if (enemy instanceof Flower) {
                enemy.draw(batch);
            }
        }

        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.GRAPHICS_KEY));

        Array<Platform> platforms = controller.getPlatforms();
        for (Platform platform : platforms) {
            platform.draw(batch);
        }

        for (Enemy enemy : enemies.values()) {
            if (!(enemy instanceof Flower)) {
                enemy.draw(batch);
            }
        }

        Mario mario = controller.getMario();
        mario.draw(batch);
        mario.getFireball().draw(batch);

        ShaderProgram prevShader = batch.getShader();
        Array<WorldCreator.WaterParams> waterList = controller.getWaterList();
        for (WorldCreator.WaterParams waterParams : waterList) {
            Rectangle waterRegion = waterParams.rectangle;
            TextureRegion texture = waterParams.isBeer ? beerLiquidTexture : waterTexture;
            batch.setShader(waterShader);
            waterShader.setUniformf("u_time", gameTime);
            waterShader.setUniformf("u_width", waterRegion.getWidth() * Cfg.PPM);
            batch.draw(texture, waterRegion.getX(), (waterRegion.getY() - 1f / Cfg.PPM),
                    waterRegion.getWidth(), waterRegion.getHeight());
        }

        batch.setShader(prevShader);

        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            // tile-objects itself are drawn in the GRAPHICS layer, while this draw-call renders the
            // particle fragments in case of a destroyed brick
            tileObject.draw(batch);
        }
    }

    private final GlyphLayout layout = new GlyphLayout();
    private void renderHud(SpriteBatch batch) {
        hud.getStage().draw();

        LinkedBlockingQueue<TextMessage> textMessages = controller.getTextMessages();
        if (textMessages.isEmpty()) {
            return;
        }

        batch.begin();
        for (TextMessage textMessage : textMessages) {
            layout.setText(font, textMessage.getMessage());
            font.draw(batch, textMessage.getMessage(), textMessage.getX() * Cfg.PPM - layout.width / 2, textMessage.getY() * Cfg.PPM - layout.height / 2);
        }
        batch.end();
    }

    public void renderEffects(SpriteBatch batch) {
        Array<ParticleEffectPool.PooledEffect> activeSplashEffects = controller.getActiveSplashEffects();
        for (int i = activeSplashEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = activeSplashEffects.get(i);
            effect.draw(batch);
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        box2DDebugRenderer.dispose();
        waterShader.dispose();
        drunkShader.dispose();
        stonedShader.dispose();
        frameBuffer.dispose();
        font.dispose();
        hud.dispose();
    }
}
