package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.LinkedBlockingQueue;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.commons.FrameBufferManager;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.rendering.ParallaxRenderer;
import de.bsautermeister.jump.scenes.Hud;
import de.bsautermeister.jump.screens.menu.GameOverOverlay;
import de.bsautermeister.jump.screens.menu.PauseOverlay;
import de.bsautermeister.jump.sprites.BoxCoin;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.enemies.DrunkenGuy;
import de.bsautermeister.jump.sprites.enemies.Enemy;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.sprites.Player;
import de.bsautermeister.jump.sprites.Pole;
import de.bsautermeister.jump.text.TextMessage;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameRenderer implements Disposable {

    private final SpriteBatch batch;
    private final GameController controller;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Viewport uiViewport;
    private final Hud hud;

    private final FrameBuffer frameBuffer;

    private final ShaderProgram waterShader;
    private final TextureRegion waterTexture;
    private final ShaderProgram drunkShader;
    private final ShaderProgram stonedShader;

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final ParallaxRenderer parallaxRenderer;

    private final Skin skin;
    private final Stage overlayStage;
    private final TextureRegion backgroundOverlayRegion;

    private final FrameBufferManager frameBufferManager;

    public GameRenderer(SpriteBatch batch, AssetManager assetManager, TextureAtlas atlas,
                        GameController controller, FrameBufferManager frameBufferManager) {
        this.batch = batch;
        this.controller = controller;
        this.camera = controller.getCamera();
        this.viewport = controller.getViewport();
        this.frameBufferManager = frameBufferManager;

        float screenPixelPerTileX = Gdx.graphics.getWidth() / Cfg.BLOCKS_X;
        float screenPixelPerTileY = Gdx.graphics.getHeight() / Cfg.BLOCKS_Y;
        frameBuffer = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                (int)(screenPixelPerTileX * (Cfg.BLOCKS_X + 4)), // 2 extra block for each left and right
                (int)(screenPixelPerTileY * (Cfg.BLOCKS_Y + 4)), // 2 extra block for each top and bottom
                false);

        uiViewport = new StretchViewport(Cfg.UI_WIDTH, Cfg.UI_HEIGHT);
        hud = new Hud(batch, uiViewport, assetManager, controller.getTotalBeers());

        waterShader = GdxUtils.loadCompiledShader("shader/default.vs","shader/water.fs");
        drunkShader = GdxUtils.loadCompiledShader("shader/default.vs", "shader/wave_distortion.fs");
        stonedShader = GdxUtils.loadCompiledShader("shader/default.vs", "shader/grayscale.fs");

        waterTexture = atlas.findRegion(RegionNames.WATER);

        font = assetManager.get(AssetDescriptors.Fonts.S);

        mapRenderer = new OrthogonalTiledMapRenderer(controller.getMap(), 1 / Cfg.PPM, batch);
        this.parallaxRenderer = new ParallaxRenderer(camera, mapRenderer);

        box2DDebugRenderer = new Box2DDebugRenderer(true, true, false, true, true, true);

        backgroundOverlayRegion = atlas.findRegion(RegionNames.BACKGROUND_OVERLAY);

        skin = assetManager.get(AssetDescriptors.Skins.UI);
        overlayStage = new Stage(uiViewport, batch);
        overlayStage.setDebugAll(Cfg.DEBUG_MODE);

        Gdx.input.setInputProcessor(overlayStage);
    }

    public void render(float delta) {
        float gameTime = controller.getGameTime();
        Player player = controller.getPlayer();
        int score = controller.getScore();
        int ttl = controller.getTimeToLive();
        int collectedBeers = controller.getCollectedBeers();

        mapRenderer.setMap(controller.getMap());
        parallaxRenderer.setMap(controller.getMap());

        GdxUtils.clearScreen(Color.BLACK);
        viewport.apply();

        frameBufferManager.begin(frameBuffer);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (player.isHammered()) {
            batch.setShader(stonedShader);
            stonedShader.setUniformf("u_effectRatio", player.getHammeredRatio());
        }
        renderBackground(batch);
        renderForeground(batch);
        renderEffects(batch);
        batch.setShader(null);
        batch.end();
        frameBufferManager.end();

        batch.begin();

        if (player.isDrunk()) {
            batch.setShader(drunkShader);
            drunkShader.setUniformf("u_time", gameTime);
            drunkShader.setUniformf("u_imageSize", Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
            drunkShader.setUniformf("u_amplitude", 7.1f * player.getDrunkRatio(), 9.1f * player.getDrunkRatio());
            drunkShader.setUniformf("u_waveLength", 111f, 311f);
            drunkShader.setUniformf("u_velocity", 71f, 111f);
        }

        float screenPixelPerTileX = Gdx.graphics.getWidth() / Cfg.BLOCKS_X;
        float screenPixelPerTileY = Gdx.graphics.getHeight() / Cfg.BLOCKS_Y;
        batch.draw(frameBuffer.getColorBufferTexture(),
                camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight,
                (int)screenPixelPerTileX * 2, (int)screenPixelPerTileY * 2, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);

        if (player.isHammered()) {
            Color c = batch.getColor();
            batch.setColor(c.r, c.g, c.b, player.getHammeredRatio());
            float offsetX1 =  screenPixelPerTileX * 0.66f * (float)Math.sin(-gameTime) * player.getHammeredRatio();
            float offsetY1 =  screenPixelPerTileY * 0.66f * (float)Math.cos(gameTime * 0.8f) * player.getHammeredRatio();
            float offsetX2 =  screenPixelPerTileX * 0.66f * (float)Math.sin(gameTime * 0.9f) * player.getHammeredRatio();
            float offsetY2 =  screenPixelPerTileY * 0.66f * (float)Math.cos(gameTime * 0.7f) * player.getHammeredRatio();
            batch.draw(frameBuffer.getColorBufferTexture(),
                    camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight,
                    (int)(screenPixelPerTileX * 2 + offsetX1), (int)(screenPixelPerTileY * 2 - offsetY1), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
            batch.draw(frameBuffer.getColorBufferTexture(),
                    camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight,
                    (int)(screenPixelPerTileX * 2 - offsetX2), (int)(screenPixelPerTileY * 2 + offsetY2), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
            batch.setColor(Color.WHITE);
        }

        batch.setShader(null);

        batch.end();

        if (Cfg.DEBUG_MODE) {
            float zoomX = Cfg.BLOCKS_X / (4f + Cfg.BLOCKS_X);
            float zoomY = Cfg.BLOCKS_Y / (4f + Cfg.BLOCKS_Y);
            camera.viewportWidth = camera.viewportWidth * zoomX;
            camera.viewportHeight = camera.viewportHeight * zoomY;
            camera.update();

            box2DDebugRenderer.render(controller.getWorld(), camera.combined);

            camera.viewportWidth = camera.viewportWidth / zoomX;
            camera.viewportHeight = camera.viewportHeight / zoomY;
            camera.update();
        }

        uiViewport.apply();
        batch.setProjectionMatrix(hud.getCamera().combined);
        hud.update(collectedBeers, score, player.getRemainingPretzels(), ttl);
        renderHud(batch);
    }

    private void renderBackground(SpriteBatch batch) {
        float munichRatio = controller.getMunichRatio();
        float munichOffset = Interpolation.smooth.apply(1.5f * 6.25f, 0f, munichRatio);
        float forestOffset = Interpolation.smooth.apply(0f, 1.0f * 6.25f, munichRatio);
        float grassOffset = Interpolation.smooth.apply(0f, -0.25f * 6.25f, munichRatio);


        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_STATIC_KEY, 1.0f, 1.0f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_CLOUDS2_KEY, 0.1f, 0.075f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_MOUNTAINS_KEY, 0.2f, 0.15f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_CLOUDS1_KEY, 0.4f, 0.25f);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_MUNICH2_KEY, 0.5f, 0.3f, munichOffset);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_MUNICH1_KEY, 0.625f, 0.35f, munichOffset);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_FORREST2_KEY, 0.65f, 0.4f, forestOffset);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_FORREST1_KEY, 0.80f, 0.45f, forestOffset);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_GRASS2_KEY, 0.85f, 0.45f, grassOffset);
        parallaxRenderer.renderLayer(WorldCreator.BG_IMG_GRASS1_KEY, 0.90f, 0.6f, grassOffset);

        mapRenderer.setView(camera);

        for (Pole pole : controller.getPoles()) {
            pole.draw(batch);
        }

        controller.getTent().draw(batch);

        mapRenderer.renderTileLayer((TiledMapTileLayer) controller.getMap().getLayers().get(WorldCreator.BG_TILES_KEY));

        Array<BoxCoin> activeBoxCoins = controller.getActiveBoxCoins();
        for (BoxCoin boxCoin : activeBoxCoins) {
            boxCoin.draw(batch);
        }
    }

    private void renderForeground(SpriteBatch batch) {
        Array<Coin> coins = controller.getCoins();
        for (Coin coin : coins) {
            coin.draw(batch);
        }

        renderWater(batch, waterTexture, 1f);

        ObjectMap<String, Item> items = controller.getItems();
        for (Item item : items.values()) {
            item.draw(batch);
        }

        ObjectMap<String, Enemy> enemies = controller.getEnemies();
        for (Enemy enemy : enemies.values()) {
            if (enemy instanceof DrunkenGuy) {
                enemy.draw(batch);
            }
        }

        Array<Platform> platforms = controller.getPlatforms();
        for (Platform platform : platforms) {
            platform.draw(batch);
        }

        for (Enemy enemy : enemies.values()) {
            if (!enemy.renderInForeground()) {
                enemy.draw(batch);
            }
        }

        Player player = controller.getPlayer();
        if (!player.isDead()) {
            player.draw(batch);
        }
        player.getPretzelBullet().draw(batch);

        renderWater(batch, waterTexture, 0.5f);

        mapRenderer.setView(camera);
        mapRenderer.renderTileLayer((TiledMapTileLayer) controller.getMap().getLayers().get(WorldCreator.FG_TILES_KEY));

        for (Enemy enemy : enemies.values()) {
            if (enemy.renderInForeground()) {
                enemy.draw(batch);
            }
        }

        if (player.isDead()) {
            player.draw(batch);
        }

        for (InteractiveTileObject tileObject : controller.getTileObjects()) {
            // tile-objects itself are drawn in the GRAPHICS layer, while this draw-call renders the
            // particle fragments in case of a destroyed brick
            tileObject.draw(batch);
        }
    }

    private void renderWater(SpriteBatch batch, TextureRegion waterTexture, float opacity) {
        float gameTime = controller.getGameTime();

        ShaderProgram prevShader = batch.getShader();
        Array<Rectangle> waterList = controller.getWaterList();
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



    private void renderHud(SpriteBatch batch) {
        hud.draw(batch);
        renderTextMessage();
        updateOverlay();
        renderHudOverlay();
    }

    private void updateOverlay() {
        if (overlayStage.getActors().isEmpty()) {
            if (controller.getState().isGameOver()) {
                overlayStage.addActor(new GameOverOverlay(skin, controller.getGameOverCallback()));
                skipNextOverlayAct = true;
            } else if (controller.getState().isPaused()) {
                overlayStage.addActor(new PauseOverlay(skin, controller.getPauseCallback()));
                skipNextOverlayAct = true;
            }
        } else {
            if (!controller.getState().isPaused() && !controller.getState().isGameOver()) {
                overlayStage.clear();
            }
        }
    }

    private void renderTextMessage() {
        LinkedBlockingQueue<TextMessage> textMessages = controller.getTextMessages();
        if (!textMessages.isEmpty()) {
            batch.begin();
            for (TextMessage textMessage : textMessages) {
                layout.setText(font, textMessage.getMessage());

                float uiX = textMessage.getNormalizedX() * Cfg.UI_WIDTH;
                float uiY = textMessage.getNormalizedY() * Cfg.UI_HEIGHT;

                font.draw(batch, textMessage.getMessage(), uiX - layout.width / 2, uiY - layout.height / 2);
            }
            batch.end();
        }
    }

    // workaround: do not act during the first frame, otherwise button event which triggered
    // this overlay to show are processed in the overlay, which could immediately close it again
    private boolean skipNextOverlayAct = false;

    private void renderHudOverlay() {
        if (!overlayStage.getActors().isEmpty()) {
            if (skipNextOverlayAct) {
                skipNextOverlayAct = false;
                return;
            }
            overlayStage.act();
            batch.begin();
            batch.draw(backgroundOverlayRegion, 0f, 0f, Cfg.UI_WIDTH, Cfg.UI_HEIGHT);
            batch.end();
            overlayStage.draw();
        }
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

    public InputProcessor getInputProcessor() {
        return overlayStage;
    }
}
