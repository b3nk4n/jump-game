package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class InteractiveTileObject implements BinarySerializable {
    private String id;
    private final GameCallbacks callbacks;
    private final World world;
    private final RectangleMapObject mapObject;
    private final Rectangle bounds;
    private final Body body;

    private final ObjectSet<String> enemiesOnTop; // TODO are these two sets combinable? Yes!
    private final ObjectSet<String> itemsOnTop;

    private float bumpUpAnimationTimer;
    private final Interpolation bumpUpInterpolation = Interpolation.linear;
    private static final float BUMP_UP_ANIMATION_TIME = 0.25f;
    private final TiledMapTileLayer.Cell cell;

    public InteractiveTileObject(GameCallbacks callbacks, short categoryBit, World world, TiledMap map, MapObject mapObject) {
        this.callbacks = callbacks;
        this.world = world;
        this.mapObject = (RectangleMapObject)mapObject;
        Rectangle screenBounds = this.mapObject.getRectangle();
        this.bounds = new Rectangle(screenBounds.x / Cfg.PPM,
                screenBounds.y / Cfg.PPM,
                screenBounds.width / Cfg.PPM,
                screenBounds.height / Cfg.PPM);
        this.enemiesOnTop = new ObjectSet<String>();
        this.itemsOnTop = new ObjectSet<String>();
        this.body = defineBody(categoryBit);
        this.bumpUpAnimationTimer = BUMP_UP_ANIMATION_TIME;
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("graphics");
        int cellX = (int)(body.getPosition().x * Cfg.PPM / Cfg.BLOCK_SIZE);
        int cellY = ((int)(body.getPosition().y * Cfg.PPM / Cfg.BLOCK_SIZE));
        this.id = cellX + "|" + cellY;
        this.cell =  layer.getCell(cellX, cellY);
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

    public void draw(SpriteBatch batch) {
        // usually tile objects are rendered by the tilemap already
    }

    public void bumpUp() {
        bumpUpAnimationTimer = 0;
    }

    private Body defineBody(short categoryBit) {
        return WorldCreator.createBody(this, world, mapObject.getRectangle(), BodyDef.BodyType.StaticBody, categoryBit, false);
    }

    public abstract void onHeadHit(Mario mario);

    public void updateCategoryFilter(short filterBit) {
        Filter filter = new Filter();
        filter.categoryBits = filterBit;
        body.getFixtureList().get(0).setFilterData(filter);
    }

    public String getId() {
        return id;
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

    public void enemySteppedOn(String enemyId) {
        if (!enemiesOnTop.contains(enemyId)) {
            enemiesOnTop.add(enemyId);
        }
    }

    public void enemySteppedOff(String enemyId) {
        if (enemiesOnTop.contains(enemyId)) {
            enemiesOnTop.remove(enemyId);
        }
    }

    public ObjectSet<String> getEnemiesOnTop() {
        return enemiesOnTop;
    }

    public void itemSteppedOn(String itemId) {
        if (!itemsOnTop.contains(itemId)) {
            itemsOnTop.add(itemId);
        }
    }

    public void itemSteppedOff(String itemId) {
        if (itemsOnTop.contains(itemId)) {
            itemsOnTop.remove(itemId);
        }
    }

    public ObjectSet<String> getItemsOnTop() {
        return itemsOnTop;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeFloat(bumpUpAnimationTimer);
        out.writeInt(enemiesOnTop.size);
        for (String id : enemiesOnTop) {
            out.writeUTF(id);
        }
        out.writeInt(itemsOnTop.size);
        for (String id : itemsOnTop) {
            out.writeUTF(id);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        bumpUpAnimationTimer = in.readFloat();
        int numEnemies = in.readInt();
        for (int i = 0; i < numEnemies; ++i) {
            enemiesOnTop.add(in.readUTF());
        }
        int numItems = in.readInt();
        for (int i = 0; i < numItems; ++i) {
            itemsOnTop.add(in.readUTF());
        }
    }
}
