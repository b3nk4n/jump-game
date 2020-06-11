package de.bsautermeister.jump.screens.finish.model;

import com.badlogic.gdx.math.MathUtils;

public class Person {
    public final boolean isPlaceholder;
    private float activeTime;
    private final Animatable spriteAnimation;
    private final boolean swinging;
    private float rotation = 0f;
    // TODO character bit, color, action (wipping, jumping, cheering)? Or even sub-typing?

    private Person(boolean isPlaceholder, float delay, Animatable spriteAnimation, boolean swinging) {
        this.isPlaceholder = isPlaceholder;
        this.activeTime = -delay;
        this.spriteAnimation = spriteAnimation;
        this.swinging = swinging;
    }

    public Person(float delay, Animatable spriteAnimation) {
        this(false, delay, spriteAnimation, false);
    }

    public Person(float delay, Animatable spriteAnimation, boolean swinging) {
        this(false, delay, spriteAnimation, swinging);
    }

    public static Person empty() {
        return new Person(true, 0f, null, false);
    }

    public void update(float delta) {
        if (isPlaceholder) return;

        activeTime += delta;
        if (activeTime < 0) {
            return;
        }

        if (swinging) {
            rotation = MathUtils.sin(activeTime * MathUtils.PI) * 10f;
        }

        spriteAnimation.update(delta);
    }

    public float getAnimationValue() {
        return spriteAnimation.getValue();
    }

    public float getRotation() {
        return rotation;
    }
}
