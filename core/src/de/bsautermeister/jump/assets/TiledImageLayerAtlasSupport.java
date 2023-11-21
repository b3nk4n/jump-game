package de.bsautermeister.jump.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;

import java.util.HashMap;
import java.util.Map;

import de.bsautermeister.jump.physics.WorldCreator;

/**
 * Workaround to replace the used image files in a Tiled TMX tilemap to instead use a single packed
 * texture atlas. While this is not strictly needed at all for Desktop or Android, it is helpful
 * for iOS support:
 * <ul>
 *     <li>
 *         OpenGL ES is optimized for POT textures, and anyways takes up that much memory per image.
 *         And is therefore more memory efficient.
 *     </li>
 *     <li>
 *         Texture wrapping on iOS causes black screen.
 *     </li>
 * </ul>
 * Implementing this honestly started with a misunderstanding that it would be possible to use
 * texture regions for texture wrapping. Which however does not work as anticipated, because texture
 * wrapping only works om texture edges.
 * However, motivated by the better memory efficiency, I decide to keep this manual workaround.
 */
// TODO check whether this is really more memory efficient, because the TMX map is still referencing
//      the individual images in asset folder, to work properly when loading the maps.
//      Relative referencing using ../../desktop/assets-raw/maps/xxx.jpg does not work in LibGdx,
//      while it works well when opening the TMX file in Tiled.
public final class TiledImageLayerAtlasSupport {

    private static final Map<String, String> MAPS = new HashMap<>();

    static {
        MAPS.put(WorldCreator.BG_IMG_STATIC_KEY, RegionNames.MAPS_SKY1);
        MAPS.put(WorldCreator.BG_IMG_CLOUDS1_KEY, RegionNames.MAPS_CLOUDS);
        MAPS.put(WorldCreator.BG_IMG_CLOUDS2_KEY, RegionNames.MAPS_SKY2);
        MAPS.put(WorldCreator.BG_IMG_MOUNTAINS_KEY, RegionNames.MAPS_MOUNTAINS);
        MAPS.put(WorldCreator.BG_IMG_FORREST1_KEY, RegionNames.MAPS_PINE1);
        MAPS.put(WorldCreator.BG_IMG_FORREST2_KEY, RegionNames.MAPS_PINE2);
        MAPS.put(WorldCreator.BG_IMG_GRASS1_KEY, RegionNames.MAPS_GRASS1);
        MAPS.put(WorldCreator.BG_IMG_GRASS2_KEY, RegionNames.MAPS_GRASS2);
    }

    public static void replaceMapImagesWithAtlas(TiledMap map, AssetManager assetManager) {
        TextureAtlas mapsAtlas = assetManager.get(AssetDescriptors.Atlas.MAPS);

        for (MapLayer layer : map.getLayers()) {
            if (!(layer instanceof TiledMapImageLayer)) {
                continue;
            }

            String layerName = layer.getName();
            String regionName = TiledImageLayerAtlasSupport.MAPS.get(layerName);
            TextureRegion textureRegion = mapsAtlas.findRegion(regionName);

            if (textureRegion  != null) {
                ((TiledMapImageLayer) layer).setTextureRegion(textureRegion);
            }
        }
    }

    private TiledImageLayerAtlasSupport() { }
}
