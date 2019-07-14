package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.tools.GameTimer;

public class Mario extends Sprite {

    public static final float INITAL_TTL = 30;

    private static final short NORMAL_FILTER_BITS = JumpGame.GROUND_BIT |
    JumpGame.COIN_BIT |
    JumpGame.BRICK_BIT |
    JumpGame.ENEMY_BIT |
    JumpGame.ENEMY_HEAD_BIT |
    JumpGame.OBJECT_BIT |
    JumpGame.ITEM_BIT;

    private static final short CHANGE_SIZE_FILTER_BITS = JumpGame.GROUND_BIT |
            JumpGame.COIN_BIT |
            JumpGame.BRICK_BIT |
            JumpGame.OBJECT_BIT |
            JumpGame.ITEM_BIT;

    private GameCallbacks callbacks;
    private World world;
    private Body body;

    private int groundContactCounter;
    private float jumpFixTimer;

    public enum State {
        STANDING, CROUCHING, JUMPING, WALKING, DROWNING, DEAD
    }

    private GameObjectState<State> state;

    private boolean runningRight;

    private TextureRegion marioStand;
    private TextureRegion marioDead;
    private TextureRegion marioTurn;
    private Animation<TextureRegion> marioWalk;
    private Animation<TextureRegion> marioJump; // is actually just 1 frame
    private TextureRegion marioDrown;

    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private TextureRegion bigMarioTurn;
    private Animation<TextureRegion> bigMarioWalk;
    private TextureRegion bigMarioCrouch;
    private TextureRegion bigMarioDrown;

    ParticleEffect slideEffect = new ParticleEffect();
    ParticleEffect splashEffect = new ParticleEffect();

    private boolean isTurning;

    private final GameTimer changeSizeTimer;

    private boolean isBig;
    private boolean markRedefineBody;

    private boolean deadAnimationStarted = false;

    private float timeToLive;
    private int score;

    public Mario(GameCallbacks callbacks, World world, TextureAtlas atlas) {
        this.callbacks = callbacks;
        this.world = world;
        state = new GameObjectState<State>(State.STANDING);
        runningRight = true;

        initTextures(atlas);

        Vector2 startPosition = new Vector2(2 * Cfg.BLOCK_SIZE / Cfg.PPM, 2 * Cfg.BLOCK_SIZE / Cfg.PPM);
        defineSmallBody(startPosition, true);

        setBounds(body.getPosition().x, body.getPosition().y,
                Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        setRegion(marioStand);

        timeToLive = INITAL_TTL;
        score = 0;

        slideEffect.load(Gdx.files.internal(AssetPaths.Pfx.SLIDE_SMOKE), atlas);
        slideEffect.scaleEffect(0.1f / Cfg.PPM);

        splashEffect.load(Gdx.files.internal(AssetPaths.Pfx.SPLASH), atlas);
        splashEffect.scaleEffect(0.1f / Cfg.PPM);

        changeSizeTimer = new GameTimer(2f);
        changeSizeTimer.setCallbacks(new GameTimer.TimerCallbacks() {
            @Override
            public void onStart() {
                // body is already created to not collide with enemies when changing the body
            }

            @Override
            public void onFinish() {
                Filter filter = new Filter();
                filter.categoryBits = JumpGame.MARIO_BIT;
                filter.maskBits = NORMAL_FILTER_BITS;
                getBody().getFixtureList().get(0).setFilterData(filter);
            }
        });
    }

    private void initTextures(TextureAtlas atlas) {
        TextureRegion littleMarioTexture = atlas.findRegion(RegionNames.LITTLE_MARIO);
        TextureRegion bigMarioTexture = atlas.findRegion(RegionNames.BIG_MARIO);

        marioStand = new TextureRegion(littleMarioTexture, 0, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
        bigMarioStand = new TextureRegion(bigMarioTexture, 0, 0, Cfg.BLOCK_SIZE, 2 * Cfg.BLOCK_SIZE);

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE));
        }
        marioWalk = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(bigMarioTexture, i * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, 2 * Cfg.BLOCK_SIZE));
        }
        bigMarioWalk = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 5; i < 6; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE));
        }
        marioJump = new Animation<TextureRegion>(0.1f, frames);
        bigMarioJump = new TextureRegion(bigMarioTexture, 5 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, 2 * Cfg.BLOCK_SIZE);

        marioDead = new TextureRegion(littleMarioTexture, 6 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);

        marioTurn = new TextureRegion(littleMarioTexture, 4 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
        bigMarioTurn = new TextureRegion(bigMarioTexture, 4 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, 2 * Cfg.BLOCK_SIZE);

        bigMarioCrouch = new TextureRegion(bigMarioTexture, 6 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, 2 * Cfg.BLOCK_SIZE);

        marioDrown = new TextureRegion(littleMarioTexture, 7 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
        bigMarioDrown = new TextureRegion(bigMarioTexture, 7 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, 2 * Cfg.BLOCK_SIZE);
    }

    public void update(float delta) {
        state.upate(delta);
        timeToLive -= delta;
        jumpFixTimer -= delta;

        if (timeToLive <= 0) {
            timeToLive = 0;
            kill();
        }

        if (isChangingSize()) {
            changeSizeTimer.update(delta);
        }

        if (!isDead() && isBelowWaterLevel()) {
            if (!state.is(State.DROWNING)) {
                splashEffect.setPosition(getX() + getWidth() / 2, getY());
                splashEffect.start();
                callbacks.touchedWater(this);
            }

            body.setLinearVelocity(Vector2.Zero);
            state.set(State.DROWNING);
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

        if (isTurning && touchesGround() && !isDead()) {
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
    }

    public void control(boolean up, boolean down, boolean left, boolean right) {
        if (state.is(State.DEAD) || state.is(State.DROWNING)) {
            return;
        }

        state.unfreeze();
        isTurning = right && body.getLinearVelocity().x < 0 || left && body.getLinearVelocity().x > 0;

        if (up && touchesGround() && !state.is(State.JUMPING)) {
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
            state.set(State.JUMPING);
            jumpFixTimer = 0.25f;
            callbacks.jump();
            return;
        }
        if (right && body.getLinearVelocity().x <= 2 && !down) {
            body.applyLinearImpulse(new Vector2(0.1f, 0), body.getWorldCenter(), true);
        }
        if (left && body.getLinearVelocity().x >= -2 && !down) {
            body.applyLinearImpulse(new Vector2(-0.1f, 0), body.getWorldCenter(), true);
        }
        if ((!left && ! right) || down) {
            // horizontally decelerate fast, but don't stop immediately
            body.setLinearVelocity(body.getLinearVelocity().x * 0.75f, body.getLinearVelocity().y);
        }

        if (!touchesGround()) {
            if (jumpFixTimer > 0 || state.is(State.JUMPING)) {
                // keep jumping state
                return;
            } else {
                state.set(State.WALKING);
                state.freeze();
            }
        } else if (jumpFixTimer < 0) {
            if (down && isBig) {
                state.set(State.CROUCHING);
            } else if (body.getLinearVelocity().x != 0) {
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

    private TextureRegion getFrame() {
        TextureRegion textureRegion;

        boolean useBigTexture = isBig;
        if (isChangingSize()) {
            useBigTexture = (int)(((changeSizeTimer.getValue() - (int) changeSizeTimer.getValue())) * 8) % 2 == 0;
        }

        switch (state.current()) {
            case DROWNING:
                textureRegion = useBigTexture ? bigMarioDrown : marioDrown;
                break;
            case DEAD:
                textureRegion = marioDead;
                break;
            case JUMPING:
                if (useBigTexture) {
                    textureRegion = bigMarioJump;
                } else {
                    textureRegion = marioJump.getKeyFrame(state.timer());
                }
                break;
            case WALKING:
                if (useBigTexture) {
                    if (isTurning) {
                        textureRegion = bigMarioTurn;
                    } else {
                        textureRegion = bigMarioWalk.getKeyFrame(state.timer(), true);
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
                textureRegion = bigMarioCrouch;
                break;
            case STANDING:
            default:
                textureRegion = useBigTexture ? bigMarioStand : marioStand;
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
        splashEffect.draw(batch, Gdx.graphics.getDeltaTime());
    }

    public State getState() {
        return state.current();
    }

    private void defineSmallBody(Vector2 position, boolean normalFilterMask) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
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
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
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
        fixtureDef.filter.categoryBits = JumpGame.MARIO_BIT;
        fixtureDef.filter.maskBits = normalFilterMask ?
                NORMAL_FILTER_BITS : CHANGE_SIZE_FILTER_BITS;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
    }

    /**
     * Feet as edge shape to circumvent edge-to-edge collision.
     */
    private void createFeetFixture(FixtureDef fixtureDef, float width, float bottomY) {
        Fixture fixture;EdgeShape feetShape = new EdgeShape();
        feetShape.set(-width / 2 / Cfg.PPM, bottomY  / Cfg.PPM,
                width / 2 / Cfg.PPM, bottomY / Cfg.PPM);
        fixtureDef.shape = feetShape;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.OBJECT_BIT;
        fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
    }

    private void createHeadSensorFixture(FixtureDef fixtureDef, float width, float topY) {
        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM),
                new Vector2(width / 2 / Cfg.PPM, topY / Cfg.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_HEAD_BIT;
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        body.createFixture(fixtureDef).setUserData(this);
    }

    /**
     * Sensor to indicate we are standing on ground.
     */
    private void createGroundSensorFixture(FixtureDef fixtureDef, float width, float bottomY) {
        EdgeShape groundSensorShape = new EdgeShape();
        groundSensorShape.set(new Vector2(-width / 2 / Cfg.PPM, bottomY / Cfg.PPM),
                new Vector2(width / 2 / Cfg.PPM, bottomY / Cfg.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_FEET_BIT;
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

    public boolean isDead() {
        return state.is(State.DEAD);
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
            isBig = false;
            markRedefineBody = true;
            callbacks.hit(this, enemy);
        } else {
            kill();
        }
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa)enemy;
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
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }

    public void addScore(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    public float getTimeToLive() {
        return timeToLive;
    }

    public void touchGround() {
        groundContactCounter++;
    }

    public void leftGround() {
        groundContactCounter--;
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

    private boolean isBelowWaterLevel() {
        return getY() < Cfg.BLOCK_SIZE / Cfg.PPM * 0.66;
    }

    private boolean isOutOfGame() {
        return getY() + getHeight() < 0;
    }
}
