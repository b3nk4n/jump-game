package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public abstract class Enemy extends Sprite {
    public static final int TIME_TO_DISAPPEAR = 1;

    private GameCallbacks callbacks;
    private World world;
    private TiledMap tiledMap;
    private Body body;
    private Vector2 velocity;

    private boolean dead;
    private boolean removable;
    private boolean markForDestory; // TODO are some of these variables redundant?
    private boolean destroyed;

    public Enemy(GameCallbacks callbacks, World world, TiledMap map, float posX, float posY) {
        this.callbacks = callbacks;
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

        boolean enemyOutOfBounds = getBody().getPosition().y < - GameConfig.BLOCK_SIZE / GameConfig.PPM;
        if (enemyOutOfBounds) {
            markRemovable();
            destroyLater();
        }

        if (isDead()) {
            setFlip(isFlipX(), true);
        }
    }

    public void kill(boolean applyPush) {
        dead = true;
        Filter filter = new Filter();
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }

        callbacks.killed(this);

        if (applyPush) {
            getBody().applyLinearImpulse(new Vector2(0, 3.5f), getBody().getWorldCenter(), true);
        }
    }

    public void destroyLater() {
        markForDestory = true;
    }

    public abstract void onHeadHit(Mario mario);

    public abstract void onEnemyHit(Enemy enemy);

    public void reverseVelocity(boolean reverseX, boolean reverseY) {
        if (reverseX) {
            velocity.x = -velocity.x;
        }
        if (reverseY) {
            velocity.y = -velocity.y;
        }
        callbacks.hitWall(this);
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

    public GameCallbacks getCallbacks() {
        return callbacks;
    }

    public boolean isDead() {
        return dead;
    }

    public void markRemovable() {
        this.removable = true;
    }

    public boolean isRemovable() {
        return removable;
    }
}
