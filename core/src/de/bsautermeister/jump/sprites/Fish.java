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
import com.badlogic.gdx.utils.Array;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;

public class Fish extends Enemy implements Drownable {
    public static final float WAIT_TIME = 3f;

    public enum State {
        WAITING, JUMPING, DROWNING
    }

    private GameObjectState<State> state;

    private Animation<TextureRegion> animation;

    private float startCenterX;
    private float startCenterY;

    public Fish(GameCallbacks callbacks, World world, TextureAtlas atlas,
                float posX, float posY) {
        super(callbacks, world, posX, 0f, 0f);
        initTextures(atlas);
        state = new GameObjectState<State>(State.WAITING);
        setBounds(getX(), getY(), Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        setRegion(animation.getKeyFrame(state.timer()));
        setOriginCenter();
        startCenterX = getX() + getWidth() / 2;
        startCenterY = getY() - getHeight() / 2;
    }

    private void initTextures(TextureAtlas atlas) {
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(atlas.findRegion(RegionNames.FISH), i * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE));
        }
        animation = new Animation(0.25f, frames, Animation.PlayMode.LOOP);
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
        setRotation((getBody().getLinearVelocity().angle() - 180f) / 4f);
        System.out.println(state);
        if (state.is(State.WAITING)) {
            getBody().setTransform(startCenterX, startCenterY, 0);

            if (state.timer() > WAIT_TIME) {
                state.set(State.JUMPING);
                getBody().setLinearVelocity(new Vector2(-0.1f, 1f).setLength(3f));
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

    @Override
    public void drown() {
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
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
    }
}
