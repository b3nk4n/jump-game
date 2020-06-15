package de.bsautermeister.jump.screens.finish.model;

import com.badlogic.gdx.math.MathUtils;

public class Person {
    public static final int VARIATIONS = 10 + 6 + 2;

    public final boolean isPlaceholder;
    private int characterIdx;
    private float activeTime;
    private Animatable spriteAnimation;
    private float swingAngle;
    private float rotation = 0f;

    private Person(boolean isPlaceholder, float delay, Animatable spriteAnimation, float swingAngle) {
        this.isPlaceholder = isPlaceholder;
        this.characterIdx = MathUtils.random(2, VARIATIONS - 1);
        this.spriteAnimation = spriteAnimation;
        this.swingAngle = swingAngle;
        setDelay(delay);
    }

    public Person(float delay, Animatable spriteAnimation) {
        this(false, delay, spriteAnimation, 0f);
    }

    public Person(float delay, Animatable spriteAnimation, float swingAngle) {
        this(false, delay, spriteAnimation, swingAngle);
    }

    public static Person empty() {
        return new Person(true, 0f, null, 0f);
    }

    public void update(float delta) {
        if (isPlaceholder) return;

        activeTime += delta;
        if (activeTime < 0) {
            return;
        }

        if (swingAngle > 0) {
            rotation = MathUtils.sin(activeTime * MathUtils.PI) * swingAngle;
        }

        spriteAnimation.update(delta);
    }

    public void setSpriteAnimation(Animatable spriteAnimation) {
        this.spriteAnimation = spriteAnimation;
    }

    public float getAnimationValue() {
        return spriteAnimation.getValue();
    }

    public float getRotation() {
        return rotation;
    }

    public void setCharacterIdx(int characterIdx) {
        this.characterIdx = characterIdx;
    }

    public int getCharacterIdx() {
        return characterIdx;
    }

    public void setSwingAngle(float swingAngle) {
        this.swingAngle = swingAngle;
    }

    public void setDelay(float delay) {
        this.activeTime = -delay;
    }
}
