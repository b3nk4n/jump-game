package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.physics.WorldCreator;

public abstract class InteractiveTileObject {
    private final GameCallbacks callbacks;
    private final World world;
    private final TiledMap map;
    private TiledMapTile tile;
    private final MapObject mapObject;
    private final Rectangle bounds;
    private final Body body;

    private final ObjectSet<Enemy> enemiesOnTop;

    public InteractiveTileObject(GameCallbacks callbacks, short categoryBit, World world, TiledMap map, MapObject mapObject) {
        this.callbacks = callbacks;
        this.world = world;
        this.map = map;
        this.mapObject = mapObject;
        this.bounds = ((RectangleMapObject)mapObject).getRectangle();
        this.enemiesOnTop = new ObjectSet<Enemy>();
        this.body = defineBody(categoryBit);
    }

    private Body defineBody(short categoryBit) {
        return WorldCreator.createBody(this, world, bounds, BodyDef.BodyType.StaticBody, categoryBit);
    }

    public abstract void onHeadHit(Mario mario);

    public void updateCategoryFilter(short filterBit) {
        Filter filter = new Filter();
        filter.categoryBits = filterBit;
        body.getFixtureList().get(0).setFilterData(filter);
    }

    public TiledMapTileLayer.Cell getCell() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("graphics");
        return layer.getCell((int)(body.getPosition().x * GameConfig.PPM / GameConfig.BLOCK_SIZE),
                ((int)(body.getPosition().y * GameConfig.PPM / GameConfig.BLOCK_SIZE)));
    }

    public World getWorld() {
        return world;
    }

    public MapObject getMapObject() {
        return mapObject;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Body getBody() {
        return body;
    }

    public GameCallbacks getCallbacks() {
        return callbacks;
    }

    public void stepOnBlock(Enemy enemy) {
        if (!enemiesOnTop.contains(enemy)) {
            enemiesOnTop.add(enemy);
        }
    }

    public void stepOffBlock(Enemy enemy) {
        if (enemiesOnTop.contains(enemy)) {
            enemiesOnTop.remove(enemy);
        }
    }

    public ObjectSet<Enemy> getEnemiesOnTop() {
        return enemiesOnTop;
    }
}
