package de.bsautermeister.jump.managers;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.effects.SimplePooledEffect;
import de.bsautermeister.jump.screens.game.GameCallbacks;

public class WaterInteractionManager {
    private final SimplePooledEffect waterSplashEffect;

    private final GameCallbacks callbacks;
    private final Array<Drownable> drownables = new Array<Drownable>();
    private final Array<Rectangle> waterRegions = new Array<Rectangle>();

    public WaterInteractionManager(TextureAtlas atlas, GameCallbacks callbacks) {
        waterSplashEffect = new SimplePooledEffect(AssetPaths.Pfx.SPLASH, atlas, 0.1f / Cfg.PPM);

        this.callbacks = callbacks;

        reset();
    }

    public void reset() {
        drownables.clear();
        waterRegions.clear();
    }

    public void update(float delta) {
        for (Rectangle waterRegion : waterRegions) {
            for (Drownable drownable : drownables) {
                if (waterRegion.contains(drownable.getWorldCenter())) {
                    doDrown(drownable, waterRegion);
                }
            }
        }

        waterSplashEffect.update(delta);
    }

    public void setWaterRegions(Array<Rectangle> waterRegions) {
        this.waterRegions.addAll(waterRegions);
    }

    public void add(Drownable drownable) {
        drownables.add(drownable);
    }

    public void remove(Drownable drownable) {
        drownables.removeValue(drownable, true);
    }

    private void doDrown(Drownable drownable, Rectangle waterRegion) {
        if (!drownable.isDead() && !drownable.isDrowning() && drownable.getLinearVelocity().y < -0.5f) {
            Vector2 center = drownable.getWorldCenter();
            waterSplashEffect.emit(center.x, waterRegion.y + waterRegion.height - Cfg.BLOCK_SIZE_PPM * 0.5f);
            callbacks.touchedWater(drownable);

            drownable.drown();
        }
    }

    public Array<ParticleEffectPool.PooledEffect> getActiveSplashEffects() {
        return waterSplashEffect.getActiveEffects();
    }
}
