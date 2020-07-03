package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import de.bsautermeister.jump.assets.AssetDescriptors;

public class GameSoundEffects implements Disposable {
    public Sound bumpSound;
    public Sound foodSpawnSound;
    public Sound beerSpawnSound;
    public Sound coinSpawnSound;
    public Sound eatFoodSound;
    public Sound coinSound;
    public Sound breakBlockSound;
    public Sound stompSound;
    public Sound complainSound;
    private Sound[] swearingSounds;
    public Sound jumpSound;
    public Sound landingSound;
    public Sound kickedSound;
    public Sound splashSound;
    public Sound fireSound;
    public Sound drinkingSound;
    public Sound successSound;
    public Sound burpSound;
    public Sound ravenSound;

    public GameSoundEffects(AssetManager assetManager) {
        bumpSound = assetManager.get(AssetDescriptors.Sounds.BUMP);
        foodSpawnSound = assetManager.get(AssetDescriptors.Sounds.FOOD_SPAWN);
        beerSpawnSound = assetManager.get(AssetDescriptors.Sounds.BEER_SPAWN);
        coinSpawnSound = assetManager.get(AssetDescriptors.Sounds.COIN_SPAWN);
        eatFoodSound = assetManager.get(AssetDescriptors.Sounds.EAT_FOOD);
        coinSound = assetManager.get(AssetDescriptors.Sounds.COIN);
        breakBlockSound = assetManager.get(AssetDescriptors.Sounds.BREAK_BLOCK);
        stompSound = assetManager.get(AssetDescriptors.Sounds.STOMP);
        complainSound = assetManager.get(AssetDescriptors.Sounds.COMPLAIN);
        swearingSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.SWEARING1),
                assetManager.get(AssetDescriptors.Sounds.SWEARING2),
                assetManager.get(AssetDescriptors.Sounds.SWEARING3),
                assetManager.get(AssetDescriptors.Sounds.SWEARING4),
                assetManager.get(AssetDescriptors.Sounds.SWEARING5),
                assetManager.get(AssetDescriptors.Sounds.SWEARING6),
                assetManager.get(AssetDescriptors.Sounds.SWEARING7)
        };
        jumpSound = assetManager.get(AssetDescriptors.Sounds.JUMP);
        landingSound = assetManager.get(AssetDescriptors.Sounds.LANDING);
        kickedSound = assetManager.get(AssetDescriptors.Sounds.KICKED);
        splashSound = assetManager.get(AssetDescriptors.Sounds.SPLASH);
        fireSound = assetManager.get(AssetDescriptors.Sounds.FIRE);
        drinkingSound = assetManager.get(AssetDescriptors.Sounds.DRINKING);
        successSound = assetManager.get(AssetDescriptors.Sounds.SUCCESS);
        burpSound = assetManager.get(AssetDescriptors.Sounds.BURP);
        ravenSound = assetManager.get(AssetDescriptors.Sounds.RAVEN);
    }

    public Sound randomSwearingSound() {
        return swearingSounds[MathUtils.random(swearingSounds.length - 1)];
    }

    public void playRandomBurpSound() {
        float volume = MathUtils.random(0.75f, 1.0f);
        float pitch = MathUtils.random(0.85f, 1.15f);
        burpSound.play(volume, pitch, 1.0f);
    }

    @Override
    public void dispose() {
        // disposing sound effects has weird side effects:
        // - Effect stop playing the next time
        // - GdxRuntimeException: Unable to allocate audio buffers.
    }
}
