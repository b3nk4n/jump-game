package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.effects.SimplePooledEffect;

public class Snorer extends Sprite {

    private final float snorrerWidth;

    private final SimplePooledEffect snoreEffect;

    private float gameTime;
    private float nextSnoreTime;
    private final Sound snoreSound;

    public Snorer(AssetManager assetManager, TextureAtlas atlas, Rectangle snorerRect) {
        setRegion(atlas.findRegion(RegionNames.SNORER));

        snorrerWidth = getRegionWidth() / Cfg.PPM;
        setBounds(snorerRect.getX() + (snorerRect.getWidth() - snorrerWidth) / 2f, snorerRect.getY(),
                snorrerWidth, getRegionHeight() / Cfg.PPM);

        snoreEffect = new SimplePooledEffect(AssetPaths.Pfx.SNORE, atlas, 0.15f / Cfg.PPM);
        snoreEffect.emit(snorerRect.getX() + snorerRect.getWidth() / 2,
                snorerRect.getY() + snorerRect.getHeight());

        snoreSound = assetManager.get(AssetDescriptors.Sounds.SNORE);
        nextSnoreTime = MathUtils.random(2f, 5f);
    }

    public void update(float delta) {
        gameTime += delta;
        snoreEffect.update(delta);

        if (gameTime > nextSnoreTime) {
            snoreSound.play();
            nextSnoreTime += MathUtils.random(17.5f, 25f);
        }
    }

    public void stop() {
        snoreSound.stop();
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);

        snoreEffect.draw(batch);
    }
}
