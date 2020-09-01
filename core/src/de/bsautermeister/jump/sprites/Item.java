package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class Item extends Sprite implements CollectableItem, BinarySerializable, Disposable {
    public static final String TAG_BASE = "base";

    /**
     * Add a tiny y-offset to the spawn position, because the block-item is moving and would
     * otherwise expose the bottom of the item as a small visual glitch.
     */
    private static final float SPAWN_ITEM_OFFSET_Y = 0.15f * Cfg.BLOCK_SIZE_PPM;

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
    private Body body;

    private MarkedAction destroyBody;
    private boolean collected;

    protected final GameObjectState<State> state = new GameObjectState<>(State.SPAWNING);

    public Item(GameCallbacks callbacks, World world, float centerX, float centerY) {
        this.id = UUID.randomUUID().toString();
        this.spawnY = centerY - Cfg.BLOCK_SIZE_PPM / 2 + SPAWN_ITEM_OFFSET_Y;
        this.targetY = spawnY + Cfg.BLOCK_SIZE_PPM - SPAWN_ITEM_OFFSET_Y;
        this.callbacks = callbacks;
        this.world = world;
        this.setSize(Cfg.BLOCK_SIZE_PPM, Cfg.BLOCK_SIZE_PPM);
        setPosition(centerX - Cfg.BLOCK_SIZE_PPM / 2, spawnY);
        destroyBody = new MarkedAction();
        body = defineBody(centerX, centerY + Cfg.BLOCK_SIZE_PPM);
    }

    public abstract Body defineBody(float x, float y);

    public void update(float delta) {
        state.update(delta);
        if (state.is(State.SPAWNING)) {
            float progress = state.timer() / SPAWN_TIME;
            setY(spawnInterpolation.apply(spawnY, targetY, progress));

            if (state.timer() > SPAWN_TIME) {
                //body.setActive(true);
                state.set(State.SPAWNED);
            }
        } else {
            boolean outOfBounds = getBody().getPosition().y < - Cfg.BLOCK_SIZE_PPM;
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
    public void collectBy(Player player) {
        if (collected) {
            // ensure items is not collected multiple times, even when a body consists of
            // multiple fixtures
            return;
        }

        collected = true;
        callbacks.use(player, this);
        markDestroyBody();

        onCollect(player);
    }

    protected abstract void onCollect(Player player);

    @Override
    public void dispose() {
        if (!destroyBody.isDone()) {
            world.destroyBody(body);
            body = null;
        }
    }

    public void markDestroyBody() {
        destroyBody.mark();
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
        destroyBody.write(out);
        out.writeBoolean(collected);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        destroyBody.read(in);
        collected = in.readBoolean();
    }
}
