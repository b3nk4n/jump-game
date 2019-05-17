package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class Mushroom extends Item {
    public Mushroom(GameCallbacks callbacks, World world, TextureAtlas atlas, float x, float y) {
        super(callbacks, world, x, y);
        setRegion(atlas.findRegion("mushroom"), 0, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE);
        velocity = new Vector2(0.7f, 0);
    }

    @Override
    public Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / GameConfig.PPM);
        fixtureDef.filter.categoryBits = JumpGame.ITEM_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.MARIO_BIT |
                JumpGame.OBJECT_BIT;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        return body;
    }

    @Override
    public void use(Mario mario) {
        getCallbacks().use(mario, this);
        destroyLater();
        mario.grow();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2);
        velocity.y = getBody().getLinearVelocity().y;
        getBody().setLinearVelocity(velocity);
    }
}
