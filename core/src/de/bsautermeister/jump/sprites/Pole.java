package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;

public class Pole extends Sprite {
    public Pole(TextureAtlas atlas, Rectangle goal) {
        setRegion(atlas.findRegion(RegionNames.POLE));

        float regionWidth = getRegionWidth() / Cfg.PPM;
        setBounds(goal.getX() + (goal.getWidth() - regionWidth) / 2f, goal.getY(),
                regionWidth, getRegionHeight() / Cfg.PPM);
    }
}
