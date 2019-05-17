package de.bsautermeister.jump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.LinkedBlockingQueue;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.physics.WorldContactListener;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.scenes.Hud;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.ItemDef;
import de.bsautermeister.jump.sprites.Mario;
import de.bsautermeister.jump.sprites.Mushroom;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameScreen extends ScreenBase {
    private TextureAtlas atlas;

    private OrthographicCamera camera;
    private Viewport viewport;
    private final Hud hud;

    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private int mapWidth;
    private int tilePixelWidth;
    private float mapPixelWidth;

    private final World world;
    private final Box2DDebugRenderer box2DDebugRenderer;

    private final Mario mario;

    private final WorldCreator worldCreator;
    private Array<Enemy> enemies;
    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    private Sound bumpSound;
    private Sound powerupSpawnSound;
    private Sound powerupSound;
    private Sound coinSound;
    private Sound breakBlockSound;
    private Sound stompSound;
    private Sound powerDownSound;
    private Sound marioDieSound;
    private Sound jumpSound;

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
                powerDownSound.play(); // TODO play other sound if Koopa was kicked
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
                mario.addScore(100);
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

        this.mapLoader = new TmxMapLoader();

        this.map = mapLoader.load("maps/level01.tmx");
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / GameConfig.PPM, game.getBatch());
        this.mapWidth = map.getProperties().get("width", Integer.class);
        this.tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        this.mapPixelWidth = mapWidth * tilePixelWidth / GameConfig.PPM;

        this.world = new World(new Vector2(0,-10f), true);
        this.box2DDebugRenderer = new Box2DDebugRenderer();
        this.worldCreator = new WorldCreator(callbacks, world, map, atlas);
        this.enemies = worldCreator.createEnemies();

        mario = new Mario(callbacks, world, atlas);

        this.hud = new Hud(game.getBatch(), mario);

        world.setContactListener(new WorldContactListener());

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

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
        handleInput(delta);
        handleSpawingItems();

        world.step(1 / 60f, 6, 2);

        mario.update(delta);
        checkPlayerInBounds();

        for (Enemy enemy : enemies) {
            enemy.update(delta);

            if (enemy.getX() < mario.getX() + 256 / GameConfig.PPM) {
                enemy.setActive(true);
            }

            if (enemy.canBeRemoved()) {
                enemies.removeValue(enemy, true);
            }
        }

        for (Item item : items) {
            item.update(delta);

            if (item.canBeRemoved()) {
                items.removeValue(item, true);
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

    private void handleInput(float delta) {
        if (mario.getState() == Mario.State.DEAD) {
            return;
        }

        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);

        int pointer = 0;
        while (Gdx.input.isTouched(pointer)) {
            float x = Gdx.input.getX(pointer);

            x = x / Gdx.graphics.getWidth();

            if (x > 0.6) {
                upPressed = true;
            } else if (x < 0.2) {
                leftPressed = true;
            } else if (x < 0.4) {
                rightPressed = true;
            }
            pointer++;
        }

        mario.control(upPressed, leftPressed, rightPressed);
    }

    @Override
    public void render(float delta) {
        update(delta);

        GdxUtils.clearScreen(Color.BLACK);

        mapRenderer.render();
        if (GameConfig.DEBUG_MODE) {
            box2DDebugRenderer.render(world, camera.combined);
        }

        viewport.apply();
        SpriteBatch batch = getGame().getBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderGame(delta);
        batch.end();

        batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        renderHud();

        if (isGameOver()) {
            getGame().setScreen(new GameOverScreen(getGame()));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    private void renderHud() {
        // TODO hud viewport apply?
        hud.getStage().draw();
    }

    private void renderGame(float delta) {
        mario.draw(getGame().getBatch());

        for (Enemy enemy : enemies) {
            enemy.draw(getGame().getBatch());
        }

        for (Item item : items) {
            item.draw(getGame().getBatch());
        }
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
        hud.dispose();
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    private boolean isGameOver() {
        return mario.getState() == Mario.State.DEAD && mario.getStateTimer() > 3f;
    }
}
