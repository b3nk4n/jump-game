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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class InteractiveTileObject implements BinarySerializable {
    private String id;
    private final GameCallbacks callbacks;
    private final World world;
    private final RectangleMapObject mapObject;
    private final Rectangle bounds;
    private final Body body;

    private final ObjectSet<String> objectOnTop;

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
        this.objectOnTop = new ObjectSet<String>();
        this.body = defineBody(categoryBit);
        this.bumpUpAnimationTimer = BUMP_UP_ANIMATION_TIME;
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("fgTiles");
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

    public abstract void onHeadHit(Player player);

    protected void updateMaskFilter(short filterBit) {
        Filter filter = new Filter();
        filter.maskBits = filterBit;
        for (Fixture fixture : getBody().getFixtureList()) {
            filter.categoryBits = fixture.getFilterData().categoryBits;
            fixture.setFilterData(filter);
        }
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

    public void steppedOn(String enemyId) {
        if (!objectOnTop.contains(enemyId)) {
            objectOnTop.add(enemyId);
        }
    }

    public void steppedOff(String enemyId) {
        if (objectOnTop.contains(enemyId)) {
            objectOnTop.remove(enemyId);
        }
    }

    public ObjectSet<String> getObjectsOnTop() {
        return objectOnTop;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeFloat(bumpUpAnimationTimer);
        out.writeInt(objectOnTop.size);
        for (String id : objectOnTop) {
            out.writeUTF(id);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        bumpUpAnimationTimer = in.readFloat();
        int numObjects = in.readInt();
        for (int i = 0; i < numObjects; ++i) {
            objectOnTop.add(in.readUTF());
        }
    }
}
