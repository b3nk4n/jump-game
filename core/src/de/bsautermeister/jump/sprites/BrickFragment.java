package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;

public class BrickFragment extends Sprite implements Pool.Poolable {

    private final Vector2 velocity = new Vector2();
    private boolean alive;
    private float rotationSpeed;

    public void init(TextureAtlas atlas, Vector2 centerPosition, Vector2 velocity,
                     float rotationSpeed) {
        setRegion(atlas.findRegion(RegionNames.BRICK_FRAGMENT));
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
}
