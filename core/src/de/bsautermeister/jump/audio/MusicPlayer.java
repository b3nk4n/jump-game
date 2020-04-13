package de.bsautermeister.jump.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class MusicPlayer implements BinarySerializable, Disposable {
    private static final Logger LOG = new Logger(MusicPlayer.class.getSimpleName(), Cfg.LOG_LEVEL);

    private final static float VOLUME_CHANGE_IN_SECONDS = 2.0f;
    public final static float MAX_VOLUME = 0.33f;

    private float currentVolume = 0.0f;
    private float targetVolume = MAX_VOLUME;
    private Music music;
    private String selectedFilePath;

    private Array<Music> fadeOutAndDisposeQueue = new Array<Music>();

    public void selectMusic(String filePath) {
        LOG.debug("Select: " + filePath);
        if (music != null) {
            fadeOutAndDisposeQueue.add(music);
        }

        FileHandle fileHandle = Gdx.files.internal(filePath);
        music = Gdx.audio.newMusic(fileHandle);
        music.setLooping(true);
        selectedFilePath = filePath;
    }

    public void update(float delta) {
        if (music == null) {
            return;
        }

        if (targetVolume != currentVolume) {
            float diff = targetVolume - currentVolume;

            if (diff > 0) {
                currentVolume += delta / VOLUME_CHANGE_IN_SECONDS;
                currentVolume = Math.min(targetVolume, currentVolume);
            } else {
                currentVolume -= delta / VOLUME_CHANGE_IN_SECONDS;
                currentVolume = Math.max(targetVolume, currentVolume);
            }
        }

        music.setVolume(currentVolume);


        if (fadeOutAndDisposeQueue.size > 0) {
            for (Music fadeOutMusic : fadeOutAndDisposeQueue) {
                float newVolume = fadeOutMusic.getVolume() - delta;
                if (newVolume > 0) {
                    fadeOutMusic.setVolume(newVolume);
                } else {
                    fadeOutMusic.dispose();
                    fadeOutAndDisposeQueue.removeValue(fadeOutMusic, true);
                }
            }
        }
    }

    public void playFromBeginning() {
        if (music == null) {
            return;
        }

        LOG.debug("Play music from beginning");
        music.stop();
        music.play();
    }

    public void resumeOrPlay() {
        if (music == null) {
            return;
        }

        music.pause();
        music.play();
    }

    public boolean isPlaying() {
        if (music == null) {
            return false;
        }

        return music.isPlaying();
    }

    public void pause() {
        if (music == null) {
            return;
        }

        LOG.debug("Pause music");
        music.pause();
    }

    public void stop() {
        if (music == null) {
            return;
        }

        LOG.debug("Stop music");
        music.stop();
    }

    public void setVolume(float volume, boolean immediate) {
        targetVolume = volume;

        if (immediate) {
            currentVolume = volume;
        }
    }

    public float getVolume() {
        return currentVolume;
    }

    public boolean isSelected(String filePath) {
        return selectedFilePath.equals(filePath);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        LOG.debug("Write music state");
        if (music == null) {
            out.writeFloat(-1f);
            return;
        }
        out.writeFloat(music.getPosition());
        out.writeUTF(selectedFilePath);
        out.writeFloat(currentVolume);
        out.writeFloat(targetVolume);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        LOG.debug("Read music state");
        float pos = in.readFloat();
        if (pos == -1f) {
            return;
        }
        String musicPath = in.readUTF();
        currentVolume = in.readFloat();
        targetVolume = in.readFloat();
        if (pos > 0) {
            // at least on Desktop it is required to call play first
            // before seeking the audio position
            selectMusic(musicPath);
            music.setVolume(currentVolume);
            music.play();
            music.setPosition(pos);
        }
    }

    @Override
    public void dispose() {
        if (music != null) {
            music.dispose();
        }
    }
}
