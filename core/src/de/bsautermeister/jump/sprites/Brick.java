package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class Brick extends InteractiveTileObject {
    public Brick(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, world, map, mapObject);
        setCategoryFilter(JumpGame.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        float xDistance = Math.abs(mario.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < GameConfig.BLOCK_SIZE / 2 / GameConfig.PPM;

        getCallbacks().hit(mario, this, closeEnough);

        if (closeEnough && mario.isBig()) {
            setCategoryFilter(JumpGame.DESTROYED_BIT);
            getCell().setTile(null);
        }
    }
}
