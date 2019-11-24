package de.bsautermeister.jump.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.physics.WorldCreator;

public class WaterInteractionManager {
    private ParticleEffectPool waterSplashEffectPool;
    private ParticleEffectPool beerSplashEffectPool;
    private Array<ParticleEffectPool.PooledEffect> activeSplashEffects = new Array<ParticleEffectPool.PooledEffect>();

    private final GameCallbacks callbacks;
    private final Array<Drownable> drownables = new Array<Drownable>();
    private final Array<WorldCreator.WaterParams> waterRegions = new Array<WorldCreator.WaterParams>();

    public WaterInteractionManager(TextureAtlas atlas, GameCallbacks callbacks) {
        waterSplashEffectPool = createEffectPool(AssetPaths.Pfx.SPLASH, atlas);
        beerSplashEffectPool = createEffectPool(AssetPaths.Pfx.BEER_SPLASH, atlas);

        this.callbacks = callbacks;

        reset();
    }

    public void reset() {
        drownables.clear();
        waterRegions.clear();
    }

    private ParticleEffectPool createEffectPool(String effectPath , TextureAtlas atlas) {
        ParticleEffect splashEffect = new ParticleEffect();
        splashEffect.load(Gdx.files.internal(effectPath), atlas); // TODO: https://stackoverflow.com/questions/12261439/assetmanager-particleeffectloader-of-libgdx-android
        splashEffect.scaleEffect(0.1f / Cfg.PPM);
        return new ParticleEffectPool(splashEffect, 8, 16);
    }

    public void update(float delta) {
        for (WorldCreator.WaterParams waterRegion : waterRegions) {
            for (Drownable drownable : drownables) {
                if (waterRegion.rectangle.contains(drownable.getWorldCenter())) {
                    doDrown(drownable, waterRegion.isBeer);
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

    public void setWaterRegions(Array<WorldCreator.WaterParams> waterRegions) {
        this.waterRegions.addAll(waterRegions);
    }

    public void add(Drownable drownable) {
        drownables.add(drownable);
    }

    public void remove(Drownable drownable) {
        drownables.removeValue(drownable, true);
    }

    private void doDrown(Drownable drownable, boolean isBeer) {
        if (!drownable.isDead() && !drownable.isDrowning() && drownable.getLinearVelocity().y < -0.5f) {
            Vector2 center = drownable.getWorldCenter();
            ParticleEffectPool.PooledEffect splashEffect = isBeer ?
                    beerSplashEffectPool.obtain() : waterSplashEffectPool.obtain();
            splashEffect.start();
            splashEffect.setPosition(center.x, center.y);
            activeSplashEffects.add(splashEffect);
            callbacks.touchedWater(drownable, isBeer);

            drownable.drown();
        }
    }

    public Array<ParticleEffectPool.PooledEffect> getActiveSplashEffects() {
        return activeSplashEffects;
    }
}
