package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import de.bsautermeister.jump.GameConfig;

public abstract class Item extends Sprite {
    private World world;
    private TiledMap tiledMap;
    protected Vector2 velocity;
    private boolean markForDestory;
    private boolean destroyed;
    private Body body;

    public Item(World world, TiledMap tiledMap, float x, float y) {
        this.world = world;
        this.tiledMap = tiledMap;
        setPosition(x, y);
        setBounds(getX(), getY(), 16 / GameConfig.PPM, 16 / GameConfig.PPM);
        body = defineBody();
        markForDestory = false;
        destroyed = false;
    }

    public abstract Body defineBody();
    public abstract void use(Mario mario);

    public void update(float delta) {
        if (markForDestory && !destroyed) {
            world.destroyBody(body);
            destroyed = true;
        }
    }

    @Override
    public void draw(Batch batch) {
        if (!destroyed) {
            super.draw(batch);
        }
    }

    public void destroy() {
        markForDestory = true; // TODO or inside use(), similar as in Goomba/Enemy?
    }

    public void reverseVelocity(boolean reverseX, boolean reverseY) {
        if (reverseX) {
            velocity.x = -velocity.x;
        }
        if (reverseY) {
            velocity.y = -velocity.y;
        }
    }

    public World getWorld() {
        return world;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public Body getBody() {
        return body;
    }
}
