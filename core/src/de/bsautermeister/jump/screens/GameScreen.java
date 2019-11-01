package de.bsautermeister.jump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
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
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.managers.WaterInteractionManager;
import de.bsautermeister.jump.physics.WorldContactListener;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.scenes.Hud;
import de.bsautermeister.jump.serializer.BinarySerializable;
import de.bsautermeister.jump.serializer.BinarySerializer;
import de.bsautermeister.jump.sprites.Beer;
import de.bsautermeister.jump.sprites.BoxCoin;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.FireFlower;
import de.bsautermeister.jump.sprites.Fireball;
import de.bsautermeister.jump.sprites.Fish;
import de.bsautermeister.jump.sprites.Flower;
import de.bsautermeister.jump.sprites.Goomba;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.ItemBox;
import de.bsautermeister.jump.sprites.ItemDef;
import de.bsautermeister.jump.sprites.Koopa;
import de.bsautermeister.jump.sprites.Mario;
import de.bsautermeister.jump.sprites.Mushroom;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.sprites.Spiky;
import de.bsautermeister.jump.text.TextMessage;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameScreen extends ScreenBase implements BinarySerializable {

    private static final Logger LOG = new Logger(GameScreen.class.getSimpleName(), Cfg.LOG_LEVEL);

    private GameStats gameStats;
    private TextureAtlas atlas;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Viewport hudViewport;
    private Hud hud;

    private FrameBuffer frameBuffer;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private float mapPixelWidth;

    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;

    private Mario mario;

    private int score;

    private WorldCreator.StartParams start;
    private Vector2 goal;
    private ObjectMap<String, Enemy> enemies;
    private ObjectMap<String, Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;
    private Array<Platform> platforms;
    private Array<Coin> coins;

    private boolean levelCompleted;
    private float levelCompletedTimer;

    private Array<BoxCoin> activeBoxCoins;

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
    private Sound fireSound;
    private Sound drinkingSound;
    private Sound ohYeahSound;

    private MusicPlayer musicPlayer;

    private float gameTime;
    private Array<Rectangle> waterRegions;
    private final ShaderProgram waterShader;
    private TextureRegion waterTexture;
    private final ShaderProgram drunkShader;
    private final ShaderProgram stonedShader;

    private WaterInteractionManager waterInteractionManager;

    private FileHandle gameToLoad;
    private Integer level;

    private BitmapFont font;
    private LinkedBlockingQueue<TextMessage> textMessages = new LinkedBlockingQueue<TextMessage>();

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
            if (item instanceof Beer) {
                drinkingSound.play();
            } else if (item instanceof Mushroom) {
                ohYeahSound.play();
            } else {
                powerupSound.play();
            }

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
        public void hit(Mario mario, ItemBox itemBox, Vector2 position, boolean closeEnough) {
            if (itemBox.isBlank() || !closeEnough) {
                bumpSound.play();
            } else if (itemBox.isMushroomBox()) {
                if (mario.isBig()) {
                    spawnItem(new ItemDef(position, FireFlower.class));
                } else {
                    spawnItem(new ItemDef(position, Mushroom.class));
                }
                powerupSpawnSound.play();
            } else if (itemBox.isBeerBox()) {
                spawnItem(new ItemDef(position, Beer.class));
                powerupSpawnSound.play();
            } else {
                coinSound.play();
                BoxCoin boxCoin = new BoxCoin(atlas, itemBox.getBody().getWorldCenter());
                activeBoxCoins.add(boxCoin);
                score += Cfg.COIN_SCORE;
                // score is shown later when the itemBox disappears
            }
        }

        @Override
        public void indirectObjectHit(InteractiveTileObject tileObject, String objectId) {
            Enemy enemy = enemies.get(objectId);
            if (enemy != null) {
                enemy.kill(true);
                score += 50;
                showScoreText("50", enemy.getBoundingRectangle());
                return;
            }

            Item item = items.get(objectId);
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
        public void touchedWater(Drownable drownable) {
            float volume = getVolumeBasedOnDistanceToCameraCenter(drownable.getWorldCenter().x);
            if (volume > 0) {
                splashSound.play(volume);
            }
        }

        @Override
        public void collectCoin() {
            coinSound.play();
            score += 100;
        }

        @Override
        public void killed(Enemy enemy) {
            float volume = getVolumeBasedOnDistanceToCameraCenter(enemy.getBody().getWorldCenter().x);
            if (volume > 0) {
                kickedSound.play(volume);
            }
            score += 50;
            showScoreText("50", enemy.getBoundingRectangle());
        }

        @Override
        public void hitWall(Enemy enemy) {
            if (enemy instanceof Koopa) {
                Koopa koopa = (Koopa) enemy;
                if (koopa.getState() == Koopa.State.MOVING_SHELL) {
                    float volume = getVolumeBasedOnDistanceToCameraCenter(enemy.getBody().getWorldCenter().x);
                    if (volume > 0) {
                        bumpSound.play(volume);
                    }
                }
            }
        }

        @Override
        public void hit(Fireball fireball, Enemy enemy) {

        }

        @Override
        public void fire() {
            fireSound.play();
        }

        @Override
        public void gameOver() {
            marioDieSound.play();
        }

        private static final float HALF_SCREEN_WIDTH = Cfg.WORLD_WIDTH / 2 / Cfg.PPM;
        private float getVolumeBasedOnDistanceToCameraCenter(float otherWorldCenterX) {
            float cameraWorldCenterX = camera.position.x;
            float distanceX = Math.abs(cameraWorldCenterX - otherWorldCenterX);
            if (distanceX <= HALF_SCREEN_WIDTH) {
                return 1f;
            }
            if (distanceX < 2 * HALF_SCREEN_WIDTH) {
                return MathUtils.clamp(-1f / HALF_SCREEN_WIDTH * distanceX + 2.1f, 0f, 1f);
            }
            if (distanceX < 3 * HALF_SCREEN_WIDTH) {
                return 0.1f;
            }
            return 0;
        }
    };

    private void showScoreText(String text, Rectangle rect) {
        float x = rect.getX() + rect.getWidth() / 2;
        float y = rect.getY() + rect.getHeight() / 2;
        float cameraLeftX = (camera.position.x - viewport.getWorldWidth() / 2);
        textMessages.add(new TextMessage(text, x - cameraLeftX, y));
    }

    public GameScreen(GameApp game, int level) {
        super(game);
        this.level = level;
        this.gameStats = new GameStats();
        this.atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);
        this.musicPlayer = game.getMusicPlayer();

        this.world = new World(new Vector2(0,-9.81f), true);
        this.world.setContactListener(new WorldContactListener());
        this.box2DDebugRenderer = new Box2DDebugRenderer(true, true, false, true, true, true);

        enemies = new ObjectMap<String, Enemy>();
        platforms = new Array<Platform>();
        coins = new Array<Coin>();

        items = new ObjectMap();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

        activeBoxCoins = new Array<BoxCoin>();

        waterShader = GdxUtils.loadCompiledShader("shader/default.vs","shader/water.fs");
        drunkShader = GdxUtils.loadCompiledShader("shader/default.vs", "shader/wave_distortion.fs");
        stonedShader = GdxUtils.loadCompiledShader("shader/default.vs", "shader/invert_colors.fs");
    }

    public GameScreen(GameApp game, FileHandle fileHandle) {
        //super(game);
        this(game, -1); // TODO whatever
        this.gameToLoad = fileHandle;
    }

    private void reset() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport((Cfg.WORLD_WIDTH + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, (Cfg.WORLD_HEIGHT + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        float screenPixelPerTile = Gdx.graphics.getWidth() / Cfg.BLOCKS_X;

        frameBuffer = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                (int)(screenPixelPerTile * (Cfg.BLOCKS_X + 4)), // 2 extra block for each left and right
                (int)(screenPixelPerTile * (Cfg.BLOCKS_Y + 4)), // 2 extra block for each top and bottom
                false);

        score = 0;

        if (level == -1) {
            level = gameStats.getLastStartedLevel();
        } else {
            gameStats.setLastStartedLevel(level);
        }

        LOG.debug("Init map level: " + level);

        initMap(level);

        WorldCreator worldCreator = new WorldCreator(callbacks, world, map, atlas);
        worldCreator.buildFromMap();
        platforms.addAll(worldCreator.createPlatforms());
        if (gameToLoad != null) {
            load(gameToLoad);
        } else {
            for (Enemy enemy : worldCreator.createEnemies()) {
                enemies.put(enemy.getId(), enemy);
            }
            coins.addAll(worldCreator.createCoins());
        }
        waterRegions = worldCreator.getWaterRegions();


        start = worldCreator.getStart();
        goal = worldCreator.getGoal();

        mario = new Mario(callbacks, world, atlas, start);

        waterInteractionManager = new WaterInteractionManager(atlas, callbacks, waterRegions);
        waterInteractionManager.add(mario);
        for (Enemy enemy : enemies.values()) {
            if (enemy instanceof Drownable) {
                Drownable drownableEnemy = (Drownable) enemy;
                waterInteractionManager.add(drownableEnemy);
            }
        }

        hudViewport = new StretchViewport((Cfg.WORLD_WIDTH + 4 * Cfg.BLOCK_SIZE), (Cfg.WORLD_HEIGHT + 4 * Cfg.BLOCK_SIZE));
        hud = new Hud(getGame().getBatch(), hudViewport, getAssetManager());

        waterTexture = atlas.findRegion(RegionNames.WATER);

        levelCompleted = false;
        levelCompletedTimer = 0;

        musicPlayer.selectMusic(AssetPaths.Music.NORMAL_AUDIO);
        musicPlayer.setVolume(MusicPlayer.MAX_VOLUME, true);

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
        fireSound = getAssetManager().get(AssetDescriptors.Sounds.FIRE);
        drinkingSound = getAssetManager().get(AssetDescriptors.Sounds.DRINKING);
        ohYeahSound = getAssetManager().get(AssetDescriptors.Sounds.OH_YEAH);

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
        LOG.debug("Spawning: " + itemDef.getType().getSimpleName());
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
            waterInteractionManager.add(mushroom);
        } else if (itemDef.getType() == FireFlower.class) {
            FireFlower fireFlower = new FireFlower(callbacks, world, atlas, itemDef.getPosition().x, itemDef.getPosition().y);
            items.put(fireFlower.getId(), fireFlower);
        } else if (itemDef.getType() == Beer.class) {
            Beer beer = new Beer(callbacks, world, atlas, itemDef.getPosition().x, itemDef.getPosition().y);
            items.put(beer.getId(), beer);
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

        mario.getFireball().update(delta);

        updateEnemies(delta);
        updateItems(delta);
        updatePlatforms(delta);

        waterInteractionManager.update(delta);

        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            tileObject.update(delta);
        }

        for(BoxCoin boxCoin : activeBoxCoins) {
            if (boxCoin.isFinished()) {
                showScoreText(Cfg.COIN_SCORE_STRING, boxCoin.getBoundingRectangle());
                activeBoxCoins.removeValue(boxCoin, true);
            } else {
                boxCoin.update(delta);
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

        // touch for platform contacts
        if (mario.hasPlatformContact()) {
            if (Math.abs(mario.getVelocityRelativeToGround().y) < 0.01) {
                mario.getPlatformContact().touch(delta);
            }
        }
    }

    private void postUpdate() {
        for (Enemy enemy : enemies.values()) {
            enemy.postUpdate();

            if (enemy.isRemovable()) {
                LOG.debug("Remove: " + enemy);
                enemy.dispose();
                enemies.remove(enemy.getId());
            }
        }

        for (Item item : items.values()) {
            item.postUpdate();

            if (item.isRemovable()) {
                item.dispose();
                items.remove(item.getId());

                if (item instanceof Drownable) {
                    waterInteractionManager.remove((Drownable) item);
                }
            }
        }

        for (Coin coin : coins) {
            coin.postUpdate();

            if (coin.isRemovable()) {
                coin.dispose();
                coins.removeValue(coin, true);
            }
        }

        if (mario.getBody().getPosition().dst2(goal) < 0.0075f) {
            completeLevel();
        }

        if (!textMessages.isEmpty() && !textMessages.peek().isAlive()) {
            textMessages.poll();
        }

        Fireball fireball = mario.getFireball();
        if (fireball.isActive() && !isVisible(fireball)) {
            fireball.reset();
        } else {
            fireball.postUpdate();
        }

        // cleanup jump-through ID of platform for player
        if (mario.getLastJumpThroughPlatformId() != null) {
            for (Platform platform : platforms) {
                if (platform.getId().equals(mario.getLastJumpThroughPlatformId()) &&
                        !mario.getBoundingRectangle().overlaps(platform.getBoundingRectangle())) {
                    mario.setLastJumpThroughPlatformId(null);
                }
            }
        }
    }

    private boolean isVisible(Sprite sprite) {
        Frustum camFrustum = camera.frustum;
        return camFrustum.pointInFrustum(sprite.getX(), sprite.getY(), 0)
                || camFrustum.pointInFrustum(sprite.getX() + sprite.getWidth(), sprite.getY(), 0)
                || camFrustum.pointInFrustum(sprite.getX() + sprite.getWidth(), sprite.getY() + sprite.getHeight(), 0)
                || camFrustum.pointInFrustum(sprite.getX(), sprite.getY() + sprite.getHeight(), 0);
    }

    private void updateItems(float delta) {
        for (Item item : items.values()) {
            item.update(delta);
        }
        for (Coin coin : coins) {
            coin.update(delta);
        }
    }

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies.values()) {
            enemy.update(delta);

            if (enemy.getX() < mario.getX() + Cfg.WORLD_WIDTH * 0.75f / Cfg.PPM) {
                if (!enemy.isActive()) {
                    enemy.setActive(true);

                    if (enemy.hasGroup()) {
                        wakeUp(enemy.getGroup());
                    }
                }
            }
        }
    }

    private void wakeUp(String enemyGroup) {
        if (enemyGroup == null) {
            return;
        }

        for (String enemyId : enemies.keys()) {
            Enemy enemy = enemies.get(enemyId);
            if (enemyGroup.equals(enemy.getGroup()) && !enemy.isActive()) {
                enemy.setActive(true);
            }
        }
    }

    private void updatePlatforms(float delta) {
        for (Platform platform : platforms) {
            platform.update(delta);

            if (platform.getX() < mario.getX() + Cfg.WORLD_WIDTH * 0.75f / Cfg.PPM) {
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
        float blockSizePpm = Cfg.BLOCK_SIZE / Cfg.PPM;
        float padding = 2 * blockSizePpm;
        float x = mario.getBody().getPosition().x;
        if (x - blockSizePpm / 2  < padding) {
            mario.getBody().setTransform(padding + blockSizePpm / 2,
                    mario.getBody().getPosition().y, 0);

        } else if (x + blockSizePpm / 2 > mapPixelWidth - padding) {
            mario.getBody().setTransform(mapPixelWidth - padding - blockSizePpm / 2,
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

    private Vector2 tmpPosition = new Vector2(0, 0);
    private Vector2 startSteerPosition = new Vector2(0, 0);

    private void handleInput() {
        if (mario.getState() == Mario.State.DEAD) {
            return;
        }

        boolean slow = false;
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean firePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        int pointer = 0;
        boolean leftSideTouched = false;
        while (Gdx.input.isTouched(pointer)) {
            float x = Gdx.input.getX(pointer);
            float y = Gdx.input.getY(pointer);

            x = x / Gdx.graphics.getWidth();
            y = y / Gdx.graphics.getHeight();

            if (x <= 0.5) {
                // left region: steering player
                leftSideTouched = true;
                if (startSteerPosition.isZero()) {
                    startSteerPosition.set(x, y);
                } else {
                    tmpPosition.set(x, y);
                    Vector2 swipeDirection = tmpPosition.sub(startSteerPosition);
                    float len = swipeDirection.len();
                    if (len > 0.005f) {
                        if (len < 0.03f) {
                            slow = true;
                        }
                        float angle = swipeDirection.angle();
                        if (angle <= 45 || angle >= 315) {
                            rightPressed = true;
                        } else if (angle >= 135 && angle <= 225) {
                            leftPressed = true;
                        } else if (len > 0.1f &&angle > 45 && angle < 135) {
                            downPressed = true;
                        }
                    }
                }
            } else {
                // right region: actions
                if (Gdx.input.justTouched()) {
                    if (y >= 0.5) {
                        upPressed = true;
                    } else {
                        firePressed = true;
                    }
                }
            }
            pointer++;
        }

        if (!leftSideTouched) {
            startSteerPosition.set(0, 0);
        }

        mario.control(upPressed, downPressed, leftPressed, rightPressed, firePressed, slow);
    }

    @Override
    public void render(float delta) {
        gameTime += delta;

        update(delta);
        postUpdate();

        GdxUtils.clearScreen(Color.BLACK);
        viewport.apply();
        SpriteBatch batch = getBatch();

        frameBuffer.begin();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (mario.isStoned()) {
            batch.setShader(stonedShader);
            stonedShader.setUniformf("u_effectRatio", mario.getStonedRatio());
        }
        renderBackground(batch);
        renderForeground(batch);
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
                camera.position.x - camera.viewportWidth / 2, 0, camera.viewportWidth, camera.viewportHeight,
                (int)screenPixelPerTile * 2, (int)screenPixelPerTile * 2, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);

        if (mario.isStoned()) {
            Color c = batch.getColor();
            batch.setColor(c.r, c.g, c.b, 0.5f);
            float offset1 =  screenPixelPerTile * 0.66f * (float)Math.sin(-gameTime) * mario.getStonedRatio();
            float offset2 =  screenPixelPerTile * 0.66f * (float)Math.cos(gameTime * 0.8f) * mario.getStonedRatio();
            float offset3 =  screenPixelPerTile * 0.66f * (float)Math.sin(gameTime * 0.9f) * mario.getStonedRatio();
            float offset4 =  screenPixelPerTile * 0.66f * (float)Math.cos(gameTime * 0.7f) * mario.getStonedRatio();
            batch.draw(frameBuffer.getColorBufferTexture(),
                    camera.position.x - camera.viewportWidth / 2, 0, camera.viewportWidth, camera.viewportHeight,
                    (int)(screenPixelPerTile * 2 + offset1), (int)(screenPixelPerTile * 2 - offset2), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
            batch.draw(frameBuffer.getColorBufferTexture(),
                    camera.position.x - camera.viewportWidth / 2, 0, camera.viewportWidth, camera.viewportHeight,
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
        renderHud(batch);

        if (isGameOver()) {
            getGame().setScreen(new GameOverScreen(getGame()));
        }
    }

    private void renderBackground(SpriteBatch batch) {
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BACKGROUND_KEY));
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BACKGROUND_GRAPHICS_KEY));

        for (BoxCoin boxCoin : activeBoxCoins) {
            boxCoin.draw(batch);
        }
    }

    private void renderForeground(SpriteBatch batch) {
        for (Coin coin : coins) {
            coin.draw(batch);
        }

        for (Item item : items.values()) {
            item.draw(batch);
        }

        for (Enemy enemy : enemies.values()) {
            if (enemy instanceof Flower) {
                enemy.draw(batch);
            }
        }

        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.GRAPHICS_KEY));

        for (Platform platform : platforms) {
            platform.draw(batch);
        }

        for (Enemy enemy : enemies.values()) {
            if (!(enemy instanceof Flower)) {
                enemy.draw(batch);
            }
        }
        mario.draw(batch);
        mario.getFireball().draw(batch);

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
        waterShader.dispose();
        drunkShader.dispose();
        stonedShader.dispose();
        frameBuffer.dispose();
        font.dispose();

        // disposing sound effects has weird side effects:
        // - Effect stop playing the next time
        // - GdxRuntimeException: Unable to allocate audio buffers.
        /*bumpSound.dispose();
        powerupSpawnSound.dispose();
        powerupSound.dispose();
        coinSound.dispose();
        breakBlockSound.dispose();
        stompSound.dispose();
        powerDownSound.dispose();
        marioDieSound.dispose();
        jumpSound.dispose();
        kickedSound.dispose();
        splashSound.dispose();
        fireSound.dispose();
        drinkingSound.dispose();*/
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
        out.writeInt(activeBoxCoins.size);
        for (BoxCoin boxCoin : activeBoxCoins) {
            boxCoin.write(out);
        }
        out.writeInt(coins.size);
        for (Coin coin : coins) {
            coin.write(out);
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
            } else if (enemyType.equals(Spiky.class.getName())) {
                enemy = new Spiky(callbacks, world, atlas, 0, 0);
            } else if (enemyType.equals(Flower.class.getName())) {
                enemy = new Flower(callbacks, world, atlas, 0, 0);
            } else if (enemyType.equals(Fish.class.getName())) {
                enemy = new Fish(callbacks, world, atlas, 0, 0);
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
            } else if (itemType.equals(FireFlower.class.getName())) {
                item = new FireFlower(callbacks, world, atlas, 0, 0);
            } else if (itemType.equals(Beer.class.getName())) {
                item = new Beer(callbacks, world, atlas, 0, 0);
            } else {
                throw new IllegalArgumentException("Unknown item type: " + itemType);
            }
            item.read(in);
            items.put(item.getId(), item);
        }
        int numBoxCoins = in.readInt();
        for (int i = 0; i < numBoxCoins; ++i) {
            BoxCoin boxCoin = new BoxCoin(atlas, Vector2.Zero);
            boxCoin.read(in);
            activeBoxCoins.add(boxCoin);
        }
        int numCoins = in.readInt();
        for (int i = 0; i < numCoins; ++i) {
            Coin coin = new Coin(callbacks, world, atlas, 0, 0);
            coin.read(in);
            coins.add(coin);
        }
        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            tileObject.read(in);
        }
        for (Platform platform : platforms) {
            platform.read(in);
        }
    }
}
