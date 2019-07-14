package de.bsautermeister.jump.assets;

public interface AssetPaths {
    interface Atlas {
        String LOADING = "loading/loading.atlas";
        String GAMEPLAY = "gameplay/gameplay.atlas";
        String UI = "ui/ui.atlas";
    }

    interface Skins {
        String UI = "ui/ui.skin";
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
        String KICKED = "audio/sounds/kicked.wav";
        String SPLASH = "audio/sounds/splash.wav";
    }

    interface Music {
        String NORMAL_AUDIO = "audio/music/mario_music.ogg";
        String HURRY_AUDIO = "audio/music/mario_hurry.mp3";
    }

    interface Pfx {
        String SLIDE_SMOKE = "pfx/slide-smoke.pfx";
        String SPLASH = "pfx/splash.pfx";
    }
}
