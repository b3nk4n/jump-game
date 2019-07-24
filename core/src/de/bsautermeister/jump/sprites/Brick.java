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
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;

public class Brick extends InteractiveTileObject {

    private final TextureAtlas atlas;

    private boolean destroyed;

    private Array<BrickFragment> activeBrickFragments = new Array<BrickFragment>(4);
    static Pool<BrickFragment> brickFragmentPool = Pools.get(BrickFragment.class);

    public Brick(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas, MapObject mapObject) {
        super(callbacks, JumpGame.BRICK_BIT, world, map, mapObject);
        this.atlas = atlas;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        for (int i = activeBrickFragments.size; --i >= 0;) {
            BrickFragment brickFragment = activeBrickFragments.get(i);
            brickFragment.update(delta);

            if (!brickFragment.isAlive()) {
                activeBrickFragments.removeIndex(i);
                brickFragmentPool.free(brickFragment);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        for (int i = 0; i < activeBrickFragments.size; ++i) {
            BrickFragment brickFragment = activeBrickFragments.get(i);
            brickFragment.draw(batch);
        }
    }

    @Override
    public void onHeadHit(Mario mario) {
        float xDistance = Math.abs(mario.getBody().getWorldCenter().x - getBody().getWorldCenter().x);
        boolean closeEnough = xDistance < Cfg.BLOCK_SIZE / 2 / Cfg.PPM;

        getCallbacks().hit(mario, this, closeEnough);

        if (closeEnough) {
            // kill enemies on top
            for (String enemyOnTop : getEnemiesOnTop()) {
                getCallbacks().indirectEnemyHit(this, enemyOnTop);

                // ensure that enemy is un-registered from enemies on top, because Box2D does not seem
                // to call endContact anymore
                enemySteppedOff(enemyOnTop);
            }

            // reverse items on top
            for (String itemOnTop : getItemsOnTop()) {
                getCallbacks().indirectItemHit(this, itemOnTop);

                // ensure that item is un-registered from items on top, because Box2D does not seem
                // to call endContact anymore
                itemSteppedOff(itemOnTop);
            }

            if (mario.isBig()) {
                destroy();
                emitFragments();
            } else {
                bumpUp();
            }
        }
    }

    private void destroy() {
        updateCategoryFilter(JumpGame.DESTROYED_BIT);
        getCell().setTile(null);
        destroyed = true;
    }

    private void emitFragments() {
        Vector2 pos = new Vector2();
        Vector2 velocity = new Vector2();
        BrickFragment fragment1 = brickFragmentPool.obtain();
        fragment1.init(atlas,
                getBounds().getPosition(pos)
                        .add(getBounds().width / 4f, getBounds().height * 3f / 4f),
                velocity.set(-0.33f, 1.0f), 180f);
        activeBrickFragments.add(fragment1);
        BrickFragment fragment2 = brickFragmentPool.obtain();
        fragment2.init(atlas,
                getBounds().getPosition(pos)
                        .add(getBounds().width * 3f / 4f, getBounds().height * 3 / 4f),
                velocity.set(0.33f, 1.0f), -180f);
        activeBrickFragments.add(fragment2);
        BrickFragment fragment3 = brickFragmentPool.obtain();
        fragment3.init(atlas,
                getBounds().getPosition(pos)
                        .add(getBounds().width / 4f, getBounds().height / 4f),
                velocity.set(-0.33f, 0.5f), 180f);
        activeBrickFragments.add(fragment3);
        BrickFragment fragment4 = brickFragmentPool.obtain();
        fragment4.init(atlas,
                getBounds().getPosition(pos)
                        .add(getBounds().width *3f / 4f, getBounds().height / 4f),
                velocity.set(0.33f, 0.5f), -180f);
        activeBrickFragments.add(fragment4);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeBoolean(destroyed);
        out.writeInt(activeBrickFragments.size);
        for (BrickFragment fragment : activeBrickFragments) {
            fragment.write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        destroyed = in.readBoolean();
        int numFragments = in.readInt();
        for (int i = 0; i < numFragments; ++i) {
            BrickFragment brickFragment = new BrickFragment();
            brickFragment.init(atlas, Vector2.Zero, Vector2.Zero, 0f);
            brickFragment.read(in);
            activeBrickFragments.add(brickFragment);
        }

        if (destroyed) {
            destroy();
        }
    }
}
