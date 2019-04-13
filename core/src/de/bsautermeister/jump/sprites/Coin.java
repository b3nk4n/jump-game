package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.scenes.Hud;

public class Coin extends InteractiveTileObject {

    private final static int BLANK_COIN_IDX = 28;

    private static TiledMapTileSet tileSet;

    public Coin(GameCallbacks callbacks, World world, TiledMap map, MapObject mapObject) {
        super(callbacks, world, map, mapObject);
        setCategoryFilter(JumpGame.COIN_BIT);

        tileSet = map.getTileSets().getTileSet("tileset");
    }

    @Override
    public void onHeadHit(Mario mario) {
        Sound sound;
        if (getCell().getTile().getId() == BLANK_COIN_IDX) {
            sound = JumpGame.assetManager.get("audio/sounds/bump.wav", Sound.class); // TODO is this sound ever played? We play it from Brick class
        } else {
            if (getMapObject().getProperties().containsKey("mushroom")) {
                getCallbacks().coinHit(
                        new Vector2(getBody().getPosition().x, getBody().getPosition().y + 16 / GameConfig.PPM));
                sound = JumpGame.assetManager.get("audio/sounds/powerup_spawn.wav", Sound.class);
            } else {
                sound = JumpGame.assetManager.get("audio/sounds/coin.wav", Sound.class);
            }
        }
        sound.play();

        getCell().setTile(tileSet.getTile(BLANK_COIN_IDX));
        Hud.addScore(100);
    }
}
