package de.bsautermeister.jump.text;

public class TextMessage {
    private static final float SPEED_Y = 0.1f;
    private static final float INITIAL_TTL = 1f;

    private final String message;
    private float normalizedX;
    private float normalizedY;
    private float ttl;

    public TextMessage(String message, float normalizedX, float normalizedY) {
        this.message = message;
        this.normalizedX = normalizedX;
        this.normalizedY = normalizedY;
        this.ttl = INITIAL_TTL;
    }

    public void update(float delta) {
        this.ttl -= delta;
        this.normalizedY += SPEED_Y * delta;
    }

    public String getMessage() {
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
