package de.bsautermeister.jump.effects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.serializer.BinarySerializable;
import de.bsautermeister.jump.sprites.Fragment;

public class SimpleFragmentEffect implements BinarySerializable {
    private Array<Fragment> activeFragments = new Array<>(16);
    private static Pool<Fragment> fragmentPool = Pools.get(Fragment.class);

    private final TextureAtlas atlas;
    private final String templateRegionName;

    public SimpleFragmentEffect(TextureAtlas atlas, String templateRegionName) {
        this.atlas = atlas;
        this.templateRegionName = templateRegionName;
    }

    public void update(float delta) {
        for (int i = activeFragments.size; --i >= 0;) {
            Fragment fragment = activeFragments.get(i);
            fragment.update(delta);

            if (!fragment.isAlive()) {
                activeFragments.removeIndex(i);
                fragmentPool.free(fragment);
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (int i = 0; i < activeFragments.size; ++i) {
            Fragment fragment = activeFragments.get(i);
            fragment.draw(batch);
        }
    }

    public void emit(Rectangle sourceBounds) {
        Vector2 pos = new Vector2();
        Vector2 velocity = new Vector2();
        Fragment fragment0 = fragmentPool.obtain();
        fragment0.init(atlas.findRegion(RegionNames.fromTemplate(templateRegionName, 0)),
                sourceBounds.getPosition(pos)
                        .add(sourceBounds.width / 4f, sourceBounds.height * 3f / 4f),
                velocity.set(-MathUtils.random(0.75f, 1.25f), MathUtils.random(2.0f, 3.5f)), MathUtils.random(150f, 210f));
        activeFragments.add(fragment0);
        Fragment fragment1 = fragmentPool.obtain();
        fragment1.init(atlas.findRegion(RegionNames.fromTemplate(templateRegionName, 1)),
                sourceBounds.getPosition(pos)
                        .add(sourceBounds.width * 3f / 4f, sourceBounds.height * 3 / 4f),
                velocity.set(MathUtils.random(0.75f, 1.25f), MathUtils.random(2.0f, 3.5f)), -MathUtils.random(150f, 210f));
        activeFragments.add(fragment1);
        Fragment fragment2 = fragmentPool.obtain();
        fragment2.init(atlas.findRegion(RegionNames.fromTemplate(templateRegionName, 2)),
                sourceBounds.getPosition(pos)
                        .add(sourceBounds.width / 4f, sourceBounds.height / 4f),
                velocity.set(-MathUtils.random(0.75f, 1.25f), MathUtils.random(1.0f, 2.0f)), MathUtils.random(150f, 210f));
        activeFragments.add(fragment2);
        Fragment fragment3 = fragmentPool.obtain();
        fragment3.init(atlas.findRegion(RegionNames.fromTemplate(templateRegionName, 3)),
                sourceBounds.getPosition(pos)
                        .add(sourceBounds.width * 3f / 4f, sourceBounds.height / 4f),
                velocity.set(MathUtils.random(0.75f, 1.25f), MathUtils.random(1.0f, 2.0f)), -MathUtils.random(150f, 210f));
        activeFragments.add(fragment3);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(activeFragments.size);
        for (Fragment fragment : activeFragments) {
            fragment.write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int numFragments = in.readInt();
        for (int i = 0; i < numFragments; ++i) {
            Fragment fragment = new Fragment();
            // FIXME we actually might assign the wrong fragment-index here (which probably nobody will ever notice)
            fragment.init(atlas.findRegion(templateRegionName, i), Vector2.Zero, Vector2.Zero, 0f);
            fragment.read(in);
            activeFragments.add(fragment);
        }
    }
}
