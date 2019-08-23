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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;

public class Goomba extends Enemy implements Drownable {
    public enum State {
        WALKING, STOMPED
    }

    private GameObjectState<State> state;
    private boolean drowning;
    private float speed;

    private final Animation<TextureRegion> walkAnimation;
    private final TextureRegion stompedTexture;

    public Goomba(GameCallbacks callbacks, World world, TextureAtlas atlas,
                  float posX, float posY) {
        super(callbacks, world, posX, posY);
        walkAnimation = new Animation(0.4f, atlas.findRegions(RegionNames.GOOMBA), Animation.PlayMode.LOOP);
        stompedTexture = atlas.findRegion(RegionNames.GOOMBA_STOMP);

        state = new GameObjectState<State>(State.WALKING);
        speed = -0.8f;
        setBounds(getX(), getY(), Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!isDead() && !isDrowning()) {
            state.upate(delta);
            getBody().setLinearVelocity(speed, getBody().getLinearVelocity().y);
        }

        setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2 + 1f / Cfg.PPM);
        setRegion(getFrame());

        if (state.is(State.STOMPED) && state.timer() > 1f) {
            markRemovable();
        }

        if (isDrowning()) {
            getBody().setLinearVelocity(getBody().getLinearVelocity().x * 0.95f, getBody().getLinearVelocity().y * 0.33f);
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

        if (speed > 0 && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (speed < 0 && textureRegion.isFlipX()) {
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
        fixtureDef.friction = 0.8f;
        CircleShape shape = new CircleShape();
        shape.setRadius(6f / Cfg.PPM);
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.MARIO |
                Bits.ENEMY |
                Bits.BLOCK_TOP;

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
        fixtureDef.filter.categoryBits = Bits.ENEMY_HEAD;
        fixtureDef.filter.maskBits = Bits.MARIO;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.GROUND
                | Bits.COLLIDER
                | Bits.OBJECT;
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
        reverseDirection();
    }

    public void reverseDirection() {
        speed = -speed;
        getCallbacks().hitWall(this);
    }

    private void stomp() {
        getCallbacks().stomp(this);

        state.set(State.STOMPED);
        markDestroyBody();
    }

    @Override
    public void drown() {
        drowning = true;
        getBody().setLinearVelocity(getBody().getLinearVelocity().x / 10, getBody().getLinearVelocity().y / 10);
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
    public Vector2 getLinearVelocity() {
        return getBody().getLinearVelocity();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
        out.writeFloat(speed);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        speed = in.readFloat();
    }
}
