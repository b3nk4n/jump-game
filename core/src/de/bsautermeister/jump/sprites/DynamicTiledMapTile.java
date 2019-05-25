package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;

public class DynamicTiledMapTile implements TiledMapTile {

    TiledMapTile tile;

    // local offset variables, so that changes in offset only impact a single instance
    private float offsetX;
    private float offsetY;

    public DynamicTiledMapTile(TiledMapTile tile) {
        this.tile = tile;
    }

    @Override
    public int getId() {
        return tile.getId();
    }

    @Override
    public void setId(int id) {
        tile.setId(id);
    }

    @Override
    public BlendMode getBlendMode() {
        return tile.getBlendMode();
    }

    @Override
    public void setBlendMode(BlendMode blendMode) {
        tile.setBlendMode(blendMode);
    }

    @Override
    public TextureRegion getTextureRegion() {
        return tile.getTextureRegion();
    }

    @Override
    public void setTextureRegion(TextureRegion textureRegion) {
        tile.setTextureRegion(textureRegion);
    }

    @Override
    public float getOffsetX() {
        return offsetX;
    }

    @Override
    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    @Override
    public float getOffsetY() {
        return offsetY;
    }

    @Override
    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public MapProperties getProperties() {
        return tile.getProperties();
    }

    @Override
    public MapObjects getObjects() {
        return tile.getObjects();
    }
}
