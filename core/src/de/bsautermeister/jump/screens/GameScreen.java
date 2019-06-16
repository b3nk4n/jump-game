package de.bsautermeister.jump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.LinkedBlockingQueue;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.physics.WorldContactListener;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.scenes.Hud;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.ItemDef;
import de.bsautermeister.jump.sprites.Koopa;
import de.bsautermeister.jump.sprites.Mario;
import de.bsautermeister.jump.sprites.Mushroom;
import de.bsautermeister.jump.sprites.SpinningCoin;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameScreen extends ScreenBase {
    private TextureAtlas atlas;

    private OrthographicCamera camera;
    private Viewport viewport;
    private final Hud hud;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private float mapPixelWidth;

    private final World world;
    private final Box2DDebugRenderer box2DDebugRenderer;

    private final Mario mario;

    private Array<Enemy> enemies;
    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

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

    private GameCallbacks callbacks = new GameCallbacks() {
        @Override
        public void jump() {
            jumpSound.play(0.33f);
        }

        @Override
        public void stomp(Enemy enemy) {
            stompSound.play();
            mario.addScore(50);
        }


        @Override
        public void use(Mario mario, Item item) {
            powerupSound.play();
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
                mario.addScore(500);
            } else {
                coinSound.play();
                spinningCoins.add(new SpinningCoin(atlas, coin.getBody().getWorldCenter()));
                mario.addScore(100);
            }
        }

        @Override
        public void kicked(Enemy enemy) {
            kickedSound.play();
        }

        @Override
        public void killed(Enemy enemy) {
            kickedSound.play();
            mario.addScore(50);
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

    public GameScreen(GameApp game) {
        super(game);
        this.atlas = new TextureAtlas(AssetPaths.Atlas.GAMEPLAY);

        this.camera = new OrthographicCamera();
        this.viewport = new StretchViewport(GameConfig.WORLD_WIDTH / GameConfig.PPM, GameConfig.WORLD_HEIGHT / GameConfig.PPM, camera);
        this.camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        this.map = new TmxMapLoader().load("maps/level01.tmx");
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / GameConfig.PPM, game.getBatch());
        float mapWidth = map.getProperties().get("width", Integer.class);
        float tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        this.mapPixelWidth = mapWidth * tilePixelWidth / GameConfig.PPM;

        this.world = new World(new Vector2(0,-10f), true);
        this.box2DDebugRenderer = new Box2DDebugRenderer();
        WorldCreator worldCreator = new WorldCreator(callbacks, world, map, atlas);
        this.enemies = worldCreator.createEnemies();

        mario = new Mario(callbacks, world, atlas);

        this.hud = new Hud(game.getBatch(), mario);

        world.setContactListener(new WorldContactListener());

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

        spinningCoins = new Array<SpinningCoin>();

        reset();
    }

    private void reset() {
        getGame().getMusicPlayer().play();
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
    }

    private void spawnItem(ItemDef itemDef) {
        itemsToSpawn.add(itemDef);
    }

    private void handleSpawingItems() {
        if (itemsToSpawn.isEmpty()) {
            return;
        }

        ItemDef itemDef = itemsToSpawn.poll();
        if (itemDef.getType() == Mushroom.class) {
            items.add(new Mushroom(callbacks, world, atlas, itemDef.getPosition().x, itemDef.getPosition().y));
        }
    }

    public void update(float delta) {
        handleInput();
        handleSpawingItems();

        world.step(1 / 60f, 6, 2);

        mario.update(delta);
        checkPlayerInBounds();

        updateEnemies(delta);
        updateItems(delta);

        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            tileObject.update(delta);
        }

        for(SpinningCoin spinningCoin : spinningCoins) {
            if (spinningCoin.isFinished()) {
                spinningCoins.removeValue(spinningCoin, true);
            } else {
                spinningCoin.update(delta);
            }
        }

        hud.update(delta);

        upateCameraPosition();
        camera.update();

        mapRenderer.setView(camera);

        if (mario.getState() == Mario.State.DEAD) {
            getGame().getMusicPlayer().stop();
        }
    }

    private void postUpdate() {
        for (Enemy enemy : enemies) {
            enemy.postUpdate();

            if (enemy.isRemovable()) {
                enemy.dispose();
                enemies.removeValue(enemy, true);
            }
        }

        for (Item item : items) {
            item.postUpdate();

            if (item.isRemovable()) {
                item.dispose();
                items.removeValue(item, true);
            }
        }
    }

    private void updateItems(float delta) {
        for (Item item : items) {
            item.update(delta);
        }
    }

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            enemy.update(delta);

            if (enemy.getX() < mario.getX() + 256 / GameConfig.PPM) {
                enemy.setActive(true);
            }
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

        // snap camera position to the PPM pixel grid, otherwise there are rendering artifacts
        camera.position.x = (float) Math.round(mario.getBody().getPosition().x * GameConfig.PPM) / GameConfig.PPM;
        //camera.position.x = mario.getBody().getPosition().x; // no snapping runs smoother

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

        if (GameConfig.DEBUG_MODE) {
            box2DDebugRenderer.render(world, camera.combined);
        }

        batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        renderHud();

        if (isGameOver()) {
            getGame().setScreen(new GameOverScreen(getGame()));
        }
    }

    private void renderBackground(SpriteBatch batch) {
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.BACKGROUND_KEY));

        for (SpinningCoin spinningCoin : spinningCoins) {
            spinningCoin.draw(batch);
        }
    }

    private void renderForeground(SpriteBatch batch) {
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get(WorldCreator.GRAPHICS_KEY));

        for (Item item : items) {
            item.draw(batch);
        }

        for (Enemy enemy : enemies) {
            enemy.draw(batch);
        }

        mario.draw(batch);

        for (InteractiveTileObject tileObject : WorldCreator.getTileObjects()) {
            tileObject.draw(batch);
        }
    }

    private void renderHud() {
        hud.getStage().draw();
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
}
