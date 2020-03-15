package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.screens.game.GameCallbacks;

public class GrilledChickenItem extends JumpingItem {

    public GrilledChickenItem(GameCallbacks callbacks, World world, TextureAtlas atlas, float x, float y) {
        super(callbacks, world, atlas, RegionNames.GRILLED_CHICKEN, x, y);
    }

    @Override
    protected void onCollect(Player player) {
        player.grow();
    }
}
