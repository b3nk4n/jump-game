package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;

public abstract class Item extends Sprite {
    private GameCallbacks callbacks;
    private World world;
    protected Vector2 velocity;
    private boolean markForDestory;
    private boolean destroyed;
    private Body body;

    public Item(GameCallbacks callbacks, World world, float x, float y) {
        this.callbacks = callbacks;
        this.world = world;
        setPosition(x, y);
        setBounds(getX(), getY(), 16 / GameConfig.PPM, 16 / GameConfig.PPM);
        body = defineBody();
        markForDestory = false;
        destroyed = false;
    }

    public abstract Body defineBody();
    public abstract void use(Mario mario);

    public void update(float delta) {
        if (markForDestory && !destroyed) {
            world.destroyBody(body);
            destroyed = true;
        }
    }

    public void destroyLater() {
        markForDestory = true;
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

    public boolean canBeRemoved() {
        return destroyed;
    }

    public GameCallbacks getCallbacks() {
        return callbacks;
    }
}
