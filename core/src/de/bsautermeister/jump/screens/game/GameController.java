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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.managers.KillSequelManager;
import de.bsautermeister.jump.managers.WaterInteractionManager;
import de.bsautermeister.jump.physics.WorldContactListener;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.screens.menu.GameOverOverlay;
import de.bsautermeister.jump.screens.menu.PauseOverlay;
import de.bsautermeister.jump.serializer.BinarySerializable;
import de.bsautermeister.jump.serializer.BinarySerializer;
import de.bsautermeister.jump.sprites.BeerItem;
import de.bsautermeister.jump.sprites.BoxCoin;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.DrunkenGuy;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.Fish;
import de.bsautermeister.jump.sprites.Fox;
import de.bsautermeister.jump.sprites.GrilledChickenItem;
import de.bsautermeister.jump.sprites.Hedgehog;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.ItemBox;
import de.bsautermeister.jump.sprites.ItemDef;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.sprites.Player;
import de.bsautermeister.jump.sprites.Pole;
import de.bsautermeister.jump.sprites.PretzelBullet;
import de.bsautermeister.jump.sprites.PretzelItem;
import de.bsautermeister.jump.sprites.Tent;
import de.bsautermeister.jump.text.TextMessage;

public class GameController  implements BinarySerializable, Disposable {

    private static final Logger LOG = new Logger(GameController.class.getSimpleName(), Cfg.LOG_LEVEL);

    private final static String[] TENT_SONGS = {
            AssetPaths.Music.PROSIT_AUDIO,
            AssetPaths.Music.PROSIT2_AUDIO
    };

    private GameState stateBeforePause;
    private GameState state;

    private final MusicPlayer backgroundMusic;
    private final MusicPlayer foregroundMusic;
    private final GameSoundEffects soundEffects;

    private TextureAtlas atlas;

    private OrthographicCamera camera;
    private Viewport viewport;

    private final TmxMapLoader mapLoader;
    private TiledMap map;
    private float mapPixelWidth;
    private float mapPixelHeight;

    private World world;

    private Player player;

    private int score;
    private int collectedBeers;
    private int totalBeers;

    private ObjectMap<String, Enemy> enemies;
    private ObjectMap<String, Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;
    private Array<Platform> platforms;
    private Array<Coin> coins;

    private Array<BoxCoin> activeBoxCoins;

    private float gameTime;
    private Array<InteractiveTileObject> tileObjects;
    private Array<Rectangle> waterList;
    private WaterInteractionManager waterInteractionManager;

    private final int level;
    private final FileHandle gameToResume;

    private final LinkedBlockingQueue<TextMessage> textMessages;

    private final KillSequelManager killSequelManager;

    private boolean gameIsCanced;

    private Array<Rectangle> spikesList;

    private Tent tent;
    private Array<Pole> poles = new Array<>();

    private float munichRatio = 0f;

    private GameCallbacks callbacks = new GameCallbacks() {
        @Override
        public void jump() {
            soundEffects.jumpSound.play(1f);
        }

        @Override
        public void landed(float landingHeight) {
            if (landingHeight > Cfg.MIN_LANDING_HEIGHT) {
                soundEffects.landingSound.play(MathUtils.clamp(0.33f + landingHeight / (8 * Cfg.BLOCK_SIZE_PPM), 0f, 1f));
            }
        }

        @Override
        public void stomp(Enemy enemy) {
            soundEffects.stompSound.play();

            if (!(enemy instanceof Hedgehog)) {
                KillSequelManager killSequelManager = getKillSequelManager();
                killSequelManager.notifyKill();
                score += killSequelManager.getKillScore();
                showTextMessage(killSequelManager.getKillScoreText(), enemy.getBoundingRectangle());
            }
        }

        @Override
        public void use(Player player, Item item) {
            String msg;
            if (item instanceof BeerItem) {
                updateCollectedBeers(collectedBeers + 1);
                soundEffects.drinkingSound.play();
                if (collectedBeers >= totalBeers) {
                    unlockGoal();
                }
                msg = "PROST";
            } else if (item instanceof GrilledChickenItem) {
                soundEffects.eatFoodSound.play();
                msg = "TASTY";
            } else { // prezel
                soundEffects.eatFoodSound.play();
                msg = "YUMMY";
            }

            score += 100;
            showTextMessage(msg, item.getBoundingRectangle());
        }

        @Override
        public void hit(Player player) {
            if (player.isBig()) {
                soundEffects.complainSound.play();
            }
        }

        @Override
        public void hit(Player player, Brick brick, boolean closeEnough) {
            if (!closeEnough) {
                soundEffects.bumpSound.play();
            } else if (player.isBig()) {
                soundEffects.breakBlockSound.play();
            } else {
                soundEffects.bumpSound.play();
            }
        }

        @Override
        public void hit(Player player, ItemBox itemBox, Vector2 position, boolean closeEnough) {
            if (itemBox.isBlank() || !closeEnough) {
                soundEffects.bumpSound.play();
            } else if (itemBox.isFoodBox()) {
                if (player.isBig()) {
                    spawnItem(new ItemDef(position, PretzelItem.class));
                } else {
                    spawnItem(new ItemDef(position, GrilledChickenItem.class));
                }
                soundEffects.foodSpawnSound.play();
            } else if (itemBox.isBeerBox()) {
                spawnItem(new ItemDef(position, BeerItem.class));
                soundEffects.beerSpawnSound.play();
            } else {
                soundEffects.coinSpawnSound.play();
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
            }
        }

        @Override
        public void kicked(Enemy enemy) {
            soundEffects.kickedSound.play();
        }

        @Override
        public void touchedWater(Drownable drownable) {
            float volume = getVolumeBasedOnDistanceToCameraCenter(drownable.getWorldCenter().x);
            if (volume > 0) {
                soundEffects.splashSound.play(volume);
            }
        }

        @Override
        public void collectCoin() {
            soundEffects.coinSound.play(0.5f);
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
            if (enemy instanceof Hedgehog) {
                Hedgehog hedgehog = (Hedgehog) enemy;
                if (hedgehog.getState() == Hedgehog.State.ROLLING) {
                    float volume = getVolumeBasedOnDistanceToCameraCenter(enemy.getBody().getWorldCenter().x);
                    if (volume > 0) {
                        soundEffects.bumpSound.play(volume);
                    }
                }
            }
        }

        @Override
        public void hitWall(PretzelBullet pretzelBullet) {
            float volume = getVolumeBasedOnDistanceToCameraCenter(pretzelBullet.getBody().getWorldCenter().x);
            if (volume > 0) {
                soundEffects.kickedSound.play(volume / 2f);
            }
        }

        @Override
        public void hit(PretzelBullet pretzelBullet, Enemy enemy) {

        }

        @Override
        public void fire() {
            soundEffects.fireSound.play();
        }

        @Override
        public void hurry() {
            if (!backgroundMusic.isSelected(AssetPaths.Music.HURRY_AUDIO)) {
                backgroundMusic.selectMusic(AssetPaths.Music.HURRY_AUDIO);
                backgroundMusic.playFromBeginning();
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
            backgroundMusic.setVolume(0f, false);
            soundEffects.successSound.play();
        }

        @Override
        public void playerDied() {
            backgroundMusic.setVolume(0.1f, false);
            soundEffects.randomSwearingSound().play();
        }

        private static final float HALF_SCREEN_WIDTH = Cfg.WORLD_WIDTH / 2f / Cfg.PPM;
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

    private final Vector2 clickScreenPosition = new Vector2();
    private boolean markBackToMenu;
    private boolean markReset;

    private final PauseOverlay.Callback pauseCallback = new PauseOverlay.Callback() {
        @Override
        public void quit(Vector2 clickScreenPosition) {
            LOG.debug("QUIT pressed");
            backgroundMusic.setVolume(0f, false);
            gameIsCanced = true;
            markBackToMenu = true;
            GameController.this.clickScreenPosition.set(clickScreenPosition);
        }

        @Override
        public void resume() {
            LOG.debug("RESUME pressed");
            state = GameState.PLAYING;
        }
    };

    private final GameOverOverlay.Callback gameOverCallback = new GameOverOverlay.Callback() {
        @Override
        public void quit() {
            LOG.debug("QUIT pressed");
            markBackToMenu = true;
        }

        @Override
        public void restart() {
            LOG.debug("RESTART pressed");
            markReset = true;
        }
    };

    private final GameScreenCallbacks screenCallbacks;

    public GameController(GameScreenCallbacks screenCallbacks, GameApp game, GameSoundEffects soundEffects,
                          int level, FileHandle gameToResume) {
        this.level = level;
        this.gameToResume = gameToResume;
        this.screenCallbacks = screenCallbacks;
        this.soundEffects = soundEffects;
        this.atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);

        mapLoader = new TmxMapLoader();
        enemies = new ObjectMap<>();
        platforms = new Array<>();
        coins = new Array<>();

        items = new ObjectMap<>();
        itemsToSpawn = new LinkedBlockingQueue<>();

        activeBoxCoins = new Array<>();

        textMessages = new LinkedBlockingQueue<>();

        killSequelManager = new KillSequelManager();

        this.backgroundMusic = game.getBackgroundMusic();
        this.foregroundMusic = game.getForegroundMusic();

        camera = new OrthographicCamera();
        viewport = new StretchViewport((Cfg.WORLD_WIDTH + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, (Cfg.WORLD_HEIGHT + 4 * Cfg.BLOCK_SIZE) / Cfg.PPM, camera);

        waterInteractionManager = new WaterInteractionManager(atlas, callbacks);

        reset();
    }

    private void reset() {
        stateBeforePause = GameState.UNDEFINED;
        state = GameState.PLAYING;

        if (world != null) {
            world.dispose();
        }

        this.world = new World(new Vector2(0,Cfg.GRAVITY), true);
        this.world.setContactListener(new WorldContactListener());

        killSequelManager.reset();

        initMap(level);

        WorldCreator worldCreator = new WorldCreator(callbacks, world, map, atlas);
        worldCreator.buildFromMap();

        enemies.clear();
        platforms.clear();
        coins.clear();
        items.clear();
        itemsToSpawn.clear();
        activeBoxCoins.clear();
        textMessages.clear();

        WorldCreator.StartParams start = worldCreator.getStart();
        Rectangle goal = worldCreator.getGoal();
        player = new Player(callbacks, world, atlas, start);

        tent = new Tent(atlas, goal);

        platforms.addAll(worldCreator.createPlatforms());
        tileObjects = worldCreator.getTileObjects();
        spikesList = worldCreator.getSpikeRegions();

        totalBeers = getTotalBeers();
        updateCollectedBeers(0);
        score = 0;

        if (gameToResume != null) {
            load(gameToResume);
        } else {
            for (Enemy enemy : worldCreator.createEnemies()) {
                enemies.put(enemy.getId(), enemy);
            }
            coins.addAll(worldCreator.createCoins());

            backgroundMusic.selectMusic(AssetPaths.Music.NORMAL_AUDIO);
            backgroundMusic.setVolume(MusicPlayer.MAX_VOLUME, true);
            backgroundMusic.playFromBeginning();
        }

        camera.position.set(player.getBody().getPosition(), 0);

        waterList = worldCreator.getWaterRegions();
        waterInteractionManager.reset();
        waterInteractionManager.setWaterRegions(waterList);

        waterInteractionManager.add(player);
        for (Enemy enemy : enemies.values()) {
            if (enemy instanceof Drownable) {
                Drownable drownableEnemy = (Drownable) enemy;
                waterInteractionManager.add(drownableEnemy);
            }
        }

        poles.clear();
        for (Rectangle poleRect : worldCreator.getPoleRegions()) {
            poles.add(new Pole(atlas, poleRect));
        }
    }

    private void initMap(int level) {
        LOG.debug("Init map level: " + level);

        if (map != null) {
            map.dispose();
        }

        this.map = mapLoader.load(String.format(Locale.ROOT, "maps/level%02d.tmx", level));
        float mapWidth = map.getProperties().get("width", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class);
        float tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        float tilePixelHeight = map.getProperties().get("tileheight", Integer.class);
        this.mapPixelWidth = mapWidth * tilePixelWidth / Cfg.PPM;
        this.mapPixelHeight = mapHeight * tilePixelHeight / Cfg.PPM;
    }

    public void update(float delta) {
        if (state.isPaused() || state.isGameOver()) {
            postUpdate();
            return;
        }

        gameTime += delta;
        world.step(delta, 8, 3);

        if (!player.isVictory()) {
            handleInput();
        }

        handleSpawningItems();

        player.update(delta);
        updateMunichRation(delta);
        checkPlayerInBounds();

        killSequelManager.update(delta);

        player.getPretzelBullet().update(delta);

        tent.setPlayerPosition(player.getWorldCenter());
        updateTent(delta);

        updateEnemies(delta);
        updateItems(delta);
        updatePlatforms(delta);

        waterInteractionManager.update(delta);

        checkPlayerSpikesCollision();

        for (InteractiveTileObject tileObject : tileObjects) {
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

        if (player.getTimeToLive() <= Cfg.HURRY_WARNING_TIME) {
            callbacks.hurry(); // TODO call only once?
        }

        if (player.isVictory() && player.getStateTimer() >= Cfg.GOAL_REACHED_FINISH_DELAY) {
            Vector2 goalCenterPosition = viewport.project(getTent().getWorldCenter());
            screenCallbacks.success(level, goalCenterPosition);
        }

        for (TextMessage textMessage : textMessages) {
            textMessage.update(delta);
        }

        // touch for platform contacts
        if (player.hasPlatformContact()) {
            if (Math.abs(player.getVelocityRelativeToGround().y) < 0.01) {
                player.getPlatformContact().touch(delta);
            }
        }

        if (player.isDead() && player.getStateTimer() > 3f) {
            state = GameState.GAME_OVER;
        }

        postUpdate();
    }

    private void updateTent(float delta) {
        tent.update(delta);

        if (tent.isOpen()) {
            float tentVolume = getTentVolume();
            if (tentVolume > 0) {
                foregroundMusic.setVolume(tentVolume, false);
                if (!foregroundMusic.isPlaying()) {
                    String randomTentSong = TENT_SONGS[MathUtils.random(TENT_SONGS.length - 1)];
                    foregroundMusic.selectMusic(randomTentSong);
                    foregroundMusic.resumeOrPlay();
                }
            } else if (foregroundMusic.isPlaying()) {
                foregroundMusic.setVolume(0f, false);
            }

            foregroundMusic.update(delta);
        }
    }

    private void updateMunichRation(float delta) {
        float cameraX = camera.position.x;
        float tentX = tent.getX() + tent.getWidth() / 2f;
        float diffX = Math.abs(tentX - cameraX);

        float targetRatio = 0f;
        if (diffX < Cfg.MUNICH_FULL_THRESHOLD_X) {
            targetRatio = 1f;
        } else if (diffX < Cfg.MUNICH_START_THRESHOLD_X) {
            float x = Cfg.MUNICH_START_THRESHOLD_X - Cfg.MUNICH_FULL_THRESHOLD_X;
            float slope = -1 / x;
            float intercept = 1 + Cfg.MUNICH_FULL_THRESHOLD_X * -slope;
            targetRatio = intercept + diffX * slope;
        }

        float diffToTarget = targetRatio - munichRatio;
        float absDiffToTarget = Math.abs(diffToTarget);
        float absTargetChange = Math.min(absDiffToTarget, Math.max(0.00025f, delta * 5 * absDiffToTarget));
        munichRatio += absTargetChange * Math.signum(diffToTarget);

        munichRatio = MathUtils.clamp(munichRatio, 0f, 1f);
    }

    private void checkPlayerSpikesCollision() {
        for (Rectangle spikeRect : spikesList) {
            if (spikeRect.overlaps(player.getBoundingRectangle())) {
                player.hit(spikeRect);
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

        if (tent.isEntering(player)) {
            completeLevel();
        }

        if (!textMessages.isEmpty() && !textMessages.peek().isAlive()) {
            textMessages.poll();
        }

        PretzelBullet pretzelBullet = player.getPretzelBullet();
        if (pretzelBullet.isActive() && !isVisible(pretzelBullet)) {
            pretzelBullet.reset();
        } else {
            pretzelBullet.postUpdate();
        }

        // cleanup jump-through ID of platform for player
        if (player.getLastJumpThroughPlatformId() != null) {
            for (Platform platform : platforms) {
                if (platform.getId().equals(player.getLastJumpThroughPlatformId()) &&
                        !player.getBoundingRectangle().overlaps(platform.getBoundingRectangle())) {
                    player.setLastJumpThroughPlatformId(null);
                }
            }
        }

        if (markReset) {
            markReset = false;
            reset();
        }

        if (markBackToMenu) {
            markBackToMenu = false;
            screenCallbacks.backToMenu(clickScreenPosition);
        }
    }

    private void handleSpawningItems() {
        if (itemsToSpawn.isEmpty()) {
            return;
        }

        ItemDef itemDef = itemsToSpawn.poll();
        Item item;
        if (itemDef.getType() == GrilledChickenItem.class) {
            item = new GrilledChickenItem(callbacks, world, atlas, itemDef.getPosition().x, itemDef.getPosition().y);
        } else if (itemDef.getType() == PretzelItem.class) {
            item = new PretzelItem(callbacks, world, atlas, itemDef.getPosition().x, itemDef.getPosition().y);
        } else {
            item = new BeerItem(callbacks, world, atlas, itemDef.getPosition().x, itemDef.getPosition().y);
        }
        items.put(item.getId(), item);
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

            float len2 = Vector2.len2(enemy.getX() - player.getX(), enemy.getY() - player.getY());

            if (!enemy.isActive() && len2 < Cfg.ENEMY_WAKE_UP_DISTANCE2) {
                enemy.setActive(true);

                if (enemy.hasGroup()) {
                    wakeUp(enemy.getGroup());
                }
            }

            if (enemy instanceof Fox) {
                ((Fox) enemy).setPlayerPosition(player.getBody().getPosition());
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

            if (platform.getX() < player.getX() + Cfg.WORLD_WIDTH * 0.75f / Cfg.PPM) {
                platform.setActive(true);
            }
        }
    }

    private void updateCollectedBeers(int value) {
        collectedBeers = Math.min(value, totalBeers);
        player.setCharacterProgress((float)value / totalBeers);
    }

    private void pauseGame() {
        if (state == GameState.PAUSED) {
            return;
        }

        stateBeforePause = state;
        state = GameState.PAUSED;
    }

    private void completeLevel() {
        if (!player.isVictory()) {
            LOG.debug("Goal reached");
            player.victory();
            callbacks.goalReached();
        }
    }

    private void unlockGoal() {
        int i = 0;
        for (InteractiveTileObject tileObject : tileObjects) {
            if (tileObject instanceof Brick) {
                Brick brick = (Brick) tileObject;
                if (brick.isGoalProtector()) {
                    brick.unlockGoal(i++ * 0.25f);
                }
            }
        }

        tent.open();
    }

    private void spawnItem(ItemDef itemDef) {
        LOG.debug("Spawning: " + itemDef.getType().getSimpleName());
        itemsToSpawn.add(itemDef);
    }

    private void checkPlayerInBounds() {
        float blockSizePpm = Cfg.BLOCK_SIZE_PPM;
        float padding = 2 * blockSizePpm;
        float x = player.getBody().getPosition().x;
        if (x - blockSizePpm / 2  < padding) {
            player.getBody().setTransform(padding + blockSizePpm / 2,
                    player.getBody().getPosition().y, 0);

        } else if (x + blockSizePpm / 2 > mapPixelWidth - padding) {
            player.getBody().setTransform(mapPixelWidth - padding - blockSizePpm / 2,
                    player.getBody().getPosition().y, 0);
        }
    }

    private void upateCameraPosition() {
        if (player.isDead()) {
            return;
        }

        camera.position.x = camera.position.x - (camera.position.x - player.getBody().getPosition().x) * 0.1f;

        // check camera in bounds (X)
        if (camera.position.x - viewport.getWorldWidth() / 2 < 0) {
            camera.position.x = viewport.getWorldWidth() / 2;
        } else if (camera.position.x + viewport.getWorldWidth() / 2 > mapPixelWidth) {
            camera.position.x = mapPixelWidth - viewport.getWorldWidth() / 2;
            camera.position.x = mapPixelWidth - viewport.getWorldWidth() / 2;
        }

        camera.position.y = camera.position.y - (camera.position.y - player.getBody().getPosition().y + viewport.getWorldHeight() * 0.075f) * 0.066f;

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
        checkPauseInput();

        if (player.isDead()) {
            return;
        }

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
                        float angle = swipeDirection.angle();
                        if (len > 0.1f &&angle > 45 && angle < 135) {
                            downPressed = true;
                        }
                    }
                }

                if (x < 0.125) {
                    leftPressed = true;
                } else if (x > 0.2 && x < 0.45) {
                    rightPressed = true;
                }
            } else if (x < 0.995) { // save last 0.5% for Android immersive mode hidden soft buttons
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

        player.control(upPressed, downPressed, leftPressed, rightPressed, firePressed);
    }

    private void checkPauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseGame();
        }
    }

    public int getTotalBeers() {
        int result = 0;
        for (InteractiveTileObject tileObject : tileObjects) {
            if (tileObject instanceof ItemBox) {
                ItemBox box = (ItemBox) tileObject;
                if (box.isBeerBox()) {
                    result++;
                }
            }
        }
        return result;
    }

    private float getTentVolume() {
        Vector2 playerPosition = player.getWorldCenter();
        Vector2 tentPosition = tent.getWorldCenter();
        float dst2 = Vector2.dst2(playerPosition.x, playerPosition.y, tentPosition.x, tentPosition.y);
        return MathUtils.clamp(1.11f - (dst2 / 10f), 0f, 1f);
    }

    public void load(FileHandle handle) {
        if (handle.exists()) {
            if (!BinarySerializer.read(this, handle.read())) {
                LOG.error("Could not load game state");
            }
        }
    }

    public void save() {
        // don't do anything in case player is dead, kind of dead or level is finished
        if (player.isDead() || player.isDrowning() || player.isVictory() || gameIsCanced) {
            LOG.error("Did NOT save game state");
            JumpGame.deleteSavedData();
            return;
        }

        pauseGame();

        final FileHandle fileHandle = JumpGame.getSavedDataHandle();
        if (!BinarySerializer.write(this, fileHandle.write(false))) {
            LOG.error("Could not save game state");
        }
        LOG.debug("Saved game state");
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        if (state.isPaused()) {
            // don't store the paused state, because otherwise the game resumes in the pause menu
            out.writeUTF(stateBeforePause.name());
        } else {
            out.writeUTF(state.name());
        }
        out.writeBoolean(gameIsCanced);
        out.writeInt(score);
        out.writeInt(collectedBeers);
        out.writeFloat(gameTime);
        player.write(out);
        out.writeFloat(munichRatio);
        backgroundMusic.write(out);
        foregroundMusic.write(out);
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
        for (InteractiveTileObject tileObject : tileObjects) {
            tileObject.write(out);
        }
        for (Platform platform : platforms) {
            platform.write(out);
        }
        tent.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        state = Enum.valueOf(GameState.class, in.readUTF());
        gameIsCanced = in.readBoolean();
        score = in.readInt();
        collectedBeers = in.readInt();
        gameTime = in.readFloat();
        player.read(in);
        munichRatio = in.readFloat();
        backgroundMusic.read(in);
        foregroundMusic.read(in);
        killSequelManager.read(in);
        int numEnemies = in.readInt();
        for (int i = 0; i < numEnemies; ++i) {
            String enemyType = in.readUTF();
            Enemy enemy;
            if (enemyType.equals(Fox.class.getName())) {
                enemy = new Fox(callbacks, world, atlas, 0, 0, false);
            } else if (enemyType.equals(Hedgehog.class.getName())) {
                enemy = new Hedgehog(callbacks, world, atlas, 0, 0, false);
            } else if (enemyType.equals(DrunkenGuy.class.getName())) {
                enemy = new DrunkenGuy(callbacks, world, atlas, 0, 0);
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
            if (itemType.equals(GrilledChickenItem.class.getName())) {
                item = new GrilledChickenItem(callbacks, world, atlas, 0, 0);
            } else if (itemType.equals(PretzelItem.class.getName())) {
                item = new PretzelItem(callbacks, world, atlas, 0, 0);
            } else if (itemType.equals(BeerItem.class.getName())) {
                item = new BeerItem(callbacks, world, atlas, 0, 0);
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
        for (InteractiveTileObject tileObject : tileObjects) {
            tileObject.read(in);
        }
        for (Platform platform : platforms) {
            platform.read(in);
        }
        tent.read(in);
    }

    @Override
    public void dispose() {
        map.dispose();
        world.dispose();
    }

    public float getGameTime() {
        return gameTime;
    }

    public Player getPlayer() {
        return player;
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

    public Array<Rectangle> getWaterList() {
        return waterList;
    }

    public Array<InteractiveTileObject> getTileObjects() {
        return tileObjects;
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

    public GameState getState() {
        return state;
    }

    public PauseOverlay.Callback getPauseCallback() {
        return pauseCallback;
    }

    public GameOverOverlay.Callback getGameOverCallback() {
        return gameOverCallback;
    }

    public Tent getTent() {
        return tent;
    }

    public Array<Pole> getPoles() {
        return poles;
    }

    public float getMunichRatio() {
        return munichRatio;
    }
}
