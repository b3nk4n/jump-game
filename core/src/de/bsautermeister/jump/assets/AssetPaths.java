package de.bsautermeister.jump.assets;

import com.badlogic.gdx.math.MathUtils;

import de.bsautermeister.jump.math.Permutation;
import de.bsautermeister.jump.tools.Supplier;

public interface AssetPaths {
    interface Atlas {
        String LOADING = "loading/loading.atlas";
        String GAMEPLAY = "gameplay/gameplay.atlas";
        String UI = "ui/ui.atlas";
    }

    interface Fonts {
        String S = "ui/fonts/KarmaticArcade-20.fnt";
        String M = "ui/fonts/KarmaticArcade-31.fnt";
        String L = "ui/fonts/KarmaticArcade-41.fnt";
        String XL = "ui/fonts/KarmaticArcade-51.fnt";
        String XXL = "ui/fonts/KarmaticArcade-61.fnt";
        String TITLE = "ui/fonts/KarmaticArcade-82.fnt";
    }

    interface Skins {
        String UI = "ui/ui.skin";
    }

    abstract class Sounds {
        static String BUMP = "audio/sounds/bump.wav";
        static String BREAK_BLOCK = "audio/sounds/breakblock.wav";
        static String BEER_SPAWN = "audio/sounds/bump_glasses.wav";
        static String COIN_SPAWN = "audio/sounds/bump_coin.wav";
        static String COIN = "audio/sounds/coin.wav";
        static String STOMP = "audio/sounds/stomp.wav";
        static String EAT_FOOD = "audio/sounds/eat_food.wav";
        static String OZAPFT1 = "audio/sounds/ozapft01.wav";
        static String OZAPFT2 = "audio/sounds/ozapft02.wav";
        static String BOOST1 = "audio/sounds/boost01.wav";
        static String BOOST2 = "audio/sounds/boost02.wav";
        static String BOOST3 = "audio/sounds/boost03.wav";
        static String BOOST4 = "audio/sounds/boost04.wav";
        static String BOOST5 = "audio/sounds/boost05.wav";
        static String NEED_BEER1 = "audio/sounds/need-beer01.wav";
        static String NEED_BEER2 = "audio/sounds/need-beer02.wav";
        static String NEED_BEER3 = "audio/sounds/need-beer03.wav";
        static String SPOT_BEER1 = "audio/sounds/spot-beer01.wav";
        static String SPOT_BEER2 = "audio/sounds/spot-beer02.wav";
        static String SPOT_BEER3 = "audio/sounds/spot-beer03.wav";
        static String JUMP = "audio/sounds/jump.wav";
        static String LANDING = "audio/sounds/landing.wav";
        static String KICKED = "audio/sounds/kicked.wav";
        static String SPLASH = "audio/sounds/splash.wav";
        static String FIRE = "audio/sounds/fire.wav";
        static String DRINKING = "audio/sounds/drinking.wav";
        static String SUCCESS = "audio/sounds/success.wav";
        static String SNORE = "audio/sounds/snore.wav";
        static String BURP = "audio/sounds/burp.wav";
        static String RAVEN = "audio/sounds/raven.wav";
        static String PLOPP = "audio/sounds/plopp.wav";
        static String FROG = "audio/sounds/frog.wav";
        static String WHINE = "audio/sounds/whine.wav";

        static final Supplier<String> COMPLAIN_GENERATOR = new Supplier<String>() {
            Permutation perm = new Permutation(1, 12);
            @Override
            public String get() {
                return template("audio/sounds/complain*.wav", perm.next());
            }
        };

        static final Supplier<String> SWEARING_GENERATOR = new Supplier<String>() {
            Permutation perm = new Permutation(1, 37);
            @Override
            public String get() {
                return template("audio/sounds/swearing*.wav", perm.next());
            }
        };

        static final Supplier<String> DRWON_GENERATOR = new Supplier<String>() {
            Permutation perm = new Permutation(1, 7);
            @Override
            public String get() {
                return template("audio/sounds/drown*.wav", perm.next());
            }
        };

        static final Supplier<String> SHOUT_GENERATOR = new Supplier<String>() {
            Permutation perm = new Permutation(1, 18);
            @Override
            public String get() {
                return template("audio/sounds/shout*.wav", perm.next());
            }
        };

        static final Supplier<String> VICTORY_GENERATOR = new Supplier<String>() {
            Permutation perm = new Permutation(1, 9);
            @Override
            public String get() {
                return template("audio/sounds/victory*.wav", perm.next());
            }
        };

        static final Supplier<String> START_GENERATOR = new Supplier<String>() {
            Permutation perm = new Permutation(1, 16);
            @Override
            public String get() {
                return template("audio/sounds/start*.wav", perm.next());
            }
        };

        static final Supplier<String> BEER_GENERATOR = new Supplier<String>() {
            Permutation perm = new Permutation(1, 6);
            @Override
            public String get() {
                return template("audio/sounds/beer*.wav", perm.next());
            }
        };

        private static String sample(String template, int min, int max) {
            return template.replace("*", String.format("%02d", MathUtils.random(min, max)));
        }

        private static String template(String template, int value) {
            return template.replace("*", String.format("%02d", value));
        }
    }

    interface Music {
        String MENU_AUDIO = "audio/music/Dee_Yan-Key_-_03_-_Zeam.mp3";
        String NORMAL_AUDIO = "audio/music/Dee_Yan-Key_-_01_-_Schlini.mp3";
        String HURRY_AUDIO = "audio/music/Dee_Yan-Key_-_04_-_Ruaschad.mp3";

        String PROSIT_AUDIO = "audio/music/prosit.mp3";
        String PROSIT2_AUDIO = "audio/music/prosit2.mp3";
    }

    interface Pfx {
        String SLIDE_SMOKE = "pfx/slide-smoke.pfx";
        String SPLASH = "pfx/splash.pfx";
        String EXPLODE = "pfx/explode.pfx";
        String MUSIC = "pfx/music.pfx";
        String SNORE = "pfx/snore.pfx";
    }

    interface I18n {
        String LANGUAGE = "i18n/Language";
        String LOADING_LANGUAGE = "i18n/LoadingLanguage";
    }
}
