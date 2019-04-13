package de.bsautermeister.jump.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MusicPlayer implements Disposable {
    private final static float VOLUME_CHANGE_IN_SECONDS = 3.0f;

    private float currentVolume = 1.0f;
    private float targetVolume = 1.0f;
    private Music music;

    public void setup(String filePath, float volume) {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        music = Gdx.audio.newMusic(fileHandle);
        music.setLooping(true);
        setVolume(volume, true);
    }

    public void update(float delta) {
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
    }

    public void play() {
        music.play();
    }

    public void pause() {
        music.pause();
    }

    public void stop() {
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
