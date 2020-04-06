package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class Pole extends Sprite {
    public Pole(TextureAtlas atlas, Rectangle goal) {
        setRegion(atlas.findRegion(RegionNames.POLE));

        float regionWidth = getRegionWidth() / Cfg.PPM;
        setBounds(goal.getX() + (goal.getWidth() - regionWidth) / 2f, goal.getY(),
                regionWidth, getRegionHeight() / Cfg.PPM);
    }
}
