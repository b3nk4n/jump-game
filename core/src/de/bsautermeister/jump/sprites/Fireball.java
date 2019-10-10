package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.Bits;

public class Fireball extends Sprite {

    private static final float VELOCITY_X = 2.25f;

    private final GameCallbacks callbacks;
    private final World world;
    private Body body;
    private float rotation;
    private boolean rightDirection;

    private float previousVelocityY;

    private MarkedAction reset;

    public Fireball(GameCallbacks callbacks, World world, TextureAtlas atlas) {
        super(atlas.findRegion(RegionNames.FIREBALL));
        this.callbacks = callbacks;
        this.world = world;
        setSize(Cfg.BLOCK_SIZE / 2 /Cfg.PPM, Cfg.BLOCK_SIZE / 2 / Cfg.PPM);
        setOrigin(getWidth() / 2, getHeight() / 2);
        body = defineBody();
        reset = new MarkedAction();
        reset();
    }

    public void reset() {
        body.setActive(false);
        body.setTransform(-1, -1, 0);
        body.setLinearVelocity(Vector2.Zero);
        setPosition(-1, -1);
        rotation = 0;
        previousVelocityY = 0;
    }

    public void fire(float posX, float posY, boolean rightDirection) {
        body.setTransform(posX, posY, 0);
        body.setActive(true);
        this.rightDirection = rightDirection;
    }

    public boolean isActive() {
        return body.isActive();
    }

    public void resetLater() {
        reset.mark();
    }

    private Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY()); // TODO top-left vs center (also e.g. in Goomba)
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1f;
        CircleShape shape = new CircleShape(); // TODO square, so that it bounces always in the same direction?
        shape.setRadius(3f / Cfg.PPM);
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Bits.FIREBALL;
        fixtureDef.filter.maskBits = Bits.ENEMY |
                Bits.GROUND |
                Bits.BRICK |
                Bits.OBJECT |
                Bits.PLATFORM;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        return body;
    }

    public void update(float delta) {
        if (!isActive()) {
            return;
        }

        if (rightDirection && body.getLinearVelocity().x < 0 ||
            !rightDirection && body.getLinearVelocity().x > 0) {
            explode();
            return;
        }

        if (previousVelocityY < 0 && body.getLinearVelocity().y > 0) {
            // started to jump up
            body.setLinearVelocity(body.getLinearVelocity().x, 2.25f);
        }

        body.setLinearVelocity(rightDirection ? VELOCITY_X : -VELOCITY_X, body.getLinearVelocity().y);
        if (rightDirection) {
            rotation -= 360 * delta;
        } else {
            rotation += 360 * delta;
        }
        setRotation(rotation);
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 + 1f / Cfg.PPM);
        setFlip(!rightDirection, false);
        previousVelocityY = body.getLinearVelocity().y;
    }

    public void postUpdate() {
        if (reset.isMarked()) {
            // reset needs to be done outside of box2d loop (contact listener)
            reset();
            reset.done();
            reset.reset();
        }
    }

    @Override
    public void draw(Batch batch) {
        if (!isActive()) {
            return;
        }

        super.draw(batch);
    }

    public void explode() {
        // todo explode when hitting other object
        reset();
    }
}
