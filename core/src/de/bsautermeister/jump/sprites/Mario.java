package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
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

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.RegionNames;

public class Mario extends Sprite {

    private static final float INITAL_TTL = 300;

    private GameCallbacks callbacks;
    private World world;
    private Body body;

    private int groundContactCounter;
    private float jumpFixTimer;

    public enum State {
        STANDING, CROUCHING, JUMPING, WALKING, DEAD
    }

    private GameObjectState<State> state;

    private boolean runningRight;

    private TextureRegion marioStand;
    private TextureRegion marioDead;
    private TextureRegion marioTurn;
    private Animation<TextureRegion> marioWalk;
    private Animation<TextureRegion> marioJump; // is actually just 1 frame

    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private TextureRegion bigMarioTurn;
    private Animation<TextureRegion> bigMarioWalk;
    private TextureRegion bigMarioCrouch;

    private boolean isTurning;

    private static final float GROW_TIME = 1f;
    private float growingTimer;

    private boolean isBig;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;

    private boolean deadAnimationStarted = false;

    private float timeToLive;
    private int score;

    public Mario(GameCallbacks callbacks, World world, TextureAtlas atlas) {
        this.callbacks = callbacks;
        this.world = world;
        state = new GameObjectState<State>(State.STANDING);
        runningRight = true;

        setBounds(0, 0, GameConfig.BLOCK_SIZE / GameConfig.PPM, GameConfig.BLOCK_SIZE / GameConfig.PPM);
        initTextures(atlas);

        Vector2 startPostion = new Vector2(2 * GameConfig.BLOCK_SIZE / GameConfig.PPM, 2 * GameConfig.BLOCK_SIZE / GameConfig.PPM);
        defineSmallBody(startPostion);

        setRegion(marioStand);

        timeToLive = INITAL_TTL;
        score = 0;
    }

    private void initTextures(TextureAtlas atlas) {
        TextureRegion littleMarioTexture = atlas.findRegion(RegionNames.LITTLE_MARIO);
        TextureRegion bigMarioTexture = atlas.findRegion(RegionNames.BIG_MARIO);

        marioStand = new TextureRegion(littleMarioTexture, 0, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE);
        bigMarioStand = new TextureRegion(bigMarioTexture, 0, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE);

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE));
        }
        marioWalk = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(bigMarioTexture, i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE));
        }
        bigMarioWalk = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 5; i < 6; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE));
        }
        marioJump = new Animation<TextureRegion>(0.1f, frames);
        bigMarioJump = new TextureRegion(bigMarioTexture, 5 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE);

        marioDead = new TextureRegion(littleMarioTexture, 6 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE);

        marioTurn = new TextureRegion(littleMarioTexture, 4 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE);
        bigMarioTurn = new TextureRegion(bigMarioTexture, 4 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE);

        bigMarioCrouch = new TextureRegion(bigMarioTexture, 6 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE);
    }

    public void update(float delta) {
        state.upate(delta);
        timeToLive -= delta;
        jumpFixTimer -= delta;

        if (isBig) {
            setPosition(body.getPosition().x - getWidth() / 2,
                    body.getPosition().y - getHeight() / 2 + 7.8f / GameConfig.PPM);
        } else {
            setPosition(body.getPosition().x - getWidth() / 2,
                    body.getPosition().y - getHeight() / 2);
        }

        if (isGrowing()) {
            growingTimer -= delta;
        }

        TextureRegion textureRegion = getFrame();
        setRegion(textureRegion);

        // set texture bounds always at the bottom of the body
        float textureWidth = textureRegion.getRegionWidth() / GameConfig.PPM;
        float textureHeight = textureRegion.getRegionHeight() / GameConfig.PPM;
        float yOffset = 0f;
        if (isGrowing() && textureRegion.getRegionHeight() == GameConfig.BLOCK_SIZE) {
            yOffset = 7.5f / GameConfig.PPM;
        }
        setBounds(getX(), getY() - yOffset, textureWidth, textureHeight);

        // these are called outside of the physics update loop
        if (timeToDefineBigMario) {
            Vector2 currentPosition = getBody().getPosition();
            world.destroyBody(getBody());
            defineBigBody(currentPosition);
            timeToDefineBigMario = false;
        } else if (timeToRedefineMario) {
            Vector2 position = getBody().getPosition();
            world.destroyBody(getBody());
            defineSmallBody(position);
            timeToRedefineMario = false;
        }

        // check fallen out of game
        if (getY() < 0) {
            kill();
        }

        if (state.is(State.DEAD)) {
            if (state.timer() <= 0.5f) {
                getBody().setActive(false);
            } else if (!deadAnimationStarted) {
                getBody().setActive(true);
                getBody().applyLinearImpulse(new Vector2(0, 4.5f), getBody().getWorldCenter(), true); // TODO currently this is dependent on the speed when Mario died
                deadAnimationStarted = true;
            }
        }
    }

    public void control(boolean up, boolean down, boolean left, boolean right) {
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
        if (isGrowing()) {
            useBigTexture = (int)(((growingTimer - (int)growingTimer)) * 8) % 2 == 0 ? isBig : false;
        }

        switch (state.current()) {
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

    public State getState() {
        return state.current();
    }

    private void defineSmallBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        final float[] SMALL_POLYGON_VERTICES = {
                -3f / GameConfig.PPM, 6f / GameConfig.PPM,
                3f / GameConfig.PPM, 6f / GameConfig.PPM,
                5f / GameConfig.PPM, -2f / GameConfig.PPM,
                5f / GameConfig.PPM, -4f / GameConfig.PPM,
                3f / GameConfig.PPM, -4.5f / GameConfig.PPM,
                -3f / GameConfig.PPM, -4.5f / GameConfig.PPM,
                -5f / GameConfig.PPM, -4f / GameConfig.PPM,
                -5f / GameConfig.PPM, -2f / GameConfig.PPM
        };
        createBodyFixture(fixtureDef, SMALL_POLYGON_VERTICES);

        createFeetFixture(fixtureDef, 9.33f, -6.5f);
        createHeadSensorFixture(fixtureDef, 4f, 6.1f);
        createGroundSensorFixture(fixtureDef, 9f, -7f);
    }

    private void defineBigBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        final float[] BIG_POLYGON_VERTICES = {
                -2.5f / GameConfig.PPM, 21f / GameConfig.PPM,
                2.5f / GameConfig.PPM, 21f / GameConfig.PPM,
                5.5f / GameConfig.PPM, 8f / GameConfig.PPM,
                5.5f / GameConfig.PPM, -1f / GameConfig.PPM,
                3f / GameConfig.PPM, -4.5f / GameConfig.PPM,
                -3f / GameConfig.PPM, -4.5f / GameConfig.PPM,
                -5.5f / GameConfig.PPM, -1f / GameConfig.PPM,
                -5.5f / GameConfig.PPM, 8f / GameConfig.PPM,
        };
        createBodyFixture(fixtureDef, BIG_POLYGON_VERTICES);

        createFeetFixture(fixtureDef, 10f, -6.5f);
        createHeadSensorFixture(fixtureDef, 4f, 21.1f);
        createGroundSensorFixture(fixtureDef, 9.33f, -7f);
    }

    private void createBodyFixture(FixtureDef fixtureDef, float[] smallPolygonVertices) {
        PolygonShape shape = new PolygonShape();
        shape.set(smallPolygonVertices);

        fixtureDef.filter.categoryBits = JumpGame.MARIO_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.ENEMY_BIT |
                JumpGame.ENEMY_HEAD_BIT |
                JumpGame.OBJECT_BIT |
                JumpGame.ITEM_BIT;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
    }

    /**
     * Feet as edge shape to circumvent edge-to-edge collision.
     */
    private void createFeetFixture(FixtureDef fixtureDef, float width, float bottomY) {
        Fixture fixture;EdgeShape feetShape = new EdgeShape();
        feetShape.set(-width / 2 / GameConfig.PPM, bottomY  / GameConfig.PPM,
                width / 2 / GameConfig.PPM, bottomY / GameConfig.PPM);
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
        headShape.set(new Vector2(width / 2 / GameConfig.PPM, topY / GameConfig.PPM),
                new Vector2(width / 2 / GameConfig.PPM, topY / GameConfig.PPM));
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
        groundSensorShape.set(new Vector2(-width / 2 / GameConfig.PPM, bottomY / GameConfig.PPM),
                new Vector2(width / 2 / GameConfig.PPM, bottomY / GameConfig.PPM));
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
            growingTimer = GROW_TIME;
            timeToDefineBigMario = true;
            isBig = true;
        }
    }

    private void smaller(Enemy enemy) {
        if (isBig) {
            isBig = false;
            timeToRedefineMario = true;
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

        smaller(enemy);
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

    public boolean isGrowing() {
        return growingTimer > 0;
    }
}
