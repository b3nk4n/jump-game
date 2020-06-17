package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.screens.game.GameCallbacks;

public class BeerItem extends JumpingItem {

    public BeerItem(GameCallbacks callbacks, World world, TextureAtlas atlas, float x, float y) {
        super(callbacks, world, atlas, RegionNames.BEER, x, y, true);
        setRegion(atlas.findRegion(RegionNames.BEER), 0, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
    }

    @Override
    protected void onCollect(Player player) {
        player.drunk();
    }
}
