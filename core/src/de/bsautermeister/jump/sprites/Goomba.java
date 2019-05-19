package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class Goomba extends Enemy {

    public enum State {
        WALKING, STOMPED
    }

    private GameObjectState<State> state;

    private Animation<TextureRegion> walkAnimation;
    private TextureAtlas atlas;

    public Goomba(GameCallbacks callbacks, World world, TiledMap map, TextureAtlas atlas,
                  float posX, float posY) {
        super(callbacks, world, map, posX, posY);

        this.state = new GameObjectState<State>(State.WALKING);

        this.atlas = atlas;
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(atlas.findRegion("goomba"), i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE));
        }
        walkAnimation = new Animation(0.4f, frames);
        setBounds(getX(), getY(), GameConfig.BLOCK_SIZE / GameConfig.PPM, GameConfig.BLOCK_SIZE / GameConfig.PPM);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!isDead()) {
            state.upate(delta);
        }

        switch (state.current()) {
            case WALKING:
                if (!isDead()) {
                    getBody().setLinearVelocity(getVelocity());
                }
                setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2);
                TextureRegion frame = walkAnimation.getKeyFrame(state.timer(), true);

                if (getVelocity().x > 0 && !frame.isFlipX()) { // TODO same in Koopa. Move this to base class?
                    frame.flip(true, false);
                } else if (getVelocity().x < 0 && frame.isFlipX()) {
                    frame.flip(true, false);
                }

                if (isDead() && !frame.isFlipY()) {
                    frame.flip(false, true);
                }

                setRegion(frame);
                break;
            case STOMPED:
                setRegion(new TextureRegion(atlas.findRegion("goomba"), 2 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE));
                if (state.timer() > 1f) {
                    markRemovable();
                }
                break;
        }
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / GameConfig.PPM);
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.MARIO_BIT |
                JumpGame.OBJECT_BIT |
                JumpGame.ENEMY_BIT |
                JumpGame.BLOCK_TOP_BIT |
                JumpGame.COLLIDER_BIT;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        // head
        PolygonShape headShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5, 8).scl(1 / GameConfig.PPM);
        vertices[1] = new Vector2(5, 8).scl(1 / GameConfig.PPM);
        vertices[2] = new Vector2(-5, 3).scl(1 / GameConfig.PPM);
        vertices[3] = new Vector2(3, 3).scl(1 / GameConfig.PPM);
        headShape.set(vertices);

        fixtureDef.shape = headShape;
        fixtureDef.restitution = 1.0f;
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_HEAD_BIT;
        fixtureDef.filter.maskBits = JumpGame.MARIO_BIT;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = JumpGame.ENEMY_SIDE_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT;
        fixtureDef.isSensor = true;
        sideShape.set(new Vector2(-6 / GameConfig.PPM, -1 / GameConfig.PPM),
                new Vector2(-6 / GameConfig.PPM, 1 / GameConfig.PPM));
        body.createFixture(fixtureDef).setUserData(this);
        sideShape.set(new Vector2(6 / GameConfig.PPM, -1 / GameConfig.PPM),
                new Vector2(6 / GameConfig.PPM, 1 / GameConfig.PPM));
        body.createFixture(fixtureDef).setUserData(this);
        return body;
    }

    @Override
    public void onHeadHit(Mario mario) {
        stomp();
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa) enemy;
            if (koopa.getState() == Koopa.State.MOVING_SHELL) {
                kill(true);
                return;
            }
        }
        reverseVelocity(true, false);
    }

    private void stomp() {
        getCallbacks().stomp(this);

        state.set(State.STOMPED);
        destroyLater();
    }
}
