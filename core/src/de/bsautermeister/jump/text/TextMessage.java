package de.bsautermeister.jump.text;

public class TextMessage {
    private static final float SPEED_Y = 0.5f;
    private static final float INITIAL_TTL = 1f;

    private final String message;
    private float x;
    private float y;
    private float ttl;

    public TextMessage(String message, float x, float y) {
        this.message = message;
        this.x = x;
        this.y = y;
        this.ttl = INITIAL_TTL;
    }

    public void update(float delta) {
        this.ttl -= delta;
        this.y += SPEED_Y * delta;
    }

    public String getMessage() {
        return message;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isAlive() {
        return ttl > 0;
    }

    @Override
    public String toString() {
        return "TextMessage{" +
                "message='" + message + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", ttl=" + ttl +
                '}';
    }
}
