package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.scenes.Hud;

public class Brick extends InteractiveTileObject {
    public Brick(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, world, map, mapObject);
        setCategoryFilter(JumpGame.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        if (mario.isBig()) {
            setCategoryFilter(JumpGame.DESTROYED_BIT);
            getCell().setTile(null);
            JumpGame.assetManager.get("audio/sounds/breakblock.wav", Sound.class).play();
        } else {
            JumpGame.assetManager.get("audio/sounds/bump.wav", Sound.class).play();
        }
    }
}
