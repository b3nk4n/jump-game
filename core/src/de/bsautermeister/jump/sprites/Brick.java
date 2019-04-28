package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;

public class Brick extends InteractiveTileObject {
    public Brick(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, world, map, mapObject);
        setCategoryFilter(JumpGame.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        getCallbacks().hit(mario, this);

        if (mario.isBig()) {
            setCategoryFilter(JumpGame.DESTROYED_BIT);
            getCell().setTile(null);
        }
    }
}
