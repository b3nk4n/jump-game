package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;

public class Flower extends Enemy {
    private static final float MOVE_SPEED = 0.25f;
    private static final float HIDDEN_TIME = 2f;
    private static final float WAIT_TIME = 1.5f;

    public enum State {
        HIDDEN, MOVE_UP, WAITING, MOVE_DOWN, KILLED
    }

    private GameObjectState<State> state;

    private final Animation<TextureRegion> animation;

    private float hiddenTargetY;
    private float waitingTargetY;
    private final static float hiddenOffsetY = Cfg.BLOCK_SIZE / 4 / Cfg.PPM;

    private boolean blocked;
    private float gameTime;

    public Flower(GameCallbacks callbacks, World world, TextureAtlas atlas,
                  float posX, float posY) {
        super(callbacks, world, posX, posY);
        animation = new Animation(0.33f, atlas.findRegions(RegionNames.FLOWER), Animation.PlayMode.LOOP);
        setBounds(getX(), getY(), Cfg.BLOCK_SIZE / Cfg.PPM, (int)(1.5f * Cfg.BLOCK_SIZE) / Cfg.PPM);
        state = new GameObjectState<State>(State.HIDDEN);
        hiddenTargetY = getBody().getPosition().y - hiddenOffsetY;
        waitingTargetY = getBody().getPosition().y + getHeight();
        setRegion(animation.getKeyFrame(state.timer()));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        gameTime += delta;

        if (!isActive()) {
            return;
        }

        state.upate(delta);
        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - Cfg.BLOCK_SIZE * 0.75f / Cfg.PPM);
        setRegion(animation.getKeyFrame(gameTime));

        if (state.is(State.KILLED)) {
            if (getBody().getPosition().y > hiddenTargetY) {
                getBody().setLinearVelocity(0, 3 * -MOVE_SPEED);
            } else {
                getBody().setLinearVelocity(Vector2.Zero);
            }
        } else if (state.is(State.HIDDEN) && state.timer() > HIDDEN_TIME && !isBlocked()) {
            getBody().setLinearVelocity(0, MOVE_SPEED);
            state.set(State.MOVE_UP);
        } else if (state.is(State.MOVE_UP) && getBody().getPosition().y >= waitingTargetY) {
            getBody().setLinearVelocity(Vector2.Zero);
            state.set(State.WAITING);
        } else if (state.is(State.WAITING) && state.timer() > WAIT_TIME) {
            getBody().setLinearVelocity(0, -MOVE_SPEED);
            state.set(State.MOVE_DOWN);
        } else if (state.is(State.MOVE_DOWN) && getBody().getPosition().y <= hiddenTargetY) {
            getBody().setLinearVelocity(Vector2.Zero);
            state.set(State.HIDDEN);
        }
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX() + Cfg.BLOCK_SIZE / 2 / Cfg.PPM, getY() + Cfg.BLOCK_SIZE * 0.75f / Cfg.PPM);
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(7f / Cfg.PPM, 12f / Cfg.PPM);
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.PLAYER | Bits.FIREBALL;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        // top sensor
        EdgeShape topSensor = new EdgeShape();
        fixtureDef.shape = topSensor;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.PLAYER | Bits.FIREBALL;
        fixtureDef.isSensor = true;
        topSensor.set(new Vector2(-Cfg.BLOCK_SIZE / Cfg.PPM, 14f / Cfg.PPM + hiddenOffsetY),
                new Vector2(Cfg.BLOCK_SIZE / Cfg.PPM, 14f / Cfg.PPM + hiddenOffsetY));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_TOP));

        return body;
    }

    @Override
    public void onHeadHit(Player player) {
        // NOOP
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        // NOOP
    }

    @Override
    public void kill(boolean applyPush) {
        super.kill(applyPush);
        state.set(State.KILLED);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
        out.writeFloat(hiddenTargetY);
        out.writeFloat(waitingTargetY);
        out.writeBoolean(blocked);
        out.writeFloat(gameTime);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        hiddenTargetY = in.readFloat();
        waitingTargetY = in.readFloat();
        blocked = in.readBoolean();
        gameTime = in.readFloat();
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
