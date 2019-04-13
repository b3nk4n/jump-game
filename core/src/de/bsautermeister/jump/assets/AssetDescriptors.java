package de.bsautermeister.jump.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public interface AssetDescriptors {
    interface Atlas {
        AssetDescriptor<TextureAtlas> GAMEPLAY =
                new AssetDescriptor<TextureAtlas>(AssetPaths.Atlas.GAMEPLAY, TextureAtlas.class);
    }
}
