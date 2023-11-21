package de.bsautermeister.jump.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import org.w3c.dom.Text;

import de.bsautermeister.jump.Cfg;

public class ParallaxRenderer {

    private final Camera globalCamera;
    private final OrthographicCamera parallaxCamera;

    private TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public ParallaxRenderer(Camera globalCamera, OrthogonalTiledMapRenderer mapRenderer) {
        this.globalCamera = globalCamera;
        this.mapRenderer = mapRenderer;
        this.parallaxCamera = new OrthographicCamera();
    }

    public void setMap(TiledMap map) {
        this.map = map;
    }

    public void renderLayer(String layer, float factorX, float factorY) {
        renderLayer(layer, factorX, factorY, 0f);
    }

    public void renderLayer(String layer, float factorX, float factorY, float yOffset) {
        parallaxCamera.setToOrtho(false, globalCamera.viewportWidth, globalCamera.viewportHeight);
        parallaxCamera.position.set(
                globalCamera.viewportWidth * (1 - factorX) + globalCamera.position.x * factorX,
                globalCamera.viewportHeight * (1 - factorY)  + globalCamera.position.y * factorY + yOffset,
                0);
        parallaxCamera.update();
        mapRenderer.setView(parallaxCamera);

        TiledMapImageLayer imageLayer = (TiledMapImageLayer) map.getLayers().get(layer);

        /*
        While texture wrapping worked well on Android/Desktop for any texture, the use of
        OpenGL ES on iOS requires image textures to be POT. NPOT textures are causing rendering
        glitches (black screen). Also, individual image textures are loaded to fit the next largest
        POT, so loading multiple individual images into memory is wasting a lot of memory.
        And the use of TextureAtlas also does not help, because texture wrapping unfortunately only
        works for the edges of an entire texture, and not for texture regions.
        See also: https://stackoverflow.com/questions/25084794/can-i-get-a-repeating-texture-with-libgdx-when-using-textureatlas
         */
        // repeat image along x axis
        // imageLayer.getTextureRegion().getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        // imageLayer.getTextureRegion().setRegionWidth(Cfg.WORLD_WIDTH * 100);

        mapRenderer.renderImageLayer(imageLayer);
    }
}
