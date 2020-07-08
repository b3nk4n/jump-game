package de.bsautermeister.jump.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

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
        AssetDescriptor<Sound> FOOD_SPAWN =
                new AssetDescriptor<>(AssetPaths.Sounds.FOOD_SPAWN, Sound.class);
        AssetDescriptor<Sound> BEER_SPAWN =
                new AssetDescriptor<>(AssetPaths.Sounds.BEER_SPAWN, Sound.class);
        AssetDescriptor<Sound> COIN_SPAWN =
                new AssetDescriptor<>(AssetPaths.Sounds.COIN_SPAWN, Sound.class);
        AssetDescriptor<Sound> EAT_FOOD =
                new AssetDescriptor<>(AssetPaths.Sounds.EAT_FOOD, Sound.class);
        AssetDescriptor<Sound> STOMP =
                new AssetDescriptor<>(AssetPaths.Sounds.STOMP, Sound.class);
        AssetDescriptor<Sound> COMPLAIN =
                new AssetDescriptor<>(AssetPaths.Sounds.COMPLAIN, Sound.class);
        AssetDescriptor<Sound> SWEARING1 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING1, Sound.class);
        AssetDescriptor<Sound> SWEARING2 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING2, Sound.class);
        AssetDescriptor<Sound> SWEARING3 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING3, Sound.class);
        AssetDescriptor<Sound> SWEARING4 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING4, Sound.class);
        AssetDescriptor<Sound> SWEARING5 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING5, Sound.class);
        AssetDescriptor<Sound> SWEARING6 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING6, Sound.class);
        AssetDescriptor<Sound> SWEARING7 =
                new AssetDescriptor<>(AssetPaths.Sounds.SWEARING7, Sound.class);
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

    AssetDescriptor[] ALL = {
            Fonts.S, Fonts.M, Fonts.L, Fonts.XL, Fonts.XXL, Fonts.TITLE,
            Atlas.LOADING, Atlas. GAMEPLAY, Atlas.UI, Skins.UI,
            Sounds.COIN, Sounds.BUMP, Sounds.BREAK_BLOCK, Sounds.FOOD_SPAWN, Sounds.BEER_SPAWN,
            Sounds.COIN_SPAWN, Sounds.EAT_FOOD, Sounds.STOMP, Sounds.COMPLAIN, Sounds.LANDING,
            Sounds.JUMP, Sounds.KICKED, Sounds.SPLASH, Sounds.FIRE, Sounds.DRINKING, Sounds.SUCCESS,
            Sounds.SWEARING1, Sounds.SWEARING2, Sounds.SWEARING3, Sounds.SWEARING4,
            Sounds.SWEARING5, Sounds.SWEARING6, Sounds.SWEARING7, Sounds.SNORE, Sounds.BURP,
            Sounds.RAVEN, Sounds.PLOPP, Sounds.FROG, Sounds.WHINE
    };

}
