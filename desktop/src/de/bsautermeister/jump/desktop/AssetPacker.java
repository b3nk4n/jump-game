package de.bsautermeister.jump.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import java.nio.file.Paths;

public class AssetPacker {
    private static final String RAW_ASSETS_PATH = "desktop/assets-raw";
    private static final String ASSETS_PATH = "assets";

    public static void main(String[] args) {
        TexturePacker.Settings defaultSettings = settings(1024, 1024);

        pack("gameplay", defaultSettings);
        pack("loading", defaultSettings);
        pack("ui", defaultSettings);

        pack("maps", settings(1024, 2048));
    }

    private static TexturePacker.Settings settings(int maxWidth, int maxHeight) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = maxWidth;
        settings.maxHeight = maxHeight;
        settings.paddingX = 4;
        settings.paddingY = 4;
        settings.debug = false;
        return settings;
    }

    private static void pack(String name, TexturePacker.Settings settings) {
        TexturePacker.process(settings,
                Paths.get(RAW_ASSETS_PATH, name).toString(),
                Paths.get(ASSETS_PATH, name).toString(),
                name);
    }
}
