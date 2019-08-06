package de.bsautermeister.jump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.commons.GameStats;
import de.bsautermeister.jump.managers.WaterInteractionManager;
import de.bsautermeister.jump.physics.WorldContactListener;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.scenes.Hud;
import de.bsautermeister.jump.serializer.BinarySerializable;
import de.bsautermeister.jump.serializer.BinarySerializer;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.Goomba;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.ItemDef;
import de.bsautermeister.jump.sprites.Koopa;
import de.bsautermeister.jump.sprites.Mario;
import de.bsautermeister.jump.sprites.Mushroom;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.sprites.SpinningCoin;
import de.bsautermeister.jump.text.TextMessage;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameScreen extends ScreenBase implements BinarySerializable {

    private static final Logger LOG = new Logger(GameScreen.class.getName(), Cfg.LOG_LEVEL);

    private GameStats gameStats;
    private TextureAtlas atlas;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Viewport hudViewport;
    private Hud hud;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private float mapPixelWidth;

    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;

    private Mario mario;

    private int score;

    private Vector2 goal;
    private ObjectMap<String, Enemy> enemies;
    private ObjectMap<String, Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;
    private Array<Platform> platforms;

    private boolean levelCompleted;
    private float levelCompletedTimer;

    private Array<SpinningCoin> spinningCoins;

    private Sound bumpSound;
    private Sound powerupSpawnSound;
    private Sound powerupSound;
    private Sound coinSound;
    private Sound breakBlockSound;
    private Sound stompSound;
    private Sound powerDownSound;
    private Sound marioDieSound;
    private Sound jumpSound;
    private Sound kickedSound;
    private Sound splashSound;

    private MusicPlayer musicPlayer;

    private GameCallbacks callbacks = new GameCallbacks() {
        @Override
        public void jump() {
            jumpSound.play(0.33f);
        }

        @Override
        public void stomp(Enemy enemy) {
            stompSound.play();

            if (!(enemy instanceof Koopa)) {
                score += 50;
                showScoreText("50", enemy.getBoundingRectangle());
            }
        }

        @Override
        public void use(Mario mario, Item item) {
            powerupSound.play();

            score += 100;
            showScoreText("100", item.getBoundingRectangle());
        }

        @Override
        public void hit(Mario mario, Enemy enemy) {
            if (mario.isBig()) {
                powerDownSound.play();
            }
        }

        @Override
        public void hit(Mario mario, Brick brick, boolean closeEnough) {
            if (!closeEnough) {
                bumpSound.play();
            } else if (mario.isBig()) {
                breakBlockSound.play();
            } else {
               bumpSound.play();
            }
        }

        @Override
        public void hit(Mario mario, Coin coin, Vector2 position, boolean closeEnough) {
            if (coin.isBlank() || !closeEnough) {
                bumpSound.play();
            } else if (coin.hasMushroom()) {
                spawnItem(new ItemDef(position, Mushroom.class));
                powerupSpawnSound.play();
            } else {
                coinSound.play();
                SpinningCoin spinningCoin = new SpinningCoin(atlas, coin.getBody().getWorldCenter());
                spinningCoins.add(spinningCoin);
                score += Cfg.COIN_SCORE;
                // score is shown later when the coin disappears
            }
        }

        @Override
        public void indirectEnemyHit(InteractiveTileObject tileObject, String enemyId) {
            Enemy enemy = enemies.get(enemyId);
            if (enemy != null) {
                enemy.kill(true);
                score += 50;
                showScoreText("50", enemy.getBoundingRectangle());
            }
        }

        @Override
        public void indirectItemHit(InteractiveTileObject tileObject, String itemId) {
            Item item = items.get(itemId);
            if (item != null) {
                item.reverseVelocity(true, false);
                item.bounceUp();
            }
        }

        @Override
        public void kicked(Enemy enemy) {
            kickedSound.play();
        }

        @Override
        public void touchedWater() {
            splashSound.play();
        }

        @Override
        public void killed(Enemy enemy) {
            kickedSound.play();
            score += 50;
            showScoreText("50", enemy.getBoundingRectangle());
        }

        @Override
        public void hitWall(Enemy enemy) {
            if (enemy instanceof Koopa) {
                Koopa koopa = (Koopa) enemy;
                if (koopa.getState() == Koopa.State.MOVING_SHELL) {
                    bumpSound.play();
                }
            }
        }

        @Override
        public void gameOver() {
            marioDieSound.play();
        }
    };

    private void showScoreText(String text, Rectangle rect) {
        float x = rect.getX() + rect.getWidth() / 2;
        float y = rect.getY() + rect.getHeight() / 2;
        float cameraLeftX = (camera.position.x - viewport.getWorldWidth() / 2);
        textMessages.add(new TextMessage(text, x - cameraLeftX, y));
    }

    private float gameTime;
    private Array<Rectangle> waterRegions;
    private final ShaderProgram waterShader;
    private TextureRegion waterTexture;

    private WaterInteractionManager waterInteractionManager;

    private FileHandle gameToLoad;
    private Integer level;

    private BitmapFont font;
    private LinkedBlockingQueue<TextMessage> textMessages = new LinkedBlockingQueue<TextMessage>();

    public GameScreen(GameApp game, int level) {
        super(game);
        this.level = level;
        this.gameStats = new GameStats();
        this.atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);
        this.musicPlayer = game.getMusicPlayer();

        this.world = new World(new Vector2(0,-9.81f), true);
        this.world.setContactListener(new WorldContactListener());
        this.box2DDebugRenderer = new Box2DDebugRenderer();

        enemies = new ObjectMap<String, Enemy>();
        platforms = new Array<Platform>();

        items = new ObjectMap();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

        spinningCoins = new Array<SpinningCoin>();

        mario = new Mario(callbacks, world, atlas);

        waterShader = GdxUtils.loadCompiledShader("shader/default.vs","shader/water.fs");
    }

    public GameScreen(GameApp game, FileHandle fileHandle) {
        //super(game);
        this(game, -1); // TODO whatever
        this.gameToLoad = fileHandle;
    }

    private void reset() {
        this.camera = new OrthographicCamera();
        this.viewport = new StretchViewport(Cfg.WORLD_WIDTH / Cfg.PPM, Cfg.WORLD_HEIGHT / Cfg.PPM, camera);
        this.camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        this.score = 0;

        if (level == -1) {
            level = gameStats.getLastStartedLevel();
        } else {
            gameStats.setLastStartedLevel(level);
        }

        LOG.debug("Init map level: " + level);

        initMap(level);

        WorldCreator worldCreator = new WorldCreator(callbacks, world, map, atlas);
        if (gameToLoad != null) {
            worldCreator.buildFromMap();
            for (Platform platform : worldCreator.createPlatforms()) {
                this.platforms.add(platform);
            }
            load(gameToLoad);
        } else {
            worldCreator.buildFromMap();
            for (Platform platform : worldCreator.createPlatforms()) {
                this.platforms.add(platform);
            }
            for (Enemy enemy : worldCreator.createEnemies()) {
                this.enemies.put(enemy.getId(), enemy);
            }
        }
        this.waterRegions = worldCreator.getWaterRegions();
        this.waterInteractionManager = new WaterInteractionManager(atlas, callbacks, waterRegions, mario);

        this.goal = worldCreator.getGoal();

        this.hudViewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        this.hud = new Hud(getGame().getBatch(), hudViewport, getAssetManager());

        waterTexture = atlas.findRegion(RegionNames.WATER);

        levelCompleted = false;
        levelCompletedTimer = 0;

        musicPlayer.selectMusic(AssetPaths.Music.NORMAL_AUDIO);
        musicPlayer.setVolume(1.0f, true);

        musicPlayer.play();
    }

    private void initMap(int level) {
        this.map = new TmxMapLoader().load(String.format("maps/level%02d.tmx", level));
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / Cfg.PPM, getGame().getBatch());
        float mapWidth = map.getProperties().get("width", Integer.class);
        float tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        this.mapPixelWidth = mapWidth * tilePixelWidth / Cfg.PPM;
    }

    @Override
    public void show() {
        super.show();

        bumpSound = getAssetManager().get(AssetDescriptors.Sounds.BUMP);
        powerupSpawnSound = getAssetManager().get(AssetDescriptors.Sounds.POWERUP_SPAWN);
        powerupSound = getAssetManager().get(AssetDescriptors.Sounds.POWERUP);
        coinSound = getAssetManager().get(AssetDescriptors.Sounds.COIN);
        breakBlockSound = getAssetManager().get(AssetDescriptors.Sounds.BREAK_BLOCK);
        stompSound = getAssetManager().get(AssetDescriptors.Sounds.STOMP);
        powerDownSound = getAssetManager().get(AssetDescriptors.Sounds.POWERDOWN);
        marioDieSound = getAssetManager().get(AssetDescriptors.Sounds.MARIO_DIE);
        jumpSound = getAssetManager().get(AssetDescriptors.Sounds.JUMP);
        kickedSound = getAssetManager().get(AssetDescriptors.Sounds.KICKED);
        splashSound = getAssetManager().get(AssetDescriptors.Sounds.SPLASH);

        font = getAssetManager().get(AssetDescriptors.Fonts.MARIO12);

        if (JumpGame.hasSavedData()) {
            //load(gameToLoad); // TODO duplicated?
            // ensure to not load this saved game later anymore
            //JumpGame.deleteSavedData();
        }

        reset();
    }

    @Override
    public void pause() {
        super.pause();
        save();
    }

    private void spawnItem(ItemDef itemDef) {
        itemsToSpawn.add(itemDef);
    }

    private void handleSpawningItems() {
        if (itemsToSpawn.isEmpty()) {
            return;
        }

        ItemDef itemDef = itemsToSpawn.poll();
        if (itemDef.getType() == Mushroom.class) {
            Mushroom mushroom = new Mushroom(callbacks, world, atlas, itemDef.getPosition().x, itemDef.getPosition().y);
            items.put(mushroom.getId(), mushroom);
        }
    }

    public void update(float delta) {
        world.step(1 / 60f, 8, 3);

        if (!levelCompleted) {
            handleInput();
        }

        handleSpawningItems();

        mario.update(delta);
        checkPlayerInBounds();

        updateEnemies(delta);
        updateItems(delta);
        updatePlatforms(delta);

        waterInteractionManager.update(delta);

        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            tileObject.update(delta);
        }

        for(SpinningCoin spinningCoin : spinningCoins) {
            if (spinningCoin.isFinished()) {
                showScoreText(Cfg.COIN_SCORE_STRING, spinningCoin.getBoundingRectangle());
                spinningCoins.removeValue(spinningCoin, true);
            } else {
                spinningCoin.update(delta);
            }
        }

        hud.update(level, score, mario.getTimeToLive());

        upateCameraPosition();
        camera.update();

        mapRenderer.setView(camera);

        if (mario.getState() == Mario.State.DEAD) {
            musicPlayer.stop();
        }

        if (mario.getTimeToLive() <= 60 && !musicPlayer.isSelected(AssetPaths.Music.HURRY_AUDIO)) {
            musicPlayer.selectMusic(AssetPaths.Music.HURRY_AUDIO);
            musicPlayer.play();
        }

        if (levelCompleted) {
            levelCompletedTimer += delta;

            if (levelCompletedTimer >= Cfg.GOAL_REACHED_FINISH_DELAY) {
                setScreen(new GameScreen(getGame(), level + 1));
            }
        }

        for (TextMessage textMessage : textMessages) {
            textMessage.update(delta);
        }
    }

    private void postUpdate() {
        for (Enemy enemy : enemies.values()) {
            enemy.postUpdate();

            if (enemy.isRemovable()) {
                enemy.dispose();
                enemies.remove(enemy.getId());
            }
        }

        for (Item item : items.values()) {
            item.postUpdate();

            if (item.isRemovable()) {
                item.dispose();
                items.remove(item.getId());
            }
        }

        if (mario.getBody().getPosition().dst2(goal) < 0.0075f) {
            completeLevel();
        }

        if (!textMessages.isEmpty() && !textMessages.peek().isAlive()) {
            textMessages.poll();
        }
    }

    private void updateItems(float delta) {
        for (Item item : items.values()) {
            item.update(delta);
        }
    }

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies.values()) {
            enemy.update(delta);

            if (enemy.getX() < mario.getX() + 256 / Cfg.PPM) {
                enemy.setActive(true);
            }
        }
    }

    private void updatePlatforms(float delta) {
        for (Platform platform : platforms) {
            platform.update(delta);

            if (platform.getX() < mario.getX() + 256 / Cfg.PPM) {
                platform.setActive(true);
            }
        }
    }

    private void completeLevel() {
        if (!levelCompleted) {
            levelCompleted = true;
            mario.setLevelCompleted(true);
            gameStats.setHighestFinishedLevel(level);
            musicPlayer.setVolume(0f, false);
        }
    }

    private void checkPlayerInBounds() {
        float x = mario.getBody().getPosition().x;
        if (x - mario.getBody().getFixtureList().get(0).getShape().getRadius() < 0) {
            mario.getBody().setTransform(mario.getBody().getFixtureList().get(0).getShape().getRadius(),
                    mario.getBody().getPosition().y, 0);

        } else if (x + mario.getBody().getFixtureList().get(0).getShape().getRadius() > mapPixelWidth) {
            mario.getBody().setTransform(mapPixelWidth - mario.getBody().getFixtureList().get(0).getShape().getRadius(),
                    mario.getBody().getPosition().y, 0);
        }
    }

    private void upateCameraPosition() {
        if (mario.getState() == Mario.State.DEAD) {
            return;
        }

        // A) snap camera position to the PPM pixel grid, otherwise there are rendering artifacts
        //camera.position.x = (float) Math.round(mario.getBody().getPosition().x * Cfg.PPM) / Cfg.PPM;

        // B) no snapping runs smoother
        camera.position.x = mario.getBody().getPosition().x;

        // check camera in bounds
        if (camera.position.x - viewport.getWorldWidth() / 2 < 0) {
            camera.position.x = viewport.getWorldWidth() / 2;
        } else if (camera.position.x + viewport.getWorldWidth() / 2 > mapPixelWidth) {
            camera.position.x = mapPixelWidth - viewport.getWorldWidth() / 2;
            camera.position.x = mapPixelWidth - viewport.getWorldWidth() / 2;
        }
    }

    private void handleInput() {
        if (mario.getState() == Mario.State.DEAD) {
            return;
        }

        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);

        int pointer = 0;
        while (Gdx.input.isTouched(pointer)) {
            float x = Gdx.input.getX(pointer);
            float y = Gdx.input.getY(pointer);

            x = x / Gdx.graphics.getWidth();
            y = y / Gdx.graphics.getHeight();

            if (x > 0.6) {
                if (y < 0.9) {
                    upPressed = true;
                } else {
                    downPressed = true; // TODO use swipe down and keep pressed for crouch, otherwise jump
                }
            } else if (x < 0.2) {
                leftPressed = true;
            } else if (x < 0.4) {
                rightPressed = true;
            }

            pointer++;
        }

        mario.control(upPressed, downPressed, leftPressed, rightPressed);
    }

    @Override
    public void render(float delta) {
        gameTime += delta;

        update(delta);
        postUpdate();

        GdxUtils.clearScreen(Color.BLACK);

        viewport.apply();
        SpriteBatch batch = getBatch();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        renderBackground(batch);
        renderForeground(batch);
        batch.end();

        if (Cfg.DEBUG_MODE) {
            box2DDebugRenderer.render(world, camera.combined);
        }

        batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        renderHud(batch);

        if (isGameOver()) {
            getGame().setScreen(new GameOverScreen(getGame()));
        }
    }

    private void renderBackground(SpriteBatch batch) {
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BACKGROUND_KEY));
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BACKGROUND_GRAPHICS_KEY));

        for (SpinningCoin spinningCoin : spinningCoins) {
            spinningCoin.draw(batch);
        }
    }

    private void renderForeground(SpriteBatch batch) {
        for (Item item : items.values()) {
            item.draw(batch);
        }

        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.GRAPHICS_KEY));

        for (Platform platform : platforms) {
            platform.draw(batch);
        }

        for (Enemy enemy : enemies.values()) {
            enemy.draw(batch);
        }
        mario.draw(batch);

        waterInteractionManager.draw(batch);

        ShaderProgram prevShader = batch.getShader();
        for (Rectangle waterRegion : waterRegions) {
            batch.setShader(waterShader);
            waterShader.setUniformf("u_time", gameTime);
            waterShader.setUniformf("u_width", waterRegion.getWidth() * Cfg.PPM);
            batch.draw(waterTexture, waterRegion.getX(), (waterRegion.getY() - 1f / Cfg.PPM),
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
        hud.dispose();
    }

    private boolean isGameOver() {
        return mario.getState() == Mario.State.DEAD && mario.getStateTimer() > 3f;
    }

    private void load(FileHandle handle) {
        if (handle.exists()) {
            if (!BinarySerializer.read(this, handle.read())) {
                LOG.error("Could not load game state");
            }

            JumpGame.deleteSavedData();
        }
    }

    private void save() {
        // TODO don't do anything in case player is dead or level is finished

        // TODO pause the game?

        final FileHandle fileHandle = JumpGame.getSavedDataHandle();
        if (!BinarySerializer.write(this, fileHandle.write(false))) {
            LOG.error("Could not save game state");
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(level);
        out.writeInt(score);
        out.writeBoolean(levelCompleted);
        out.writeFloat(levelCompletedTimer);
        out.writeFloat(gameTime);
        mario.write(out);
        out.writeInt(enemies.size);
        for (Enemy enemy : enemies.values()) {
            out.writeUTF(enemy.getClass().getName());
            enemy.write(out);
        }
        out.writeInt(items.size);
        for (Item item : items.values()) {
            out.writeUTF(item.getClass().getName());
            item.write(out);
        }
        out.writeInt(spinningCoins.size);
        for (SpinningCoin spinningCoin : spinningCoins) {
            spinningCoin.write(out);
        }
        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            tileObject.write(out);
        }
        for (Platform platform : platforms) {
            platform.write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        level = in.readInt();
        score = in.readInt();
        levelCompleted = in.readBoolean();
        levelCompletedTimer = in.readFloat();
        gameTime = in.readFloat();
        mario.read(in);
        int numEnemies = in.readInt();
        for (int i = 0; i < numEnemies; ++i) {
            String enemyType = in.readUTF();
            Enemy enemy;
            if (enemyType.equals(Goomba.class.getName())) {
                enemy = new Goomba(callbacks, world, atlas, 0, 0);
            } else if (enemyType.equals(Koopa.class.getName())) {
                enemy = new Koopa(callbacks, world, atlas, 0, 0);
            } else {
                throw new IllegalArgumentException("Unknown enemy type: " + enemyType);
            }
            enemy.read(in);
            enemies.put(enemy.getId(), enemy);
        }
        int numItems = in.readInt();
        for (int i = 0; i < numItems; ++i) {
            String itemType = in.readUTF();
            Item item;
            if (itemType.equals(Mushroom.class.getName())) {
                item = new Mushroom(callbacks, world, atlas, 0, 0);
            } else {
                throw new IllegalArgumentException("Unknown item type: " + itemType);
            }
            item.read(in);
            items.put(item.getId(), item);
        }
        int numSpinningCoins = in.readInt();
        for (int i = 0; i < numSpinningCoins; ++i) {
            SpinningCoin spinningCoin = new SpinningCoin(atlas, Vector2.Zero);
            spinningCoin.read(in);
            spinningCoins.add(spinningCoin);
        }
        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            tileObject.read(in);
        }
        for (Platform platform : platforms) {
            platform.read(in);
        }
    }
}
