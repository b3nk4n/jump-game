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
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;

public class Spiky extends Enemy implements Drownable {
    private static final float SPEED_VALUE = 0.4f;

    public enum State {
        WALKING
    }

    private GameObjectState<State> state;
    private boolean drowning;

    private float speed;

    private final Animation<TextureRegion> walkAnimation;

    public Spiky(GameCallbacks callbacks, World world, TextureAtlas atlas,
                 float posX, float posY, boolean rightDirection) {
        super(callbacks, world, posX, posY);
        walkAnimation = new Animation(0.2f, atlas.findRegions(RegionNames.SPIKY), Animation.PlayMode.LOOP);

        state = new GameObjectState<State>(State.WALKING);
        speed = rightDirection ? SPEED_VALUE : -SPEED_VALUE;
        setBounds(getX(), getY(), Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!isDead() && !isDrowning()) {
            state.upate(delta);
            getBody().setLinearVelocity(speed, getBody().getLinearVelocity().y);
        }

        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - 7 / Cfg.PPM);
        setRegion(getFrame());

        if (isDrowning()) {
            getBody().setLinearVelocity(getBody().getLinearVelocity().x * 0.95f, getBody().getLinearVelocity().y * 0.33f);
        }
    }

    private TextureRegion getFrame() {
        TextureRegion textureRegion;

        switch (state.current()) {
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
        CircleShape shape = new CircleShape();
        shape.setRadius(6f / Cfg.PPM);
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.MARIO |
                Bits.ENEMY |
                Bits.BLOCK_TOP |
                Bits.FIREBALL;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.GROUND
                | Bits.COLLIDER
                | Bits.ITEM_BOX
                | Bits.BRICK
                | Bits.OBJECT;
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
    public void onHeadHit(Mario mario) {
        // NOOP
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa otherKoopa = (Koopa) enemy;
            if (otherKoopa.getState() == Koopa.State.MOVING_SHELL) {
                kill(true);
            } else {
                reverseDirection();
            }
        } else {
            reverseDirection();
        }
    }

    public void reverseDirection() {
        speed = -speed;
        getCallbacks().hitWall(this);
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

    public State getState() {
        return state.current();
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
        out.writeBoolean(drowning);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        speed = in.readFloat();
        drowning = in.readBoolean();
    }
}
