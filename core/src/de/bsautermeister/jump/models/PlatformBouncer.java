package de.bsautermeister.jump.models;

import com.badlogic.gdx.math.Rectangle;

public class PlatformBouncer {
    Rectangle region;
    int angle;

    public PlatformBouncer(Rectangle region, int angle) {
        this.region = region;
        this.angle = angle;
    }

    public Rectangle getRegion() {
        return region;
    }

    public int getAngle() {
        return angle;
    }
}
