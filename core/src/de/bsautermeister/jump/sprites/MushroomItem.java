package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;

public class MushroomItem extends Item implements Drownable {
    private boolean drowning = false;

    public MushroomItem(GameCallbacks callbacks, World world, TextureAtlas atlas, float x, float y) {
        super(callbacks, world, x, y);
        setRegion(atlas.findRegion(RegionNames.MUSHROOM), 0, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE);
        velocity = new Vector2(0.6f, 0);
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
                Bits.PLAYER |
                Bits.OBJECT |
                Bits.BLOCK_TOP;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(new TaggedUserData<Item>(this, TAG_BASE));

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = Bits.ITEM;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.OBJECT;
        fixtureDef.isSensor = true;
        sideShape.set(new Vector2(-6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(-6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Item>(this, TAG_LEFT));
        sideShape.set(new Vector2(6 / Cfg.PPM, -1 / Cfg.PPM),
                new Vector2(6 / Cfg.PPM, 1 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Item>(this, TAG_RIGHT));

        return body;
    }

    @Override
    public void collectBy(Player player) {
        getCallbacks().use(player, this);
        markDestroyBody();
        player.grow();
        player.stoned();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (state.is(State.SPAWNED)) {
            setPosition(getBody().getPosition().x - getWidth() / 2,
                    getBody().getPosition().y - getHeight() / 2 + 2f / Cfg.PPM);

            if (!drowning) {
                velocity.y = getBody().getLinearVelocity().y;
                getBody().setLinearVelocity(velocity);
            } else {
                getBody().setLinearVelocity(getBody().getLinearVelocity().x * 0.95f, getBody().getLinearVelocity().y * 0.33f);
            }
        }
    }

    @Override
    public boolean isDead() {
        return false;
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
}
