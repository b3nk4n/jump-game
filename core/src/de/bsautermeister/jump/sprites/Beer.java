package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.tools.GameTimer;

public class Beer extends Item {

    private GameTimer impulsTimer;

    public Beer(GameCallbacks callbacks, World world, TextureAtlas atlas, float x, float y) {
        super(callbacks, world, x, y);
        setRegion(atlas.findRegion(RegionNames.BEER), 0, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
        velocity = Vector2.Zero;
        impulsTimer = new GameTimer(3f);
        state.setStateCallback(new GameObjectState.StateCallback<State>() {
            @Override
            public void changed(State previousState, State newState) {
                if (newState == State.SPAWNED) {
                    impulsTimer.restart();
                }
            }
        });
    }

    @Override
    public Body defineBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = 0.66f;
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / Cfg.PPM);
        fixtureDef.filter.categoryBits = Bits.ITEM;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.MARIO |
                Bits.OBJECT |
                Bits.BLOCK_TOP;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        return body;
    }

    @Override
    public void collectBy(Mario mario) {
        getCallbacks().use(mario, this);
        markDestroyBody();
        mario.drunk();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (state.is(State.SPAWNED)) {
            setPosition(getBody().getPosition().x - getWidth() / 2,
                    getBody().getPosition().y - getHeight() / 2 + 2f / Cfg.PPM);

            impulsTimer.update(delta);
            if (impulsTimer.isFinished()) {
                impulsTimer.restart();
                getBody().setLinearVelocity(0f, 2f);
            }
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        impulsTimer.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        impulsTimer.read(in);
    }
}
