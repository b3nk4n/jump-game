package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class Tent extends Sprite implements BinarySerializable {

    private final TextureAtlas atlas;
    private final Rectangle goal;
    private boolean open;

    public Tent(TextureAtlas atlas, Rectangle goal) {
        this.atlas = atlas;
        this.goal = goal;
        setRegion(atlas.findRegion(RegionNames.TENT_CLOSED));

        float tentWidth = getRegionWidth() / Cfg.PPM;
        setBounds(goal.getX() + (goal.getWidth() - tentWidth) / 2f, goal.getY(),
                tentWidth, getRegionHeight() / Cfg.PPM);
    }

    public void open() {
        open = true;
        setRegion(atlas.findRegion(RegionNames.TENT_OPEN));
    }

    public boolean isEntering(Player player) {
        return open && Intersector.overlaps(goal, player.getBoundingRectangle()) && player.getBody().getLinearVelocity().len2() < 0.001f;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeBoolean(open);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        open = in.readBoolean();
    }
}
