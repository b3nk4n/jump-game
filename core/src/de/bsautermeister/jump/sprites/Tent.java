package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.effects.SimplePooledEffect;
import de.bsautermeister.jump.serializer.BinarySerializable;

public class Tent extends Sprite implements BinarySerializable, Disposable {

    private final TextureAtlas atlas;
    private final float tentWidth;
    private final Rectangle goal;
    private final Vector2 center;

    private final MusicPlayer music;

    private final static String[] SONGS = {
            AssetPaths.Music.PROSIT_AUDIO,
            AssetPaths.Music.PROSIT2_AUDIO
    };

    private boolean open;
    private float wabbleTime;
    private Vector2 playerPosition = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);

    private final SimplePooledEffect singingEffect;

    public Tent(TextureAtlas atlas, Rectangle goal) {
        setRegion(atlas.findRegion(RegionNames.TENT_CLOSED));
        this.center = new Vector2(goal.getX() + goal.getWidth() / 2,
                goal.getY() + goal.getHeight() / 2);
        this.atlas = atlas;
        this.goal = new Rectangle(goal.x - Cfg.BLOCK_SIZE_PPM, goal.y, goal.width + 2 * Cfg.BLOCK_SIZE_PPM, goal.height);

        tentWidth = getRegionWidth() / Cfg.PPM;
        setBounds(goal.getX() + (goal.getWidth() - tentWidth) / 2f, goal.getY(),
                tentWidth, getRegionHeight() / Cfg.PPM);

        singingEffect = new SimplePooledEffect(AssetPaths.Pfx.MUSIC, atlas, 0.2f / Cfg.PPM);

        music = new MusicPlayer();
    }

    public void update(float delta) {
        if (open) {
            wabbleTime += delta;

            float heightFactor  = 1.0f - (0.02f + MathUtils.sin(3f * wabbleTime) * 0.02f);
            setSize(tentWidth, getRegionHeight() * heightFactor / Cfg.PPM);

            float tentVolume = getTentVolume();
            if (tentVolume > 0) {
                music.setVolume(tentVolume, false);
                if (!music.isPlaying()) {
                    music.selectMusic(pickRandomSong());
                    music.resumeOrPlay();
                }
            } else if (music.isPlaying()) {
                music.setVolume(0f, false);
            }

            music.update(delta);
            singingEffect.update(delta);
        }
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);

        singingEffect.draw(batch);
    }

    private String pickRandomSong() {
        return SONGS[MathUtils.random(SONGS.length - 1)];
    }

    private float getTentVolume() {
        float dst2 = Vector2.dst2(playerPosition.x, playerPosition.y, center.x, center.y);
        return MathUtils.clamp(1.11f - (dst2 / 10f), 0f, 1f);
    }

    public void setPlayerPosition(Vector2 playerPosition) {
        this.playerPosition.set(playerPosition.x, playerPosition.y);
    }

    public void open() {
        open = true;
        setRegion(atlas.findRegion(RegionNames.TENT_OPEN));

        singingEffect.emit(center.x, center.y + 2 * Cfg.BLOCK_SIZE_PPM);
    }

    public boolean isEntering(Player player) {
        return open && Intersector.overlaps(goal, player.getBoundingRectangle()) && player.getBody().getLinearVelocity().len2() < 0.001f;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeBoolean(open);
        out.writeFloat(wabbleTime);
        music.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        open = in.readBoolean();
        wabbleTime = in.readFloat();
        music.read(in);

        if (open) {
            open();
        }
    }

    @Override
    public void dispose() {
        music.stop();
        music.dispose();
    }
}
