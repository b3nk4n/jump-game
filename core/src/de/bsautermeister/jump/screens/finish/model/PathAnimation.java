package de.bsautermeister.jump.screens.finish.model;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;

public class PathAnimation implements Animatable {
    private float pathItemTimeElapsed;

    private final boolean loop;
    private final float initialValue;
    private float value;
    private final Array<Item> path;
    private int currentPathIndex;

    private final Interpolation interpolation = Interpolation.linear;
    private float previousValue;

    public PathAnimation(float initialValue, boolean loop, Array<Item> path) {
        this.initialValue = initialValue;
        this.loop = loop;
        this.path = path;
        reset();
    }

    private void reset() {
        pathItemTimeElapsed = 0;
        currentPathIndex = 0;
        value = initialValue;
        previousValue = initialValue;
    }

    @Override
    public void update(float delta) {
        if (currentPathIndex >= path.size) {
            if (loop) {
                currentPathIndex = 0;
            } else {
                return;
            }
        }

        pathItemTimeElapsed += delta;

        Item currentItem = path.get(currentPathIndex);
        float progress = Math.min(pathItemTimeElapsed / currentItem.duration, 1.0f);

        value = interpolation.apply(previousValue, currentItem.value, progress);

        if (progress >= 1.0f) {
            currentPathIndex++;
            previousValue = currentItem.value;
            pathItemTimeElapsed = 0f;
        }
    }

    @Override
    public float getValue() {
        return value;
    }

    public static class Item {
        public final float duration;
        public final float value;

        public Item(float duration, float value) {
            this.duration = duration;
            this.value = value;
        }
    }
}
