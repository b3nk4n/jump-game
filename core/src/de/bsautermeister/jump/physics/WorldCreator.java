package de.bsautermeister.jump.physics;

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

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.Goomba;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Koopa;

public class WorldCreator {

    public static final String BACKGROUND_KEY = "backgroundColor";
    public static final String GRAPHICS_KEY = "graphics";

    public static final String GROUND_KEY = "ground";
    public static final String PIPES_KEY = "pipes";
    public static final String COINS_KEY = "coins";
    public static final String BRICKS_KEY = "bricks";
    public static final String GOOMBAS_KEY = "goombas";
    public static final String KOOPAS_KEY = "koopas";
    public static final String COLLIDER_KEY = "collider";

    private final World world;
    private final TiledMap map;
    private TextureAtlas atlas;
    private GameCallbacks callbacks;

    private static Array<InteractiveTileObject> tileObjects = new Array<InteractiveTileObject>();

    public WorldCreator(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas) {
        this.callbacks = callbacks;
        this.world = world;
        this.map = map;
        this.atlas = atlas;

        buildPhysicalLayer(GROUND_KEY, BodyDef.BodyType.StaticBody, JumpGame.GROUND_BIT);
        buildPhysicalLayer(PIPES_KEY, BodyDef.BodyType.StaticBody, JumpGame.OBJECT_BIT);
        buildPhysicalLayer(COLLIDER_KEY, BodyDef.BodyType.StaticBody, JumpGame.COLLIDER_BIT);

        for (MapObject mapObject : map.getLayers().get(BRICKS_KEY).getObjects().getByType(RectangleMapObject.class)) {
            tileObjects.add(new Brick(callbacks, world, map, mapObject));
        }

        for (MapObject mapObject : map.getLayers().get(COINS_KEY).getObjects().getByType(RectangleMapObject.class)) {
            tileObjects.add(new Coin(callbacks, world, map, mapObject));
        }
    }

    private void buildPhysicalLayer(String layer, BodyDef.BodyType bodyType, short categoryBit) {
        for (MapObject mapObject : map.getLayers().get(layer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle bounds = ((RectangleMapObject) mapObject).getRectangle();
            createBody(null, world, bounds, bodyType, categoryBit);
        }
    }

    public static Body createBody(InteractiveTileObject parent, World world, Rectangle bounds, BodyDef.BodyType bodyType, short categoryBit) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        bodyDef.type = bodyType;
        bodyDef.position.set((bounds.getX() + bounds.getWidth() / 2) / GameConfig.PPM,
                (bounds.getY() + bounds.getHeight() / 2) / GameConfig.PPM);
        body = world.createBody(bodyDef);
        shape.setAsBox(bounds.getWidth() / 2 / GameConfig.PPM,
                bounds.getHeight() / 2 / GameConfig.PPM);
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = categoryBit;
        Fixture fixture = body.createFixture(fixtureDef);
        if (parent != null) {
            fixture.setUserData(parent);
        }

        if (categoryBit == JumpGame.BRICK_BIT | categoryBit == JumpGame.COIN_BIT) {
            EdgeShape topCornerShape = new EdgeShape();
            fixtureDef.shape = topCornerShape;
            fixtureDef.filter.categoryBits = JumpGame.BLOCK_TOP_BIT;
            fixtureDef.filter.maskBits = JumpGame.ENEMY_BIT | JumpGame.ITEM_BIT;
            fixtureDef.isSensor = true;
            topCornerShape.set(new Vector2(-6 / GameConfig.PPM, 8.5f / GameConfig.PPM),
                    new Vector2(6 / GameConfig.PPM, 8.5f / GameConfig.PPM));
            body.createFixture(fixtureDef).setUserData(parent);
        }

        return body;
    }

    public Array<Enemy> createEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        for (MapObject mapObject : map.getLayers().get(GOOMBAS_KEY).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
            enemies.add(new Goomba(callbacks, world, atlas,
                    rect.getX() / GameConfig.PPM, rect.getY() / GameConfig.PPM));
        }

        for (MapObject mapObject : map.getLayers().get(KOOPAS_KEY).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
            enemies.add(new Koopa(callbacks, world, atlas,
                    rect.getX() / GameConfig.PPM, rect.getY() / GameConfig.PPM));
        }
        return enemies;
    }

    public static Array<InteractiveTileObject> getTileObjects() {
        return tileObjects;
    }
}
