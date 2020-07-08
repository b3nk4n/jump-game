package de.bsautermeister.jump.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class SimplePooledEffect {

    private final ParticleEffectPool effectPool;
    private final Array<ParticleEffectPool.PooledEffect> activeEffects = new Array<>(16);

    public SimplePooledEffect(String effectPath , TextureAtlas atlas, float scaleFactor) {
        ParticleEffect effect = new ParticleEffect();
        effect.load(Gdx.files.internal(effectPath), atlas);
        effect.scaleEffect(scaleFactor);
        effectPool = new ParticleEffectPool(effect, 8, 16);
    }

    public void update(float delta) {
        for (int i = activeEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = activeEffects.get(i);
            effect.update(delta);
            if (effect.isComplete()) {
                activeEffects.removeIndex(i);
                effect.free();
            }
        }
    }

    public void draw(Batch batch) {
        for (ParticleEffectPool.PooledEffect effect : activeEffects) {
            effect.draw(batch);
        }
    }

    public void emit(float x, float y) {
        ParticleEffectPool.PooledEffect effect = effectPool.obtain();
        effect.setPosition(x, y);
        effect.start();
        activeEffects.add(effect);
    }

    public Array<ParticleEffectPool.PooledEffect> getActiveEffects() {
        return activeEffects;
    }
}
