package de.bsautermeister.jump.tools;

import com.badlogic.gdx.math.MathUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.serializer.BinarySerializable;

public class GameTimer implements BinarySerializable {
    private float value;
    private final float resetValue;
    private TimerCallbacks callbacks;
    private final boolean initialIsStarted;
    private boolean isStarted;

    public GameTimer(float resetValue) {
        this(resetValue, false);
    }

    public GameTimer(float resetValue, boolean autoStart) {
        this.resetValue = resetValue;
        this.initialIsStarted = autoStart;
        this.isStarted = autoStart;
    }

    public void reset() {
        this.value = resetValue;
        this.isStarted = initialIsStarted;
    }

    public void restart() {
        restart(0f);
    }

    public void restart(float skip) {
        this.value = resetValue - skip;
        this.isStarted = true;

        if (callbacks != null) {
            callbacks.onStart();
        }
    }

    public void update(float delta) {
        if (!isStarted) {
            return;
        }

        value -= delta;

        if (callbacks != null && isFinished()) {
            isStarted = false;
            callbacks.onFinish();
        }
    }

    public float getValue() {
        return value;
    }

    public float getProgress() {
        return MathUtils.clamp(1f - value / resetValue, 0f, 1f);
    }

    public boolean isFinished() {
        return value <= 0;
    }

    public boolean isRunning() {
        return isStarted && !isFinished();
    }

    public void setCallbacks(TimerCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public interface TimerCallbacks {
        void onStart();
        void onFinish();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(value);
        out.writeBoolean(isStarted);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        value = in.readFloat();
        isStarted = in.readBoolean();
    }
}
