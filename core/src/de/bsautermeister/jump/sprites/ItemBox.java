package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.effects.SimpleFragmentEffect;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.screens.game.GameCallbacks;

public class ItemBox extends InteractiveTileObject {

    private static final int USED_BOX_IDX = 67;

    public enum Type {
        COIN,
        FOOD,
        FORCED_PRETZEL,
        BEER,
        FOOD_IF_SMALL
    }

    private Type type;
    private int remainingItems;

    private static TiledMapTileSet tileSet;

    private final SimpleFragmentEffect simpleFragmentEffect;

    private boolean spotted;

    public ItemBox(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas, MapObject mapObject) {
        super(callbacks, Bits.ITEM_BOX, world, map, mapObject);

        tileSet = map.getTileSets().getTileSet("OctoberBro");
        Boolean coin = (Boolean) mapObject.getProperties().get("coin");
        Boolean multiCoin = (Boolean) mapObject.getProperties().get("multiCoin");
        Boolean food = (Boolean) mapObject.getProperties().get("food");
        Boolean foodIfSmall = (Boolean) mapObject.getProperties().get("foodIfSmall");
        Boolean pretzel = (Boolean) mapObject.getProperties().get("pretzel");
        Boolean beer = (Boolean) mapObject.getProperties().get("beer");
        if (multiCoin != null && multiCoin) {
            type = Type.COIN;
            remainingItems = 5;
        } else if (Boolean.TRUE.equals(food)) {
            type = Type.FOOD;
            remainingItems = 1;
        } else if (Boolean.TRUE.equals(foodIfSmall)) {
            type = Type.FOOD_IF_SMALL;
            remainingItems = 1;
        } else if (Boolean.TRUE.equals(pretzel)) {
            type = Type.FORCED_PRETZEL;
            remainingItems = 1;
        } else if (Boolean.TRUE.equals(beer)) {
            type = Type.BEER;
            remainingItems = 1;
        } else if (Boolean.TRUE.equals(coin)) {
            type = Type.COIN;
            remainingItems = 1;
        } else {
            type = Type.COIN;
            remainingItems = 0;
            updateCellBlankState();
        }

        simpleFragmentEffect = new SimpleFragmentEffect(atlas, RegionNames.BOX_FRAGMENT_TPL);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        simpleFragmentEffect.update(delta);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        simpleFragmentEffect.draw(batch);
    }

    public void isInCameraView() {
        if (!spotted) {
            spotted = true;
            getCallbacks().spotted(this);
        }
    }

    @Override
    public void onHeadHit(Player player) {
        float xDistance = Math.abs(player.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < Cfg.BLOCK_SIZE / 2f / Cfg.PPM;
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
                simpleFragmentEffect.emit(getBounds());
            }
            bumpUp();
        }
    }

    public boolean isFoodBox() {
        return type == Type.FOOD;
    }

    public boolean isFoodIfSmallBox() {
        return type == Type.FOOD_IF_SMALL;
    }

    public boolean isForcedPretzelBox() {
        return type == Type.FORCED_PRETZEL;
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

    public boolean isSpotted() {
        return spotted;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeInt(remainingItems);
        out.writeUTF(type.name());
        simpleFragmentEffect.write(out);
        out.writeBoolean(spotted);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        remainingItems = in.readInt();
        type = Enum.valueOf(Type.class, in.readUTF());
        simpleFragmentEffect.read(in);
        spotted = in.readBoolean();

        updateCellBlankState();
    }
}
