package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;

public abstract class Item extends Sprite implements Disposable {
    private GameCallbacks callbacks;
    private World world;
    protected Vector2 velocity;
    private Body body;

    private MarkedAction destroyBody;

    public Item(GameCallbacks callbacks, World world, float x, float y) {
        this.callbacks = callbacks;
        this.world = world;
        setPosition(x, y);
        setBounds(getX(), getY(), 16 / GameConfig.PPM, 16 / GameConfig.PPM);
        body = defineBody();
        destroyBody = new MarkedAction();
    }

    public abstract Body defineBody();
    public abstract void usedBy(Mario mario);

    public void update(float delta) {
        boolean outOfBounds = getBody().getPosition().y < - GameConfig.BLOCK_SIZE / GameConfig.PPM;
        if (outOfBounds) {
            destroyBody.mark();
        }
    }

    public void postUpdate() {
        if (destroyBody.needsAction()) {
            dispose();
            destroyBody.done();
        }
    }

    @Override
    public void dispose() {
        if (!destroyBody.isDone()) {
            world.destroyBody(body);
        }
    }

    public void markDestroyBody() {
        destroyBody.mark();
    }

    public void reverseVelocity(boolean reverseX, boolean reverseY) {
        if (reverseX) {
            velocity.x = -velocity.x;
        }
        if (reverseY) {
            velocity.y = -velocity.y;
        }
    }

    public World getWorld() {
        return world;
    }

    public Body getBody() {
        return body;
    }

    public boolean isRemovable() {
        return destroyBody.isDone();
    }

    public GameCallbacks getCallbacks() {
        return callbacks;
    }
}
