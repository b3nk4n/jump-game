package de.bsautermeister.jump.assets;

public interface AssetPaths {
    interface Atlas {
        String LOADING = "loading/loading.atlas";
        String GAMEPLAY = "gameplay/gameplay.atlas";
        String UI = "ui/ui.atlas";
    }

    interface Fonts {
        String MARIO12 = "ui/fonts/new-super-mario-font-12.fnt";
        String MARIO18 = "ui/fonts/new-super-mario-font-18.fnt";
        String MARIO24 = "ui/fonts/new-super-mario-font-24.fnt";
        String MARIO32 = "ui/fonts/new-super-mario-font-32.fnt";
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
        String PLAYER_DIE = "audio/sounds/player_die.wav";
        String JUMP = "audio/sounds/jump.wav";
        String KICKED = "audio/sounds/kicked.wav";
        String SPLASH = "audio/sounds/splash.wav";
        String FIRE = "audio/sounds/fire.wav";
        String DRINKING = "audio/sounds/drinking.wav";
        String OH_YEAH = "audio/sounds/oh_yeah.wav";
        String SUCCESS = "audio/sounds/success.wav";
    }

    interface Music {
        String NORMAL_AUDIO = "audio/music/game_music.ogg";
        String HURRY_AUDIO = "audio/music/game_hurry_music.mp3";
    }

    interface Pfx {
        String SLIDE_SMOKE = "pfx/slide-smoke.pfx";
        String SPLASH = "pfx/splash.pfx";
    }
}
