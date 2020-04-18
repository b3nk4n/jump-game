package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.physics.TaggedUserData;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class PretzelBullet extends Sprite implements BinarySerializable {

    private static final float VELOCITY_X = 2.25f;

    private final GameCallbacks callbacks;
    private final World world;
    private Body body;
    private float rotation;
    private boolean rightDirection;
    private float activeTime;

    private float previousVelocityY;

    private MarkedAction reset;

    private static ParticleEffectPool explodeEffectPool;
    private static Array<ParticleEffectPool.PooledEffect> activeExplodeEffects = new Array<ParticleEffectPool.PooledEffect>();

    public PretzelBullet(GameCallbacks callbacks, World world, TextureAtlas atlas) {
        super(atlas.findRegion(RegionNames.PRETZEL_BULLET));
        this.callbacks = callbacks;
        this.world = world;
        setSize(Cfg.BLOCK_SIZE / 2 / Cfg.PPM, Cfg.BLOCK_SIZE / 2 / Cfg.PPM);
        setOrigin(getWidth() / 2, getHeight() / 2);
        body = defineBody();
        reset = new MarkedAction();

        // TODO only works like this, because PrezelBullet is only instantiated a single time
        explodeEffectPool = createEffectPool(AssetPaths.Pfx.EXPLODE, atlas);

        reset();
    }

    private ParticleEffectPool createEffectPool(String effectPath , TextureAtlas atlas) {
        ParticleEffect effect = new ParticleEffect();
        effect.load(Gdx.files.internal(effectPath), atlas); // TODO: https://stackoverflow.com/questions/12261439/assetmanager-particleeffectloader-of-libgdx-android
        effect.scaleEffect(0.1f / Cfg.PPM);
        return new ParticleEffectPool(effect, 8, 16);
    }

    public void reset() {
        body.setActive(false);
        body.setTransform(-1, -1, 0);
        body.setLinearVelocity(Vector2.Zero);
        setPosition(-1, -1);
        rotation = 0;
        previousVelocityY = 0;
        activeTime = 0f;
    }

    public void fire(float posX, float posY, boolean rightDirection) {
        body.setTransform(posX, posY, 0);
        body.setActive(true);
        this.rightDirection = rightDirection;
    }

    public boolean isActive() {
        return body.isActive();
    }

    public void resetLater() {
        reset.mark();
    }

    private Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1f;
        CircleShape shape = new CircleShape();
        shape.setRadius(3f / Cfg.PPM);
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Bits.BULLET;
        fixtureDef.filter.maskBits = Bits.ENEMY |
                Bits.GROUND |
                Bits.BRICK |
                Bits.ITEM_BOX |
                Bits.PLATFORM;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        return body;
    }

    public void update(float delta) {
        if (isActive()) {
            if (activeTime > 0.01f && (rightDirection && body.getLinearVelocity().x <= 0 || !rightDirection && body.getLinearVelocity().x >= 0)) {
                // ensure that the bullet never changes direction and does not get stuck! (not sure whether this is still needed)
                reset.mark();
                return;
            }

            if (previousVelocityY < 0 && body.getLinearVelocity().y >= 0) {
                // started to jump up
                body.setLinearVelocity(body.getLinearVelocity().x, 1.75f);
            }

            body.setLinearVelocity(rightDirection ? VELOCITY_X : -VELOCITY_X, body.getLinearVelocity().y);
            if (rightDirection) {
                rotation -= 360 * delta;
            } else {
                rotation += 360 * delta;
            }
            setRotation(rotation);
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 + 1f / Cfg.PPM);
            setFlip(!rightDirection, false);
            previousVelocityY = body.getLinearVelocity().y;
            activeTime += delta;
        }

        for (int i = activeExplodeEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = activeExplodeEffects.get(i);
            effect.update(delta);
            if (effect.isComplete()) {
                activeExplodeEffects.removeIndex(i);
                effect.free();
            }
        }
    }

    public void postUpdate() {
        if (reset.isMarked()) {
            // reset needs to be done outside of box2d loop (contact listener)
            reset();
            reset.done();
            reset.reset();
        }
    }

    @Override
    public void draw(Batch batch) {
        if (isActive()) {
            super.draw(batch);
        }

        for (ParticleEffectPool.PooledEffect effect : activeExplodeEffects) {
            effect.draw(batch);
        }
    }

    public void explode(Vector2 contactPosition) {
        ParticleEffectPool.PooledEffect effect = explodeEffectPool.obtain();
        effect.start();
        effect.setPosition(contactPosition.x, contactPosition.y);
        activeExplodeEffects.add(effect);

        reset.mark();
    }

    public void hitWall(Vector2 contactPosition) {
        explode(contactPosition);
        callbacks.hitWall(this);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(rotation);
        out.writeBoolean(rightDirection);
        out.writeFloat(previousVelocityY);
        out.writeFloat(activeTime);
        reset.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        rotation = in.readFloat();
        rightDirection = in.readBoolean();
        previousVelocityY = in.readFloat();
        activeTime = in.readFloat();
        reset.read(in);
    }

    public Body getBody() {
        return body;
    }
}
