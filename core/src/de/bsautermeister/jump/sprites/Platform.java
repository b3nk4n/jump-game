package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.models.PlatformBouncer;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class Platform extends Sprite implements BinarySerializable {
    public static final float SPEED = 0.5f;

    private String id;
    private GameCallbacks callbacks;
    private World world;
    private Body body;
    private Vector2 currentVelocity;
    private Vector2 targetVelocity;

    private Array<PlatformBouncer> bouncerRegions;

    public Platform(GameCallbacks callbacks, World world, TextureAtlas atlas, Rectangle bounds,
                    int startAngle, Array<PlatformBouncer> bouncerRegions) {
        this.id = UUID.randomUUID().toString();
        this.callbacks = callbacks;
        this.world = world;
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);

        int width = Math.round(bounds.width / (Cfg.BLOCK_SIZE / Cfg.PPM));
        switch (width) {
            case 2:
                setRegion(atlas.findRegion(RegionNames.PLATFORM2));
                break;
            case 3:
                setRegion(atlas.findRegion(RegionNames.PLATFORM3));
                break;
            case 4:
                setRegion(atlas.findRegion(RegionNames.PLATFORM4));
                break;
            default:
                throw new IllegalArgumentException("Unsupported block_width for platform: " + width);
        }

        this.body = defineBody();
        this.currentVelocity = new Vector2();
        this.targetVelocity = getDirectionOfSimpleAngle(startAngle).scl(SPEED);
        this.bouncerRegions = bouncerRegions;
        setActive(true); // sleep and activate as soon as player gets close
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
        shape.setAsBox(bounds.getWidth() / 2,
                bounds.getHeight() / 2);
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = JumpGame.GROUND_BIT;
        fixtureDef.filter.maskBits = JumpGame.MARIO_BIT |
                JumpGame.MARIO_FEET_BIT |
                JumpGame.ENEMY_BIT |
                JumpGame.ITEM_BIT;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        return body;
    }

    public void update(float delta) {
        for (PlatformBouncer bouncer : bouncerRegions) {
            Rectangle rect = getBoundingRectangle();
            if (Intersector.overlaps(bouncer.getRegion(), rect)) {
                bounce(bouncer.getAngle());
                break;
            }
        }

        if (!currentVelocity.equals(targetVelocity)) {
            currentVelocity.x += (targetVelocity.x - currentVelocity.x) * delta;
            currentVelocity.y += (targetVelocity.y - currentVelocity.y) * delta;
        }

        body.setLinearVelocity(getCurrentVelocity());

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
    }

    public void bounce(int angle) {
        targetVelocity = getDirectionOfSimpleAngle(angle).scl(SPEED);
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

    public Vector2 getCurrentVelocity() {
        return currentVelocity;
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
        out.writeFloat(currentVelocity.x);
        out.writeFloat(currentVelocity.y);
        out.writeFloat(targetVelocity.x);
        out.writeFloat(targetVelocity.y);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        currentVelocity.set(in.readFloat(), in.readFloat());
        targetVelocity.set(in.readFloat(), in.readFloat());
    }
}
