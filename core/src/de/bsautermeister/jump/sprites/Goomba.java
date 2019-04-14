package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;

public class Goomba extends Enemy {

    public enum State {
        WALKING, STOMPED, DEAD, REMOVABLE
    }

    private GameObjectState<State> state;

    private Animation<TextureRegion> walkAnimation;
    private TextureAtlas atlas;

    public Goomba(World world, TiledMap map, TextureAtlas atlas, float posX, float posY) {
        super(world, map, posX, posY);

        this.state = new GameObjectState<State>(State.WALKING);

        this.atlas = atlas;
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(atlas.findRegion("goomba"), i * 16, 0, 16, 16));
        }
        walkAnimation = new Animation(0.4f, frames);
        setBounds(getX(), getY(), 16 / GameConfig.PPM, 16 / GameConfig.PPM);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        state.upate(delta);

        switch (state.current()) {
            case WALKING:
                getBody().setLinearVelocity(getVelocity());
                setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2);
                setRegion(walkAnimation.getKeyFrame(state.timer(), true));
                break;
            case STOMPED:
                setRegion(new TextureRegion(atlas.findRegion("goomba"), 32, 0, 16, 16));
                if (state.timer() > 1f) {
                    state.set(State.REMOVABLE);
                }
                break;
            case DEAD:
                if (state.timer() > 5f) {
                    state.set(State.REMOVABLE);
                    destroyLater();
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
                JumpGame.ENEMY_BIT;

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

        /*EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(-2 / GameConfig.PPM, 6 / GameConfig.PPM),
                new Vector2(2 / GameConfig.PPM, 6 / GameConfig.PPM));
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true; // does not collide, but provides user-data
        body.createFixture(fixtureDef).setUserData("head");*/
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
                kill(State.DEAD);
            } else {
                reverseVelocity(true, false);
            }
        }
    }

    private void kill(State killState) {
        state.set(killState);
        Filter filter = new Filter();
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }

        if (killState != State.STOMPED) {
            getBody().applyLinearImpulse(new Vector2(0, 5f), getBody().getWorldCenter(), true);
        }
    }

    private void stomp() {
        state.set(State.STOMPED);
        destroyLater();
        JumpGame.assetManager.get(AssetPaths.Sounds.STOMP, Sound.class).play();
    }

    @Override
    public boolean canBeRemoved() {
        return state.is(State.REMOVABLE);
    }
}
