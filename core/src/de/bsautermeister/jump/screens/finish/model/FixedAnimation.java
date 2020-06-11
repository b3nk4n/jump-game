package de.bsautermeister.jump.screens.finish.model;

public class FixedAnimation implements Animatable {

    private final float value;

    public FixedAnimation(float value) {
        this.value = value;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public float getValue() {
        return value;
    }
}
