package de.bsautermeister.jump.models;

import com.badlogic.gdx.math.Rectangle;

public class PlatformBouncer {
    Rectangle region;
    int angle;
    float speed;

    public PlatformBouncer(Rectangle region, int angle, float speed) {
        this.region = region;
        this.angle = angle;
        this.speed = speed;
    }

    public Rectangle getRegion() {
        return region;
    }

    public int getAngle() {
        return angle;
    }

    public float getSpeed() {
        return speed;
    }
}
