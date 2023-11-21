package de.bsautermeister.jump.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;

public interface AssetDescriptors {

    interface Fonts {
        AssetDescriptor<BitmapFont> S =
                new AssetDescriptor<>(AssetPaths.Fonts.S, BitmapFont.class);
        AssetDescriptor<BitmapFont> M =
                new AssetDescriptor<>(AssetPaths.Fonts.M, BitmapFont.class);
        AssetDescriptor<BitmapFont> L =
                new AssetDescriptor<>(AssetPaths.Fonts.L, BitmapFont.class);
        AssetDescriptor<BitmapFont> XL =
                new AssetDescriptor<>(AssetPaths.Fonts.XL, BitmapFont.class);
        AssetDescriptor<BitmapFont> XXL =
                new AssetDescriptor<>(AssetPaths.Fonts.XXL, BitmapFont.class);
        AssetDescriptor<BitmapFont> TITLE =
                new AssetDescriptor<>(AssetPaths.Fonts.TITLE, BitmapFont.class);
    }

    interface Atlas {
        AssetDescriptor<TextureAtlas> LOADING =
                new AssetDescriptor<>(AssetPaths.Atlas.LOADING, TextureAtlas.class);
        AssetDescriptor<TextureAtlas> GAMEPLAY =
                new AssetDescriptor<>(AssetPaths.Atlas.GAMEPLAY, TextureAtlas.class);
        AssetDescriptor<TextureAtlas> UI =
                new AssetDescriptor<>(AssetPaths.Atlas.UI, TextureAtlas.class);
        AssetDescriptor<TextureAtlas> MAPS =
                new AssetDescriptor<>(AssetPaths.Atlas.MAPS, TextureAtlas.class);
    }

    interface Skins {
        AssetDescriptor<Skin> UI =
                new AssetDescriptor<>(AssetPaths.Skins.UI, Skin.class);
    }

    interface Sounds {
        AssetDescriptor<Sound> COIN =
                new AssetDescriptor<>(AssetPaths.Sounds.COIN, Sound.class);
        AssetDescriptor<Sound> BUMP =
                new AssetDescriptor<>(AssetPaths.Sounds.BUMP, Sound.class);
        AssetDescriptor<Sound> BREAK_BLOCK =
                new AssetDescriptor<>(AssetPaths.Sounds.BREAK_BLOCK, Sound.class);
        AssetDescriptor<Sound> BEER_SPAWN =
                new AssetDescriptor<>(AssetPaths.Sounds.BEER_SPAWN, Sound.class);
        AssetDescriptor<Sound> COIN_SPAWN =
                new AssetDescriptor<>(AssetPaths.Sounds.COIN_SPAWN, Sound.class);
        AssetDescriptor<Sound> EAT_FOOD =
                new AssetDescriptor<>(AssetPaths.Sounds.EAT_FOOD, Sound.class);
        AssetDescriptor<Sound> STOMP =
                new AssetDescriptor<>(AssetPaths.Sounds.STOMP, Sound.class);
        AssetDescriptor<Sound> COMPLAIN1 =
                new AssetDescriptor<>(AssetPaths.Sounds.COMPLAIN_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> COMPLAIN2 =
                new AssetDescriptor<>(AssetPaths.Sounds.COMPLAIN_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> COMPLAIN3 =
                new AssetDescriptor<>(AssetPaths.Sounds.COMPLAIN_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> COMPLAIN4 =
                new AssetDescriptor<>(AssetPaths.Sounds.COMPLAIN_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> COMPLAIN5 =
                new AssetDescriptor<>(AssetPaths.Sounds.COMPLAIN_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING1 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING2 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING3 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING4 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING5 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING6 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING7 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING8 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING9 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SWEARING10 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> DROWN1 =
                new AssetDescriptor<>(AssetPaths.Sounds.DRWON_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> DROWN2 =
                new AssetDescriptor<>(AssetPaths.Sounds.DRWON_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> DROWN3 =
                new AssetDescriptor<>(AssetPaths.Sounds.DRWON_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> DROWN4 =
                new AssetDescriptor<>(AssetPaths.Sounds.DRWON_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> DROWN5 =
                new AssetDescriptor<>(AssetPaths.Sounds.DRWON_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SHOUT1 =
                new AssetDescriptor<>(AssetPaths.Sounds.SHOUT_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SHOUT2 =
                new AssetDescriptor<>(AssetPaths.Sounds.SHOUT_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SHOUT3 =
                new AssetDescriptor<>(AssetPaths.Sounds.SHOUT_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SHOUT4 =
                new AssetDescriptor<>(AssetPaths.Sounds.SHOUT_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> SHOUT5 =
                new AssetDescriptor<>(AssetPaths.Sounds.SHOUT_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> BEER1 =
                new AssetDescriptor<>(AssetPaths.Sounds.BEER_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> BEER2 =
                new AssetDescriptor<>(AssetPaths.Sounds.BEER_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> BEER3 =
                new AssetDescriptor<>(AssetPaths.Sounds.BEER_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> BEER4 =
                new AssetDescriptor<>(AssetPaths.Sounds.BEER_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> BEER5 =
                new AssetDescriptor<>(AssetPaths.Sounds.BEER_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> OZAPFT1 =
                new AssetDescriptor<>(AssetPaths.Sounds.OZAPFT1, Sound.class);
        AssetDescriptor<Sound> OZAPFT2 =
                new AssetDescriptor<>(AssetPaths.Sounds.OZAPFT2, Sound.class);
        AssetDescriptor<Sound> BOOST1 =
                new AssetDescriptor<>(AssetPaths.Sounds.BOOST1, Sound.class);
        AssetDescriptor<Sound> BOOST2 =
                new AssetDescriptor<>(AssetPaths.Sounds.BOOST2, Sound.class);
        AssetDescriptor<Sound> BOOST3 =
                new AssetDescriptor<>(AssetPaths.Sounds.BOOST3, Sound.class);
        AssetDescriptor<Sound> BOOST4 =
                new AssetDescriptor<>(AssetPaths.Sounds.BOOST4, Sound.class);
        AssetDescriptor<Sound> BOOST5 =
                new AssetDescriptor<>(AssetPaths.Sounds.BOOST5, Sound.class);
        AssetDescriptor<Sound> NEED_BEER1 =
                new AssetDescriptor<>(AssetPaths.Sounds.NEED_BEER1, Sound.class);
        AssetDescriptor<Sound> NEED_BEER2 =
                new AssetDescriptor<>(AssetPaths.Sounds.NEED_BEER2, Sound.class);
        AssetDescriptor<Sound> NEED_BEER3 =
                new AssetDescriptor<>(AssetPaths.Sounds.NEED_BEER3, Sound.class);
        AssetDescriptor<Sound> SPOT_BEER1 =
                new AssetDescriptor<>(AssetPaths.Sounds.SPOT_BEER1, Sound.class);
        AssetDescriptor<Sound> SPOT_BEER2 =
                new AssetDescriptor<>(AssetPaths.Sounds.SPOT_BEER2, Sound.class);
        AssetDescriptor<Sound> SPOT_BEER3 =
                new AssetDescriptor<>(AssetPaths.Sounds.SPOT_BEER3, Sound.class);
        AssetDescriptor<Sound> START1 =
                new AssetDescriptor<>(AssetPaths.Sounds.START_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> START2 =
                new AssetDescriptor<>(AssetPaths.Sounds.START_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> START3 =
                new AssetDescriptor<>(AssetPaths.Sounds.START_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> START4 =
                new AssetDescriptor<>(AssetPaths.Sounds.START_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> START5 =
                new AssetDescriptor<>(AssetPaths.Sounds.START_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> VICTORY1 =
                new AssetDescriptor<>(AssetPaths.Sounds.VICTORY_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> VICTORY2 =
                new AssetDescriptor<>(AssetPaths.Sounds.VICTORY_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> VICTORY3 =
                new AssetDescriptor<>(AssetPaths.Sounds.VICTORY_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> VICTORY4 =
                new AssetDescriptor<>(AssetPaths.Sounds.VICTORY_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> VICTORY5 =
                new AssetDescriptor<>(AssetPaths.Sounds.VICTORY_GENERATOR.get(), Sound.class);
        AssetDescriptor<Sound> JUMP =
                new AssetDescriptor<>(AssetPaths.Sounds.JUMP, Sound.class);
        AssetDescriptor<Sound> LANDING =
                new AssetDescriptor<>(AssetPaths.Sounds.LANDING, Sound.class);
        AssetDescriptor<Sound> KICKED =
                new AssetDescriptor<>(AssetPaths.Sounds.KICKED, Sound.class);
        AssetDescriptor<Sound> SPLASH =
                new AssetDescriptor<>(AssetPaths.Sounds.SPLASH, Sound.class);
        AssetDescriptor<Sound> FIRE =
                new AssetDescriptor<>(AssetPaths.Sounds.FIRE, Sound.class);
        AssetDescriptor<Sound> DRINKING =
                new AssetDescriptor<>(AssetPaths.Sounds.DRINKING, Sound.class);
        AssetDescriptor<Sound> SUCCESS =
                new AssetDescriptor<>(AssetPaths.Sounds.SUCCESS, Sound.class);
        AssetDescriptor<Sound> SNORE =
                new AssetDescriptor<>(AssetPaths.Sounds.SNORE, Sound.class);
        AssetDescriptor<Sound> BURP =
                new AssetDescriptor<>(AssetPaths.Sounds.BURP, Sound.class);
        AssetDescriptor<Sound> RAVEN =
                new AssetDescriptor<>(AssetPaths.Sounds.RAVEN, Sound.class);
        AssetDescriptor<Sound> PLOPP =
                new AssetDescriptor<>(AssetPaths.Sounds.PLOPP, Sound.class);
        AssetDescriptor<Sound> FROG =
                new AssetDescriptor<>(AssetPaths.Sounds.FROG, Sound.class);
        AssetDescriptor<Sound> WHINE =
                new AssetDescriptor<>(AssetPaths.Sounds.WHINE, Sound.class);
    }

    interface I18n {
        AssetDescriptor<I18NBundle> LANGUAGE =
                new AssetDescriptor<>(AssetPaths.I18n.LANGUAGE, I18NBundle.class);
        AssetDescriptor<I18NBundle> LOADING_LANGUAGE =
                new AssetDescriptor<>(AssetPaths.I18n.LOADING_LANGUAGE, I18NBundle.class);
    }

    AssetDescriptor<?>[] ALL = {
            Fonts.S, Fonts.M, Fonts.L, Fonts.XL, Fonts.XXL, Fonts.TITLE,
            Atlas.LOADING, Atlas. GAMEPLAY, Atlas.UI, Skins.UI, Atlas.MAPS,
            Sounds.COIN, Sounds.BUMP, Sounds.BREAK_BLOCK, Sounds.BEER_SPAWN,
            Sounds.COIN_SPAWN, Sounds.EAT_FOOD, Sounds.STOMP, Sounds.LANDING,
            Sounds.JUMP, Sounds.KICKED, Sounds.SPLASH, Sounds.FIRE, Sounds.DRINKING, Sounds.SUCCESS,
            Sounds.COMPLAIN1, Sounds.COMPLAIN2, Sounds.COMPLAIN3, Sounds.COMPLAIN4, Sounds.COMPLAIN5,
            Sounds.SWEARING1, Sounds.SWEARING2, Sounds.SWEARING3, Sounds.SWEARING4,
            Sounds.SWEARING5, Sounds.SWEARING6, Sounds.SWEARING7, Sounds.SWEARING8,
            Sounds.SWEARING9, Sounds.SWEARING10,
            Sounds.DROWN1, Sounds.DROWN2, Sounds.DROWN3, Sounds.DROWN4, Sounds.DROWN5,
            Sounds.SHOUT1, Sounds.SHOUT2, Sounds.SHOUT3, Sounds.SHOUT4, Sounds.SHOUT5,
            Sounds.BEER1, Sounds.BEER2, Sounds.BEER3, Sounds.BEER4, Sounds.BEER5,
            Sounds.OZAPFT1, Sounds.OZAPFT2,
            Sounds.BOOST1, Sounds.BOOST2, Sounds.BOOST3, Sounds.BOOST4, Sounds.BOOST5,
            Sounds.NEED_BEER1, Sounds.NEED_BEER2, Sounds.NEED_BEER3,
            Sounds.SPOT_BEER1, Sounds.SPOT_BEER2, Sounds.SPOT_BEER3,
            Sounds.START1, Sounds.START2, Sounds.START3, Sounds.START4, Sounds.START5,
            Sounds.VICTORY1, Sounds.VICTORY2, Sounds.VICTORY3, Sounds.VICTORY4, Sounds.VICTORY5,
            Sounds.SNORE, Sounds.BURP, Sounds.RAVEN, Sounds.PLOPP, Sounds.FROG, Sounds.WHINE,
            I18n.LANGUAGE
    };

}
