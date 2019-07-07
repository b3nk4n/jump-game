package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;

public abstract class Enemy extends Sprite implements Disposable {
    private GameCallbacks callbacks;
    private World world;
    private Body body;
    private Vector2 velocity;

    private boolean dead;
    private boolean removable;

    private MarkedAction destroyBody;

    public Enemy(GameCallbacks callbacks, World world, float posX, float posY) {
        this.callbacks = callbacks;
        this.world = world;
        setPosition(posX, posY);
        this.body = defineBody();
        this.velocity = new Vector2(-1, -1);
        destroyBody = new MarkedAction();
        setActive(false); // sleep and activate as soon as player gets close
    }

    protected abstract Body defineBody();

    public void update(float delta) {
        boolean outOfBounds = getBody().getPosition().y < - Cfg.BLOCK_SIZE / Cfg.PPM;
        if (outOfBounds) {
            markRemovable();
        }

        if (isDead()) {
            setFlip(isFlipX(), true);
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

    public void kill(boolean applyPush) {
        dead = true;
        Filter filter = new Filter();
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }

        callbacks.killed(this);

        if (applyPush) {
            getBody().setLinearVelocity(getBody().getLinearVelocity().x / 2, 0);
            getBody().applyLinearImpulse(new Vector2(0, 3f), getBody().getWorldCenter(), true);
        }
    }

    public void markDestroyBody() {
        destroyBody.mark();
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

    public Body getBody() {
        return body;
    }

    public Vector2 getVelocity() {
        return velocity;
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
