package de.bsautermeister.jump.physics;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.models.PlatformBouncer;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.ItemBox;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.sprites.enemies.DrunkenGuy;
import de.bsautermeister.jump.sprites.enemies.Enemy;
import de.bsautermeister.jump.sprites.enemies.Fish;
import de.bsautermeister.jump.sprites.enemies.Fox;
import de.bsautermeister.jump.sprites.enemies.Frog;
import de.bsautermeister.jump.sprites.enemies.Hedgehog;
import de.bsautermeister.jump.sprites.enemies.Raven;

public class WorldCreator {

    public static final String BG_IMG_GRASS1_KEY = "bgImageGrass1";
    public static final String BG_IMG_GRASS2_KEY = "bgImageGrass2";
    public static final String BG_IMG_FORREST1_KEY = "bgImageForrest1";
    public static final String BG_IMG_FORREST2_KEY = "bgImageForrest2";
    public static final String BG_IMG_MUNICH1_KEY = "bgImageMunich1";
    public static final String BG_IMG_MUNICH2_KEY = "bgImageMunich2";
    public static final String BG_IMG_CLOUDS1_KEY = "bgImageClouds1";
    public static final String BG_IMG_MOUNTAINS_KEY = "bgImageMountains";
    public static final String BG_IMG_CLOUDS2_KEY = "bgImageClouds2";
    public static final String BG_IMG_STATIC_KEY = "bgImageStatic";
    public static final String BG_TILES_KEY = "bgTiles";
    public static final String FG_TILES_KEY = "fgTiles";

    private static final String GROUND_KEY = "ground";
    private static final String BOXES_KEY = "boxes";
    private static final String BRICKS_KEY = "bricks";
    private static final String FOXES_KEY = "foxes";
    private static final String HEDGEHOGS_KEY = "hedgehogs";
    private static final String DRUNKEN_GUYS_KEY = "drunkenGuys";
    private static final String FISHES_KEY = "fishes";
    private static final String FROGS_KEY = "frogs";
    private static final String RAVENS_KEY = "ravens";
    private static final String COLLIDER_KEY = "collider";
    private static final String WATER_KEY = "water";
    private static final String GOAL_KEY = "goal";
    private static final String START_KEY = "start";
    private static final String BOUNCERS_KEY = "bouncers";
    private static final String PLATFORMS_KEY = "platforms";
    private static final String COINS_KEY = "coins";
    private static final String SPIKES_KEY = "spikes";
    private static final String POLES_KEY = "poles";
    private static final String SNORER_KEY = "snorer";
    private static final String ENEMY_SIGNAL_TRIGGERS_KEY = "enemySignalTriggers";

    private final World world;
    private final TiledMap map;
    private TextureAtlas atlas;
    private GameCallbacks callbacks;

    private Array<InteractiveTileObject> tileObjects = new Array<InteractiveTileObject>();

    public WorldCreator(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas) {
        this.callbacks = callbacks;
        this.world = world;
        this.map = map;
        this.atlas = atlas;
    }

    public void buildFromMap() {
        buildStaticLayer(GROUND_KEY, Bits.GROUND, false);
        buildStaticLayer(COLLIDER_KEY, Bits.COLLIDER, true);

        if (hasLayer(map, BRICKS_KEY)) {
            for (MapObject mapObject : map.getLayers().get(BRICKS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                tileObjects.add(new Brick(callbacks, world, map, atlas, mapObject));
            }
        }

        if (hasLayer(map, BOXES_KEY)) {
            for (MapObject mapObject : map.getLayers().get(BOXES_KEY).getObjects().getByType(RectangleMapObject.class)) {
                tileObjects.add(new ItemBox(callbacks, world, map, atlas, mapObject));
            }
        }
    }

    private void buildStaticLayer(String layer, short categoryBit, boolean asSensor) {
        if (!hasLayer(map, layer)) {
            return;
        }

        for (MapObject mapObject : map.getLayers().get(layer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle bounds = ((RectangleMapObject) mapObject).getRectangle();
            createBody(null, world, bounds, BodyDef.BodyType.StaticBody, categoryBit, asSensor);
        }
    }

    public static Body createBody(InteractiveTileObject parent, World world, Rectangle bounds, BodyDef.BodyType bodyType, short categoryBit, boolean asSensor) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        bodyDef.type = bodyType;
        bodyDef.position.set((bounds.getX() + bounds.getWidth() / 2) / Cfg.PPM,
                (bounds.getY() + bounds.getHeight() / 2) / Cfg.PPM);
        body = world.createBody(bodyDef);
        shape.setAsBox(bounds.getWidth() / 2 / Cfg.PPM,
                bounds.getHeight() / 2 / Cfg.PPM);
        fixtureDef.shape = shape;
        fixtureDef.friction = Cfg.GROUND_FRICTION;
        fixtureDef.filter.categoryBits = categoryBit;
        fixtureDef.isSensor = asSensor;
        Fixture fixture = body.createFixture(fixtureDef);
        if (parent != null) {
            fixture.setUserData(parent);
        }

        if (categoryBit == Bits.BRICK | categoryBit == Bits.ITEM_BOX) {
            EdgeShape topCornerShape = new EdgeShape();
            fixtureDef.shape = topCornerShape;
            fixtureDef.filter.categoryBits = Bits.BLOCK_TOP;
            fixtureDef.filter.maskBits = Bits.ENEMY | Bits.ITEM;
            fixtureDef.isSensor = true;
            topCornerShape.set(new Vector2(-6 / Cfg.PPM, 8.5f / Cfg.PPM),
                    new Vector2(6 / Cfg.PPM, 8.5f / Cfg.PPM));
            body.createFixture(fixtureDef).setUserData(parent);
        }

        return body;
    }

    public Array<Enemy> createEnemies() {
        Array<Enemy> enemies = new Array<>();
        if (hasLayer(map, FOXES_KEY)) {
            for (MapObject mapObject : map.getLayers().get(FOXES_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                Fox fox = new Fox(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM, rightDirection);
                fox.setGroup(group);
                enemies.add(fox);
            }
        }
        if (hasLayer(map, HEDGEHOGS_KEY)) {
            for (MapObject mapObject : map.getLayers().get(HEDGEHOGS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                Hedgehog hedgehog = new Hedgehog(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM, rightDirection);
                hedgehog.setGroup(group);
                enemies.add(hedgehog);
            }
        }
        if (hasLayer(map, DRUNKEN_GUYS_KEY)) {
            for (MapObject mapObject : map.getLayers().get(DRUNKEN_GUYS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                DrunkenGuy drunkenGuy = new DrunkenGuy(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM);
                drunkenGuy.setGroup(group);
                enemies.add(drunkenGuy);
            }
        }
        if (hasLayer(map, FISHES_KEY)) {
            for (MapObject mapObject : map.getLayers().get(FISHES_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                Float startDelay = (Float) mapObject.getProperties().get("startDelay");
                Integer startAngle = (Integer) mapObject.getProperties().get("startAngle");
                Float velocityFactor = (Float) mapObject.getProperties().get("velocityFactor");
                String group = (String) mapObject.getProperties().get("group");
                Fish fish = new Fish(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM);
                fish.setStartDelay(startDelay != null ? startDelay : 0f);
                fish.setStartAngle(startAngle != null ? startAngle : 90);
                fish.setVelocityFactor(velocityFactor != null ? velocityFactor : 1f);
                fish.setGroup(group);
                enemies.add(fish);
            }
        }
        if (hasLayer(map, FROGS_KEY)) {
            for (MapObject mapObject : map.getLayers().get(FROGS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                Frog frog = new Frog(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM, rightDirection);
                frog.setGroup(group);
                enemies.add(frog);
            }
        }
        if (hasLayer(map, RAVENS_KEY)) {
            for (MapObject mapObject : map.getLayers().get(RAVENS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                boolean swinging = mapObject.getProperties().get("swinging", false, Boolean.class);
                Raven raven = new Raven(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM, rightDirection, swinging);
                raven.setGroup(group);
                enemies.add(raven);
            }
        }
        return enemies;
    }

    public Array<Platform> createPlatforms() {
        Array<PlatformBouncer> bouncerRegions = getPlatformBouncerRegions();
        Array<Platform> platforms = new Array<>();

        if (hasLayer(map, PLATFORMS_KEY)) {
            for (RectangleMapObject mapObject : map.getLayers().get(PLATFORMS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = mapObject.getRectangle();
                Integer startAngle = (Integer) mapObject.getProperties().get("startAngle");
                Boolean breakable = (Boolean) mapObject.getProperties().get("breakable");
                Platform platform = new Platform(callbacks, world, atlas,
                        toPPM(rect),
                        startAngle != null ? startAngle : 0,
                        breakable != null ? breakable : false,
                        bouncerRegions);
                platforms.add(platform);
            }
        }
        return platforms;
    }

    public Array<Coin> createCoins() {
        Array<Coin> coins = new Array<>();
        if (hasLayer(map, COINS_KEY)) {
            for (RectangleMapObject mapObject : map.getLayers().get(COINS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = mapObject.getRectangle();
                coins.add(new Coin(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM));
            }
        }
        return coins;
    }

    public Array<Rectangle> getWaterRegions() {
        Array<Rectangle> waterRegions = new Array<>();
        if (hasLayer(map, WATER_KEY)) {
            for (RectangleMapObject mapObject : map.getLayers().get(WATER_KEY).getObjects().getByType(RectangleMapObject.class)) {
                waterRegions.add(toPPM(mapObject.getRectangle()));
            }
        }
        return waterRegions;
    }

    public Array<Rectangle> getPoleRegions() {
        Array<Rectangle> waterRegions = new Array<>();
        if (hasLayer(map, POLES_KEY)) {
            for (RectangleMapObject mapObject : map.getLayers().get(POLES_KEY).getObjects().getByType(RectangleMapObject.class)) {
                waterRegions.add(toPPM(mapObject.getRectangle()));
            }
        }
        return waterRegions;
    }

    public Rectangle getSnorerRegion() {
        if (hasLayer(map, SNORER_KEY)) {
            RectangleMapObject mapObject = map.getLayers().get(SNORER_KEY).getObjects().getByType(RectangleMapObject.class).first();
            return toPPM(mapObject.getRectangle());
        }
        return null;
    }

    public Array<Rectangle> getSpikeRegions() {
        Array<Rectangle> spikeRegions = new Array<>();
        if (hasLayer(map, SPIKES_KEY)) {
            for (RectangleMapObject mapObject : map.getLayers().get(SPIKES_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = toPPM(mapObject.getRectangle());;
                rect.setHeight(rect.getHeight() / 2);
                spikeRegions.add(rect);
            }
        }
        return spikeRegions;
    }

    private static Rectangle toPPM(Rectangle rect) {
        return new Rectangle(rect.x / Cfg.PPM, rect.y / Cfg.PPM,
                rect.width / Cfg.PPM, rect.height / Cfg.PPM);
    }

    private Array<PlatformBouncer> getPlatformBouncerRegions() {
        Array<PlatformBouncer> bouncerRegions = new Array<>();
        if (hasLayer(map, BOUNCERS_KEY)) {
            for (MapObject mapObject : map.getLayers().get(BOUNCERS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                Integer angle = (Integer) mapObject.getProperties().get("bounceAngle");
                PlatformBouncer platformBouncer = new PlatformBouncer(
                        toPPM(rect), angle != null ? angle : 0
                );
                bouncerRegions.add(platformBouncer);
            }
        }
        return bouncerRegions;
    }

    public Array<InteractiveTileObject> getTileObjects() {
        return tileObjects;
    }

    public Rectangle getGoal() {
        Rectangle rect = map.getLayers()
                .get(GOAL_KEY)
                .getObjects()
                .getByType(RectangleMapObject.class)
                .first()
                .getRectangle();
        return toPPM(rect);
    }

    public StartParams getStart() {
        RectangleMapObject rect = map.getLayers()
                .get(START_KEY)
                .getObjects()
                .getByType(RectangleMapObject.class)
                .first();
        return new StartParams(rect);
    }

    public Array<EnemySignalTrigger> getEnemySignalTriggers() {
        Array<EnemySignalTrigger> signalTriggers = new Array<>();
        if (hasLayer(map, ENEMY_SIGNAL_TRIGGERS_KEY)) {
            for (MapObject mapObject : map.getLayers().get(ENEMY_SIGNAL_TRIGGERS_KEY).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                signalTriggers.add(new EnemySignalTrigger(toPPM(rect), group));
            }
        }
        return signalTriggers;
    }

    private boolean hasLayer(Map map, String layer) {
        return map.getLayers().get(layer) != null;
    }

    public static class StartParams {
        public final Vector2 centerPosition;
        public final boolean leftDirection;

        public StartParams(RectangleMapObject mapObject) {
            Rectangle rect = mapObject.getRectangle();
            centerPosition = new Vector2((rect.x + rect.width / 2) / Cfg.PPM, (rect.y + rect.height / 2) / Cfg.PPM);
            leftDirection = mapObject.getProperties().get("leftDirection", false, Boolean.class);
        }
    }

    public static class EnemySignalTrigger {
        public final Rectangle rect;
        public final String group;

        public EnemySignalTrigger(Rectangle rect, String group) {
            this.rect = rect;
            this.group = group;
        }
    }
}
