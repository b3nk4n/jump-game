package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;

public class Fish extends Enemy implements Drownable {
    private static final float WAIT_TIME = 3f;
    private static final float JUMP_POWER = 3f;

    public enum State {
        WAITING, JUMPING, DROWNING
    }

    private GameObjectState<State> state;

    private final Animation<TextureRegion> animation;

    private float startCenterX;
    private float startCenterY;
    private float startDelay;
    private Vector2 startVector = new Vector2(0, JUMP_POWER);

    public Fish(GameCallbacks callbacks, World world, TextureAtlas atlas,
                float posX, float posY) {
        super(callbacks, world, posX, 0f);
        animation = new Animation(0.25f, atlas.findRegions(RegionNames.FISH), Animation.PlayMode.LOOP);
        state = new GameObjectState<State>(State.WAITING);
        setBounds(getX(), getY(), Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        setRegion(animation.getKeyFrame(state.timer()));
        setOriginCenter();
        startCenterX = getX() + getWidth() / 2;
        startCenterY = getY() - getHeight() / 2;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!isActive()) {
            getBody().setTransform(startCenterX, startCenterY, 0);
            return;
        }

        state.upate(delta);
        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - getHeight() / 2);
        setRegion(animation.getKeyFrame(state.timer()));
        float angle = getBody().getLinearVelocity().angle();
        if (startVector.x < 0) {
            setRotation((angle - 180f) / 4f);
        } else  {
            if (angle >= 180) {
                angle -= 360;
            }
            setFlip(true, false);
            setRotation(angle / 4f);
        }

        startDelay -= delta;
        if (startDelay > 0) {
            getBody().setTransform(startCenterX, startCenterY, 0);
            return;
        }

        if (state.is(State.WAITING)) {
            getBody().setTransform(startCenterX, startCenterY, 0);

            if (state.timer() > WAIT_TIME) {
                state.set(State.JUMPING);
                getBody().setLinearVelocity(startVector);
            }
        } else if (state.is(State.JUMPING) || state.is(State.DROWNING)) {
            if (isAlmostOutOfBounds()) {
                state.set(State.WAITING);
            }
        }
    }

    protected boolean isAlmostOutOfBounds() {
        return getBody().getPosition().y < - Cfg.BLOCK_SIZE / Cfg.PPM;
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.gravityScale = 0.25f;
        bodyDef.position.set(startCenterX, startCenterY);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6f / Cfg.PPM);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_BIT;
        fixtureDef.filter.maskBits = JumpGame.MARIO_BIT;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        return body;
    }

    @Override
    public void onHeadHit(Mario mario) {
        // NOOP
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        // NOOP
    }

    public void setStartDelay(float delay) {
        startDelay = delay;
    }

    public void setStartAngle(int degrees) {
        startVector.setAngle(degrees);
    }

    public void setVelocityFactor(float velocityFactor) {
        startVector.setLength(velocityFactor * JUMP_POWER);
    }

    @Override
    public void drown() {
        state.set(State.DROWNING);
        getBody().setLinearVelocity(getBody().getLinearVelocity().x / 10, getBody().getLinearVelocity().y / 10);
    }

    @Override
    public boolean isDrowning() {
        return state.is(State.DROWNING);
    }

    private final Vector2 outCenter = new Vector2();
    @Override
    public Vector2 getWorldCenter() {
        Rectangle rect = getBoundingRectangle();
        outCenter.set(rect.x + rect.width / 2, rect.y + rect.height / 2);
        return outCenter;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return getBody().getLinearVelocity();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
        out.writeFloat(startCenterX);
        out.writeFloat(startCenterY);
        out.writeFloat(startVector.x);
        out.writeFloat(startVector.y);
        out.writeFloat(startDelay);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        startCenterX = in.readFloat();
        startCenterY = in.readFloat();
        startVector.set(in.readFloat(), in.readFloat());
        startDelay = in.readFloat();
    }
}
