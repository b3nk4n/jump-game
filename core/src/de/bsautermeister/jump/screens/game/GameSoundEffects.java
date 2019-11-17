package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import de.bsautermeister.jump.assets.AssetDescriptors;

public class GameSoundEffects implements Disposable {
    public Sound bumpSound;
    public Sound powerupSpawnSound;
    public Sound powerupSound;
    public Sound coinSound;
    public Sound breakBlockSound;
    public Sound stompSound;
    public Sound powerDownSound;
    public Sound playerDieSound;
    public Sound jumpSound;
    public Sound kickedSound;
    public Sound splashSound;
    public Sound fireSound;
    public Sound drinkingSound;
    public Sound ohYeahSound;
    public Sound successSound;

    public GameSoundEffects(AssetManager assetManager) {
        bumpSound = assetManager.get(AssetDescriptors.Sounds.BUMP);
        powerupSpawnSound = assetManager.get(AssetDescriptors.Sounds.POWERUP_SPAWN);
        powerupSound = assetManager.get(AssetDescriptors.Sounds.POWERUP);
        coinSound = assetManager.get(AssetDescriptors.Sounds.COIN);
        breakBlockSound = assetManager.get(AssetDescriptors.Sounds.BREAK_BLOCK);
        stompSound = assetManager.get(AssetDescriptors.Sounds.STOMP);
        powerDownSound = assetManager.get(AssetDescriptors.Sounds.POWERDOWN);
        playerDieSound = assetManager.get(AssetDescriptors.Sounds.PLAYER_DIE);
        jumpSound = assetManager.get(AssetDescriptors.Sounds.JUMP);
        kickedSound = assetManager.get(AssetDescriptors.Sounds.KICKED);
        splashSound = assetManager.get(AssetDescriptors.Sounds.SPLASH);
        fireSound = assetManager.get(AssetDescriptors.Sounds.FIRE);
        drinkingSound = assetManager.get(AssetDescriptors.Sounds.DRINKING);
        ohYeahSound = assetManager.get(AssetDescriptors.Sounds.OH_YEAH);
        successSound = assetManager.get(AssetDescriptors.Sounds.SUCCESS);
    }

    @Override
    public void dispose() {
        // disposing sound effects has weird side effects:
        // - Effect stop playing the next time
        // - GdxRuntimeException: Unable to allocate audio buffers.
        /*bumpSound.dispose();
        powerupSpawnSound.dispose();
        powerupSound.dispose();
        coinSound.dispose();
        breakBlockSound.dispose();
        stompSound.dispose();
        powerDownSound.dispose();
        playerDieSound.dispose();
        jumpSound.dispose();
        kickedSound.dispose();
        splashSound.dispose();
        fireSound.dispose();
        drinkingSound.dispose();*/
    }
}
