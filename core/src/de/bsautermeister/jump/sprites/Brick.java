package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class Brick extends InteractiveTileObject {
    public Brick(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, JumpGame.BRICK_BIT, world, map, mapObject);
    }

    @Override
    public void onHeadHit(Mario mario) {
        float xDistance = Math.abs(mario.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < GameConfig.BLOCK_SIZE / 2 / GameConfig.PPM;

        getCallbacks().hit(mario, this, closeEnough);

        if (closeEnough) {
            // kill enemies on top
            for (Enemy enemyOnTop : getEnemiesOnTop()) {
                enemyOnTop.kill(true);

                // ensure that enemy is un-registered from enemies on top, because Box2D does not seem
                // to call endContact anymore
                enemySteppedOff(enemyOnTop);
            }

            // reverse items on top
            for (Item itemOnTop : getItemsOnTop()) {
                itemOnTop.reverseVelocity(true, false);
                itemOnTop.bounceUp();

                // ensure that item is un-registered from items on top, because Box2D does not seem
                // to call endContact anymore
                itemSteppedOff(itemOnTop);
            }

            if (mario.isBig()) {
                // remove brick
                updateCategoryFilter(JumpGame.DESTROYED_BIT);
                getCell().setTile(null);
            } else {
                bumpUp();
            }
        }
    }
}
