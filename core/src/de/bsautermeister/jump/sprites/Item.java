package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class Item extends Sprite implements BinarySerializable, Disposable {

    /**
     * Add a tiny y-offset to the spawn postition, because the block-item is moving and would
     * otherwise expose the bottom of the item as a small visual glitch.
     */
    private static final float SPAWN_ITEM_OFFSET_Y = 0.15f * Cfg.BLOCK_SIZE / Cfg.PPM;

    private static final float SPAWN_TIME = 0.75f;

    protected enum State {
        SPAWNING,
        SPAWNED
    }

    private String id;
    private final float spawnY;
    private final float targetY;
    private final Interpolation spawnInterpolation = Interpolation.linear;

    private GameCallbacks callbacks;
    private World world;
    protected Vector2 velocity;
    private final Body body;

    private MarkedAction destroyBody;

    protected final GameObjectState<State> state = new GameObjectState<State>(State.SPAWNING);

    public Item(GameCallbacks callbacks, World world, float centerX, float centerY) {
        this.id = UUID.randomUUID().toString();
        this.spawnY = centerY - Cfg.BLOCK_SIZE / Cfg.PPM / 2 + SPAWN_ITEM_OFFSET_Y;
        this.targetY = spawnY + Cfg.BLOCK_SIZE / Cfg.PPM;
        this.callbacks = callbacks;
        this.world = world;
        this.setSize(Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        setPosition(centerX - Cfg.BLOCK_SIZE / Cfg.PPM / 2, spawnY);
        destroyBody = new MarkedAction();
        body = defineBody(centerX, centerY + Cfg.BLOCK_SIZE / Cfg.PPM);
        body.setActive(false);
    }

    public abstract Body defineBody(float x, float y);
    public abstract void usedBy(Mario mario);

    public void update(float delta) {
        state.upate(delta);
        if (state.is(State.SPAWNING)) {
            float progress = state.timer() / SPAWN_TIME;
            setY(spawnInterpolation.apply(spawnY, targetY, progress));

            if (state.timer() > SPAWN_TIME) {
                body.setActive(true);
                state.set(State.SPAWNED);
            }
        } else {
            boolean outOfBounds = getBody().getPosition().y < - Cfg.BLOCK_SIZE / Cfg.PPM;
            if (outOfBounds) {
                destroyBody.mark();
            }
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

    public void bounceUp() {
        body.applyLinearImpulse(new Vector2(0, 1.5f), body.getWorldCenter(), true);
    }

    public String getId() {
        return id;
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

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(velocity.x);
        out.writeFloat(velocity.y);
        destroyBody.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        velocity.set(in.readFloat(), in.readFloat());
        destroyBody.read(in);
    }
}
