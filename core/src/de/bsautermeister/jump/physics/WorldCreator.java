package de.bsautermeister.jump.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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

    private static final String GROUND_TYPE = "ground";
    private static final String BOX_TYPE = "box";
    private static final String BRICK_TYPE = "brick";
    private static final String FOX_TYPE = "fox";
    private static final String HEDGEHOG_TYPE = "hedgehog";
    private static final String DRUNKEN_GUY_TYPE = "drunkenGuy";
    private static final String FISH_TYPE = "fish";
    private static final String FROG_TYPE = "frog";
    private static final String RAVEN_TYPE = "raven";
    private static final String COLLIDER_TYPE = "collider";
    private static final String WATER_TYPE = "water";
    private static final String GOAL_TYPE = "goal";
    private static final String START_TYPE = "start";
    private static final String BOUNCER_TYPE = "bouncer";
    private static final String PLATFORM_TYPE = "platform";
    private static final String COIN_TYPE = "coin";
    private static final String SPIKE_TYPE = "spike";
    private static final String POLE_TYPE = "pole";
    private static final String SNORER_TYPE = "snorer";
    private static final String ENEMY_SIGNAL_TRIGGER_TYPE = "enemySignalTrigger";
    private static final String INFO_TYPE = "info";
    private static final String INFO_HELP_TYPE = "infoHelp";

    private static final String OBJECTS_LAYER = "objects";

    private final World world;
    private final TiledMap map;
    private TextureAtlas atlas;
    private GameCallbacks callbacks;

    private Array<InteractiveTileObject> tileObjects = new Array<>();

    public WorldCreator(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas) {
        this.callbacks = callbacks;
        this.world = world;
        this.map = map;
        this.atlas = atlas;
    }

    public void buildFromMap() {
        buildStaticObjects(GROUND_TYPE, Bits.GROUND, false);
        buildStaticObjects(COLLIDER_TYPE, Bits.COLLIDER, true);

        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, BRICK_TYPE)) {
                tileObjects.add(new Brick(callbacks, world, map, atlas, mapObject));
            } else if (isType(mapObject, BOX_TYPE)) {
                tileObjects.add(new ItemBox(callbacks, world, map, atlas, mapObject));
            }
        }
    }

    private void buildStaticObjects(String type, short categoryBit, boolean asSensor) {
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, type)) {
                createBody(null, world, mapObject.getRectangle(), BodyDef.BodyType.StaticBody,
                        categoryBit, asSensor);
            }
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

        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, FOX_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                Fox fox = new Fox(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM, rightDirection);
                fox.setGroup(group);
                enemies.add(fox);
            } else if (isType(mapObject, HEDGEHOG_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                Hedgehog hedgehog = new Hedgehog(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM, rightDirection);
                hedgehog.setGroup(group);
                enemies.add(hedgehog);
            } else if (isType(mapObject, DRUNKEN_GUY_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                DrunkenGuy drunkenGuy = new DrunkenGuy(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM);
                drunkenGuy.setGroup(group);
                enemies.add(drunkenGuy);
            } else if (isType(mapObject, FISH_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
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
            } else if (isType(mapObject, FROG_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                Frog frog = new Frog(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM, rightDirection);
                frog.setGroup(group);
                enemies.add(frog);
            } else if (isType(mapObject, RAVEN_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
                String group = (String) mapObject.getProperties().get("group");
                boolean rightDirection = mapObject.getProperties().get("rightDirection", false, Boolean.class);
                boolean swinging = mapObject.getProperties().get("swinging", false, Boolean.class);
                boolean autoAttack = mapObject.getProperties().get("autoAttack", true, Boolean.class);
                Raven raven = new Raven(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM,
                        rightDirection, swinging, autoAttack);
                raven.setGroup(group);
                enemies.add(raven);
            }
        }

        return enemies;
    }

    public Array<Platform> createPlatforms() {
        Array<PlatformBouncer> bouncerRegions = getPlatformBouncerRegions();
        Array<Platform> platforms = new Array<>();

        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, PLATFORM_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
                Integer startAngle = (Integer) mapObject.getProperties().get("startAngle");
                Boolean breakable = (Boolean) mapObject.getProperties().get("breakable");
                Float speed = (Float) mapObject.getProperties().get("speed");
                String group = (String) mapObject.getProperties().get("group");
                Platform platform = new Platform(callbacks, world, atlas,
                        toPPM(rect),
                        group,
                        startAngle != null ? startAngle : 0,
                        breakable != null ? breakable : false,
                        speed != null ? speed : Platform.DEFAULT_SPEED,
                        bouncerRegions);
                platforms.add(platform);
            }
        }
        return platforms;
    }

    public Array<Coin> createCoins() {
        Array<Coin> coins = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, COIN_TYPE)) {
                Rectangle rect = mapObject.getRectangle();
                coins.add(new Coin(callbacks, world, atlas,
                        rect.getX() / Cfg.PPM, rect.getY() / Cfg.PPM));
            }

        }
        return coins;
    }

    public Array<Rectangle> getWaterRegions() {
        Array<Rectangle> waterRegions = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, WATER_TYPE)) {
                waterRegions.add(toPPM(mapObject.getRectangle()));
            }
        }
        return waterRegions;
    }

    public Array<Rectangle> getPoleRegions() {
        Array<Rectangle> waterRegions = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, POLE_TYPE)) {
                waterRegions.add(toPPM(mapObject.getRectangle()));
            }
        }
        return waterRegions;
    }

    public Array<Rectangle> getSpikeRegions() {
        Array<Rectangle> spikeRegions = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, SPIKE_TYPE)) {
                Rectangle rect = toPPM(mapObject.getRectangle());
                rect.setHeight(rect.getHeight() / 2);
                spikeRegions.add(rect);
            }
        }
        return spikeRegions;
    }

    private Array<PlatformBouncer> getPlatformBouncerRegions() {
        Array<PlatformBouncer> bouncerRegions = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, BOUNCER_TYPE)) {
                Integer angle = (Integer) mapObject.getProperties().get("bounceAngle");
                Float speed = (Float) mapObject.getProperties().get("speed");
                PlatformBouncer platformBouncer = new PlatformBouncer(
                        toPPM(mapObject.getRectangle()),
                        angle != null ? angle : 0,
                        speed != null ? speed : Platform.DEFAULT_SPEED
                );
                bouncerRegions.add(platformBouncer);
            }
        }
        return bouncerRegions;
    }

    public Array<InteractiveTileObject> getTileObjects() {
        return tileObjects;
    }

    public Rectangle getSnorerRegion() {
        return toPPM(getFirstRectObject(map, SNORER_TYPE).getRectangle());
    }

    public Rectangle getGoal() {
        return toPPM(getFirstRectObject(map, GOAL_TYPE).getRectangle());
    }

    public StartParams getStart() {
        return new StartParams(getFirstRectObject(map, START_TYPE));
    }

    public Array<EnemySignalTrigger> getEnemySignalTriggers() {
        Array<EnemySignalTrigger> signalTriggers = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, ENEMY_SIGNAL_TRIGGER_TYPE)) {
                String group = (String) mapObject.getProperties().get("group");
                signalTriggers.add(new EnemySignalTrigger(toPPM(mapObject.getRectangle()), group));
            }
        }
        return signalTriggers;
    }

    public Array<InfoRect> getInfoSigns() {
        Array<InfoRect> infos = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, INFO_TYPE)) {
                String languageKey = (String) mapObject.getProperties().get("languageKey");
                infos.add(new InfoRect(toPPM(mapObject.getRectangle()), languageKey));
            }
        }
        return infos;
    }

    public Array<InfoRect> getInfoHelps() {
        Array<InfoRect> helps = new Array<>();
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, INFO_HELP_TYPE)) {
                String languageKey = (String) mapObject.getProperties().get("languageKey");
                helps.add(new InfoRect(toPPM(mapObject.getRectangle()), languageKey));
            }
        }
        return helps;
    }

    public static class StartParams {
        public final Vector2 centerPosition;
        public final boolean leftDirection;

        StartParams(RectangleMapObject mapObject) {
            Rectangle rect = mapObject.getRectangle();
            centerPosition = new Vector2((rect.x + rect.width / 2) / Cfg.PPM, (rect.y + rect.height / 2) / Cfg.PPM);
            leftDirection = mapObject.getProperties().get("leftDirection", false, Boolean.class);
        }
    }

    public static class EnemySignalTrigger {
        public final Rectangle rect;
        public final String group;

        EnemySignalTrigger(Rectangle rect, String group) {
            this.rect = rect;
            this.group = group;
        }
    }

    public static class InfoRect {
        public final Rectangle rect;
        public final String languageKey;

        InfoRect(Rectangle rect, String languageKey) {
            this.rect = rect;
            this.languageKey = languageKey;
        }
    }

    private static boolean isType(MapObject mapObject, String type) {
        String objType = (String) mapObject.getProperties().get("type");
        if (objType == null) {
            Gdx.app.log("WorldCreator", String.format("Missing object type in x: %f, y: %f",
                    mapObject.getProperties().get("x"),
                    mapObject.getProperties().get("y")));
        }
        return objType.equals(type);
    }

    private static Array<RectangleMapObject> getRectObjects(TiledMap map) {
        return map.getLayers().get(OBJECTS_LAYER).getObjects().getByType(RectangleMapObject.class);
    }

    private static RectangleMapObject getFirstRectObject(TiledMap map, String type) {
        for (RectangleMapObject mapObject : getRectObjects(map)) {
            if (isType(mapObject, type)) {
                return mapObject;
            }
        }
        return null;
    }

    private static Rectangle toPPM(Rectangle rect) {
        return new Rectangle(rect.x / Cfg.PPM, rect.y / Cfg.PPM,
                rect.width / Cfg.PPM, rect.height / Cfg.PPM);
    }
}
