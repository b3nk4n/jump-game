package de.bsautermeister.jump.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MusicPlayer implements Disposable {
    private final static float VOLUME_CHANGE_IN_SECONDS = 2.0f;

    private float currentVolume = 1.0f;
    private float targetVolume = 1.0f;
    private Music music;
    private String selectedFilePath;

    private Array<Music> fadeOutAndDisposeQueue = new Array<Music>();

    public void selectMusic(String filePath) {
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

    public void play() {
        if (music == null) {
            return;
        }

        music.play();
    }

    public void pause() {
        if (music == null) {
            return;
        }

        music.pause();
    }

    public void stop() {
        if (music == null) {
            return;
        }

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

    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(music.getPosition());
    }

    public void read(DataInputStream in) throws IOException {
        music.setPosition(in.readFloat());
    }

    @Override
    public void dispose() {
        if (music != null) {
            music.dispose();
        }
    }
}
