package de.bsautermeister.jump.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public interface AssetDescriptors {

    interface Fonts {
        AssetDescriptor<BitmapFont> MARIO12 =
                new AssetDescriptor<BitmapFont>(AssetPaths.Fonts.MARIO12, BitmapFont.class);
        AssetDescriptor<BitmapFont> MARIO18 =
                new AssetDescriptor<BitmapFont>(AssetPaths.Fonts.MARIO18, BitmapFont.class);
        AssetDescriptor<BitmapFont> MARIO24 =
                new AssetDescriptor<BitmapFont>(AssetPaths.Fonts.MARIO24, BitmapFont.class);
        AssetDescriptor<BitmapFont> MARIO32 =
                new AssetDescriptor<BitmapFont>(AssetPaths.Fonts.MARIO32, BitmapFont.class);
    }

    interface Atlas {
        AssetDescriptor<TextureAtlas> LOADING =
                new AssetDescriptor<TextureAtlas>(AssetPaths.Atlas.LOADING, TextureAtlas.class);
        AssetDescriptor<TextureAtlas> GAMEPLAY =
                new AssetDescriptor<TextureAtlas>(AssetPaths.Atlas.GAMEPLAY, TextureAtlas.class);
        AssetDescriptor<TextureAtlas> UI =
                new AssetDescriptor<TextureAtlas>(AssetPaths.Atlas.UI, TextureAtlas.class);
    }

    interface Skins {
        AssetDescriptor<Skin> UI =
                new AssetDescriptor<Skin>(AssetPaths.Skins.UI, Skin.class);
    }

    interface Sounds {
        AssetDescriptor<Sound> COIN =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.COIN, Sound.class);
        AssetDescriptor<Sound> BUMP =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.BUMP, Sound.class);
        AssetDescriptor<Sound> BREAK_BLOCK =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.BREAK_BLOCK, Sound.class);
        AssetDescriptor<Sound> FOOD_SPAWN =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.FOOD_SPAWN, Sound.class);
        AssetDescriptor<Sound> BEER_SPAWN =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.BEER_SPAWN, Sound.class);
        AssetDescriptor<Sound> COIN_SPAWN =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.COIN_SPAWN, Sound.class);
        AssetDescriptor<Sound> EAT_FOOD =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.EAT_FOOD, Sound.class);
        AssetDescriptor<Sound> STOMP =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.STOMP, Sound.class);
        AssetDescriptor<Sound> COMPLAIN =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.COMPLAIN, Sound.class);
        AssetDescriptor<Sound> SWEARING1 =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SWEARING1, Sound.class);
        AssetDescriptor<Sound> SWEARING2 =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SWEARING2, Sound.class);
        AssetDescriptor<Sound> SWEARING3 =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SWEARING3, Sound.class);
        AssetDescriptor<Sound> SWEARING4 =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SWEARING4, Sound.class);
        AssetDescriptor<Sound> SWEARING5 =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SWEARING5, Sound.class);
        AssetDescriptor<Sound> SWEARING6 =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SWEARING6, Sound.class);
        AssetDescriptor<Sound> SWEARING7 =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SWEARING7, Sound.class);
        AssetDescriptor<Sound> JUMP =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.JUMP, Sound.class);
        AssetDescriptor<Sound> LANDING =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.LANDING, Sound.class);
        AssetDescriptor<Sound> KICKED =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.KICKED, Sound.class);
        AssetDescriptor<Sound> SPLASH =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SPLASH, Sound.class);
        AssetDescriptor<Sound> FIRE =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.FIRE, Sound.class);
        AssetDescriptor<Sound> DRINKING =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.DRINKING, Sound.class);
        AssetDescriptor<Sound> SUCCESS =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SUCCESS, Sound.class);
    }

    AssetDescriptor[] ALL = {
            Fonts.MARIO12, Fonts.MARIO18,Fonts.MARIO24, Fonts.MARIO32,
            Atlas.LOADING, Atlas. GAMEPLAY, Atlas.UI, Skins.UI,
            Sounds.COIN, Sounds.BUMP, Sounds.BREAK_BLOCK, Sounds.FOOD_SPAWN, Sounds.BEER_SPAWN,
            Sounds.COIN_SPAWN, Sounds.EAT_FOOD, Sounds.STOMP, Sounds.COMPLAIN, Sounds.LANDING,
            Sounds.JUMP, Sounds.KICKED, Sounds.SPLASH, Sounds.FIRE, Sounds.DRINKING, Sounds.SUCCESS,
            Sounds.SWEARING1, Sounds.SWEARING2, Sounds.SWEARING3, Sounds.SWEARING4,
            Sounds.SWEARING5, Sounds.SWEARING6, Sounds.SWEARING7
    };

}
