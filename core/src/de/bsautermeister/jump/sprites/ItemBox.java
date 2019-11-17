package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.physics.Bits;

public class ItemBox extends InteractiveTileObject {

    public enum Type {
        COIN,
        MUSHROOM,
        BEER
    }

    private Type type;
    private int remainingItems;

    private static TiledMapTileSet tileSet;

    public ItemBox(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, Bits.ITEM_BOX, world, map, mapObject);
        tileSet = map.getTileSets().getTileSet("tileset");
        Boolean multiCoin = (Boolean) mapObject.getProperties().get("multi_coin");
        Boolean mushroom = (Boolean) mapObject.getProperties().get("mushroom");
        Boolean beer = (Boolean) mapObject.getProperties().get("beer");
        if (multiCoin != null && multiCoin) {
            type = Type.COIN;
            remainingItems = 5;
        } else if (Boolean.TRUE.equals(mushroom)) {
            type = Type.MUSHROOM;
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
            bumpUp();
        }
    }

    public boolean isMushroomBox() {
        return type == Type.MUSHROOM;
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
                            tileSet.getTile(Cfg.BLANK_COIN_IDX)));
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeInt(remainingItems);
        out.writeUTF(type.name());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        remainingItems = in.readInt();
        type = Enum.valueOf(Type.class, in.readUTF());

        updateCellBlankState();
    }
}
