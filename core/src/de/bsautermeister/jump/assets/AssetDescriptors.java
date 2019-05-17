package de.bsautermeister.jump.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public interface AssetDescriptors {

    interface Fonts {

    }

    interface Atlas {
        AssetDescriptor<TextureAtlas> LOADING =
                new AssetDescriptor<TextureAtlas>(AssetPaths.Atlas.LOADING, TextureAtlas.class);

        AssetDescriptor<TextureAtlas> GAMEPLAY =
                new AssetDescriptor<TextureAtlas>(AssetPaths.Atlas.GAMEPLAY, TextureAtlas.class);
    }

    interface Skins {

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
    }

    public static final AssetDescriptor[] ALL = {
            Atlas.LOADING, Atlas. GAMEPLAY,
            Sounds.COIN, Sounds.BUMP, Sounds.BREAK_BLOCK, Sounds.POWERUP_SPAWN, Sounds.POWERUP,
            Sounds.STOMP, Sounds.POWERDOWN, Sounds.MARIO_DIE, Sounds.JUMP
    };

}
