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

import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class Enemy extends Sprite implements BinarySerializable, Disposable {
    protected static final String TAG_LEFT = "left";
    protected static final String TAG_RIGHT = "right";
    protected static final String TAG_TOP = "top";

    private String id;

    /**
     * An optional group, which is used so that all enemies of the same group are woken up together.
     */
    private String group;
    private GameCallbacks callbacks;
    private World world;
    private Body body;

    private boolean dead;
    private boolean removable;

    private MarkedAction destroyBody;

    public Enemy(GameCallbacks callbacks, World world, float posX, float posY, float width, float height) {
        this.id = UUID.randomUUID().toString();
        this.callbacks = callbacks;
        this.world = world;
        setBounds(posX, posY, width, height);
        this.body = defineBody();
        destroyBody = new MarkedAction();
        setActive(false); // sleep and activate as soon as player gets close
    }

    protected abstract Body defineBody();

    public void update(float delta) {
        if (isOutOfBounds()) {
            markRemovable();
        }

        if (isDead()) {
            setFlip(isFlipX(), true);
        }

        // limit falling speed
        if (body != null && body.getLinearVelocity().y < Cfg.MAX_FALLING_SPEED) {
            body.setLinearVelocity(body.getLinearVelocity().x, Cfg.MAX_FALLING_SPEED);
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
            body = null;
        }
    }

    public void kill(boolean applyPush) {
        dead = true;
        updateMaskFilter(Bits.NOTHING);

        callbacks.killed(this);

        if (applyPush) {
            getBody().setLinearVelocity(getBody().getLinearVelocity().x / 2, 0);
            getBody().applyLinearImpulse(new Vector2(0, 8f), getBody().getWorldCenter(), true);
        }
    }

    protected void updateMaskFilter(short filterBit) {
        Filter filter = new Filter();
        filter.maskBits = filterBit;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }

    protected boolean isOutOfBounds() {
        return body != null && getBody().getPosition().y < - 2 * Cfg.BLOCK_SIZE_PPM;
    }

    public void markDestroyBody() {
        destroyBody.mark();
    }

    public abstract void onHeadHit(Player player);

    public abstract void onEnemyHit(Enemy enemy);

    public String getId() {
        return id;
    }

    public void setActive(boolean active) {
        body.setActive(active);
    }

    public boolean isActive() {
        return body.isActive();
    }

    public World getWorld() {
        return world;
    }

    public Body getBody() {
        return body;
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

    public boolean hasGroup() {
        return group != null;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(group != null ? group : "null");
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeBoolean(dead);
        out.writeBoolean(removable);
        destroyBody.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        group = in.readUTF();
        if (group.equals("null")) {
            group = null;
        }
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        dead = in.readBoolean();
        removable = in.readBoolean();
        destroyBody.read(in);
    }
}
