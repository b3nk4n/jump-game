package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.serializer.BinarySerializable;
import de.bsautermeister.jump.tools.GameTimer;

public class Mario extends Sprite implements BinarySerializable, Drownable {

    public static final float INITAL_TTL = 200;

    private static final short NORMAL_FILTER_BITS = Bits.GROUND |
            Bits.PLATFORM |
            Bits.ITEM_BOX |
            Bits.BRICK |
            Bits.ENEMY |
            Bits.ENEMY_HEAD |
            Bits.ENEMY_SIDE |
            Bits.OBJECT |
            Bits.ITEM;

    private static final short NO_ENEMY_FILTER_BITS = Bits.GROUND |
            Bits.PLATFORM |
            Bits.ITEM_BOX |
            Bits.BRICK |
            Bits.ENEMY_SIDE | // to still block the Flower
            Bits.OBJECT |
            Bits.ITEM;

    private GameCallbacks callbacks;
    private World world;
    private TextureAtlas atlas;
    private Body body;

    private Platform platformContact;
    private int groundContactCounter;

    private float blockJumpTimer;

    public enum State {
        STANDING, CROUCHING, JUMPING, WALKING, DROWNING, DEAD
    }

    private GameObjectState<State> state;

    private boolean runningRight;

    private TextureRegion marioStand;
    private TextureRegion marioDead;
    private TextureRegion marioTurn;
    private Animation<TextureRegion> marioWalk;
    private TextureRegion marioJump;
    private TextureRegion marioDrown;

    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private TextureRegion bigMarioTurn;
    private Animation<TextureRegion> bigMarioWalk;
    private TextureRegion bigMarioCrouch;
    private TextureRegion bigMarioDrown;

    private TextureRegion bigMarioOnFireStand;
    private TextureRegion bigMarioOnFireJump;
    private TextureRegion bigMarioOnFireTurn;
    private Animation<TextureRegion> bigMarioOnFireWalk;
    private TextureRegion bigMarioOnFireCrouch;
    private TextureRegion bigMarioOnFireDrown;

    ParticleEffect slideEffect = new ParticleEffect();

    private boolean isTurning;

    private final GameTimer changeSizeTimer;

    private boolean onFire = true;
    private boolean doFire = false;
    private Fireball fireball;

    private boolean isBig = true;
    private boolean markRedefineBody;

    private boolean deadAnimationStarted = false;

    private float timeToLive;

    private boolean levelCompleted;

    private String lastJumpThroughPlatformId;

    public Mario(GameCallbacks callbacks, World world, TextureAtlas atlas) {
        this.callbacks = callbacks;
        this.world = world;
        this.atlas = atlas;
        initTextures(atlas);

        state = new GameObjectState<State>(State.STANDING);
        runningRight = true;

        Vector2 startPosition = new Vector2(2 * Cfg.BLOCK_SIZE / Cfg.PPM, 2 * Cfg.BLOCK_SIZE / Cfg.PPM);
        defineBigBody(startPosition, true);

        setBounds(body.getPosition().x, body.getPosition().y,
                Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        setRegion(marioStand);

        timeToLive = INITAL_TTL;

        slideEffect.load(Gdx.files.internal(AssetPaths.Pfx.SLIDE_SMOKE), atlas);
        slideEffect.scaleEffect(0.1f / Cfg.PPM);

        changeSizeTimer = new GameTimer(2f);
        changeSizeTimer.setCallbacks(new GameTimer.TimerCallbacks() {
            @Override
            public void onStart() {
                // body is already created to not collide with enemies when changing the body
            }

            @Override
            public void onFinish() {
                Filter filter = new Filter();
                filter.categoryBits = Bits.MARIO;
                filter.maskBits = NORMAL_FILTER_BITS;
                getBody().getFixtureList().get(0).setFilterData(filter);
            }
        });

        fireball = new Fireball(callbacks, world, atlas);
    }

    private void initTextures(TextureAtlas atlas) {
        marioStand = atlas.findRegion(RegionNames.LITTLE_MARIO_STAND);
        marioWalk = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.LITTLE_MARIO_WALK), Animation.PlayMode.LOOP_PINGPONG);
        marioTurn = atlas.findRegion(RegionNames.LITTLE_MARIO_TURN);
        marioJump = atlas.findRegion(RegionNames.LITTLE_MARIO_JUMP);
        marioDrown = atlas.findRegion(RegionNames.LITTLE_MARIO_DROWN);
        marioDead = atlas.findRegion(RegionNames.LITTLE_MARIO_DEAD);

        bigMarioStand = atlas.findRegion(RegionNames.BIG_MARIO_STAND);
        bigMarioWalk = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.BIG_MARIO_WALK), Animation.PlayMode.LOOP_PINGPONG);
        bigMarioTurn = atlas.findRegion(RegionNames.BIG_MARIO_TURN);
        bigMarioJump = atlas.findRegion(RegionNames.BIG_MARIO_JUMP);
        bigMarioCrouch = atlas.findRegion(RegionNames.BIG_MARIO_CROUCH);
        bigMarioDrown = atlas.findRegion(RegionNames.BIG_MARIO_DROWN);

        bigMarioOnFireStand = atlas.findRegion(RegionNames.BIG_MARIO_STAND_ON_FIRE);
        bigMarioOnFireWalk = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.BIG_MARIO_WALK_ON_FIRE), Animation.PlayMode.LOOP_PINGPONG);
        bigMarioOnFireTurn = atlas.findRegion(RegionNames.BIG_MARIO_TURN_ON_FIRE);
        bigMarioOnFireJump = atlas.findRegion(RegionNames.BIG_MARIO_JUMP_ON_FIRE);
        bigMarioOnFireCrouch = atlas.findRegion(RegionNames.BIG_MARIO_CROUCH_ON_FIRE);
        bigMarioOnFireDrown = atlas.findRegion(RegionNames.BIG_MARIO_DROWN_ON_FIRE);
    }

    public void update(float delta) {
        state.upate(delta);

        if (!levelCompleted) {
            timeToLive -= delta;
        }

        if (Math.abs(getVelocityRelativeToGround().y) < 0.1) {
            // unblock jump if standing still for a while
            blockJumpTimer -= delta;
        }

        if (timeToLive <= 0) {
            timeToLive = 0;
            kill();
        }

        if (isChangingSize()) {
            changeSizeTimer.update(delta);
        }

        TextureRegion textureRegion = getFrame();
        setRegion(textureRegion);

        // set texture bounds always at the bottom of the body
        float leftX = body.getPosition().x - getWidth() / 2;
        float bottomY = body.getPosition().y - 8f / Cfg.PPM;
        float textureWidth = textureRegion.getRegionWidth() / Cfg.PPM;
        float textureHeight = textureRegion.getRegionHeight() / Cfg.PPM;
        setBounds(leftX, bottomY, textureWidth, textureHeight);

        // these are called outside of the physics update loop
        if (markRedefineBody && isBig()) {
            Vector2 currentPosition = getBody().getPosition();
            world.destroyBody(getBody());
            defineBigBody(currentPosition, false);
            markRedefineBody = false;
        } else if (markRedefineBody && !isBig()) {
            Vector2 position = getBody().getPosition();
            world.destroyBody(getBody());
            defineSmallBody(position, false);
            markRedefineBody = false;
        }

        // check fallen out of game
        if (isOutOfGame()) {
            kill();
        }

        if (state.is(State.DEAD)) {
            if (state.timer() <= 0.5f) {
                getBody().setLinearVelocity(Vector2.Zero);
                getBody().setActive(false);
            } else if (!deadAnimationStarted) {
                getBody().setActive(true);
                if (!isOutOfGame()) {
                    getBody().applyLinearImpulse(new Vector2(0, 4.5f), getBody().getWorldCenter(), true);
                }
                deadAnimationStarted = true;
            }
        }

        if (isTurning && touchesGround() && !isDead() && !state.is(State.JUMPING)) {
            slideEffect.setPosition(getX() + getWidth() / 2, getY());
            slideEffect.start();

            // particle emission direction
            ParticleEmitter.ScaledNumericValue angle = slideEffect.getEmitters().get(0).getAngle();
            if (textureRegion.isFlipX()) {
                angle.setLow(-10f);
                angle.setHigh(30f);
            } else {
                angle.setLow(150f);
                angle.setHigh(190f);
            }
        }

        if (isDrowning()) {
            body.setLinearVelocity(body.getLinearVelocity().x * 0.95f, body.getLinearVelocity().y * 0.33f);
        }
    }

    @Override
    public void drown() {
        state.set(State.DROWNING);
        body.setLinearVelocity(body.getLinearVelocity().x / 10, body.getLinearVelocity().y / 10);
    }

    public void control(boolean up, boolean down, boolean left, boolean right, boolean fire) {
        if (isDead() || isDrowning()) {
            return;
        }

        if (fire) {
            tryFire();
        }

        Vector2 relativeBodyVelocity = getVelocityRelativeToGround();

        state.unfreeze();
        isTurning = right && relativeBodyVelocity.x < 0 || left && relativeBodyVelocity.x > 0;

        if (up && touchesGround() && !state.is(State.JUMPING) && blockJumpTimer <= 0) {
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
            state.set(State.JUMPING);
            blockJumpTimer = 0.05f;
            callbacks.jump();
            return;
        }
        if (right && body.getLinearVelocity().x <= 2 && !down) {
            body.applyForceToCenter(new Vector2(7.5f, 0), true);
        }
        if (left && body.getLinearVelocity().x >= -2 && !down) {
            body.applyForceToCenter(new Vector2(-7.5f, 0), true);
        }
        if ((!left && !right && state.is(State.JUMPING))) {
            // horizontally decelerate fast, but don't stop immediately
            body.applyForceToCenter(new Vector2(-5 * relativeBodyVelocity.x, 0), true);
        }
        if (down) {
            body.applyForceToCenter(new Vector2(0f, -2f), true);
        }

        if (!touchesGround()) {
            if (blockJumpTimer > 0 || state.is(State.JUMPING)) {
                // keep jumping state
                return;
            } else {
                state.set(State.WALKING);
                state.freeze();
            }
        } else if (blockJumpTimer < 0 /*touchesGround() && Math.abs(getVelocityRelativeToGround().y) < 0.1*/) {
            if (down && isBig) {
                state.set(State.CROUCHING);
            } else if (Math.abs(relativeBodyVelocity.x) > 1e-4) {
                state.set(State.WALKING);
            } else {
                state.set(State.STANDING);
            }
        }

        if (right && !left) {
            runningRight = true;
        } else if (!right && left) {
            runningRight = false;
        }
    }

    public void tryFire() {
        if (!isOnFire()) {
            return;
        }

        if (!fireball.isActive()) {
            float offsetX = runningRight ? getWidth() : 0f;
            fireball.fire(getX() + offsetX, getY() + getHeight() / 2, runningRight);
            callbacks.fire();
        }
    }

    public Vector2 getVelocityRelativeToGround() {
        Vector2 relativeBodyVelocity;
        if (platformContact != null) {
            relativeBodyVelocity = platformContact.getRelativeVelocityOf(body);
        } else {
            relativeBodyVelocity = body.getLinearVelocity();
        }
        return relativeBodyVelocity;
    }

    private TextureRegion getFrame() {
        TextureRegion textureRegion;

        boolean useBigTexture = isBig;
        if (isChangingSize()) {
            useBigTexture = (int) (((changeSizeTimer.getValue() - (int) changeSizeTimer.getValue())) * 8) % 2 == 0;
        }

        switch (state.current()) {
            case DROWNING:
                if (useBigTexture) {
                    textureRegion = onFire ? bigMarioOnFireDrown : bigMarioDrown;
                } else {
                    textureRegion = marioDrown;
                }
                break;
            case DEAD:
                textureRegion = marioDead;
                break;
            case JUMPING:
                if (useBigTexture) {
                    textureRegion = onFire ? bigMarioOnFireJump : bigMarioJump;
                } else {
                    textureRegion = marioJump;
                }
                break;
            case WALKING:
                if (useBigTexture) {
                    if (isTurning) {
                        textureRegion = onFire ? bigMarioOnFireTurn : bigMarioTurn;
                    } else {
                        Animation<TextureRegion> walk = onFire ? bigMarioOnFireWalk : bigMarioWalk;
                        textureRegion = walk.getKeyFrame(state.timer(), true);
                    }

                } else {
                    if (isTurning) {
                        textureRegion = marioTurn;
                    } else {
                        textureRegion = marioWalk.getKeyFrame(state.timer(), true);
                    }
                }
                break;
            case CROUCHING:
                textureRegion = onFire ? bigMarioOnFireCrouch : bigMarioCrouch;
                break;
            case STANDING:
            default:
                if (useBigTexture) {
                    textureRegion = onFire ? bigMarioOnFireStand : bigMarioStand;
                } else {
                    textureRegion = marioStand;
                }
                break;
        }

        if (!runningRight && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (runningRight && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

        return textureRegion;
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);

        slideEffect.draw(batch, Gdx.graphics.getDeltaTime());
    }

    public State getState() {
        return state.current();
    }

    private void defineSmallBody(Vector2 position, boolean normalFilterMask) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.8f;
        final float[] SMALL_POLYGON_VERTICES = {
                -3f / Cfg.PPM, 6f / Cfg.PPM,
                3f / Cfg.PPM, 6f / Cfg.PPM,
                5f / Cfg.PPM, -2f / Cfg.PPM,
                5f / Cfg.PPM, -4f / Cfg.PPM,
                3f / Cfg.PPM, -4.5f / Cfg.PPM,
                -3f / Cfg.PPM, -4.5f / Cfg.PPM,
                -5f / Cfg.PPM, -4f / Cfg.PPM,
                -5f / Cfg.PPM, -2f / Cfg.PPM
        };
        createBodyFixture(fixtureDef, SMALL_POLYGON_VERTICES, normalFilterMask);
        createFeetFixture(fixtureDef, 9.33f, -6.5f);
        createHeadSensorFixture(fixtureDef, 4f, 6.1f);
        createGroundSensorFixture(fixtureDef, 9f, -7f);
    }

    private void defineBigBody(Vector2 position, boolean normalFilterMask) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.8f;
        final float[] BIG_POLYGON_VERTICES = {
                -2.5f / Cfg.PPM, 21f / Cfg.PPM,
                2.5f / Cfg.PPM, 21f / Cfg.PPM,
                5.5f / Cfg.PPM, 8f / Cfg.PPM,
                5.5f / Cfg.PPM, -1f / Cfg.PPM,
                3f / Cfg.PPM, -4.5f / Cfg.PPM,
                -3f / Cfg.PPM, -4.5f / Cfg.PPM,
                -5.5f / Cfg.PPM, -1f / Cfg.PPM,
                -5.5f / Cfg.PPM, 8f / Cfg.PPM,
        };
        createBodyFixture(fixtureDef, BIG_POLYGON_VERTICES, normalFilterMask);
        createFeetFixture(fixtureDef, 10f, -6.5f);
        createHeadSensorFixture(fixtureDef, 4f, 21.1f);
        createGroundSensorFixture(fixtureDef, 9.33f, -7f);
    }

    private void createBodyFixture(FixtureDef fixtureDef, float[] smallPolygonVertices, boolean normalFilterMask) {
        PolygonShape shape = new PolygonShape();
        shape.set(smallPolygonVertices);
        fixtureDef.filter.categoryBits = Bits.MARIO;
        fixtureDef.filter.maskBits = normalFilterMask ?
                NORMAL_FILTER_BITS : NO_ENEMY_FILTER_BITS;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
    }

    /**
     * Feet as edge shape to circumvent edge-to-edge collision.
     */
    private void createFeetFixture(FixtureDef fixtureDef, float width, float bottomY) {
        EdgeShape feetShape = new EdgeShape();
        feetShape.set(-width / 2 / Cfg.PPM, bottomY / Cfg.PPM,
                width / 2 / Cfg.PPM, bottomY / Cfg.PPM);
        fixtureDef.shape = feetShape;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.OBJECT;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
    }

    private void createHeadSensorFixture(FixtureDef fixtureDef, float width, float topY) {
        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM),
                new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM));
        fixtureDef.filter.categoryBits = Bits.MARIO_HEAD;
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
    }

    /**
     * Sensor to indicate we are standing on ground.
     */
    private void createGroundSensorFixture(FixtureDef fixtureDef, float width, float bottomY) {
        EdgeShape groundSensorShape = new EdgeShape();
        groundSensorShape.set(new Vector2(-width / 2 / Cfg.PPM, bottomY / Cfg.PPM),
                new Vector2(width / 2 / Cfg.PPM, bottomY / Cfg.PPM));
        fixtureDef.filter.categoryBits = Bits.MARIO_FEET;
        fixtureDef.shape = groundSensorShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        body.createFixture(fixtureDef).setUserData(this);
    }

    public Body getBody() {
        return body;
    }

    public boolean isBig() {
        return isBig;
    }

    @Override
    public boolean isDead() {
        return state.is(State.DEAD);
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
        return body.getLinearVelocity();
    }

    @Override
    public boolean isDrowning() {
        return state.is(State.DROWNING);
    }

    public float getStateTimer() {
        return state.timer();
    }

    public void grow() {
        if (!isBig()) {
            changeSizeTimer.restart();
            isBig = true;
            markRedefineBody = true;
        }
    }

    private void shrinkOrKill(Enemy enemy) {
        if (isBig()) {
            changeSizeTimer.restart();
            callbacks.hit(this, enemy);
            isBig = false;
            markRedefineBody = true;

            if (isOnFire()) {
                onFire = false;
            }
        } else {
            kill();
        }
    }

    public void setOnFire() {
        onFire = true;
    }

    public boolean isOnFire() {
        return onFire;
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa) enemy;
            if (koopa.getState() == Koopa.State.STANDING_SHELL) {
                koopa.kick(getX() <= enemy.getX());
                return;
            }
        }

        if (!isInvincible()) {
            shrinkOrKill(enemy);
        }
    }

    private void kill() {
        if (state.is(State.DEAD))
            return;

        callbacks.gameOver();

        state.set(State.DEAD);

        Filter filter = new Filter();
        filter.maskBits = Bits.NOTHING;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }

    public float getTimeToLive() {
        return timeToLive;
    }

    public void touchGround(Object ground) {
        groundContactCounter++;

        if (ground instanceof Platform) {
            platformContact = (Platform) ground;
        }
    }

    public void leftGround(Object ground) {
        groundContactCounter--;
        if (ground == platformContact) {
            platformContact = null;
        }
    }

    public boolean touchesGround() {
        return groundContactCounter > 0;
    }

    public boolean isChangingSize() {
        return changeSizeTimer.isRunning();
    }

    public boolean isInvincible() {
        return isChangingSize();
    }

    private boolean isOutOfGame() {
        return getY() + getHeight() < 0;
    }

    public void setLevelCompleted(boolean levelCompleted) {
        this.levelCompleted = levelCompleted;

        body.setActive(false);

        /*Filter filter = new Filter();
        filter.maskBits = NO_ENEMY_FILTER_BITS;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }*/
    }

    public String getLastJumpThroughPlatformId() {
        return lastJumpThroughPlatformId;
    }

    public boolean hasLastJumpThroughPlatformId() {
        return lastJumpThroughPlatformId != null;
    }

    public void setLastJumpThroughPlatformId(String lastJumpThroughPlatformId) {
        this.lastJumpThroughPlatformId = lastJumpThroughPlatformId;
    }

    public Fireball getFireball() {
        return fireball;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(blockJumpTimer);
        state.write(out);
        out.writeBoolean(runningRight);
        out.writeBoolean(isTurning);
        changeSizeTimer.write(out);
        out.writeBoolean(onFire);
        out.writeBoolean(isBig);
        out.writeBoolean(markRedefineBody);
        out.writeBoolean(deadAnimationStarted);
        out.writeFloat(timeToLive);
        out.writeBoolean(levelCompleted);
        out.writeUTF(lastJumpThroughPlatformId != null ? lastJumpThroughPlatformId : "null");
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        blockJumpTimer = in.readFloat();
        state.read(in);
        runningRight = in.readBoolean();
        isTurning = in.readBoolean();
        changeSizeTimer.read(in);
        onFire = in.readBoolean();
        isBig = in.readBoolean();
        markRedefineBody = in.readBoolean();
        deadAnimationStarted = in.readBoolean();
        timeToLive = in.readFloat();
        levelCompleted = in.readBoolean();
        lastJumpThroughPlatformId = in.readUTF();
        if (lastJumpThroughPlatformId.equals("null")) {
            lastJumpThroughPlatformId = null;
        }

        // this is just a lazy workaround, that is needed because we generally create a small mario by default
        if (isBig) {
            Vector2 currentPosition = body.getPosition();
            world.destroyBody(getBody());
            defineBigBody(currentPosition, !changeSizeTimer.isRunning());
        }
    }
}
