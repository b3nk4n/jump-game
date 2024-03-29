package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.Gdx;
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
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.models.PlatformBouncer;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class Platform extends Sprite implements BinarySerializable {
    public static final float DEFAULT_SPEED = 2.5f;
    private static final float SHAKE_TIME = 0.66f;
    private static final Vector2 FALLING_VELOCITY = new Vector2(0, Cfg.GRAVITY);

    private enum State {
        MOVING, BREAKING, FALLING
    }

    private String id;
    private final GameCallbacks callbacks;
    private final World world;
    private final Body body;
    private Vector2 targetVelocity;

    private final GameObjectState<State> state;
    private float touchTTL = 0.33f;
    private boolean breakable;

    private final Array<PlatformBouncer> bouncerRegions;

    private String group;

    public Platform(GameCallbacks callbacks, World world, TextureAtlas atlas, Rectangle bounds,
                    String group, int startAngle, boolean breakable, float speed,
                    Array<PlatformBouncer> bouncerRegions) {
        this.id = UUID.randomUUID().toString();
        this.callbacks = callbacks;
        this.world = world;
        state = new GameObjectState<>(State.MOVING);
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);

        setRegion(getTextureRegion(atlas, bounds, breakable));

        body = defineBody();
        targetVelocity = getDirectionOfSimpleAngle(startAngle).scl(speed);
        this.breakable = breakable;
        this.bouncerRegions = bouncerRegions;
        setActive(false); // sleep and activate as soon as player gets close
        this.group = group;
    }

    private TextureAtlas.AtlasRegion getTextureRegion(TextureAtlas atlas, Rectangle bounds,
                                                      boolean breakable) {
        int width = Math.round(bounds.width / (Cfg.BLOCK_SIZE_PPM));
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
        } else if (angle == 45) {
            return new Vector2(1, 1).setLength(1f);
        } else if (angle == 90) {
            return new Vector2(0, 1);
        } else if (angle == 135) {
            return new Vector2(-1, 1).setLength(1f);
        } else if (angle == 180) {
            return new Vector2(-1, 0);
        } else if (angle == 225) {
            return new Vector2(-1, -1).setLength(1f);
        } else if (angle == 270) {
            return new Vector2(0, -1);
        } else if (angle == 315) {
            return new Vector2(1, -1).setLength(1f);
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
        fixtureDef.friction = Cfg.GROUND_FRICTION;
        fixtureDef.filter.categoryBits = Bits.PLATFORM;
        fixtureDef.filter.maskBits = Bits.PLAYER |
                Bits.PLAYER_GROUND |
                Bits.PLAYER_FEET |
                Bits.ENEMY |
                Bits.ENEMY_SIDE |
                Bits.ITEM |
                Bits.BULLET;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        return body;
    }

    public void update(float delta) {
        state.update(delta);

        if (!state.is(State.FALLING)) {
            for (PlatformBouncer bouncer : bouncerRegions) {
                Rectangle rect = getBoundingRectangle();
                if (Intersector.overlaps(bouncer.getRegion(), rect)) {
                    bounce(bouncer.getAngle(), bouncer.getSpeed());
                    break;
                }
            }
        }

        Vector2 currentVelocity = body.getLinearVelocity();

        if (!currentVelocity.equals(targetVelocity)) {
            currentVelocity.x += (targetVelocity.x - currentVelocity.x) * delta;
            currentVelocity.y += (targetVelocity.y - currentVelocity.y) * delta;
        }

        float xOffset = 0f;
        float yOffset = 0f;

        if (state.is(State.BREAKING)) {
            xOffset = (float) Math.sin(state.timer() * 31f) / Cfg.PPM;
            yOffset = (float) Math.sin(state.timer() * 51f) / Cfg.PPM;

            if (state.timer() > SHAKE_TIME) {
                state.set(State.FALLING);
                targetVelocity = FALLING_VELOCITY;
                Filter filter = body.getFixtureList().get(0).getFilterData();
                filter.maskBits = Bits.NOTHING;
                for (Fixture fixture : getBody().getFixtureList()) {
                    fixture.setFilterData(filter);
                }
            }
        }

        body.setLinearVelocity(currentVelocity);
        setPosition(body.getPosition().x + xOffset - getWidth() / 2,
                body.getPosition().y + yOffset - getHeight() / 2);
    }

    private void bounce(int angle, float speed) {
        targetVelocity = getDirectionOfSimpleAngle(angle).scl(speed);
    }

    public void touch(float delta) {
        touchTTL -= delta;
        if (touchTTL < 0 && breakable && state.is(State.MOVING)) {
            state.set(State.BREAKING);
            Gdx.input.vibrate((int)((SHAKE_TIME) * 1000));
        }
    }

    private final Vector2 otherBodyLinearVelocity = new Vector2();
    public Vector2 getRelativeVelocityOf(Body otherBody) {
        // take copy, otherwise other calls to otherBody.getLinearVelocity() would override this
        // value because they happen on the same (inside LibGdx) recycled reference
        otherBodyLinearVelocity.set(otherBody.getLinearVelocity());
        return otherBodyLinearVelocity.sub(body.getLinearVelocity());
    }

    public String getId() {
        return id;
    }

    public void setActive(boolean active) {
        body.setActive(active);
    }

    public boolean isActive() {
        return body.isActive();
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

    public boolean hasGroup() {
        return group != null;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(group != null ? group : "null");
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(targetVelocity.x);
        out.writeFloat(targetVelocity.y);
        out.writeBoolean(breakable);
        out.writeFloat(touchTTL);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        group = in.readUTF();
        if (group.equals("null")) {
            group = null;
        }
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        targetVelocity.set(in.readFloat(), in.readFloat());
        breakable = in.readBoolean();
        touchTTL = in.readFloat();
    }
}
