package de.bsautermeister.jump.sprites;

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

public class FireFlower extends Item {

    public FireFlower(GameCallbacks callbacks, World world, TextureAtlas atlas, float x, float y) {
        super(callbacks, world, x, y);
        setRegion(atlas.findRegion(RegionNames.FIRE_FLOWER), 0, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
        velocity = Vector2.Zero;
    }

    @Override
    public Body defineBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / Cfg.PPM);
        fixtureDef.filter.categoryBits = Bits.ITEM;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
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
        mario.grow();
        mario.setOnFire();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (state.is(State.SPAWNED)) {
            setPosition(getBody().getPosition().x - getWidth() / 2,
                    getBody().getPosition().y - getHeight() / 2 + 2f / Cfg.PPM);
        }
    }
}
