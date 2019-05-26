package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
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
    private final RectangleMapObject mapObject;
    private final Rectangle bounds;
    private final Body body;

    private final ObjectSet<Enemy> enemiesOnTop;

    private float bumpUpAnimationTimer;
    private final Interpolation bumpUpInterpolation = Interpolation.linear;
    private static final float BUMP_UP_ANIMATION_TIME = 0.25f;
    private final TiledMapTileLayer.Cell cell;

    public InteractiveTileObject(GameCallbacks callbacks, short categoryBit, World world, TiledMap map, MapObject mapObject) {
        this.callbacks = callbacks;
        this.world = world;
        this.mapObject = (RectangleMapObject)mapObject;
        this.bounds = this.mapObject.getRectangle();
        this.enemiesOnTop = new ObjectSet<Enemy>();
        this.body = defineBody(categoryBit);
        this.bumpUpAnimationTimer = BUMP_UP_ANIMATION_TIME;
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("graphics");
        this.cell =  layer.getCell((int)(body.getPosition().x * GameConfig.PPM / GameConfig.BLOCK_SIZE),
                ((int)(body.getPosition().y * GameConfig.PPM / GameConfig.BLOCK_SIZE)));
        cell.setTile(new DynamicTiledMapTile(cell.getTile()));
    }

    public void update(float delta) {
        bumpUpAnimationTimer += delta;

        float totalProgress = bumpUpAnimationTimer / BUMP_UP_ANIMATION_TIME;

        TiledMapTile tile = cell.getTile();
        if (tile != null) {
            float offset = 0f;
            if (totalProgress < 1f) {
                float animationProgress;
                if (totalProgress <= 0.5f) {
                    animationProgress = totalProgress * 2;
                } else {
                    animationProgress = 1.0f - (totalProgress * 2 - 1f);
                }
                offset = bumpUpInterpolation.apply(animationProgress) * 5f;
            }
            tile.setOffsetY(offset);
        }
    }

    public void bumpUp() {
        bumpUpAnimationTimer = 0;
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

    public World getWorld() {
        return world;
    }

    public MapObject getMapObject() {
        return mapObject;
    }

    public Body getBody() {
        return body;
    }

    public TiledMapTileLayer.Cell getCell() {
        return cell;
    }

    public GameCallbacks getCallbacks() {
        return callbacks;
    }

    public void enemySteppedOn(Enemy enemy) {
        if (!enemiesOnTop.contains(enemy)) {
            enemiesOnTop.add(enemy);
        }
    }

    public void enemySteppedOff(Enemy enemy) {
        if (enemiesOnTop.contains(enemy)) {
            enemiesOnTop.remove(enemy); // TODO step off not called when enemy was killed? When small mario hits the brick again, the kicked sound is played again
        }
    }

    public ObjectSet<Enemy> getEnemiesOnTop() {
        return enemiesOnTop;
    }
}
