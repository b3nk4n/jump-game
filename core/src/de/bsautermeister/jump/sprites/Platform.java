package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.models.PlatformBouncer;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class Platform extends Sprite implements BinarySerializable {
    private static final float SPEED = 0.5f;
    private static final float SHAKE_TIME = 1f;
    private static final Vector2 FALLING_VELOCITY = new Vector2(0, -9.81f);

    private enum State {
        MOVING, BREAKING, FALLING
    }

    private String id;
    private GameCallbacks callbacks;
    private World world;
    private Body body;
    private Vector2 targetVelocity;

    private GameObjectState<State> state; // TODO: due to the state, restore/removable logic in GameScreen has to be changed, because Platforms can now disappear
    private boolean breakable;

    private Array<PlatformBouncer> bouncerRegions;

    public Platform(GameCallbacks callbacks, World world, TextureAtlas atlas, Rectangle bounds,
                    int startAngle, boolean breakable, Array<PlatformBouncer> bouncerRegions) {
        this.id = UUID.randomUUID().toString();
        this.callbacks = callbacks;
        this.world = world;
        state = new GameObjectState<State>(State.MOVING);
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);

        setRegion(getTextureRegion(atlas, bounds, breakable));

        body = defineBody();
        targetVelocity = getDirectionOfSimpleAngle(startAngle).scl(SPEED);
        this.breakable = breakable;
        this.bouncerRegions = bouncerRegions;
        setActive(true); // sleep and activate as soon as player gets close
    }

    private TextureAtlas.AtlasRegion getTextureRegion(TextureAtlas atlas, Rectangle bounds,
                                                      boolean breakable) {
        int width = Math.round(bounds.width / (Cfg.BLOCK_SIZE / Cfg.PPM));
        switch (width) {
            case 2:
                return atlas.findRegion(breakable ?
                        RegionNames.BREAK_PLATFORM2 : RegionNames.PLATFORM2);
            case 3:
                return atlas.findRegion(breakable ?
                        RegionNames.BREAK_PLATFORM3 : RegionNames.PLATFORM3);
            case 4:
                return atlas.findRegion(breakable ?
                        RegionNames.BREAK_PLATFORM4 : RegionNames.PLATFORM4);
            default:
                throw new IllegalArgumentException("Unsupported block_width for platform: " + width);
        }
    }

    private Vector2 getDirectionOfSimpleAngle(int angle) {
        if (angle == 0) {
            return new Vector2(1, 0);
        } else if (angle == 90) {
            return new Vector2(0, 1);
        } else if (angle == 180) {
            return new Vector2(-1, 0);
        } else if (angle == 270) {
            return new Vector2(0, -1);
        } else {
            throw new IllegalArgumentException("Unsupported angle: " + angle);
        }
    }

    private Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        Rectangle bounds = getBoundingRectangle();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set((bounds.getX() + bounds.getWidth() / 2),
                (bounds.getY() + bounds.getHeight() / 2));
        body = world.createBody(bodyDef);
        float cornerWidth = 3 / Cfg.PPM;
        shape.set(new float[] {
                -bounds.getWidth() / 2, bounds.getHeight() / 2 - cornerWidth,
                -bounds.getWidth() / 2 + cornerWidth, bounds.getHeight() / 2,
                bounds.getWidth() / 2 - cornerWidth, bounds.getHeight() / 2,
                bounds.getWidth() / 2, bounds.getHeight() / 2 - cornerWidth,
                bounds.getWidth() / 2, -bounds.getHeight() / 2 + cornerWidth,
                bounds.getWidth() / 2 - cornerWidth, -bounds.getHeight() / 2,
                -bounds.getWidth() / 2 + cornerWidth, -bounds.getHeight() / 2,
                -bounds.getWidth() / 2, -bounds.getHeight() / 2 + cornerWidth,
        });
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Bits.PLATFORM;
        fixtureDef.filter.maskBits = Bits.MARIO |
                Bits.MARIO_FEET |
                Bits.ENEMY |
                Bits.ITEM;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        return body;
    }

    public void update(float delta) {
        state.upate(delta);

        if (!state.is(State.FALLING)) {
            for (PlatformBouncer bouncer : bouncerRegions) {
                Rectangle rect = getBoundingRectangle();
                if (Intersector.overlaps(bouncer.getRegion(), rect)) {
                    bounce(bouncer.getAngle());
                    break;
                }
            }
        }

        Vector2 currentVelocity = body.getLinearVelocity();

        if (!currentVelocity.equals(targetVelocity)) {
            currentVelocity.x += (targetVelocity.x - currentVelocity.x) * delta;
            currentVelocity.y += (targetVelocity.y - currentVelocity.y) * delta;
        }

        if (state.is(State.BREAKING)) {

            if (state.timer() > SHAKE_TIME) {
                state.set(State.FALLING);
                targetVelocity = FALLING_VELOCITY;
                Filter filter = body.getFixtureList().get(0).getFilterData();
                filter.maskBits = Bits.NOTHING;
            }
        }

        body.setLinearVelocity(currentVelocity);
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
    }

    public void bounce(int angle) {
        targetVelocity = getDirectionOfSimpleAngle(angle).scl(SPEED);
    }

    public void touch() {
        if (breakable && state.is(State.MOVING)) {
            state.set(State.BREAKING);
        }
    }

    public Vector2 getRelativeVelocityOf(Body otherBody) {
        return otherBody.getLinearVelocity().sub(body.getLinearVelocity());
    }

    public String getId() {
        return id;
    }

    public void setActive(boolean active) {
        body.setActive(active);
    }

    public World getWorld() {
        return world;
    }

    public Body getBody() {
        return body;
    }

    public GameCallbacks getCallbacks() {
        return callbacks;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(targetVelocity.x);
        out.writeFloat(targetVelocity.y);
        out.writeBoolean(breakable);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        targetVelocity.set(in.readFloat(), in.readFloat());
        breakable = in.readBoolean();
    }
}
