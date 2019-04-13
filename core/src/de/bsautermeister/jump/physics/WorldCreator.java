package de.bsautermeister.jump.physics;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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

    private final World world;
    private final TiledMap map;

    private final Array<Goomba> goombas;
    private final Array<Koopa> koopas;

    public WorldCreator(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas) {
        this.world = world;
        this.map = map;

        buildPhysicalLayer("ground", BodyDef.BodyType.StaticBody, JumpGame.GROUND_BIT);
        buildPhysicalLayer("pipes", BodyDef.BodyType.StaticBody, JumpGame.OBJECT_BIT);

        for (MapObject mapObject : map.getLayers().get("bricks").getObjects().getByType(RectangleMapObject.class)) {
            new Brick(callbacks, world, map, mapObject);
        }

        for (MapObject mapObject : map.getLayers().get("coins").getObjects().getByType(RectangleMapObject.class)) {
            new Coin(callbacks, world, map, mapObject);
        }

        goombas = new Array<Goomba>();
        for (MapObject mapObject : map.getLayers().get("goombas").getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
            goombas.add(new Goomba(world, map, atlas, rect.getX() / GameConfig.PPM, rect.getY() / GameConfig.PPM));
        }

        koopas = new Array<Koopa>();
        for (MapObject mapObject : map.getLayers().get("koopas").getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
            koopas.add(new Koopa(world, map, atlas, rect.getX() / GameConfig.PPM, rect.getY() / GameConfig.PPM));
        }
    }

    private void buildPhysicalLayer(String layer, BodyDef.BodyType bodyType, short categoryBit) {
        for (MapObject mapObject : map.getLayers().get(layer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle bounds = ((RectangleMapObject) mapObject).getRectangle();
            createBody(null, world, bounds, bodyType, categoryBit);
        }
    }

    public static Body createBody(InteractiveTileObject parent, World world, Rectangle bounds, BodyDef.BodyType bodyType) {
        return createBody(parent, world, bounds, bodyType, JumpGame.GROUND_BIT);
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
            fixture.setUserData(parent); // TODO refactor this (Part 12: 6min)
        }
        return body;
    }

    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(koopas);
        return enemies;
    }
}
