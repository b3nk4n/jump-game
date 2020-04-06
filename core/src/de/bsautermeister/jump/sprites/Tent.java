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

public class Tent extends Sprite implements BinarySerializable {

    private final TextureAtlas atlas;
    private final float tentWidth;
    private final Rectangle goal;

    private boolean open;
    private float wabbleTime;

    public Tent(TextureAtlas atlas, Rectangle goal) {
        this.atlas = atlas;
        this.goal = goal;
        setRegion(atlas.findRegion(RegionNames.TENT_CLOSED));

        tentWidth = getRegionWidth() / Cfg.PPM;
        setBounds(goal.getX() + (goal.getWidth() - tentWidth) / 2f, goal.getY(),
                tentWidth, getRegionHeight() / 2 / Cfg.PPM);
    }

    public void update(float delta) {
        if (open) {
            wabbleTime += delta;

            float heightFactor  = 1.0f - (0.02f + MathUtils.sin(3f * wabbleTime) * 0.02f);
            setSize(tentWidth, getRegionHeight() * heightFactor / Cfg.PPM);
        }
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
        out.writeFloat(wabbleTime);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        open = in.readBoolean();
        wabbleTime = in.readFloat();

        if (open) {
            open();
        }
    }
}
