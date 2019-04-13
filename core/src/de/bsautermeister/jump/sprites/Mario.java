package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.RegionNames;

public class Mario extends Sprite {
    private World world;
    private Body body;

    public enum State {
        FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD
    }

    private State currentState;
    private State previousState;
    private float stateTimer;
    private boolean runningRight;

    private TextureRegion marioStand;
    private TextureRegion marioDead;
    private Animation<TextureRegion> marioRun;
    private Animation<TextureRegion> marioJump; // TODO is actually just 1 frame

    private TextureRegion bigMarionStand;
    private TextureRegion bigMarioJump;
    private Animation<TextureRegion> bigMarioRun;
    private Animation<TextureRegion> growMario;

    private boolean isBig;
    private boolean runGrowAnimation;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;

    private boolean isDead;
    private boolean deadAnimationStarted = false;

    public Mario(World world, TextureAtlas atlas) {
        this.world = world;

        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        setBounds(0, 0, 16 / GameConfig.PPM, 16 / GameConfig.PPM);

        TextureRegion littleMarioTexture = atlas.findRegion(RegionNames.LITTLE_MARIO);
        TextureRegion bigMarioTexture = atlas.findRegion(RegionNames.BIG_MARIO);

        marioStand = new TextureRegion(littleMarioTexture, 0, 0, 16, 16);
        bigMarionStand = new TextureRegion(bigMarioTexture, 0, 0, 16, 32);

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * 16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);

        frames.clear();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(bigMarioTexture, i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);

        frames.clear();
        for (int i = 5; i < 6; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * 16, 0, 16, 16));
        }
        marioJump = new Animation(0.1f, frames);
        bigMarioJump = new TextureRegion(bigMarioTexture, 5 * 16, 0, 16, 32);

        frames.clear();
        // growing animation
        TextureRegion halfSizeMario = new TextureRegion(bigMarioTexture, 15 * 16, 0, 16, 32);
        frames.add(halfSizeMario);
        frames.add(bigMarionStand);
        frames.add(halfSizeMario);
        frames.add(bigMarionStand);
        frames.add(halfSizeMario);
        frames.add(bigMarionStand);
        growMario = new Animation<TextureRegion>(0.33f, frames);

        marioDead = new TextureRegion(littleMarioTexture, 6 * 16, 0, 16, 16);

        Vector2 startPostion = new Vector2(32 / GameConfig.PPM, 32 / GameConfig.PPM);
        defineBody(startPostion);

        setRegion(marioStand);
    }

    public void update(float delta) {
        if (isBig) {
            setPosition(body.getPosition().x - getWidth() / 2,
                    body.getPosition().y - getHeight() / 2 - 6 / GameConfig.PPM);
        } else {
            setPosition(body.getPosition().x - getWidth() / 2,
                    body.getPosition().y - getHeight() / 2);
        }

        setRegion(getFrame(delta));

        // these are called outside of the physics update loop
        if (timeToDefineBigMario) {
            defineBigMario();
        } else if (timeToRedefineMario) {
            Vector2 position = getBody().getPosition();
            world.destroyBody(getBody());

            defineBody(position);

            timeToRedefineMario = false;
        }

        // check fallen out of game
        if (getY() < 0) {
            kill();
        }

        if (isDead) {
            if (stateTimer <= 0.5f) {
                getBody().setActive(false);
            } else if (!deadAnimationStarted) {
                getBody().setActive(true);
                getBody().applyLinearImpulse(new Vector2(0, 4.5f), getBody().getWorldCenter(), true); // TODO currently this is dependent on the speed when Mario died
                deadAnimationStarted = true;
            }
        }
    }

    private void defineBigMario() { // TODO refactor with defineBody(), because 90% is duplicated
        Vector2 currentPosition = getBody().getPosition();
        world.destroyBody(getBody());

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(currentPosition.add(0, 10 / GameConfig.PPM));
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / GameConfig.PPM);
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
        shape.setPosition(new Vector2(0, -14 / GameConfig.PPM));
        fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(-2 / GameConfig.PPM, 6 / GameConfig.PPM),
                new Vector2(2 / GameConfig.PPM, 6 / GameConfig.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_HEAD_BIT;
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true; // does not collide, but provides user-data TODO but we use it in the WorldContactListner, so it does collide?
        body.createFixture(fixtureDef).setUserData(this);
        timeToDefineBigMario = false;
    }

    private TextureRegion getFrame(float delta) {
        currentState = getState();

        TextureRegion textureRegion;
        switch (currentState) {
            case DEAD:
                textureRegion = marioDead;
                break;
            case GROWING:
                textureRegion = growMario.getKeyFrame(stateTimer);
                if (growMario.isAnimationFinished(stateTimer)) {
                    runGrowAnimation = false;
                }
                break;
            case JUMPING:
                if (isBig) {
                    textureRegion = bigMarioJump;
                } else {
                    textureRegion = marioJump.getKeyFrame(stateTimer);
                }
                break;
            case RUNNING:
                if (isBig) {
                    textureRegion = bigMarioRun.getKeyFrame(stateTimer, true);
                } else {
                    textureRegion = marioRun.getKeyFrame(stateTimer, true);
                }
                break;
            case FALLING:
            case STANDING:
            default:
                textureRegion = isBig ? bigMarionStand : marioStand;
                break;
        }

        if ((body.getLinearVelocity().x < 0 || !runningRight) && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + delta : 0;
        previousState = currentState;
        return textureRegion;
    }

    public State getState() {
        if (isDead) {
            return State.DEAD;
        } else if (runGrowAnimation) {
            return State.GROWING;
        } else if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        } else if (body.getLinearVelocity().y < 0) {
            return State.FALLING;
        } else if (body.getLinearVelocity().x > 0 || body.getLinearVelocity().x < 0) {
            return State.RUNNING;
        }
        return State.STANDING;
    }

    private void defineBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / GameConfig.PPM);
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

        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(-2 / GameConfig.PPM, 6 / GameConfig.PPM),
                new Vector2(2 / GameConfig.PPM, 6 / GameConfig.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_HEAD_BIT;
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true; // does not collide, but provides user-data TODO but we use it in the WorldContactListner, so it does collide?
        body.createFixture(fixtureDef).setUserData(this);
    }

    public Body getBody() {
        return body;
    }

    public boolean isBig() {
        return isBig;
    }

    public boolean isDead() {
        return isDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public void grow() {
        runGrowAnimation = true;
        timeToDefineBigMario = true;
        isBig = true;
        setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        JumpGame.assetManager.get("audio/sounds/powerup.wav", Sound.class).play();
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa)enemy;
            if (koopa.getState() == Koopa.State.STANDING_SHELL) {
                koopa.kick(getX() <= enemy.getX() ? Koopa.KICK_SPEED : -Koopa.KICK_SPEED);
            } else {
                kill();
            }
            return;
        }

        if (isBig) {
            isBig = false;
            timeToRedefineMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() / 2);
            JumpGame.assetManager.get("audio/sounds/powerdown.wav", Sound.class).play();
        } else {
            kill();
        }
    }

    private void kill() {
        if (isDead)
            return;

        JumpGame.assetManager.get("audio/sounds/mariodie.wav", Sound.class).play();
        isDead = true;
        Filter filter = new Filter();
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }
}
