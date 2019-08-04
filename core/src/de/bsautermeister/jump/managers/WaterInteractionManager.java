package de.bsautermeister.jump.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.AssetPaths;

public class WaterInteractionManager {
    private ParticleEffect splashEffect = new ParticleEffect();

    private final GameCallbacks callbacks;
    private final Array<Rectangle> waterRegions;
    private final Drownable drownable;

    public WaterInteractionManager(TextureAtlas atlas, GameCallbacks callbacks, Array<Rectangle> waterRegions, Drownable drownable) {
        splashEffect.load(Gdx.files.internal(AssetPaths.Pfx.SPLASH), atlas);
        splashEffect.scaleEffect(0.1f / Cfg.PPM);

        this.callbacks = callbacks;
        this.waterRegions = waterRegions;
        this.drownable = drownable;
    }

    public void update(float delta) {
        for (Rectangle waterRegion : waterRegions) {
            if (waterRegion.contains(drownable.getWorldCenter())) {
                doDrown(drownable);
            }
        }
    }

    private void doDrown(Drownable drownable) {
        if (!drownable.isDead() && !drownable.isDrowning()) {
            Vector2 center = drownable.getWorldCenter();
            splashEffect.setPosition(center.x, center.y);
            splashEffect.start();
            callbacks.touchedWater();

            drownable.drown();
        }
    }

    public void draw(SpriteBatch batch) {
        splashEffect.draw(batch, Gdx.graphics.getDeltaTime());
    }
}
