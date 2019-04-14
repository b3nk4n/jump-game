package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Enemy extends Sprite {
    public static final int TIME_TO_DISAPPEAR = 1;
    
    private World world;
    private TiledMap tiledMap;
    private Body body;
    private Vector2 velocity;
    private boolean markForDestory;
    private boolean destroyed;

    public Enemy(World world, TiledMap map, float posX, float posY) {
        this.world = world;
        this.tiledMap = map;
        setPosition(posX, posY);
        this.body = defineBody();
        this.velocity = new Vector2(-1, -1);
        markForDestory = false;
        destroyed = false;
        setActive(false); // sleep and activate as soon as player gets close
    }

    protected abstract Body defineBody();

    public void update(float delta) {
        if (markForDestory && !destroyed) {
            world.destroyBody(body);
            destroyed = true;
        }
    }

    public void destroyLater() {
        markForDestory = true;
    }

    public abstract void onHeadHit(Mario mario);

    public abstract void onEnemyHit(Enemy enemy);

    public abstract boolean canBeRemoved();

    public void reverseVelocity(boolean reverseX, boolean reverseY) {
        if (reverseX) {
            velocity.x = -velocity.x;
        }
        if (reverseY) {
            velocity.y = -velocity.y;
        }
    }

    public void setActive(boolean active) {
        body.setActive(active);
    }

    public World getWorld() {
        return world;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public Body getBody() {
        return body;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
