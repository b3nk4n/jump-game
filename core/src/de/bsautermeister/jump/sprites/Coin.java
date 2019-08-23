package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class Coin extends Sprite implements CollectableItem, BinarySerializable, Disposable {

    private final GameCallbacks callbacks;
    private final World world;

    private Animation<TextureRegion> animation;

    private final Body body;
    private MarkedAction destroyBody;

    private float gameTime;

    public Coin(GameCallbacks callbacks, World world, TextureAtlas atlas,
                float posX, float posY) {
        this.callbacks = callbacks;
        this.world = world;
        initAnimation(atlas);
        setBounds(posX, posY, Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        destroyBody = new MarkedAction();
        body = defineBody(posX + getWidth() / 2, posY + getHeight() / 2);

        // start with an offset, so that not all coins have the same animation frame cycle
        gameTime = 1000f - 0.75f * posX + 0.75f * posY;
    }

    private void initAnimation(TextureAtlas atlas) {
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 10; ++i) {
            frames.add(atlas.findRegion(RegionNames.COIN, 0));
        }
        for (int i = 0; i < 4; ++i) {
            frames.add(atlas.findRegion(RegionNames.COIN, i));
        }
        animation = new Animation<TextureRegion>(0.1f, frames, Animation.PlayMode.LOOP);
    }

    private Body defineBody(float centerX, float centerY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(centerX, centerY);
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / Cfg.PPM);
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Bits.ITEM;
        fixtureDef.filter.maskBits = Bits.MARIO;
        fixtureDef.isSensor = true;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        return body;
    }

    public void update(float delta) {
        gameTime += delta;
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        setRegion(animation.getKeyFrame(gameTime));
    }

    public void postUpdate() {
        if (destroyBody.needsAction()) {
            dispose();
            destroyBody.done();
        }
    }

    @Override
    public void collectBy(Mario mario) {
        callbacks.collectCoin();
        destroyBody.mark();
    }

    public boolean isRemovable() {
        return destroyBody.isDone();
    }

    @Override
    public void dispose() {
        if (!destroyBody.isDone()) {
            world.destroyBody(body);
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(gameTime);
        destroyBody.write(out);
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        gameTime = in.readFloat();
        destroyBody.read(in);
        body.setTransform(in.readFloat(), in.readFloat(), 0);
    }
}
