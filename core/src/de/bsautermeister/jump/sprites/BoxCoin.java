package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class BoxCoin extends Sprite implements BinarySerializable {
    private final Vector2 spawnPosition;
    private Animation<TextureRegion> spinningAnimation;
    private float stateTime;
    private static final float ANIMATION_TIME = 0.5f;
    private static final float ANIMATION_OFFSET_Y = 4 * Cfg.BLOCK_SIZE_PPM;
    private final Interpolation bumpUpInterpolation = Interpolation.linear;

    public BoxCoin(TextureAtlas atlas, Vector2 position) {
        spinningAnimation = new Animation<TextureRegion>(0.1f, atlas.findRegions(RegionNames.COIN), Animation.PlayMode.LOOP);
        spawnPosition = new Vector2(position.x - (Cfg.BLOCK_SIZE / 2f / Cfg.PPM),
                position.y - (Cfg.BLOCK_SIZE / 2f / Cfg.PPM));
        setBounds(0, 0, Cfg.BLOCK_SIZE_PPM, Cfg.BLOCK_SIZE_PPM);
        setPosition(spawnPosition.x, spawnPosition.y);
        stateTime = 0;
        setRegion(spinningAnimation.getKeyFrame(stateTime));
    }

    public void update(float delta) {
        stateTime += delta;

        setRegion(spinningAnimation.getKeyFrame(stateTime, true));

        float totalProgress = getProgress();

        float offset = 0f;
        if (totalProgress < 1f) {
            float animationProgress;
            if (totalProgress <= 0.5f) {
                animationProgress = totalProgress * 2;
            } else {
                animationProgress = 1.0f - (totalProgress * 2 - 1f);
            }
            offset = bumpUpInterpolation.apply(animationProgress) * ANIMATION_OFFSET_Y;
        }
        setY(spawnPosition.y + offset);
    }

    private float getProgress() {
        return stateTime / ANIMATION_TIME;
    }

    public boolean isFinished() {
        // finish before it actually reaches its starting position
        return getProgress() > 0.8f;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(spawnPosition.x);
        out.writeFloat(spawnPosition.y);
        out.writeFloat(stateTime);
        out.writeFloat(getX());
        out.writeFloat(getY());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        spawnPosition.set(in.readFloat(), in.readFloat());
        stateTime = in.readFloat();
        setX(in.readFloat());
        setY(in.readFloat());
    }
}
