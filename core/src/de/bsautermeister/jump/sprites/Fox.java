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
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;

public class Fox extends Enemy implements Drownable {
    private static final float SPEED = 0.8f;

    private static final int NORMAL_IDX = 0;
    private static final int ANGRY_IDX = 1;

    public enum State {
        WALKING, STANDING, STOMPED
    }

    private GameObjectState<State> state;
    private boolean drowning;
    private float speed;
    private boolean previousDirectionLeft;

    private Vector2 playerPosition = new Vector2();

    private final Animation<TextureRegion>[] walkAnimation;
    private final Animation<TextureRegion>[] standingAnimation;
    private final Animation<TextureRegion> stompedAnimation;

    public Fox(GameCallbacks callbacks, World world, TextureAtlas atlas,
               float posX, float posY, boolean rightDirection) {
        super(callbacks, world, posX, posY, Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);

        walkAnimation = new Animation[2];
        walkAnimation[NORMAL_IDX] = new Animation<TextureRegion>(0.05f, atlas.findRegions(RegionNames.FOX_WALK), Animation.PlayMode.LOOP);
        walkAnimation[ANGRY_IDX] = new Animation<TextureRegion>(0.05f, atlas.findRegions(RegionNames.FOX_ANGRY_WALK), Animation.PlayMode.LOOP);

        standingAnimation = new Animation[2];
        standingAnimation[NORMAL_IDX] = new Animation<TextureRegion>(0.05f, atlas.findRegions(RegionNames.FOX_STANDING), Animation.PlayMode.LOOP);
        standingAnimation[ANGRY_IDX] = new Animation<TextureRegion>(0.05f, atlas.findRegions(RegionNames.FOX_ANGRY_STANDING), Animation.PlayMode.LOOP);

        stompedAnimation = new Animation<TextureRegion>(0.05f, atlas.findRegions(RegionNames.FOX_STOMP), Animation.PlayMode.NORMAL);

        state = new GameObjectState<State>(State.WALKING);
        speed = rightDirection ? SPEED : -SPEED;
        setRegion(getFrame());
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

        if (state.is(State.STANDING) && state.timer() > 2f) {
            state.set(State.WALKING);
            speed = previousDirectionLeft ? SPEED : - SPEED;
        }

        if (isDrowning()) {
            getBody().setLinearVelocity(getBody().getLinearVelocity().x * 0.95f, getBody().getLinearVelocity().y * 0.33f);
        }
    }

    private TextureRegion getFrame() {
        boolean isLeft = speed < 0 || speed == 0 && previousDirectionLeft;

        TextureRegion textureRegion;

        float x = getBody().getPosition().x;
        float y = getBody().getPosition().y;
        int characterIdx = (isLeft && playerPosition.x < x || !isLeft && playerPosition.x > x) &&
                Vector2.len2(playerPosition.x - x, playerPosition.y - y) < 1.0f
                ? ANGRY_IDX : NORMAL_IDX;

        switch (state.current()) {
            case STOMPED:
                textureRegion = stompedAnimation.getKeyFrame(state.timer());
                break;
            case STANDING:
                textureRegion = standingAnimation[characterIdx].getKeyFrame(state.timer());
                break;
            case WALKING:
            default:
                textureRegion = walkAnimation[characterIdx].getKeyFrame(state.timer());
                break;
        }



        if (!isLeft && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (isLeft && textureRegion.isFlipX()) {
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
        fixtureDef.friction = 0.8f;
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

        // head
        PolygonShape headShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-3.5f, 12).scl(1 / Cfg.PPM);
        vertices[1] = new Vector2(3.5f, 12).scl(1 / Cfg.PPM);
        vertices[2] = new Vector2(-2.5f, 6).scl(1 / Cfg.PPM);
        vertices[3] = new Vector2(2.5f, 6).scl(1 / Cfg.PPM);
        headShape.set(vertices);

        fixtureDef.shape = headShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_HEAD;
        fixtureDef.filter.maskBits = Bits.PLAYER;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.GROUND
                | Bits.COLLIDER
                | Bits.ITEM_BOX
                | Bits.BRICK;
        fixtureDef.isSensor = true;
        sideShape.set(new Vector2(-6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(-6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_LEFT));
        sideShape.set(new Vector2(6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_RIGHT));
        return body;
    }

    @Override
    public void onHeadHit(Player player) {
        if (player.isDead() || player.isInvincible()) {
            return;
        }

        stomp();
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
        speed = (getBody().getPosition().x < otherEnemy.getBody().getPosition().x)
                ? -SPEED : SPEED;
    }

    public void changeDirectionBySideSensorTag(String sideSensorTag) {
        float absoluteSpeed = Math.abs(speed);
        if (sideSensorTag.equals(TAG_LEFT)) {
            speed = absoluteSpeed;
        } else {
            speed = -absoluteSpeed;
        }

        getCallbacks().hitWall(this);
    }

    public void waitAndThenChangeDirectionBySideSensorTag(String sideSensorTag) {
        state.set(State.STANDING);
        previousDirectionLeft = speed <= 0;
        speed = 0;
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

    public void setPlayerPosition(Vector2 playerPosition) {
        this.playerPosition.set(playerPosition);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
        out.writeFloat(speed);
        out.writeBoolean(previousDirectionLeft);
        out.writeFloat(playerPosition.x);
        out.writeFloat(playerPosition.y);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        speed = in.readFloat();
        previousDirectionLeft = in.readBoolean();
        playerPosition.set(in.readFloat(), in.readFloat());
    }
}
