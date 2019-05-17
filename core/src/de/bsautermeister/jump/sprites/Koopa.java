package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
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

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class Koopa extends Enemy {
    public static final float KICK_SPEED = 2f;

    public enum State {
        WALKING, STANDING_SHELL, MOVING_SHELL, DEAD, REMOVABLE
    }

    private GameObjectState<State> state;

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> shellAnimation;
    private TextureAtlas atlas;

    private float deadRotation;

    public Koopa(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas,
                 float posX, float posY) {
        super(callbacks, world, map, posX, posY);

        this.atlas = atlas;
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(atlas.findRegion("koopa"), i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, (int)(1.5f * GameConfig.BLOCK_SIZE)));
        }
        walkAnimation = new Animation(0.2f, frames);

        frames.clear();
        for (int i = 4; i < 6; i++) {
            frames.add(new TextureRegion(atlas.findRegion("koopa"), i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, (int)(1.5f * GameConfig.BLOCK_SIZE)));
        }
        shellAnimation = new Animation(0.4f, frames);

        state = new GameObjectState<State>(State.WALKING);

        setBounds(getX(), getY(), GameConfig.BLOCK_SIZE / GameConfig.PPM, (int)(1.5f * GameConfig.BLOCK_SIZE) / GameConfig.PPM);

        deadRotation = 0;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        state.upate(delta);

        setRegion(getFrame(delta));

        if (state.is(State.STANDING_SHELL) && state.timer() > 5f) {
            state.set(State.WALKING);
            getVelocity().x = 1;
        }

        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - 8 / GameConfig.PPM);

        if (state.is(State.DEAD)) {
            deadRotation += delta * 90;
            rotate(deadRotation);

            if (state.timer() > 5 && !isDestroyed()) {
                state.set(State.REMOVABLE);
                destroyLater();
            }
        } else {
            getBody().setLinearVelocity(getVelocity());
        }

        if (state.changed()) {
            state.resetTimer();
        }
    }

    private TextureRegion getFrame(float delta) {
        TextureRegion textureRegion;

        switch (state.current()) {
            case MOVING_SHELL:
            case STANDING_SHELL:
                textureRegion = shellAnimation.getKeyFrame(state.timer(), true);
                break;
            case WALKING:
                default:
                textureRegion = walkAnimation.getKeyFrame(state.timer(), true);
                break;
        }

        if (getVelocity().x > 0 && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (getVelocity().x < 0 && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

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
        fixtureDef.restitution = 1f;
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_HEAD_BIT;
        fixtureDef.filter.maskBits = JumpGame.MARIO_BIT;
        body.createFixture(fixtureDef).setUserData(this);

        return body;
    }

    @Override
    public void onHeadHit(Mario mario) {
        if (!state.is(State.STANDING_SHELL)) {
            state.set(State.STANDING_SHELL);
            getVelocity().x = 0;
            getCallbacks().stomp(this);
        } else {
            kick(mario.getX() <= getX() ? KICK_SPEED : -KICK_SPEED);
        }
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa otherKoopa = (Koopa) enemy;
            if (!state.is(State.MOVING_SHELL) && otherKoopa.getState() == State.MOVING_SHELL) {
                kill();
            } else if(state.is(State.MOVING_SHELL) && otherKoopa.getState() == State.WALKING) {
                return;
            } else {
                reverseVelocity(true, false);
            }
        } else if (!state.is(State.MOVING_SHELL)) {
            reverseVelocity(true, false);
        }
    }

    public void kick(float speed) {
        getVelocity().x = speed;
        state.set(State.MOVING_SHELL);
    }

    public State getState() {
        return state.current();
    }

    private void kill() {
        state.set(State.DEAD);
        Filter filter = new Filter();
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }
        getBody().applyLinearImpulse(new Vector2(0, 5f), getBody().getWorldCenter(), true);
    }

    @Override
    public boolean canBeRemoved() {
        return state.is(State.REMOVABLE);
    }
}
