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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.managers.Drownable;

public class Goomba extends Enemy implements Drownable {
    public enum State {
        WALKING, STOMPED
    }

    private GameObjectState<State> state;
    private boolean drowning;

    private Animation<TextureRegion> walkAnimation;
    private TextureRegion stompedTexture;

    public Goomba(GameCallbacks callbacks, World world, TextureAtlas atlas,
                  float posX, float posY) {
        super(callbacks, world, posX, posY);
        initTextures(atlas);

        this.state = new GameObjectState<State>(State.WALKING);
        setBounds(getX(), getY(), Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
    }

    private void initTextures(TextureAtlas atlas) {
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(atlas.findRegion("goomba"), i * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE));
        }
        walkAnimation = new Animation(0.4f, frames);

        stompedTexture = new TextureRegion(atlas.findRegion("goomba"), 2 * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!isDead() && !isDrowning()) {
            state.upate(delta);
            getBody().setLinearVelocity(getVelocity());
        }

        setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2 + 1f / Cfg.PPM);
        setRegion(getFrame());

        if (state.is(State.STOMPED) && state.timer() > 1f) {
            markRemovable();
        }
    }

    private TextureRegion getFrame() {
        TextureRegion textureRegion;

        switch (state.current()) {
            case STOMPED:
                textureRegion = stompedTexture;
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

        if (isDead() && !textureRegion.isFlipY()) {
            textureRegion.flip(false, true);
        }

        return textureRegion;
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6f / Cfg.PPM);
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.PLATFORM_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.MARIO_BIT |
                JumpGame.OBJECT_BIT |
                JumpGame.ENEMY_BIT |
                JumpGame.BLOCK_TOP_BIT |
                JumpGame.COLLIDER_BIT;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        // head
        PolygonShape headShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-4, 9).scl(1 / Cfg.PPM);
        vertices[1] = new Vector2(4, 9).scl(1 / Cfg.PPM);
        vertices[2] = new Vector2(-3, 3).scl(1 / Cfg.PPM);
        vertices[3] = new Vector2(3, 3).scl(1 / Cfg.PPM);
        headShape.set(vertices);

        fixtureDef.shape = headShape;
        fixtureDef.restitution = 1.0f;
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_HEAD_BIT;
        fixtureDef.filter.maskBits = JumpGame.MARIO_BIT;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_SIDE_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT;
        fixtureDef.isSensor = true;
        sideShape.set(new Vector2(-6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(-6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(this);
        sideShape.set(new Vector2(6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(this);
        return body;
    }

    @Override
    public void onHeadHit(Mario mario) {
        if (mario.isDead() || mario.isInvincible()) {
            return;
        }

        stomp();
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa) enemy;
            if (koopa.getState() == Koopa.State.MOVING_SHELL) {
                kill(true);
                return;
            }
        }
        reverseVelocity(true, false);
    }

    private void stomp() {
        getCallbacks().stomp(this);

        state.set(State.STOMPED);
        markDestroyBody();
    }

    @Override
    public void drown() {
        drowning = true;
        getBody().setLinearVelocity(getBody().getLinearVelocity().x / 8, getBody().getLinearVelocity().y / 12);
        getBody().setGravityScale(0.05f);
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
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
    }
}
