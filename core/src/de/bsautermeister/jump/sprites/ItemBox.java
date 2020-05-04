package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.screens.game.GameCallbacks;

public class ItemBox extends InteractiveTileObject {

    private static final int USED_BOX_IDX = 67;

    public enum Type {
        COIN,
        FOOD,
        BEER
    }

    private Type type;
    private int remainingItems;

    private final TextureAtlas atlas;

    private static TiledMapTileSet tileSet;

    private Array<ItemBoxFragment> activeFragments = new Array<>(16);
    private static Pool<ItemBoxFragment> fragmentPool = Pools.get(ItemBoxFragment.class);

    public ItemBox(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas, MapObject mapObject) {
        super(callbacks, Bits.ITEM_BOX, world, map, mapObject);
        this.atlas = atlas;
        tileSet = map.getTileSets().getTileSet("OctoberBro");
        Boolean multiCoin = (Boolean) mapObject.getProperties().get("multiCoin");
        Boolean food = (Boolean) mapObject.getProperties().get("food");
        Boolean beer = (Boolean) mapObject.getProperties().get("beer");
        if (multiCoin != null && multiCoin) {
            type = Type.COIN;
            remainingItems = 5;
        } else if (Boolean.TRUE.equals(food)) {
            type = Type.FOOD;
            remainingItems = 1;
        } else if (Boolean.TRUE.equals(beer)) {
            type = Type.BEER;
            remainingItems = 1;
        } else {
            type = Type.COIN;
            remainingItems = 1;
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        updateFragments(delta);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        drawFragments(batch);
    }

    private void drawFragments(SpriteBatch batch) {
        for (int i = 0; i < activeFragments.size; ++i) {
            ItemBoxFragment fragment = activeFragments.get(i);
            fragment.draw(batch);
        }
    }

    private void updateFragments(float delta) {
        for (int i = activeFragments.size; --i >= 0;) {
            ItemBoxFragment fragment = activeFragments.get(i);
            fragment.update(delta);

            if (!fragment.isAlive()) {
                activeFragments.removeIndex(i);
                fragmentPool.free(fragment);
            }
        }
    }

    @Override
    public void onHeadHit(Player player) {
        float xDistance = Math.abs(player.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < Cfg.BLOCK_SIZE / 2 / Cfg.PPM;
        getCallbacks().hit(
                player,
                this,
                new Vector2(getBody().getPosition().x, getBody().getPosition().y),
                closeEnough);

        if(closeEnough && !isBlank()) {
            // apply effect to objects on top
            for (String objectOnTop : getObjectsOnTop()) {
                getCallbacks().indirectObjectHit(this, objectOnTop);

                // ensure that object is un-registered from objects on top, because Box2D does not seem
                // to call endContact anymore
                steppedOff(objectOnTop);
            }

            remainingItems--;
            updateCellBlankState();
            if (isBlank()) {
                emitFragments();
            }
            bumpUp();
        }
    }

    public boolean isFoodBox() {
        return type == Type.FOOD;
    }

    public boolean isBeerBox() {
        return type == Type.BEER;
    }

    public boolean isBlank() {
        return remainingItems <= 0;
    }

    private void updateCellBlankState() {
        if (isBlank()) {
            getCell().setTile(
                    new DynamicTiledMapTile(
                            tileSet.getTile(USED_BOX_IDX)));

        }
    }

    private void emitFragments() {
        Vector2 pos = new Vector2();
        Vector2 velocity = new Vector2();
        ItemBoxFragment fragment0 = fragmentPool.obtain();
        fragment0.init(atlas, 0,
                getBounds().getPosition(pos)
                        .add(getBounds().width / 4f, getBounds().height * 3f / 4f),
                velocity.set(-0.33f, 1.0f), 180f);
        activeFragments.add(fragment0);
        ItemBoxFragment fragment1 = fragmentPool.obtain();
        fragment1.init(atlas, 1,
                getBounds().getPosition(pos)
                        .add(getBounds().width * 3f / 4f, getBounds().height * 3 / 4f),
                velocity.set(0.33f, 1.0f), -180f);
        activeFragments.add(fragment1);
        ItemBoxFragment fragment2 = fragmentPool.obtain();
        fragment2.init(atlas, 2,
                getBounds().getPosition(pos)
                        .add(getBounds().width / 4f, getBounds().height / 4f),
                velocity.set(-0.33f, 0.5f), 180f);
        activeFragments.add(fragment2);
        ItemBoxFragment fragment3 = fragmentPool.obtain();
        fragment3.init(atlas, 3,
                getBounds().getPosition(pos)
                        .add(getBounds().width *3f / 4f, getBounds().height / 4f),
                velocity.set(0.33f, 0.5f), -180f);
        activeFragments.add(fragment3);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeInt(remainingItems);
        out.writeUTF(type.name());
        out.writeInt(activeFragments.size);
        for (ItemBoxFragment fragment : activeFragments) {
            fragment.write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        remainingItems = in.readInt();
        type = Enum.valueOf(Type.class, in.readUTF());
        int numFragments = in.readInt();
        for (int i = 0; i < numFragments; ++i) {
            ItemBoxFragment brickFragment = new ItemBoxFragment();
            // TODO we actually might asign the wrong fragment-index here (which probably nobody will ever notice)
            brickFragment.init(atlas, i, Vector2.Zero, Vector2.Zero, 0f);
            brickFragment.read(in);
            activeFragments.add(brickFragment);
        }

        updateCellBlankState();
    }
}
