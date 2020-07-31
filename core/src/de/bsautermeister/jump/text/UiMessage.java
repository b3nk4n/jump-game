package de.bsautermeister.jump.text;

public abstract class UiMessage<T> {
    private static final float SPEED_Y = 0.1f;
    private static final float INITIAL_TTL = 1f;

    private final T message;
    private float normalizedX;
    private float normalizedY;
    private float ttl;

    public UiMessage(T message) {
        this.message = message;
        this.ttl = INITIAL_TTL;
    }

    public void setPosition(float normalizedX, float normalizedY) {
        this.normalizedX = normalizedX;
        this.normalizedY = normalizedY;
    }

    public void update(float delta) {
        this.ttl -= delta;
        this.normalizedY += SPEED_Y * delta;
    }

    public T getMessage() {
        return message;
    }

    public float getNormalizedX() {
        return normalizedX;
    }

    public float getNormalizedY() {
        return normalizedY;
    }

    public boolean isAlive() {
        return ttl > 0;
    }
}
