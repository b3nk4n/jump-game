package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class Koopa extends Enemy {
    public static final float KICK_SPEED = 2f;

    public enum State {
        WALKING, STANDING_SHELL, MOVING_SHELL, DEAD
    }
    private State currentState;
    private State previousState;

    private float stateTimer;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> shellAnimation;
    private Array<TextureRegion> frames;
    private boolean markForDestory;
    private boolean destroyed;
    private TextureAtlas atlas;

    private float deadRotation;

    public Koopa(World world, TiledMap map, TextureAtlas atlas, float posX, float posY) {
        super(world, map, posX, posY);

        this.atlas = atlas;
        frames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(atlas.findRegion("koopa"), i * 16, 0, 16, 24));
        }
        walkAnimation = new Animation(0.2f, frames);

        frames.clear();
        for (int i = 4; i < 6; i++) {
            frames.add(new TextureRegion(atlas.findRegion("koopa"), i * 16, 0, 16, 24));
        }
        shellAnimation = new Animation(0.4f, frames);

        currentState = State.WALKING;

        stateTimer = 0;
        setBounds(getX(), getY(), 16 / GameConfig.PPM, 24 / GameConfig.PPM);
        markForDestory = false;
        destroyed = false;

        deadRotation = 0;
    }

    @Override
    public void update(float delta) {
        setRegion(getFrame(delta));

        if (currentState == State.STANDING_SHELL && stateTimer > 5f) {
            currentState = State.WALKING;
            getVelocity().x = 1;
        }

        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - 8 / GameConfig.PPM);

        if (currentState == State.DEAD) {
            deadRotation += delta * 90;
            rotate(deadRotation);

            if (stateTimer > 5 && !destroyed) {
                getWorld().destroyBody(getBody());
                destroyed = true;
            }
        } else {
            getBody().setLinearVelocity(getVelocity());
        }
    }

    private TextureRegion getFrame(float delta) {
        TextureRegion textureRegion;

        switch (currentState) {
            case MOVING_SHELL:
            case STANDING_SHELL:
                textureRegion = shellAnimation.getKeyFrame(stateTimer, true);
                break;
            case WALKING:
                default:
                textureRegion = walkAnimation.getKeyFrame(stateTimer, true);
                break;
        }

        if (getVelocity().x > 0 && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (getVelocity().x < 0 && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

        stateTimer = currentState == previousState ? stateTimer + delta : 0;
        previousState = currentState;

        return textureRegion;
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef(); // TODO refactor: this is a copy of Goomba.defineBody()
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / GameConfig.PPM);
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.MARIO_BIT |
                JumpGame.OBJECT_BIT |
                JumpGame.ENEMY_BIT;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        // head
        PolygonShape headShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5, 8).scl(1 / GameConfig.PPM);
        vertices[1] = new Vector2(5, 8).scl(1 / GameConfig.PPM);
        vertices[2] = new Vector2(-5, 3).scl(1 / GameConfig.PPM);
        vertices[3] = new Vector2(3, 3).scl(1 / GameConfig.PPM);
        headShape.set(vertices);

        fixtureDef.shape = headShape;
        fixtureDef.restitution = 1.5f; // TODO otherwise it pumped of? check out how restitution behaves (bouncyness)
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_HEAD_BIT;
        fixtureDef.filter.maskBits = JumpGame.MARIO_BIT;
        body.createFixture(fixtureDef).setUserData(this);

        return body;
    }

    @Override
    public void onHeadHit(Mario mario) {
        if (currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            getVelocity().x = 0;
        } else {
            kick(mario.getX() <= getX() ? KICK_SPEED : -KICK_SPEED);
        }
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa) enemy;
            if (koopa.currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL) {
                kill();
            } else if(currentState == State.MOVING_SHELL && koopa.currentState == State.WALKING) {
                return;
            } else {
                reverseVelocity(true, false);
            }
        } else if (currentState != State.MOVING_SHELL) {
            reverseVelocity(true, false);
        }
    }

    public void kick(float speed) {
        getVelocity().x = speed;
        currentState = State.MOVING_SHELL;
    }

    public State getState() {
        return currentState;
    }

    @Override
    public void draw(Batch batch) {
        if (!destroyed) { // TODO are we doing this just because of a  BUG? Video 31, 11:00 (body is attached to something else!?) Better: WorldCreator: remove enemy from arrays
            super.draw(batch);
        }
    }

    public void kill() {
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }
        getBody().applyLinearImpulse(new Vector2(0, 5f), getBody().getWorldCenter(), true);
    }
}
