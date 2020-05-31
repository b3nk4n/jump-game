package de.bsautermeister.jump.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import de.bsautermeister.jump.Cfg;

public class ParallaxRenderer {

    private final Camera globalCamera;
    private final OrthographicCamera parallaxCamera;

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public ParallaxRenderer(Camera globalCamera, TiledMap map, OrthogonalTiledMapRenderer mapRenderer) {
        this.globalCamera = globalCamera;
        this.map = map;
        this.mapRenderer = mapRenderer;
        this.parallaxCamera = new OrthographicCamera();
    }

    public void renderLayer(String layer, float factorX, float factorY) {
        renderLayer(layer, factorX, factorY, 0f);
    }

    public void renderLayer(String layer, float factorX,
                                     float factorY, float yOffset) {
        parallaxCamera.setToOrtho(false, globalCamera.viewportWidth, globalCamera.viewportHeight);
        parallaxCamera.position.set(
                globalCamera.viewportWidth * (1 - factorX) + globalCamera.position.x * factorX,
                globalCamera.viewportHeight * (1 - factorY)  + globalCamera.position.y * factorY + yOffset,
                0);
        parallaxCamera.update();
        mapRenderer.setView(parallaxCamera);

        // repeat image along x axis
        TiledMapImageLayer imageLayer = (TiledMapImageLayer) map.getLayers().get(layer);
        imageLayer.getTextureRegion().getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        imageLayer.getTextureRegion().setRegionWidth(Cfg.WORLD_WIDTH * 100);

        mapRenderer.renderImageLayer(imageLayer);
    }
}
