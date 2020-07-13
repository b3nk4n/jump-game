package de.bsautermeister.jump.sprites.enemies;

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
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.sprites.GameObjectState;
import de.bsautermeister.jump.sprites.Player;

public class Frog extends Enemy implements Drownable {
    public enum State {
        STANDING,
        JUMPING,
        STOMPED
    }

    private GameObjectState<State> state;
    private boolean drowning;
    private boolean isLeft;

    private final TextureRegion standing;
    private final Animation<TextureRegion> jumpingAnimation;
    private final Animation<TextureRegion> stompedAnimation;

    public Frog(GameCallbacks callbacks, World world, TextureAtlas atlas,
                float posX, float posY, boolean rightDirection) {
        super(callbacks, world, posX, posY, Cfg.BLOCK_SIZE_PPM, Cfg.BLOCK_SIZE_PPM);
        this.isLeft = !rightDirection;

        standing = atlas.findRegion(RegionNames.FROG_STANDING);
        jumpingAnimation = new Animation<TextureRegion>(0.15f,
                atlas.findRegions(RegionNames.FROG_JUMPING), Animation.PlayMode.NORMAL);
        stompedAnimation = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.FROG_STOMPED), Animation.PlayMode.NORMAL);

        state = new GameObjectState<>(State.STANDING);
        setRegion(getFrame());
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        state.upate(delta);

        setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2 + 2f / Cfg.PPM);
        setRegion(getFrame());

        if (!state.is(State.STOMPED)) {
            if (state.is(State.STANDING)) {
                getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);

                if (state.timer() > 1f && !isDrowning() && !isDead()) {
                    state.set(State.JUMPING);
                    getBody().applyLinearImpulse(isLeft ? -5f : 5f, 6f, 0f, 0f, true);
                }
            }

            if (isDrowning()) {
                getBody().setLinearVelocity(getBody().getLinearVelocity().x * 0.95f, getBody().getLinearVelocity().y * 0.33f);
            }
        } else if (state.is(State.STOMPED)) {
            if (state.timer() > 0.7f) {
                markDestroyBody();
                markRemovable();
            }
        }
    }

    private TextureRegion getFrame() {
        TextureRegion textureRegion;
        switch (state.current()) {
            case STOMPED:
                textureRegion = stompedAnimation.getKeyFrame(state.timer());
                break;
            case STANDING:
                textureRegion = standing;
                break;
            case JUMPING:
            default:
                textureRegion = jumpingAnimation.getKeyFrame(state.timer());
                break;
        }

        if (!isLeft && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (isLeft && !textureRegion.isFlipX()) {
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
        bodyDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 1.0f;
        CircleShape shape = new CircleShape();
        shape.setRadius(6f / Cfg.PPM);
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.PLAYER |
                Bits.ENEMY |
                Bits.BLOCK_TOP |
                Bits.BULLET;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();

        // head
        PolygonShape headShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5f, 8).scl(1 / Cfg.PPM);
        vertices[1] = new Vector2(5f, 8).scl(1 / Cfg.PPM);
        vertices[2] = new Vector2(-2.5f, 0).scl(1 / Cfg.PPM);
        vertices[3] = new Vector2(2.5f, 0).scl(1 / Cfg.PPM);
        headShape.set(vertices);

        fixtureDef.shape = headShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_HEAD;
        fixtureDef.filter.maskBits = Bits.PLAYER_FEET;
        body.createFixture(fixtureDef).setUserData(this);
        headShape.dispose();

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.GROUND
                //| Bits.COLLIDER frogs ignore the collider: they like jumping into water :)
                | Bits.ITEM_BOX
                | Bits.BRICK
                | Bits.PLATFORM;
        fixtureDef.isSensor = true;
        sideShape.set(new Vector2(-6.5f / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(-6.5f / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_LEFT));
        sideShape.set(new Vector2(6.5f / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(6.5f / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_RIGHT));
        sideShape.set(new Vector2(-4 / Cfg.PPM, -6.5f / Cfg.PPM),
                new Vector2(4 / Cfg.PPM, -6.5f / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_BOTTOM));
        sideShape.dispose();
        return body;
    }

    @Override
    public void onHeadHit(Player player) {
        if (player.isDead() || player.isInvincible()) {
            return;
        }
        Vector2 velocity = getBody().getLinearVelocity();
        getBody().setLinearVelocity(velocity.x * 0.5f, velocity.y);
        stomp();
    }

    private void stomp() {
        getCallbacks().stomp(this);

        state.set(State.STOMPED);
        updateMaskFilter(Bits.ENVIRONMENT_ONLY);
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Hedgehog) {
            Hedgehog hedgehog = (Hedgehog) enemy;
            if (hedgehog.getState() == Hedgehog.State.ROLLING) {
                kill(true);
                return;
            }
        }
        runAwayFrom(enemy);
        getCallbacks().hitWall(this);
    }

    private void runAwayFrom(Enemy otherEnemy) {
        isLeft = getBody().getPosition().x < otherEnemy.getBody().getPosition().x;
    }

    public void reactOnSideSensor(String sideSensorTag) {
        if (sideSensorTag.equals(TAG_LEFT)) {
            isLeft = false;
            getCallbacks().hitWall(this);
        } else if (sideSensorTag.equals(TAG_RIGHT)) {
            isLeft = true;
            getCallbacks().hitWall(this);
        } else if (sideSensorTag.equals(TAG_BOTTOM)) {
            if (state.is(State.JUMPING) && !isDrowning() && !isDead()) {
                state.set(State.STANDING);
            }
        }
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
        out.writeBoolean(isLeft);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        isLeft = in.readBoolean();
    }
}
