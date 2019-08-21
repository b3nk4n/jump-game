package de.bsautermeister.jump.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.assets.AssetPaths;

public class WaterInteractionManager {
    private ParticleEffectPool splashEffectPool;
    private Array<ParticleEffectPool.PooledEffect> activeSplashEffects = new Array<ParticleEffectPool.PooledEffect>();

    private final GameCallbacks callbacks;
    private final Array<Rectangle> waterRegions;
    private final Array<Drownable> drownables = new Array<Drownable>();

    public WaterInteractionManager(TextureAtlas atlas, GameCallbacks callbacks, Array<Rectangle> waterRegions) {
        ParticleEffect splashEffect = new ParticleEffect();
        splashEffect.load(Gdx.files.internal(AssetPaths.Pfx.SPLASH), atlas);
        splashEffect.scaleEffect(0.1f / Cfg.PPM);
        splashEffectPool = new ParticleEffectPool(splashEffect, 8, 16);

        this.callbacks = callbacks;
        this.waterRegions = waterRegions;
    }

    public void update(float delta) {
        for (Rectangle waterRegion : waterRegions) {
            for (Drownable drownable : drownables) {
                if (waterRegion.contains(drownable.getWorldCenter())) {
                    doDrown(drownable);
                }
            }
        }

        for (int i = activeSplashEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = activeSplashEffects.get(i);
            effect.update(delta);
            if (effect.isComplete()) {
                activeSplashEffects.removeIndex(i);
                effect.free();
            }
        }
    }

    public void add(Drownable drownable) {
        drownables.add(drownable);
    }

    public void remove(Drownable drownable) {
        drownables.removeValue(drownable, true);
    }

    private void doDrown(Drownable drownable) {
        if (!drownable.isDead() && !drownable.isDrowning() && drownable.getLinearVelocity().y < -0.5f) {
            Vector2 center = drownable.getWorldCenter();
            ParticleEffectPool.PooledEffect splashEffect = splashEffectPool.obtain();
            splashEffect.setPosition(center.x, center.y);
            activeSplashEffects.add(splashEffect);
            callbacks.touchedWater(drownable);

            drownable.drown();
        }
    }

    public void draw(SpriteBatch batch) {
        for (int i = activeSplashEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = activeSplashEffects.get(i);
            effect.draw(batch);
        }
    }
}
