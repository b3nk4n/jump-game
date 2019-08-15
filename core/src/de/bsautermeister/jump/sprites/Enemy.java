package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class Enemy extends Sprite implements BinarySerializable, Disposable {
    private String id;
    private GameCallbacks callbacks;
    private World world;
    private Body body;
    private float velocityX;

    private boolean dead;
    private boolean removable;

    private MarkedAction destroyBody;

    public Enemy(GameCallbacks callbacks, World world, float posX, float posY, float speed) {
        this.id = UUID.randomUUID().toString();
        this.callbacks = callbacks;
        this.world = world;
        setPosition(posX, posY);
        this.body = defineBody();
        this.velocityX = -speed;
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

    public void reverseDirection() {
        velocityX = -velocityX;
        callbacks.hitWall(this);
    }

    public String getId() {
        return id;
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

    public float getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(float value) {
        velocityX = value;
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

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(velocityX);
        out.writeBoolean(dead);
        out.writeBoolean(removable);
        destroyBody.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        velocityX = in.readFloat();
        dead = in.readBoolean();
        removable = in.readBoolean();
        destroyBody.read(in);
    }
}
