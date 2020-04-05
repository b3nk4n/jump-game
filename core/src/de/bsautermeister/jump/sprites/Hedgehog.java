package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
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
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;

public class Hedgehog extends Enemy implements Drownable {
    private static final float KICK_SPEED = 2f;
    private static final float ROTATION_SPEED = 540f;
    private static final float WAIT_FOR_UNROLL_TIME = 5f;

    public enum State {
        WALKING, ROLL, ROLLING, UNROLL
    }

    private GameObjectState<State> state;
    private boolean drowning;
    private float speed;

    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> rollAnimation;
    private final Animation<TextureRegion> unrollAnimation;
    private final TextureRegion rollingTexture;

    private static final float SPEED_VALUE = 0.6f;

    private boolean previousDirectionLeft;

    public Hedgehog(GameCallbacks callbacks, World world, TextureAtlas atlas,
                    float posX, float posY, boolean rightDirection) {
        super(callbacks, world, posX, posY, Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        walkAnimation = new Animation(0.05f, atlas.findRegions(RegionNames.HEDGEHOG_WALK), Animation.PlayMode.LOOP);
        rollAnimation = new Animation(0.33f, atlas.findRegions(RegionNames.HEDGEHOG_ROLL), Animation.PlayMode.NORMAL);
        unrollAnimation = new Animation(0.33f, atlas.findRegions(RegionNames.HEDGEHOG_ROLL), Animation.PlayMode.REVERSED);
        rollingTexture = atlas.findRegion(RegionNames.HEDGEHOG_ROLLING);

        state = new GameObjectState<State>(State.WALKING);
        state.setStateCallback(new GameObjectState.StateCallback<State>() {
            @Override
            public void changed(State previousState, State newState) {
                if (previousState != State.ROLLING && newState != State.ROLLING) {
                    return;
                }

                Fixture leftSideSensor = getBody().getFixtureList().get(2);
                updateColliderBit(leftSideSensor, previousState, newState);

                Fixture rightSideSensor = getBody().getFixtureList().get(3);
                updateColliderBit(rightSideSensor, previousState, newState);
            }

            private void updateColliderBit(Fixture fixture, State previousState, State newState) {
                Filter filter = fixture.getFilterData();
                if (previousState == State.ROLLING) {
                    filter.maskBits |= ~Bits.COLLIDER;
                } else if (newState == State.ROLLING) {
                    filter.maskBits &= ~Bits.COLLIDER;
                }
                fixture.setFilterData(filter);
            }
        });
        speed = rightDirection ? SPEED_VALUE : -SPEED_VALUE;
        setRegion(getFrame());
        setOrigin(getWidth() / 2, getHeight() * 5.5f / 16f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!isDead() && !isDrowning()) {
            state.upate(delta);
            getBody().setLinearVelocity(speed, getBody().getLinearVelocity().y);

            if (state.is(State.ROLLING)) {
                rotate(delta * (speed < 0 ? ROTATION_SPEED : -ROTATION_SPEED));
            }
        }

        if (!state.is(State.ROLLING)) {
            setRotation(0f);
        }

        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - 7 / Cfg.PPM);
        setRegion(getFrame());

        if (state.is(State.ROLL) && state.timer() > WAIT_FOR_UNROLL_TIME) {
            state.set(State.UNROLL);
        }

        if (state.is(State.UNROLL) && state.timer() > 1f) {
            state.set(State.WALKING);
            speed = previousDirectionLeft ? -SPEED_VALUE : SPEED_VALUE;
        }

        if (isDrowning()) {
            getBody().setLinearVelocity(getBody().getLinearVelocity().x * 0.95f, getBody().getLinearVelocity().y * 0.33f);
        }

        if (state.is(State.ROLL) || state.is(State.UNROLL) && speed == 0) {
            // ensure speed is zero, even after other enemy collision
            getBody().setLinearVelocity(Vector2.Zero);
        }
    }

    private TextureRegion getFrame() {
        TextureRegion textureRegion;

        switch (state.current()) {
            case ROLLING:
                textureRegion = rollingTexture;
                break;
            case ROLL:
                textureRegion = rollAnimation.getKeyFrame(state.timer());
                break;
            case UNROLL:
                textureRegion = unrollAnimation.getKeyFrame(state.timer());
                break;
            case WALKING:
            default:
                textureRegion = walkAnimation.getKeyFrame(state.timer());
                break;
        }

        boolean isLeft = speed < 0 || speed == 0 && previousDirectionLeft;

        if (!isLeft && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (isLeft && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

        if (isDead() && !textureRegion.isFlipY()) {
            textureRegion.flip(false, true);
        }

        return textureRegion;
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6f / Cfg.PPM);
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.PLAYER |
                Bits.ENEMY |
                Bits.BLOCK_TOP |
                Bits.BULLET;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        // head
        PolygonShape headShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-3.5f, 12).scl(1 / Cfg.PPM);
        vertices[1] = new Vector2(3.5f, 12).scl(1 / Cfg.PPM);
        vertices[2] = new Vector2(-2.5f, 6).scl(1 / Cfg.PPM);
        vertices[3] = new Vector2(2.5f, 6).scl(1 / Cfg.PPM);
        headShape.set(vertices);

        fixtureDef.shape = headShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_HEAD;
        fixtureDef.filter.maskBits = Bits.PLAYER;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.GROUND
                | Bits.COLLIDER
                | Bits.ITEM_BOX
                | Bits.BRICK
                | Bits.PLATFORM;
        fixtureDef.isSensor = true;
        sideShape.set(new Vector2(-6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(-6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_LEFT));
        sideShape.set(new Vector2(6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_RIGHT));

        return body;
    }

    @Override
    public void onHeadHit(Player player) {
        if (player.isDead() || player.isInvincible()) {
            return;
        }

        if (!state.is(State.ROLL)) {
            stomp();
        } else {
            kick(player.getX() <= getX());
        }
    }

    private void stomp() {
        state.set(State.ROLL);

        // skip roll animation, when rolling hedgehog is stopped
        state.setTimer(0.5f);

        previousDirectionLeft = speed <= 0;
        speed = 0;
        getCallbacks().stomp(this);
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        boolean updateDirection = false;
        if (enemy instanceof Hedgehog) {
            Hedgehog otherHedgehog = (Hedgehog) enemy;
            if (!state.is(State.ROLLING) && otherHedgehog.getState() == State.ROLLING) {
                kill(true);
                return;
            }
        }

        if (state.is(State.WALKING)) {
            updateDirection = true;
        }

        if (updateDirection) {
            runAwayFrom(enemy);
            getCallbacks().hitWall(this);
        }
    }

    private void runAwayFrom(Enemy otherEnemy) {
        speed = (getBody().getPosition().x < otherEnemy.getBody().getPosition().x)
                ? -SPEED_VALUE : SPEED_VALUE;
    }

    public void changeDirectionBySideSensorTag(String sideSensorTag) {
        float absoluteSpeed = Math.abs(speed);
        if (sideSensorTag.equals(TAG_LEFT)) {
            speed = absoluteSpeed;
        } else {
            speed = -absoluteSpeed;
        }

        getCallbacks().hitWall(this);
    }

    public void kick(boolean directionRight) {
        state.set(State.ROLLING);
        speed = directionRight ? KICK_SPEED : -KICK_SPEED;
        getCallbacks().kicked(this);
    }

    public State getState() {
        return state.current();
    }

    @Override
    public void drown() {
        drowning = true;
        getBody().setLinearVelocity(getBody().getLinearVelocity().x / 10, getBody().getLinearVelocity().y / 10);
    }

    @Override
    public boolean isDrowning() {
        return drowning;
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
        out.writeFloat(speed);
        out.writeBoolean(previousDirectionLeft);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        speed = in.readFloat();
        previousDirectionLeft = in.readBoolean();
    }
}
