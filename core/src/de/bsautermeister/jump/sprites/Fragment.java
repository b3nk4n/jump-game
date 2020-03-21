package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class Fragment extends Sprite implements Pool.Poolable, BinarySerializable {

    private final Vector2 velocity = new Vector2();
    private boolean alive;
    private float rotationSpeed;
    private final String templateName;

    public Fragment(String templateName) {
        this.templateName = templateName;
    }

    public void init(TextureAtlas atlas, int templateIndex, Vector2 centerPosition, Vector2 velocity,
                     float rotationSpeed) {
        setRegion(atlas.findRegion(RegionNames.fromTemplate(templateName, templateIndex)));
        setSize(8f / Cfg.PPM, 8f / Cfg.PPM);
        setCenter(centerPosition.x, centerPosition.y);
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        this.velocity.set(velocity);
        this.rotationSpeed = rotationSpeed;
        reset();
    }

    @Override
    public void reset() {
        // reset is called when object is freed, not when the object is obtained
        alive = true;
        setRotation(0f);
    }

    public void update(float delta) {
        velocity.set(velocity.x * 0.99f, velocity.y - 0.05f);
        setPosition(getX() + velocity.x * delta, getY() + velocity.y * delta);
        setRotation(getRotation() + rotationSpeed * delta);

        if (isOutOfScreen()) {
            alive = false;
        }
    }

    public boolean isOutOfScreen() {
        return getY() < -getHeight();
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(getX());
        out.writeFloat(getY());
        out.writeFloat(velocity.x);
        out.writeFloat(velocity.y);
        out.writeBoolean(alive);
        out.writeFloat(rotationSpeed);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        setX(in.readFloat());
        setY(in.readFloat());
        velocity.set(in.readFloat(), in.readFloat());
        alive = in.readBoolean();
        rotationSpeed = in.readFloat();
    }
}
