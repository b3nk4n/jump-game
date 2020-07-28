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
import de.bsautermeister.jump.sprites.enemies.Enemy;
import de.bsautermeister.jump.sprites.enemies.Hedgehog;
import de.bsautermeister.jump.tools.GameTimer;

public class Player extends Sprite implements BinarySerializable, Drownable {

    private static final short NO_ENEMY_FILTER_BITS = Bits.ENVIRONMENT_ONLY |
            Bits.ENEMY_SIDE | // to still block the DrunkenGuy
            Bits.ITEM;

    private static final short NORMAL_FILTER_BITS = NO_ENEMY_FILTER_BITS |
            Bits.ENEMY |
            Bits.ENEMY_HEAD;

    private GameCallbacks callbacks;
    private World world;
    private TextureAtlas atlas;
    private Body body;

    private Platform platformContact;
    private int groundContactCounter;

    private float blockJumpTimer;

    public void onHeadHit() {
        if (state.is(State.JUMPING)) {
            // don't allow double jump, when the player just hit his head
            didDoubleJump = true;
        }
    }

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

    private TextureRegion bigPlayerPrezelizedStand;
    private TextureRegion bigPlayerPrezelizedTurn;
    private Animation<TextureRegion> bigPlayerPrezelizedWalk;
    private Animation<TextureRegion> bigPlayerPrezelizedJump;
    private TextureRegion bigPlayerPrezelizedCrouch;

    private final ParticleEffect slideEffect = new ParticleEffect();

    private boolean isTurning;

    private final GameTimer changeSizeTimer;

    private int remainingPretzels;
    private GameTimer throwPretzelTimer;
    private PretzelBullet pretzelBullet;
    private TextureRegion pretzelizedTexture;

    private boolean isBig;
    private boolean markRedefineBody;

    private boolean deadAnimationStarted = false;

    private float timeToLive;

    private String lastJumpThroughPlatformId;

    private static final float BEER_EFFECT_TRANSITION_DURATION = 5f;

    private int beers;
    private float currentDrunkEffect;
    private float targetDrunkEffect;
    private float currentHammeredEffect;
    private float targetHammeredEffect;

    private final int randomVictoryIdx;

    /**
     * Stores the recent highest Y position to determine whether to play the landing sound effect.
     */
    private float recentHighestYForLanding;

    /**
     * When the player landed after jumping, wait until the player released the up-button, until he
     * can jump again.
     */
    private boolean upWaitForRelease;

    public Player(GameCallbacks callbacks, World world, TextureAtlas atlas,
                  WorldCreator.StartParams start, int initialTimeToLive) {
        this.callbacks = callbacks;
        this.world = world;
        this.atlas = atlas;
        initTextures(atlas);

        state = new GameObjectState<>(State.STANDING);
        runningRight = !start.leftDirection;

        defineSmallBody(start.centerPosition, true);

        setBounds(body.getPosition().x, body.getPosition().y,
                Cfg.BLOCK_SIZE_PPM, Cfg.BLOCK_SIZE_PPM);
        setRegion(smallPlayerStand[0]);

        timeToLive = initialTimeToLive;

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
                setMainBodyFilterMask(NORMAL_FILTER_BITS);
            }
        });

        pretzelBullet = new PretzelBullet(callbacks, world, atlas);
        throwPretzelTimer = new GameTimer(1.0f, true);

        randomVictoryIdx = MathUtils.random(VICTORY_VARIATIONS - 1);

        recentHighestYForLanding = -Float.MAX_VALUE;
    }

    private void setMainBodyFilterMask(short maskBits) {
        Filter filter = new Filter();
        filter.categoryBits = Bits.PLAYER;
        filter.maskBits = maskBits;
        getBody().getFixtureList().get(0).setFilterData(filter);
        getBody().getFixtureList().get(1).setFilterData(filter);
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
                0.1f, Animation.PlayMode.NORMAL);
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

        bigPlayerPrezelizedStand = atlas.findRegion(RegionNames.BIG_PLAYER_PREZELIZED_STAND);
        bigPlayerPrezelizedWalk = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.BIG_PLAYER_PREZELIZED_WALK), Animation.PlayMode.LOOP_PINGPONG);
        bigPlayerPrezelizedTurn = atlas.findRegion(RegionNames.BIG_PLAYER_PREZELIZED_TURN);
        bigPlayerPrezelizedJump = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.BIG_PLAYER_PREZELIZED_JUMP), Animation.PlayMode.NORMAL);
        bigPlayerPrezelizedCrouch = atlas.findRegion(RegionNames.BIG_PLAYER_PREZELIZED_CROUCH);
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

        updateDrunkEffect(delta);

        updateTextureRegions();

        // set texture bounds always at the bottom of the body
        float leftX = body.getPosition().x - getWidth() / 2;
        float bottomY = body.getPosition().y - 5.75f / Cfg.PPM;
        float textureWidth = getRegionWidth() / Cfg.PPM;
        float textureHeight = getRegionHeight() / Cfg.PPM;
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
                    getBody().applyLinearImpulse(new Vector2(0, 10f), getBody().getWorldCenter(), true);
                }
                deadAnimationStarted = true;
            }
        }

        if (isTurning && touchesGround() && !isDead() && !state.is(State.JUMPING)) {
            slideEffect.setPosition(getX() + getWidth() / 2, getY());
            slideEffect.start();

            // particle emission direction
            ParticleEmitter.ScaledNumericValue angle = slideEffect.getEmitters().get(0).getAngle();
            if (isFlipX()) {
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

        if (body.getPosition().y > recentHighestYForLanding) {
            recentHighestYForLanding = body.getPosition().y;
        }
    }

    private void updateDrunkEffect(float delta) {
        if (currentDrunkEffect != targetDrunkEffect) {
            float signum = Math.signum(targetDrunkEffect - currentDrunkEffect);
            currentDrunkEffect += Math.signum(targetDrunkEffect - currentDrunkEffect) * delta / BEER_EFFECT_TRANSITION_DURATION;
            if (signum > 0 && currentDrunkEffect > targetDrunkEffect) {
                currentDrunkEffect = targetDrunkEffect;
            }
        }

        if (currentHammeredEffect != targetHammeredEffect) {
            float signum = Math.signum(targetHammeredEffect - currentHammeredEffect);
            currentHammeredEffect += Math.signum(targetHammeredEffect - currentHammeredEffect) * delta / BEER_EFFECT_TRANSITION_DURATION;
            if (signum > 0 && currentHammeredEffect > targetHammeredEffect) {
                currentHammeredEffect = targetHammeredEffect;
            }
        }
    }

    @Override
    public void drown() {
        state.set(State.DROWNING);
        body.setLinearVelocity(body.getLinearVelocity().x / 10, body.getLinearVelocity().y / 10);
    }

    /**
     * Flag indicating whether the player released the jump button after jumping, which is required
     * to unlock the second jump.
     */
    private boolean canDoubleJump;
    /**
     * Flag indicating whether the player did the second jump of the double jump already.
     */
    private boolean didDoubleJump;

    public void control(boolean up, boolean down, boolean left, boolean right, boolean fire) {
        if (isDead() || isDrowning()) {
            return;
        }

        if (fire) {
            tryThrowPretzel();
        }

        Vector2 relativeBodyVelocity = getVelocityRelativeToGround();

        state.unfreeze();
        isTurning = right && relativeBodyVelocity.x < -2 && !left || left && relativeBodyVelocity.x > 2 && !right;

        if (!upWaitForRelease && up && touchesGround() && !state.is(State.JUMPING) && blockJumpTimer <= 0) {
            body.applyLinearImpulse(new Vector2(0, 15.25f), body.getWorldCenter(), true);
            state.set(State.JUMPING);
            blockJumpTimer = 0.01f;
            callbacks.jump();
            return;
        }
        if (canDoubleJump && !didDoubleJump && state.is(State.JUMPING) && up) {
            body.setLinearVelocity(getLinearVelocity().x, 0f);
            body.applyLinearImpulse(new Vector2(0, 12.66f), body.getWorldCenter(), true);
            state.setTimer(isBig ? 0.05f : 0f);
            blockJumpTimer = 0.01f;
            callbacks.jump();
            didDoubleJump = true;
        }
        if (right && body.getLinearVelocity().x <= Cfg.MAX_HORIZONTAL_SPEED && !down) {
            body.applyForceToCenter(new Vector2(25f, 0), true);
        }
        if (left && body.getLinearVelocity().x >= -Cfg.MAX_HORIZONTAL_SPEED && !down) {
            body.applyForceToCenter(new Vector2(-25f, 0), true);
        }
        if ((!left && !right)) {
            // horizontally decelerate fast, but don't stop immediately
            body.applyForceToCenter(new Vector2(-6 * relativeBodyVelocity.x, 0), true);
        }
        if (down) {
            body.applyForceToCenter(new Vector2(-6 * relativeBodyVelocity.x, -3f), true);
        }

        if (state.is(State.JUMPING) && !didDoubleJump && !canDoubleJump && !up) {
            canDoubleJump = true;
        }

        if (upWaitForRelease && !up) {
            upWaitForRelease = false;
        }

        if (!touchesGround()) {
            if (blockJumpTimer > 0 || state.is(State.JUMPING)) {
                // keep jumping
            } else {
                state.set(State.WALKING);
                state.freeze();
            }
        } else if (blockJumpTimer < 0) {
            if (state.is(State.JUMPING)) {
                upWaitForRelease = true;
                didDoubleJump = false;
                canDoubleJump = false;
            }
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


    private void tryThrowPretzel() {
        if (canThrowPretzel()) {
            float offsetX = runningRight ? getWidth() : 0f;
            float crouchOffset = state.is(State.CROUCHING) ? getHeight() / 4 : 0f;
            pretzelBullet.fire(getX() + offsetX, getY() + getHeight() / 2 - crouchOffset, runningRight);
            callbacks.fire();
            throwPretzelTimer.restart();
            remainingPretzels--;
        }
    }

    private boolean canThrowPretzel() {
        return hasPretzels() && !pretzelBullet.isActive() && throwPretzelTimer.isFinished();
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

    private void updateTextureRegions() {
        TextureRegion textureRegion;

        int chIdx = (int)Math.floor(characterProgress * (CHARACTER_LEVELS - 1));

        boolean useBigTexture = isBig;
        if (isChangingSize()) {
            useBigTexture = (int) (((changeSizeTimer.getValue() - (int) changeSizeTimer.getValue())) * 8) % 2 == 0;
        }
        if (isVictory()) {
            if (useBigTexture) {
                setRegion(bigPlayerVictory[randomVictoryIdx].getKeyFrame(state.timer()));
            } else {
                setRegion(smallPlayerVictory[randomVictoryIdx].getKeyFrame(state.timer()));
            }
            return;
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
                    textureRegion = bigPlayerJump[chIdx].getKeyFrame(state.timer());
                    pretzelizedTexture = bigPlayerPrezelizedJump.getKeyFrame(state.timer());
                } else {
                    textureRegion = smallPlayerJump[chIdx].getKeyFrame(state.timer());
                }
                break;
            case WALKING:
                if (useBigTexture) {
                    if (isTurning) {
                        textureRegion = bigPlayerTurn[chIdx];
                        pretzelizedTexture = bigPlayerPrezelizedTurn;
                    } else {
                        textureRegion = bigPlayerWalk[chIdx].getKeyFrame(state.timer());
                        pretzelizedTexture = bigPlayerPrezelizedWalk.getKeyFrame(state.timer());
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
                    textureRegion = bigPlayerCrouch[chIdx];
                    pretzelizedTexture = bigPlayerPrezelizedCrouch;
                } else {
                    textureRegion = smallPlayerCrouch[chIdx];
                }
                break;
            case STANDING:
            default:
                if (useBigTexture) {
                    textureRegion = bigPlayerStand[chIdx];
                    pretzelizedTexture = bigPlayerPrezelizedStand;
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

        if (pretzelizedTexture != null ) {
            if (!runningRight && !pretzelizedTexture.isFlipX()) {
                pretzelizedTexture.flip(true, false);
            } else if (runningRight && pretzelizedTexture.isFlipX()) {
                pretzelizedTexture.flip(true, false);
            }
        }

        setRegion(textureRegion);
    }

    @Override
    public void draw(Batch batch) {
        if (isDrowning()) {
            // Because we want to render the enemy in front of the foreground map-layer, to
            // e.g.not have the hands/feets behind the bricks/blocks, there is a circular z-index
            // dependency between the player, the foreground map-layer and the front-water layer.
            // Thus, we generally render the player in front, but we render him semi-transparent
            // when he is drowning as a workaround.
            setColor(1f, 1f, 1f, 0.85f);
        }
        super.draw(batch);

        if (canThrowPretzel() && !isDrowning() && !isDead()) {
            float origU = getU();
            float origU2 = getU2();
            float origV = getV();
            float origV2 = getV2();

            setRegion(pretzelizedTexture);
            super.draw(batch);
            setRegion(origU, origV, origU2, origV2);
        }

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
        CircleShape shape = new CircleShape();
        shape.setRadius(5.5f / Cfg.PPM);
        createBodyFixture(shape, normalFilterMask);
        shape.setPosition(new Vector2(0, 4f / Cfg.PPM));
        shape.setRadius(4.5f / Cfg.PPM);
        createBodyFixture(shape, normalFilterMask);
        createFeetFixture(1f, 11.0f, -5.5f);
        createHeadSensorFixture(4f, 8.6f);
        createGroundSensorFixture(9f, -6.5f);
        shape.dispose();
    }

    private void defineBigBody(Vector2 position, boolean normalFilterMask) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(5.5f / Cfg.PPM);
        createBodyFixture(shape, normalFilterMask);
        shape.setPosition(new Vector2(0, 11f / Cfg.PPM));
        shape.setRadius(4.5f / Cfg.PPM);
        createBodyFixture(shape, normalFilterMask);
        createFeetFixture(1f, 11f, -5.5f);
        createHeadSensorFixture(4f, 15.6f);
        createGroundSensorFixture(9.0f, -6.5f);
        shape.dispose();
    }

    private void createBodyFixture(Shape shape, boolean normalFilterMask) {
        FixtureDef fixtureDef = new FixtureDef();
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
    private void createFeetFixture(float width, float bodyWidth, float bottomY) {
        FixtureDef fixtureDef = new FixtureDef();

        // narrow fixture to circumvent edge-to-edge collision
        EdgeShape feetShape = new EdgeShape();
        feetShape.set(-width / 2 / Cfg.PPM, bottomY / Cfg.PPM,
                width / 2 / Cfg.PPM, bottomY / Cfg.PPM);
        fixtureDef.shape = feetShape;
        fixtureDef.filter.categoryBits = Bits.PLAYER_FEET;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        // wider sensor for enemy-head-collision
        EdgeShape feetSensorShape = new EdgeShape();
        feetSensorShape.set(-bodyWidth / 2 / Cfg.PPM, bottomY / Cfg.PPM,
                bodyWidth / 2 / Cfg.PPM, bottomY / Cfg.PPM);
        fixtureDef.shape = feetSensorShape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Bits.PLAYER_FEET;
        fixtureDef.filter.maskBits =
                Bits.ENEMY_HEAD;
        fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        feetShape.dispose();
    }

    private void createHeadSensorFixture(float width, float topY) {
        FixtureDef fixtureDef = new FixtureDef();
        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM),
                new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM));
        fixtureDef.filter.categoryBits = Bits.PLAYER_HEAD;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.ITEM_BOX |
                Bits.BRICK;
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        headShape.dispose();
    }

    /**
     * Sensor to indicate we are standing on ground.
     */
    private void createGroundSensorFixture(float width, float bottomY) {
        FixtureDef fixtureDef = new FixtureDef();
        EdgeShape groundSensorShape = new EdgeShape();
        groundSensorShape.set(new Vector2(-width / 2 / Cfg.PPM, bottomY / Cfg.PPM),
                new Vector2(width / 2 / Cfg.PPM, bottomY / Cfg.PPM));
        fixtureDef.filter.categoryBits = Bits.PLAYER_GROUND;
        fixtureDef.shape = groundSensorShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        body.createFixture(fixtureDef).setUserData(this);
        groundSensorShape.dispose();
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

            if (hasPretzels()) {
                remainingPretzels = 0;
            }
        } else {
            kill();
        }
    }

    public void addPretzels(int value) {
        remainingPretzels += value;
    }

    public boolean hasPretzels() {
        return remainingPretzels > 0;
    }

    public int getRemainingPretzels() {
        return remainingPretzels;
    }

    public void drunk() {
        beers++;

        if (beers == 1) {
            targetDrunkEffect = 0.5f;
        }
        if (beers == 2) {
            targetHammeredEffect = 0.33f;
        }
        if (beers == 3) {
            targetHammeredEffect = 0.5f;
            targetDrunkEffect = 1.0f;
        }
    }

    public boolean isDrunk() {
        return currentDrunkEffect > 0f;
    }

    public float getDrunkRatio() {
        return currentDrunkEffect;
    }

    public boolean isHammered() {
        return currentHammeredEffect > 0f;
    }

    public float getHammeredRatio() {
        return currentHammeredEffect;
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Hedgehog) {
            Hedgehog hedgehog = (Hedgehog) enemy;
            if (hedgehog.getState() == Hedgehog.State.ROLL) {
                hedgehog.kick(getX() <= enemy.getX());
                return;
            }
            if (hedgehog.getState() == Hedgehog.State.UNROLL) {
                // don't do anything when the hedgehog is just about to unroll, otherwise the player
                // might be a lil surprised why he is dying, because the hedgehog was just still rolled
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
            targetHammeredEffect = 0f;
        }

        if (isDrunk()) {
            targetDrunkEffect = 0f;
        }
    }

    public float getTimeToLive() {
        return timeToLive;
    }

    public void touchGround(Object ground) {
        groundContactCounter++;

        if (groundContactCounter == 1) {
            callbacks.landed(recentHighestYForLanding - body.getPosition().y);
            recentHighestYForLanding = body.getPosition().y;
        }

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

    private boolean touchesGround() {
        return groundContactCounter > 0;
    }

    public boolean hasPlatformContact() {
        return platformContact != null;
    }

    public Platform getPlatformContact() {
        return platformContact;
    }

    private boolean isChangingSize() {
        return changeSizeTimer.isRunning();
    }

    public boolean isInvincible() {
        return isChangingSize() || state.is(State.VICTORY);
    }

    private boolean isOutOfGame() {
        return getY() + getHeight() < 0 * Cfg.BLOCK_SIZE_PPM;
    }

    public void victory() {
        state.set(State.VICTORY);
        targetDrunkEffect /= 4f;
        targetHammeredEffect /= 4f;
        setMainBodyFilterMask(NO_ENEMY_FILTER_BITS);
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
        body.setLinearVelocity(getLinearVelocity().x, 10f);
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
        out.writeInt(remainingPretzels);
        throwPretzelTimer.write(out);
        pretzelBullet.write(out);
        out.writeBoolean(isBig);
        out.writeInt(beers);
        out.writeFloat(currentDrunkEffect);
        out.writeFloat(targetDrunkEffect);
        out.writeFloat(currentHammeredEffect);
        out.writeFloat(targetHammeredEffect);
        out.writeBoolean(markRedefineBody);
        out.writeBoolean(deadAnimationStarted);
        out.writeFloat(timeToLive);
        out.writeUTF(lastJumpThroughPlatformId != null ? lastJumpThroughPlatformId : "null");
        out.writeFloat(characterProgress);
        out.writeFloat(recentHighestYForLanding);
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
        remainingPretzels = in.readInt();
        throwPretzelTimer.read(in);
        pretzelBullet.read(in);
        isBig = in.readBoolean();
        beers = in.readInt();
        currentDrunkEffect = in.readFloat();
        targetDrunkEffect = in.readFloat();
        currentHammeredEffect = in .readFloat();
        targetHammeredEffect = in.readFloat();
        markRedefineBody = in.readBoolean();
        deadAnimationStarted = in.readBoolean();
        timeToLive = in.readFloat();
        lastJumpThroughPlatformId = in.readUTF();
        if (lastJumpThroughPlatformId.equals("null")) {
            lastJumpThroughPlatformId = null;
        }
        characterProgress = in.readFloat();
        recentHighestYForLanding = in.readFloat();

        // this is just a lazy workaround, that is needed because we generally create a small player by default
        if (isBig) {
            Vector2 currentPosition = body.getPosition();
            world.destroyBody(getBody());
            defineBigBody(currentPosition, !changeSizeTimer.isRunning());
        }

        if (isVictory()) {
            setMainBodyFilterMask(NO_ENEMY_FILTER_BITS);
        }
    }
}
