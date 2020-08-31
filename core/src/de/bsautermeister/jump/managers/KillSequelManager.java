package de.bsautermeister.jump.managers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.serializer.BinarySerializable;
import de.bsautermeister.jump.tools.GameTimer;

public class KillSequelManager implements BinarySerializable {
    private static final long SIMPLE_KILL_SCORE = 200;
    private final int MAX_KILL_SEQUEL = 5;
    private final String[] scoreStrings = new String[MAX_KILL_SEQUEL];

    private final GameTimer killSequelTimer;
    private int killSequelCount;

    public KillSequelManager(final Callbacks callbacks) {
        for (int i = 0; i < MAX_KILL_SEQUEL; ++i) {
            scoreStrings[i] = String.valueOf(calScore(i + 1));
        }

        killSequelTimer = new GameTimer(1.25f);
        killSequelTimer.setCallbacks(new GameTimer.TimerCallbacks() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
                callbacks.completed(killSequelCount);
                killSequelCount = 0;
            }
        });
    }

    public void reset() {
        killSequelCount = 0;
        killSequelTimer.reset();
    }

    public void update(float delta) {
        killSequelTimer.update(delta);
    }

    public void notifyKill() {
        killSequelCount += 1;
        if (killSequelCount > MAX_KILL_SEQUEL) {
            killSequelCount = MAX_KILL_SEQUEL;
        }
        killSequelTimer.restart();
    }

    public long getKillScore() {
        return calScore(killSequelCount);
    }

    private long calScore(int sequelCount) {
        return sequelCount * SIMPLE_KILL_SCORE;
    }

    public String getKillScoreText() {
        if (killSequelCount == 0) {
            return "";
        }

        return scoreStrings[killSequelCount - 1];
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(killSequelCount);
        killSequelTimer.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        killSequelCount = in.readInt();
        killSequelTimer.read(in);
    }

    public interface Callbacks {
        void completed(int count);
    }
}
