package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.physics.Bits;

public class Brick extends InteractiveTileObject {

    private final TextureAtlas atlas;

    private MarkedAction unlockGoal;
    private float timeToUnlockGoal;
    private boolean goalProtector;
    private boolean destroyed;

    private Array<BrickFragment> activeFragments = new Array<BrickFragment>(16);
    private static Pool<BrickFragment> fragmentPool = Pools.get(BrickFragment.class);

    public Brick(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas, MapObject mapObject) {
        super(callbacks, Bits.BRICK, world, map, mapObject);
        this.goalProtector = mapObject.getProperties().get("unlockGoal", false, Boolean.class);
        this.atlas = atlas;
        this.unlockGoal = new MarkedAction();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        updateFragments(delta);

        if (unlockGoal.needsAction()) {
            timeToUnlockGoal -= delta;
            if (timeToUnlockGoal < 0) {
                getCallbacks().unlockGoalBrick(this);
                destroy();
                emitFragments();
                unlockGoal.done();
            }
        }
    }

    private void updateFragments(float delta) {
        for (int i = activeFragments.size; --i >= 0;) {
            BrickFragment fragment = activeFragments.get(i);
            fragment.update(delta);

            if (!fragment.isAlive()) {
                activeFragments.removeIndex(i);
                fragmentPool.free(fragment);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        drawFragments(batch);
    }

    private void drawFragments(SpriteBatch batch) {
        for (int i = 0; i < activeFragments.size; ++i) {
            BrickFragment fragment = activeFragments.get(i);
            fragment.draw(batch);
        }
    }

    @Override
    public void onHeadHit(Player player) {
        float xDistance = Math.abs(player.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < Cfg.BLOCK_SIZE / 2 / Cfg.PPM;

        getCallbacks().hit(player, this, closeEnough);

        if (closeEnough) {
            // apply effect to objects on top
            for (String objectOnTop : getObjectsOnTop()) {
                getCallbacks().indirectObjectHit(this, objectOnTop);

                // ensure that object is un-registered from objects on top, because Box2D does not seem
                // to call endContact anymore
                steppedOff(objectOnTop);
            }

            if (player.isBig()) {
                destroy();
                emitFragments();
            } else {
                bumpUp();
            }
        }
    }

    public void unlockGoal(float unlockDelay) {
        if (!destroyed && isGoalProtector()) {
            timeToUnlockGoal = unlockDelay;
            unlockGoal.mark();
        }
    }

    private void destroy() {
        updateCategoryFilter(Bits.NOTHING);
        getCell().setTile(null);
        destroyed = true;
    }

    private void emitFragments() {
        Vector2 pos = new Vector2();
        Vector2 velocity = new Vector2();
        BrickFragment fragment0 = fragmentPool.obtain();
        fragment0.init(atlas, 0,
                getBounds().getPosition(pos)
                        .add(getBounds().width / 4f, getBounds().height * 3f / 4f),
                velocity.set(-0.33f, 1.0f), 180f);
        activeFragments.add(fragment0);
        BrickFragment fragment1 = fragmentPool.obtain();
        fragment1.init(atlas, 1,
                getBounds().getPosition(pos)
                        .add(getBounds().width * 3f / 4f, getBounds().height * 3 / 4f),
                velocity.set(0.33f, 1.0f), -180f);
        activeFragments.add(fragment1);
        BrickFragment fragment2 = fragmentPool.obtain();
        fragment2.init(atlas, 2,
                getBounds().getPosition(pos)
                        .add(getBounds().width / 4f, getBounds().height / 4f),
                velocity.set(-0.33f, 0.5f), 180f);
        activeFragments.add(fragment2);
        BrickFragment fragment3 = fragmentPool.obtain();
        fragment3.init(atlas, 3,
                getBounds().getPosition(pos)
                        .add(getBounds().width *3f / 4f, getBounds().height / 4f),
                velocity.set(0.33f, 0.5f), -180f);
        activeFragments.add(fragment3);
    }

    public boolean isGoalProtector() {
        return goalProtector;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeBoolean(destroyed);
        out.writeBoolean(goalProtector);
        out.writeInt(activeFragments.size);
        for (BrickFragment fragment : activeFragments) {
            fragment.write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        destroyed = in.readBoolean();
        goalProtector = in.readBoolean();
        int numFragments = in.readInt();
        for (int i = 0; i < numFragments; ++i) {
            BrickFragment brickFragment = new BrickFragment();
            // TODO we actually might asign the wrong fragment-index here (which probably nobody will ever notice)
            brickFragment.init(atlas, i, Vector2.Zero, Vector2.Zero, 0f);
            brickFragment.read(in);
            activeFragments.add(brickFragment);
        }

        if (destroyed) {
            destroy();
        }
    }
}
