package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.commons.GameStats;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.managers.KillSequelManager;
import de.bsautermeister.jump.managers.WaterInteractionManager;
import de.bsautermeister.jump.physics.WorldContactListener;
import de.bsautermeister.jump.physics.WorldCreator;
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

public class GameController  implements BinarySerializable, Disposable {

    private static final Logger LOG = new Logger(GameController.class.getSimpleName(), Cfg.LOG_LEVEL);

    private final MusicPlayer musicPlayer;
    private final GameSoundEffects soundEffects;

    private GameStats gameStats;
    private TextureAtlas atlas;

    private OrthographicCamera camera;
    private Viewport viewport;

    private TiledMap map;
    private float mapPixelWidth;
    private float mapPixelHeight;

    private World world;

    private Mario mario;

    private int score;
    private int collectedBeers;
    private int totalBeers;

    private WorldCreator.StartParams start;
    private Rectangle goal;
    private ObjectMap<String, Enemy> enemies;
    private ObjectMap<String, Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;
    private Array<Platform> platforms;
    private Array<Coin> coins;

    private boolean levelCompleted;
    private float levelCompletedTimer;

    private Array<BoxCoin> activeBoxCoins;

    private float gameTime;
    private Array<WorldCreator.WaterParams> waterList;

    private WaterInteractionManager waterInteractionManager;

    private FileHandle gameToLoad;
    private Integer level;

    private LinkedBlockingQueue<TextMessage> textMessages = new LinkedBlockingQueue<TextMessage>();

    private final KillSequelManager killSequelManager;

    private GameCallbacks callbacks = new GameCallbacks() {
        @Override
        public void jump() {
            soundEffects.jumpSound.play(0.33f);
        }

        @Override
        public void stomp(Enemy enemy) {
            soundEffects.stompSound.play();

            if (!(enemy instanceof Koopa)) {
                KillSequelManager killSequelManager = getKillSequelManager();
                killSequelManager.notifyKill();
                score += killSequelManager.getKillScore();
                showTextMessage(killSequelManager.getKillScoreText(), enemy.getBoundingRectangle());
            }
        }

        @Override
        public void use(Mario mario, Item item) {
            String msg;
            if (item instanceof Beer) {
                collectedBeers++;
                soundEffects.drinkingSound.play();
                if (collectedBeers >= totalBeers) {
                    unlockGoal();
                }
                msg = "PROST";
            } else if (item instanceof Mushroom) {
                soundEffects.ohYeahSound.play();
                msg = "SWEET";
            } else { // prezel
                soundEffects.powerupSound.play();
                msg = "YUMMY";
            }

            score += 100;
            showTextMessage(msg, item.getBoundingRectangle());
        }

        @Override
        public void hit(Mario mario, Enemy enemy) {
            if (mario.isBig()) {
                soundEffects.powerDownSound.play();
            }
        }

        @Override
        public void hit(Mario mario, Brick brick, boolean closeEnough) {
            if (!closeEnough) {
                soundEffects.bumpSound.play();
            } else if (mario.isBig()) {
                soundEffects.breakBlockSound.play();
            } else {
                soundEffects.bumpSound.play();
            }
        }

        @Override
        public void hit(Mario mario, ItemBox itemBox, Vector2 position, boolean closeEnough) {
            if (itemBox.isBlank() || !closeEnough) {
                soundEffects.bumpSound.play();
            } else if (itemBox.isMushroomBox()) {
                if (mario.isBig()) {
                    spawnItem(new ItemDef(position, FireFlower.class));
                } else {
                    spawnItem(new ItemDef(position, Mushroom.class));
                }
                soundEffects.powerupSpawnSound.play();
            } else if (itemBox.isBeerBox()) {
                spawnItem(new ItemDef(position, Beer.class));
                soundEffects.powerupSpawnSound.play();
            } else {
                soundEffects.coinSound.play();
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
                KillSequelManager killSequelManager = getKillSequelManager();
                killSequelManager.notifyKill();
                score += killSequelManager.getKillScore();
                showTextMessage(killSequelManager.getKillScoreText(), enemy.getBoundingRectangle());
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
            soundEffects.kickedSound.play();
        }

        @Override
        public void touchedWater(Drownable drownable, boolean isBeer) {
            float volume = getVolumeBasedOnDistanceToCameraCenter(drownable.getWorldCenter().x);
            if (volume > 0) {
                soundEffects.splashSound.play(volume);
                if (isBeer) {
                    soundEffects.drinkingSound.play(volume, 0.9f, 0f);
                }
            }
        }

        @Override
        public void collectCoin() {
            soundEffects.coinSound.play();
            score += Cfg.COIN_SCORE;
        }

        @Override
        public void killed(Enemy enemy) {
            float volume = getVolumeBasedOnDistanceToCameraCenter(enemy.getBody().getWorldCenter().x);
            if (volume > 0) {
                soundEffects.kickedSound.play(volume);
            }
            KillSequelManager killSequelManager = getKillSequelManager();
            killSequelManager.notifyKill();
            score += killSequelManager.getKillScore();
            showTextMessage(killSequelManager.getKillScoreText(), enemy.getBoundingRectangle());
        }

        @Override
        public void hitWall(Enemy enemy) {
            if (enemy instanceof Koopa) {
                Koopa koopa = (Koopa) enemy;
                if (koopa.getState() == Koopa.State.MOVING_SHELL) {
                    float volume = getVolumeBasedOnDistanceToCameraCenter(enemy.getBody().getWorldCenter().x);
                    if (volume > 0) {
                        soundEffects.bumpSound.play(volume);
                    }
                }
            }
        }

        @Override
        public void hit(Fireball fireball, Enemy enemy) {

        }

        @Override
        public void fire() {
            soundEffects.fireSound.play();
        }

        @Override
        public void hurry() {
            if (!musicPlayer.isSelected(AssetPaths.Music.HURRY_AUDIO)) {
                musicPlayer.selectMusic(AssetPaths.Music.HURRY_AUDIO);
                musicPlayer.play();
            }
        }

        @Override
        public void unlockGoalBrick(Brick brick) {
            float volume = getVolumeBasedOnDistanceToCameraCenter(brick.getBody().getWorldCenter().x);
            if (volume > 0) {
                soundEffects.breakBlockSound.play();
            }
        }

        @Override
        public void goalReached() {
            musicPlayer.setVolume(0f, false);
            soundEffects.successSound.play();
        }

        @Override
        public void gameOver() {
            musicPlayer.stop();
            soundEffects.marioDieSound.play();
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

    private final GameScreenCallbacks screenCallbacks;

    public GameController(GameScreenCallbacks screenCallbacks, MusicPlayer musicPlayer, GameSoundEffects soundEffects, int level) {
        this.screenCallbacks = screenCallbacks;
        this.soundEffects = soundEffects;
        this.level = level;
        this.gameStats = new GameStats();
        this.atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);

        this.world = new World(new Vector2(0,-9.81f), true);
        this.world.setContactListener(new WorldContactListener());

        enemies = new ObjectMap<String, Enemy>();
        platforms = new Array<Platform>();
        coins = new Array<Coin>();

        items = new ObjectMap();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

        activeBoxCoins = new Array<BoxCoin>();

        killSequelManager = new KillSequelManager();

        this.musicPlayer = musicPlayer;

        reset();
    }

    private void reset() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport((Cfg.WORLD_WIDTH + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, (Cfg.WORLD_HEIGHT + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, camera);

        score = 0;
        collectedBeers = 0;

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
        waterList = worldCreator.getWaterRegions();


        start = worldCreator.getStart();
        goal = worldCreator.getGoal();

        mario = new Mario(callbacks, world, atlas, start);

        camera.position.set(start.position, 0);

        waterInteractionManager = new WaterInteractionManager(atlas, callbacks, waterList);
        waterInteractionManager.add(mario);
        for (Enemy enemy : enemies.values()) {
            if (enemy instanceof Drownable) {
                Drownable drownableEnemy = (Drownable) enemy;
                waterInteractionManager.add(drownableEnemy);
            }
        }

        totalBeers = getTotalBeers();

        levelCompleted = false;
        levelCompletedTimer = 0;

        musicPlayer.selectMusic(AssetPaths.Music.NORMAL_AUDIO);
        musicPlayer.setVolume(MusicPlayer.MAX_VOLUME, true);
        musicPlayer.play();
    }

    private void initMap(int level) {
        this.map = new TmxMapLoader().load(String.format("maps/level%02d.tmx", level));
        float mapWidth = map.getProperties().get("width", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class);
        float tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        float tilePixelHeight = map.getProperties().get("tileheight", Integer.class);
        this.mapPixelWidth = mapWidth * tilePixelWidth / Cfg.PPM;
        this.mapPixelHeight = mapHeight * tilePixelHeight / Cfg.PPM;
    }

    public void update(float delta) {
        world.step(1 / 60f, 8, 3);

        if (!levelCompleted) {
            handleInput();
        }

        handleSpawningItems();

        mario.update(delta);
        checkPlayerInBounds();

        killSequelManager.update(delta);

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
                showTextMessage(Cfg.COIN_SCORE_STRING, boxCoin.getBoundingRectangle());
                activeBoxCoins.removeValue(boxCoin, true);
            } else {
                boxCoin.update(delta);
            }
        }

        upateCameraPosition();
        camera.update();

        /*if (mario.getState() == Mario.State.DEAD) {
            musicPlayer.stop();
        }*/

        if (mario.getTimeToLive() <= 60) {
            callbacks.hurry(); // TODO call only once?
        }

        if (levelCompleted) {
            levelCompletedTimer += delta;

            if (levelCompletedTimer >= Cfg.GOAL_REACHED_FINISH_DELAY) {
                screenCallbacks.success(level);
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

        postUpdate();
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

        if (Intersector.overlaps(goal, mario.getBoundingRectangle())) {
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

        if (isGameOver()) {
            screenCallbacks.fail();
        }
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

    private void showTextMessage(String text, Rectangle rect) {
        float x = rect.getX() + rect.getWidth() / 2;
        float y = rect.getY() + rect.getHeight() / 2;
        float cameraLeftX = (camera.position.x - viewport.getWorldWidth() / 2);
        textMessages.add(new TextMessage(text, x - cameraLeftX, y));
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
            if (!enemy.isActive() && Vector2.len2(
                    enemy.getX() - mario.getX(), enemy.getY() - mario.getY()) < Cfg.ENEMY_WAKE_UP_DISTANCE2) {
                enemy.setActive(true);

                if (enemy.hasGroup()) {
                    wakeUp(enemy.getGroup());
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
            LOG.debug("Goal reached");
            levelCompleted = true;
            mario.setLevelCompleted(true);
            gameStats.setHighestFinishedLevel(level);
            callbacks.goalReached();
        }
    }

    private void unlockGoal() {
        int i = 0;
        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            if (tileObject instanceof Brick) {
                Brick brick = (Brick) tileObject;
                if (brick.isGoalProtector()) {
                    brick.unlockGoal(i++ * 0.25f);
                }
            }
        }
    }

    private void spawnItem(ItemDef itemDef) {
        LOG.debug("Spawning: " + itemDef.getType().getSimpleName());
        itemsToSpawn.add(itemDef);
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
        //camera.position.x = mario.getBody().getPosition().x;

        // C) interpolation (super smooth)
        camera.position.x = camera.position.x - (camera.position.x - mario.getBody().getPosition().x) * 0.1f;

        // check camera in bounds (X)
        if (camera.position.x - viewport.getWorldWidth() / 2 < 0) {
            camera.position.x = viewport.getWorldWidth() / 2;
        } else if (camera.position.x + viewport.getWorldWidth() / 2 > mapPixelWidth) {
            camera.position.x = mapPixelWidth - viewport.getWorldWidth() / 2;
            camera.position.x = mapPixelWidth - viewport.getWorldWidth() / 2;
        }

        camera.position.y = camera.position.y - (camera.position.y - mario.getBody().getPosition().y + viewport.getWorldHeight() * 0.133f) * 0.066f;

        // check camera in bounds (Y)
        if (camera.position.y - viewport.getWorldHeight() / 2 < 0) {
            camera.position.y = viewport.getWorldHeight() / 2;
        } else if (camera.position.y + viewport.getWorldHeight() / 2 > mapPixelHeight) {
            camera.position.y = mapPixelHeight - viewport.getWorldHeight() / 2;
            camera.position.y = mapPixelHeight - viewport.getWorldHeight() / 2;
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

    private boolean isGameOver() {
        return mario.getState() == Mario.State.DEAD && mario.getStateTimer() > 3f;
    }

    public int getTotalBeers() {
        int result = 0;
        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            if (tileObject instanceof ItemBox) {
                ItemBox box = (ItemBox) tileObject;
                if (box.isBeerBox()) {
                    result++;
                }
            }
        }
        return result;
    }

    public void load(FileHandle handle) {
        if (handle.exists()) {
            if (!BinarySerializer.read(this, handle.read())) {
                LOG.error("Could not load game state");
            }

            JumpGame.deleteSavedData();
        }
    }

    public void save() {
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
        out.writeInt(collectedBeers);
        out.writeBoolean(levelCompleted);
        out.writeFloat(levelCompletedTimer);
        out.writeFloat(gameTime);
        mario.write(out);
        killSequelManager.write(out);
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
        collectedBeers = in.readInt();
        levelCompleted = in.readBoolean();
        levelCompletedTimer = in.readFloat();
        gameTime = in.readFloat();
        mario.read(in);
        killSequelManager.read(in);
        int numEnemies = in.readInt();
        for (int i = 0; i < numEnemies; ++i) {
            String enemyType = in.readUTF();
            Enemy enemy;
            if (enemyType.equals(Goomba.class.getName())) {
                enemy = new Goomba(callbacks, world, atlas, 0, 0, false);
            } else if (enemyType.equals(Koopa.class.getName())) {
                enemy = new Koopa(callbacks, world, atlas, 0, 0, false);
            } else if (enemyType.equals(Spiky.class.getName())) {
                enemy = new Spiky(callbacks, world, atlas, 0, 0, false);
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

    @Override
    public void dispose() {
        map.dispose();
        world.dispose();
    }

    public float getGameTime() {
        return gameTime;
    }

    public Mario getMario() {
        return mario;
    }

    public int getScore() {
        return score;
    }

    public int getCollectedBeers() {
        return collectedBeers;
    }

    public LinkedBlockingQueue<TextMessage> getTextMessages() {
        return textMessages;
    }

    public ObjectMap<String, Item> getItems() {
        return items;
    }

    public Array<Coin> getCoins() {
        return coins;
    }

    public Array<BoxCoin> getActiveBoxCoins() {
        return activeBoxCoins;
    }

    public ObjectMap<String, Enemy> getEnemies() {
        return enemies;
    }

    public Array<Platform> getPlatforms() {
        return platforms;
    }

    public Array<WorldCreator.WaterParams> getWaterList() {
        return waterList;
    }

    public Array<ParticleEffectPool.PooledEffect> getActiveSplashEffects() {
        return waterInteractionManager.getActiveSplashEffects();
    }

    public KillSequelManager getKillSequelManager() {
        return killSequelManager;
    }

    public World getWorld() {
        return world;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public TiledMap getMap() {
        return map;
    }
}
