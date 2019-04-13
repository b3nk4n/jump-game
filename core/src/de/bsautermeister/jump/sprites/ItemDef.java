package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.math.Vector2;

public class ItemDef {
    private Vector2 position;
    private Class<?> type;

    public ItemDef(Vector2 position, Class<?> type) {
        this.position = position;
        this.type = type;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Class<?> getType() {
        return type;
    }
}
