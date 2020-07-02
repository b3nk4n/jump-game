package de.bsautermeister.jump.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.sprites.GameObjectState;
import de.bsautermeister.jump.sprites.Player;

public class Raven extends Enemy {
    private static final float SWING_DISTANCE = Cfg.BLOCK_SIZE_PPM / 4;
    private static final float SWING_VELOCITY = 0.5f;
    private static final float CRASH_VELOCITY = 4f;
    private static final float ATTACK_VELOCITY = 4f;

    public enum State {
        WAITING, SWINGING, SPOTTED, ATTACKING, LEAVING, CRASHING
    }

    private GameObjectState<State> state;
    private boolean drowning;
    private boolean isLeft;

    private boolean upwards;
    private float upperY;
    private float lowerY;

    private final Vector2 playerPosition = new Vector2();

    private final Animation<TextureRegion> flyingAnimation;
    private final Animation<TextureRegion> spottedAnimation;
    private final Animation<TextureRegion> attackingAnimation;
    private final Animation<TextureRegion> crashingAnimation;

    public Raven(GameCallbacks callbacks, World world, TextureAtlas atlas, float posX, float posY,
                 boolean rightDirection, boolean swinging) {
        super(callbacks, world, posX, posY, Cfg.BLOCK_SIZE_PPM, Cfg.BLOCK_SIZE_PPM);
        this.isLeft = !rightDirection;
        this.upperY = getBody().getPosition().y + SWING_DISTANCE;
        this.lowerY = getBody().getPosition().y - SWING_DISTANCE;
        this.upwards = true;

        flyingAnimation = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.RAVEN_FLYING), Animation.PlayMode.LOOP);
        spottedAnimation = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.RAVEN_SPOTTED), Animation.PlayMode.NORMAL);
        attackingAnimation = new Animation<TextureRegion>(0.25f,
                atlas.findRegions(RegionNames.RAVEN_ATTACKING), Animation.PlayMode.LOOP);
        crashingAnimation = new Animation<TextureRegion>(0.066f,
                atlas.findRegions(RegionNames.RAVEN_CRASHING), Animation.PlayMode.LOOP);

        state = new GameObjectState<>(swinging ? State.SWINGING : State.WAITING);
        setRegion(getFrame());
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        state.upate(delta);
        setRegion(getFrame());

        if (isDead() || state.is(State.CRASHING)) {
            // TODO implement
            Vector2 currentVelocity = getBody().getLinearVelocity();
            currentVelocity.y = -CRASH_VELOCITY;
            currentVelocity.x = isLeft ? -CRASH_VELOCITY / 2f : CRASH_VELOCITY / 2f;
            if (state.timer() >= 1f) {
                currentVelocity.x /= state.timer();
            }
            getBody().setLinearVelocity(currentVelocity);
        } else {
            if (state.is(State.SWINGING)) {
                if (getBody().getPosition().y > upperY) {
                    upwards = false;
                } else if (getBody().getPosition().y < lowerY) {
                    upwards = true;
                }

                Vector2 currentVelocity = getBody().getLinearVelocity();
                if (upwards){
                    currentVelocity.y += SWING_VELOCITY * delta;
                } else {
                    currentVelocity.y -= SWING_VELOCITY * delta;
                }
                currentVelocity.y = MathUtils.clamp(currentVelocity.y, -SWING_VELOCITY, SWING_VELOCITY);
                getBody().setLinearVelocity(currentVelocity);
            }

            if (state.is(State.ATTACKING)) {
                getBody().setLinearVelocity(isLeft ? -ATTACK_VELOCITY : ATTACK_VELOCITY, -ATTACK_VELOCITY);
            }

            if (state.is(State.SPOTTED)) {
                getBody().setLinearVelocity(Vector2.Zero);
                if (state.timer() > 0.2f) {
                    state.set(State.ATTACKING);
                }
            }

            if (state.is(State.SWINGING) || state.is(State.WAITING)) {
                float angle = MathUtils.atan2(
                        playerPosition.y - getBody().getPosition().y,
                        playerPosition.x - getBody().getPosition().x
                ) * 180.0f / MathUtils.PI;
                if (state.is(State.SWINGING)) {
                    System.out.println("Angle: " + angle);
                }
                if (angle < -130 && angle > -140) {
                    state.set(State.SPOTTED);
                }
            }
        }

        setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2);
    }

    private TextureRegion getFrame() {
        TextureRegion textureRegion;
        switch (state.current()) {
            case CRASHING:
                textureRegion = crashingAnimation.getKeyFrame(state.timer());
                break;
            case SPOTTED:
                textureRegion = spottedAnimation.getKeyFrame(state.timer());
                break;
            case ATTACKING:
                textureRegion = attackingAnimation.getKeyFrame(state.timer());
                break;
            case WAITING:
            case LEAVING:
            default:
                textureRegion = flyingAnimation.getKeyFrame(state.timer());
                break;
        }

        if (!isLeft && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (isLeft && !textureRegion.isFlipX()) {
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
        bodyDef.type = BodyDef.BodyType.KinematicBody;
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
        vertices[0] = new Vector2(-3.5f, 10).scl(1 / Cfg.PPM);
        vertices[1] = new Vector2(3.5f, 10).scl(1 / Cfg.PPM);
        vertices[2] = new Vector2(-2.5f, 4).scl(1 / Cfg.PPM);
        vertices[3] = new Vector2(2.5f, 4).scl(1 / Cfg.PPM);
        headShape.set(vertices);

        fixtureDef.shape = headShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_HEAD;
        fixtureDef.filter.maskBits = Bits.PLAYER_FEET;
        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
        return body;
    }

    @Override
    public void onHeadHit(Player player) {
        if (player.isDead() || player.isInvincible()) {
            return;
        }
        Vector2 velocity = getBody().getLinearVelocity();
        getBody().setLinearVelocity(velocity.x * 0.5f, velocity.y);
        stomp();
    }

    private void stomp() {
        getCallbacks().stomp(this);

        state.set(State.CRASHING);
        updateMaskFilter(Bits.ENVIRONMENT_ONLY);
    }
    public void setPlayerPosition(Vector2 playerPosition) {
        this.playerPosition.set(playerPosition);
    }


    @Override
    public void onEnemyHit(Enemy enemy) {
        // NOOP
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
        out.writeBoolean(isLeft);
        out.writeFloat(upperY);
        out.writeFloat(lowerY);
        out.writeBoolean(upwards);
        out.writeFloat(playerPosition.x);
        out.writeFloat(playerPosition.y);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        isLeft = in.readBoolean();
        upperY = in.readFloat();
        lowerY = in.readFloat();
        upwards = in.readBoolean();
        playerPosition.set(in.readFloat(), in.readFloat());
    }
}
