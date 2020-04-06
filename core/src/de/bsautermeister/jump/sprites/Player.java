package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.WorldCreator;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.serializer.BinarySerializable;
import de.bsautermeister.jump.tools.GameTimer;

public class Player extends Sprite implements BinarySerializable, Drownable {

    public static final float INITAL_TTL = 200;

    private static final float EFFECT_DURATION = 10f;

    private static final short NORMAL_FILTER_BITS = Bits.GROUND |
            Bits.PLATFORM |
            Bits.ITEM_BOX |
            Bits.BRICK |
            Bits.ENEMY |
            Bits.ENEMY_HEAD |
            Bits.ENEMY_SIDE |
            Bits.ITEM;

    private static final short NO_ENEMY_FILTER_BITS = Bits.GROUND |
            Bits.PLATFORM |
            Bits.ITEM_BOX |
            Bits.BRICK |
            Bits.ENEMY_SIDE | // to still block the DrunkenGuy
            Bits.ITEM;

    private GameCallbacks callbacks;
    private World world;
    private TextureAtlas atlas;
    private Body body;

    private Platform platformContact;
    private int groundContactCounter;

    private float blockJumpTimer;

    public enum State {
        STANDING, CROUCHING, JUMPING, WALKING, VICTORY, DROWNING, DEAD
    }

    private GameObjectState<State> state;

    private boolean runningRight;

    private static final int VICTORY_VARIATIONS = 2;
    private static final int CHARACTER_LEVELS = 4;
    private float characterProgress;

    private TextureRegion[] smallPlayerStand;
    private Animation<TextureRegion>[] smallPlayerDead;
    private TextureRegion[] smallPlayerTurn;
    private Animation<TextureRegion>[] smallPlayerWalk;
    private Animation<TextureRegion>[] smallPlayerJump;
    private TextureRegion[] smallPlayerCrouch;
    private Animation<TextureRegion>[] smallPlayerDrown;
    private Animation<TextureRegion>[] smallPlayerVictory;

    private TextureRegion[] bigPlayerStand;
    private Animation<TextureRegion>[] bigPlayerDead;
    private TextureRegion[] bigPlayerTurn;
    private Animation<TextureRegion>[] bigPlayerWalk;
    private Animation<TextureRegion>[] bigPlayerJump;
    private TextureRegion[] bigPlayerCrouch;
    private Animation<TextureRegion>[] bigPlayerDrown;
    private Animation<TextureRegion>[] bigPlayerVictory;

    private TextureRegion[] bigPlayerPrezelizedStand;
    private TextureRegion[] bigPlayerPrezelizedTurn;
    private Animation<TextureRegion>[] bigPlayerPrezelizedWalk;
    private Animation<TextureRegion>[] bigPlayerPrezelizedJump;
    private TextureRegion[] bigPlayerPrezelizedCrouch;

    ParticleEffect slideEffect = new ParticleEffect();

    private boolean isTurning;

    private final GameTimer changeSizeTimer;

    private boolean pretzelized;
    private GameTimer throwPretzelTimer;
    private PretzelBullet pretzelBullet;

    private boolean isBig;
    private boolean markRedefineBody;

    private boolean deadAnimationStarted = false;

    private float timeToLive;

    private String lastJumpThroughPlatformId;

    private GameTimer drunkTimer;
    private GameTimer hammeredTimer;

    private final int randomVictoryIdx;

    public Player(GameCallbacks callbacks, World world, TextureAtlas atlas, WorldCreator.StartParams start) {
        this.callbacks = callbacks;
        this.world = world;
        this.atlas = atlas;
        initTextures(atlas);

        state = new GameObjectState<State>(State.STANDING);
        runningRight = !start.leftDirection;

        defineSmallBody(start.centerPosition, true);

        setBounds(body.getPosition().x, body.getPosition().y,
                Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        setRegion(smallPlayerStand[0]);

        timeToLive = INITAL_TTL;

        slideEffect.load(Gdx.files.internal(AssetPaths.Pfx.SLIDE_SMOKE), atlas);
        slideEffect.scaleEffect(0.1f / Cfg.PPM);

        changeSizeTimer = new GameTimer(0.75f);
        changeSizeTimer.setCallbacks(new GameTimer.TimerCallbacks() {
            @Override
            public void onStart() {
                // body is already created to not collide with enemies when changing the body
            }

            @Override
            public void onFinish() {
                Filter filter = new Filter();
                filter.categoryBits = Bits.PLAYER;
                filter.maskBits = NORMAL_FILTER_BITS;
                getBody().getFixtureList().get(0).setFilterData(filter);
            }
        });

        pretzelBullet = new PretzelBullet(callbacks, world, atlas);
        throwPretzelTimer = new GameTimer(1.0f, true);

        drunkTimer = new GameTimer(EFFECT_DURATION);
        hammeredTimer = new GameTimer(EFFECT_DURATION);

        randomVictoryIdx = MathUtils.random(VICTORY_VARIATIONS - 1);
    }

    private TextureRegion[] createTextureArray(String templateName, int count) {
        TextureRegion[] result = new TextureRegion[count];
        for (int i = 0; i < count; ++i) {
            result[i] = atlas.findRegion(RegionNames.fromTemplate(templateName, i));
        }
        return result;
    }

    private Animation<TextureRegion>[] createAnimationArray(
            String templateName, int count, float frameDuration, Animation.PlayMode playMode) {
        Animation<TextureRegion>[] result = new Animation[count];
        for (int i = 0; i < count; ++i) {
            result[i] = new Animation<TextureRegion>(frameDuration,
                    atlas.findRegions(RegionNames.fromTemplate(templateName, i)), playMode);
        }
        return result;
    }

    private void initTextures(TextureAtlas atlas) {
        smallPlayerStand = createTextureArray(RegionNames.SMALL_PLAYER_STAND_TPL, CHARACTER_LEVELS);
        smallPlayerWalk = createAnimationArray(RegionNames.SMALL_PLAYER_WALK_TPL, CHARACTER_LEVELS,
                0.1f, Animation.PlayMode.LOOP_PINGPONG);
        smallPlayerTurn = createTextureArray(RegionNames.SMALL_PLAYER_TURN_TPL, CHARACTER_LEVELS);
        smallPlayerJump = createAnimationArray(RegionNames.SMALL_PLAYER_JUMP_TPL, CHARACTER_LEVELS,
                0.125f, Animation.PlayMode.NORMAL);
        smallPlayerCrouch = createTextureArray(RegionNames.SMALL_PLAYER_CROUCH_TPL, CHARACTER_LEVELS);
        smallPlayerDrown = createAnimationArray(RegionNames.SMALL_PLAYER_DROWN_TPL, CHARACTER_LEVELS,
                0.25f, Animation.PlayMode.LOOP);
        smallPlayerDead = createAnimationArray(RegionNames.SMALL_PLAYER_DEAD_TPL, CHARACTER_LEVELS,
                0.25f, Animation.PlayMode.LOOP);
        smallPlayerVictory = new Animation[VICTORY_VARIATIONS];
        smallPlayerVictory[0] = new Animation<TextureRegion>(0.125f,
                atlas.findRegions(RegionNames.SMALL_PLAYER_VICTORY), Animation.PlayMode.NORMAL);
        smallPlayerVictory[1] = new Animation<TextureRegion>(0.125f,
                atlas.findRegions(RegionNames.SMALL_PLAYER_BEER_VICTORY), Animation.PlayMode.NORMAL);

        bigPlayerStand = createTextureArray(RegionNames.BIG_PLAYER_STAND_TPL, CHARACTER_LEVELS);
        bigPlayerWalk = createAnimationArray(RegionNames.BIG_PLAYER_WALK_TPL, CHARACTER_LEVELS,
                0.1f, Animation.PlayMode.LOOP_PINGPONG);
        bigPlayerTurn = createTextureArray(RegionNames.BIG_PLAYER_TURN_TPL, CHARACTER_LEVELS);
        bigPlayerJump = createAnimationArray(RegionNames.BIG_PLAYER_JUMP_TPL, CHARACTER_LEVELS,
                0.125f, Animation.PlayMode.NORMAL);
        bigPlayerCrouch = createTextureArray(RegionNames.BIG_PLAYER_CROUCH_TPL, CHARACTER_LEVELS);
        bigPlayerDrown = createAnimationArray(RegionNames.BIG_PLAYER_DROWN_TPL, CHARACTER_LEVELS,
                0.25f, Animation.PlayMode.LOOP);
        bigPlayerDead = createAnimationArray(RegionNames.BIG_PLAYER_DEAD_TPL, CHARACTER_LEVELS,
                0.25f, Animation.PlayMode.LOOP);
        bigPlayerVictory = new Animation[VICTORY_VARIATIONS];
        bigPlayerVictory[0] = new Animation<TextureRegion>(0.125f,
                atlas.findRegions(RegionNames.BIG_PLAYER_VICTORY), Animation.PlayMode.NORMAL);
        bigPlayerVictory[1] = new Animation<TextureRegion>(0.125f,
                atlas.findRegions(RegionNames.BIG_PLAYER_BEER_VICTORY), Animation.PlayMode.NORMAL);

        bigPlayerPrezelizedStand = createTextureArray(RegionNames.BIG_PLAYER_PREZELIZED_STAND_TPL, CHARACTER_LEVELS);
        bigPlayerPrezelizedWalk = createAnimationArray(RegionNames.BIG_PLAYER_PREZELIZED_WALK_TPL, CHARACTER_LEVELS,
                0.1f, Animation.PlayMode.LOOP_PINGPONG);
        bigPlayerPrezelizedTurn = createTextureArray(RegionNames.BIG_PLAYER_PREZELIZED_TURN_TPL, CHARACTER_LEVELS);
        bigPlayerPrezelizedJump = createAnimationArray(RegionNames.BIG_PLAYER_PREZELIZED_JUMP_TPL, CHARACTER_LEVELS,
                0.125f, Animation.PlayMode.NORMAL);
        bigPlayerPrezelizedCrouch = createTextureArray(RegionNames.BIG_PLAYER_PREZELIZED_CROUCH_TPL, CHARACTER_LEVELS);
    }

    public void update(float delta) {
        state.upate(delta);

        if (!isVictory() && !isDead()) {
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

        throwPretzelTimer.update(delta);
        drunkTimer.update(delta);
        hammeredTimer.update(delta);

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

        // limit falling speed
        if (body.getLinearVelocity().y < Cfg.MAX_FALLING_SPEED) {
            body.setLinearVelocity(body.getLinearVelocity().x, Cfg.MAX_FALLING_SPEED);
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
            tryThrowPretzel();
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
                // keep jumping
            } else {
                state.set(State.WALKING);
                state.freeze();
            }
        } else if (blockJumpTimer < 0) {
            if (down) {
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

    public void tryThrowPretzel() {
        if (canThrowPretzel()) {
            float offsetX = runningRight ? getWidth() : 0f;
            float crouchOffset = state.is(State.CROUCHING) ? getHeight() / 4 : 0f;
            pretzelBullet.fire(getX() + offsetX, getY() + getHeight() / 2 - crouchOffset, runningRight);
            callbacks.fire();
            throwPretzelTimer.restart();
        }
    }

    private boolean canThrowPretzel() {
        return pretzelized && !pretzelBullet.isActive() && throwPretzelTimer.isFinished();
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

        int chIdx = (int)Math.floor(characterProgress * (CHARACTER_LEVELS - 1));

        boolean useBigTexture = isBig;
        if (isChangingSize()) {
            useBigTexture = (int) (((changeSizeTimer.getValue() - (int) changeSizeTimer.getValue())) * 8) % 2 == 0;
        }
        if (isVictory()) {
            if (useBigTexture) {
                return bigPlayerVictory[randomVictoryIdx].getKeyFrame(state.timer());
            } else {
                return smallPlayerVictory[randomVictoryIdx].getKeyFrame(state.timer());
            }
        }

        switch (state.current()) {
            case DROWNING:
                if (useBigTexture) {
                    textureRegion = bigPlayerDrown[chIdx].getKeyFrame(state.timer());
                } else {
                    textureRegion = smallPlayerDrown[chIdx].getKeyFrame(state.timer());
                }
                break;
            case DEAD:
                if (useBigTexture) {
                    textureRegion = bigPlayerDead[chIdx].getKeyFrame(state.timer());
                } else {
                    textureRegion = smallPlayerDead[chIdx].getKeyFrame(state.timer());
                }
                break;
            case JUMPING:
                if (useBigTexture) {
                    textureRegion = canThrowPretzel()
                            ? bigPlayerPrezelizedJump[chIdx].getKeyFrame(state.timer())
                            : bigPlayerJump[chIdx].getKeyFrame(state.timer());
                } else {
                    textureRegion = smallPlayerJump[chIdx].getKeyFrame(state.timer());
                }
                break;
            case WALKING:
                if (useBigTexture) {
                    if (isTurning) {
                        textureRegion = canThrowPretzel()
                                ? bigPlayerPrezelizedTurn[chIdx]
                                : bigPlayerTurn[chIdx];
                    } else {
                        Animation<TextureRegion> walk = canThrowPretzel()
                                ? bigPlayerPrezelizedWalk[chIdx]
                                : bigPlayerWalk[chIdx];
                        textureRegion = walk.getKeyFrame(state.timer());
                    }

                } else {
                    if (isTurning) {
                        textureRegion = smallPlayerTurn[chIdx];
                    } else {
                        textureRegion = smallPlayerWalk[chIdx].getKeyFrame(state.timer());
                    }
                }
                break;
            case CROUCHING:
                if (useBigTexture) {
                    textureRegion = canThrowPretzel()
                            ? bigPlayerPrezelizedCrouch[chIdx]
                            : bigPlayerCrouch[chIdx];
                } else {
                    textureRegion = smallPlayerCrouch[chIdx];
                }
                break;
            case STANDING:
            default:
                if (useBigTexture) {
                    textureRegion = canThrowPretzel()
                            ? bigPlayerPrezelizedStand[chIdx]
                            : bigPlayerStand[chIdx];
                } else {
                    textureRegion = smallPlayerStand[chIdx];
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
        CircleShape shape = new CircleShape();
        shape.setRadius(5.5f / Cfg.PPM);
        createBodyFixture(fixtureDef, shape, normalFilterMask);
        createFeetFixture(fixtureDef, 9.33f, -6.0f);
        createHeadSensorFixture(fixtureDef, 4f, 5.6f);
        createGroundSensorFixture(fixtureDef, 9f, -6.5f);
    }

    private void defineBigBody(Vector2 position, boolean normalFilterMask) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.8f;
        CircleShape shape = new CircleShape();
        shape.setRadius(5.5f / Cfg.PPM);
        createBodyFixture(fixtureDef, shape, normalFilterMask);
        shape.setPosition(new Vector2(0, 10f / Cfg.PPM));
        createBodyFixture(fixtureDef, shape, normalFilterMask);
        createFeetFixture(fixtureDef, 10f, -6f);
        createHeadSensorFixture(fixtureDef, 4f, 15.6f);
        createGroundSensorFixture(fixtureDef, 9.33f, -6.5f);
    }

    private void createBodyFixture(FixtureDef fixtureDef, Shape shape, boolean normalFilterMask) {
        fixtureDef.filter.categoryBits = Bits.PLAYER;
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
        fixtureDef.filter.categoryBits = Bits.PLAYER_FEET;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.ENEMY_HEAD |
                Bits.BRICK;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
    }

    private void createHeadSensorFixture(FixtureDef fixtureDef, float width, float topY) {
        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM),
                new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM));
        fixtureDef.filter.categoryBits = Bits.PLAYER_HEAD;
        fixtureDef.shape = headShape;
        //fixtureDef.isSensor = true; // does not collide in the physics simulation
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
        fixtureDef.filter.categoryBits = Bits.PLAYER_GROUND;
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

    public boolean isVictory() {
        return state.is(State.VICTORY);
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

    private void shrinkOrKill() {
        if (isBig()) {
            changeSizeTimer.restart();
            callbacks.hit(this);
            isBig = false;
            blockJumpTimer = 0.33f;
            markRedefineBody = true;

            if (isPretzelized()) {
                pretzelized = false;
            }
        } else {
            kill();
        }
    }

    public void pretzelize() {
        pretzelized = true;
    }

    public boolean isPretzelized() {
        return pretzelized;
    }

    public void drunk() {
        boolean useNormalDrunk;
        if (isDrunk()) {
            useNormalDrunk = false;
        } else if (isHammered()) {
            useNormalDrunk = true;
        } else {
            useNormalDrunk = MathUtils.random(1) == 0;
        }

        if (useNormalDrunk) {
            float skip = isDrunk() ? 0.1f * EFFECT_DURATION : 0f;
            drunkTimer.restart(skip);
        } else {
            float skip = isHammered() ? 0.05f * EFFECT_DURATION : 0f;
            hammeredTimer.restart(skip);
        }
    }

    public boolean isDrunk() {
        return drunkTimer.isRunning();
    }

    public float getDrunkRatio() {
        if (drunkTimer.getProgress() < 0.1) {
            return drunkTimer.getProgress() * 10f;
        }
        if (drunkTimer.getProgress() >= 0.9) {
            return (1f - drunkTimer.getProgress()) * 10f;
        }
        return 1f;
    }

    public boolean isHammered() {
        return hammeredTimer.isRunning();
    }

    public float getHammeredRatio() {
        if (hammeredTimer.getProgress() < 0.1) {
            return hammeredTimer.getProgress() * 5f;
        }
        if (hammeredTimer.getProgress() >= 0.9) {
            return (1f - hammeredTimer.getProgress()) * 5f;
        }
        return 0.5f;
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Hedgehog) {
            Hedgehog hedgehog = (Hedgehog) enemy;
            if (hedgehog.getState() == Hedgehog.State.ROLL) {
                hedgehog.kick(getX() <= enemy.getX());
                return;
            }
        }

        if (!isInvincible()) {
            shrinkOrKill();
        }
    }

    public void hit(Rectangle spike) {
        if (!isInvincible()) {
            shrinkOrKill();
        }
    }

    private void kill() {
        if (state.is(State.DEAD))
            return;

        callbacks.playerDied();

        state.set(State.DEAD);

        Filter filter = new Filter();
        filter.maskBits = Bits.NOTHING;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }

        // stop player effects
        if (isHammered()) {
            hammeredTimer.restart(0.9f * EFFECT_DURATION);
        }

        if (isDrunk()) {
            drunkTimer.restart(0.9f * EFFECT_DURATION);
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

    public boolean hasPlatformContact() {
        return platformContact != null;
    }

    public Platform getPlatformContact() {
        return platformContact;
    }

    public boolean isChangingSize() {
        return changeSizeTimer.isRunning();
    }

    public boolean isInvincible() {
        return isChangingSize();
    }

    private boolean isOutOfGame() {
        return getY() + getHeight() < 0 * Cfg.BLOCK_SIZE / Cfg.PPM;
    }

    public void victory() {
        state.set(State.VICTORY);
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

    public PretzelBullet getPretzelBullet() {
        return pretzelBullet;
    }

    public void pumpUp() {
        body.setLinearVelocity(getLinearVelocity().x, 3f);
    }

    public void setCharacterProgress(float characterProgress) {
        this.characterProgress = MathUtils.clamp(characterProgress, 0, 1);
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
        out.writeBoolean(pretzelized);
        throwPretzelTimer.write(out);
        pretzelBullet.write(out);
        out.writeBoolean(isBig);
        drunkTimer.write(out);
        hammeredTimer.write(out);
        out.writeBoolean(markRedefineBody);
        out.writeBoolean(deadAnimationStarted);
        out.writeFloat(timeToLive);
        out.writeUTF(lastJumpThroughPlatformId != null ? lastJumpThroughPlatformId : "null");
        out.writeFloat(characterProgress);
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
        pretzelized = in.readBoolean();
        throwPretzelTimer.read(in);
        pretzelBullet.read(in);
        isBig = in.readBoolean();
        drunkTimer.read(in);
        hammeredTimer.read(in);
        markRedefineBody = in.readBoolean();
        deadAnimationStarted = in.readBoolean();
        timeToLive = in.readFloat();
        lastJumpThroughPlatformId = in.readUTF();
        if (lastJumpThroughPlatformId.equals("null")) {
            lastJumpThroughPlatformId = null;
        }
        characterProgress = in.readFloat();

        // this is just a lazy workaround, that is needed because we generally create a small player by default
        if (isBig) {
            Vector2 currentPosition = body.getPosition();
            world.destroyBody(getBody());
            defineBigBody(currentPosition, !changeSizeTimer.isRunning());
        }
    }
}
