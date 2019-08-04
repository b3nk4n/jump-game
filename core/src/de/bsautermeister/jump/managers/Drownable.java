package de.bsautermeister.jump.managers;

import com.badlogic.gdx.math.Vector2;

public interface Drownable {
    void drown();
    boolean isDrowning();

    /**
     * Whether the drownable object is dead, because dead object don't drown.
     */
    boolean isDead();

    Vector2 getWorldCenter();
}
