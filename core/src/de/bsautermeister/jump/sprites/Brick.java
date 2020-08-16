package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.effects.SimpleFragmentEffect;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.screens.game.GameCallbacks;

public class Brick extends InteractiveTileObject {

    private final TextureAtlas atlas;

    private MarkedAction unlockGoal;
    private float timeToUnlockGoal;
    private boolean goalProtector;
    private boolean destroyed;

    private final SimpleFragmentEffect simpleFragmentEffect;

    public Brick(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas, MapObject mapObject) {
        super(callbacks, Bits.BRICK, world, map, mapObject);
        this.goalProtector = mapObject.getProperties().get("unlockGoal", false, Boolean.class);
        this.atlas = atlas;
        this.unlockGoal = new MarkedAction();

        simpleFragmentEffect = new SimpleFragmentEffect(atlas, RegionNames.BRICK_FRAGMENT_TPL);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        simpleFragmentEffect.update(delta);

        if (unlockGoal.needsAction()) {
            timeToUnlockGoal -= delta;
            if (timeToUnlockGoal < 0) {
                getCallbacks().unlockGoalBrick(this);
                destroy();
                simpleFragmentEffect.emit(getBounds());
                unlockGoal.done();
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        simpleFragmentEffect.draw(batch);
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
                simpleFragmentEffect.emit(getBounds());
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
        updateMaskFilter(Bits.NOTHING);
        getCell().setTile(null);
        destroyed = true;
    }

    public boolean isGoalProtector() {
        return goalProtector;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeBoolean(destroyed);
        out.writeBoolean(goalProtector);
        simpleFragmentEffect.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        destroyed = in.readBoolean();
        goalProtector = in.readBoolean();
        simpleFragmentEffect.read(in);

        if (destroyed) {
            destroy();
        }
    }
}
