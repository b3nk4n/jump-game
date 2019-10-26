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
        AssetDescriptor<Sound> POWERUP_SPAWN =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.POWERUP_SPAWN, Sound.class);
        AssetDescriptor<Sound> POWERUP =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.POWERUP, Sound.class);
        AssetDescriptor<Sound> STOMP =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.STOMP, Sound.class);
        AssetDescriptor<Sound> POWERDOWN =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.POWERDOWN, Sound.class);
        AssetDescriptor<Sound> MARIO_DIE =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.MARIO_DIE, Sound.class);
        AssetDescriptor<Sound> JUMP =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.JUMP, Sound.class);
        AssetDescriptor<Sound> KICKED =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.KICKED, Sound.class);
        AssetDescriptor<Sound> SPLASH =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.SPLASH, Sound.class);
        AssetDescriptor<Sound> FIRE =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.FIRE, Sound.class);
        AssetDescriptor<Sound> DRINKING =
                new AssetDescriptor<Sound>(AssetPaths.Sounds.DRINKING, Sound.class);
    }

    AssetDescriptor[] ALL = {
            Fonts.MARIO12, Fonts.MARIO18,Fonts.MARIO24, Fonts.MARIO32,
            Atlas.LOADING, Atlas. GAMEPLAY, Atlas.UI, Skins.UI,
            Sounds.COIN, Sounds.BUMP, Sounds.BREAK_BLOCK, Sounds.POWERUP_SPAWN, Sounds.POWERUP,
            Sounds.STOMP, Sounds.POWERDOWN, Sounds.MARIO_DIE, Sounds.JUMP, Sounds.KICKED,
            Sounds.SPLASH, Sounds.FIRE, Sounds.DRINKING
    };

}
