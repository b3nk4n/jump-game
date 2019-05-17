package de.bsautermeister.jump.assets;

public interface AssetPaths {
    interface Atlas {
        String LOADING = "loading/loading.atlas";
        String GAMEPLAY = "gameplay/gameplay.atlas";
    }

    interface Sounds {
        String BUMP = "audio/sounds/bump.wav";
        String BREAK_BLOCK = "audio/sounds/breakblock.wav";
        String POWERUP_SPAWN = "audio/sounds/powerup_spawn.wav";
        String COIN = "audio/sounds/coin.wav";
        String STOMP = "audio/sounds/stomp.wav";
        String POWERUP = "audio/sounds/powerup.wav";
        String POWERDOWN = "audio/sounds/powerdown.wav";
        String MARIO_DIE = "audio/sounds/mariodie.wav";
        String JUMP = "audio/sounds/jump.wav";
    }

    interface Music {
        String BACKGROUND_AUDIO = "audio/music/mario_music.ogg";
    }
}
