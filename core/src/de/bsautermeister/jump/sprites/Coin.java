package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class Coin extends InteractiveTileObject {

    private final static int BLANK_COIN_IDX = 28;

    private static TiledMapTileSet tileSet;

    public Coin(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, JumpGame.COIN_BIT, world, map, mapObject);

        tileSet = map.getTileSets().getTileSet("tileset");
    }

    @Override
    public void onHeadHit(Mario mario) {
        float xDistance = Math.abs(mario.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < GameConfig.BLOCK_SIZE / 2 / GameConfig.PPM;
        getCallbacks().hit(
                mario,
                this,
                new Vector2(getBody().getPosition().x, getBody().getPosition().y + GameConfig.BLOCK_SIZE / GameConfig.PPM),
                closeEnough);

        if(closeEnough && !isBlank()) {
            // kill enemies on top
            for (Enemy enemyOnTop : getEnemiesOnTop()) {
                enemyOnTop.kill(true);
            }

            setBlank();
            bumpUp();
        }
    }

    public boolean hasMushroom() {
        return getMapObject().getProperties().containsKey("mushroom");
    }

    public boolean isBlank() {
        return getCell().getTile().getId() == BLANK_COIN_IDX;
    }

    private void setBlank() {
        getCell().setTile(
                new DynamicTiledMapTile(
                        tileSet.getTile(BLANK_COIN_IDX)));
    }
}
