package de.bsautermeister.jump.screens.finish.model;

public class Person {
    public final boolean isPlaceholder;
    private float delay;
    private final Animatable spriteAnimation;
    // TODO character bit, color, action (wipping, jumping, cheering)? Or even sub-typing?

    private Person(boolean isPlaceholder) {
        this.isPlaceholder = isPlaceholder;
        this.delay = 0f;
        this.spriteAnimation = null;
    }

    public Person(float delay, Animatable spriteAnimation) {
        isPlaceholder = false;
        this.delay = delay;
        this.spriteAnimation = spriteAnimation;
    }

    public static Person empty() {
        return new Person(true);
    }

    public void update(float delta) {
        if (isPlaceholder) return;

        if (delay >= 0) {
            delay -= delta;
            return;
        }

        spriteAnimation.update(delta);
    }

    public float getAnimationValue() {
        return spriteAnimation.getValue();
    }
}
