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
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;

public class Coin extends InteractiveTileObject {

    private final static int BLANK_COIN_IDX = 28;

    private boolean blank;

    private static TiledMapTileSet tileSet;

    public Coin(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, JumpGame.COIN_BIT, world, map, mapObject);

        tileSet = map.getTileSets().getTileSet("tileset");
    }

    @Override
    public void onHeadHit(Mario mario) {
        float xDistance = Math.abs(mario.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < Cfg.BLOCK_SIZE / 2 / Cfg.PPM;
        getCallbacks().hit(
                mario,
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

            setBlank();
            bumpUp();
        }
    }

    public boolean hasMushroom() {
        return getMapObject().getProperties().containsKey("mushroom");
    }

    public boolean isBlank() {
        return blank;
    }

    private void setBlank() {
        blank = true;
        getCell().setTile(
                new DynamicTiledMapTile(
                        tileSet.getTile(BLANK_COIN_IDX)));
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeBoolean(blank);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        blank = in.readBoolean();

        if (blank) {
            setBlank();
        }
    }
}
