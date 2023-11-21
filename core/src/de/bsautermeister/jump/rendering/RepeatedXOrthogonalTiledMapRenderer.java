package de.bsautermeister.jump.rendering;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * An {@link BatchTiledMapRenderer} implementation that supports (enforced) {@code repeatX},
 * which is part of TMX format, but not yet supported in LibGDX.
 * <p>
 * Implementation is based on: <a href="https://github.com/libgdx/libgdx/pull/7080">PR#7080</a>
 */
public class RepeatedXOrthogonalTiledMapRenderer extends OrthogonalTiledMapRenderer {

    protected Rectangle repeatedImageBounds = new Rectangle();

    public RepeatedXOrthogonalTiledMapRenderer(TiledMap map, float unitScale, Batch batch) {
        super(map, unitScale, batch);
    }

    /**
     * Renders an image layer using repeated X.
     * <p>
     * See also: <a href="https://stackoverflow.com/questions/25084794/can-i-get-a-repeating-texture-with-libgdx-when-using-textureatlas">StackOverflow</a>
     * @param layer The layer to render with repeated X.
     */
    @Override
    public void renderImageLayer (TiledMapImageLayer layer) {
        final Color batchColor = batch.getColor();
        final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

        final float[] vertices = this.vertices;

        TextureRegion region = layer.getTextureRegion();

        if (region == null) {
            return;
        }

        final float x = layer.getX();
        final float y = layer.getY();
        final float x1 = x * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
        final float y1 = y * unitScale - viewBounds.y * (layer.getParallaxY() - 1);
        final float x2 = x1 + region.getRegionWidth() * unitScale;
        final float y2 = y1 + region.getRegionHeight() * unitScale;

        imageBounds.set(x1, y1, x2 - x1, y2 - y1);

        // Determine number of times to repeat image across X and Y, + 4 for padding to avoid pop in/out
        int repeatX = (int) Math.ceil((viewBounds.width / imageBounds.width) + 4);
        int repeatY = 0;

        // Calculate the offset of the first image to align with the camera
        float startX = viewBounds.x;
        startX = startX - (startX % imageBounds.width);

        for (int i = 0; i <= repeatX; i++) {
            for (int j = 0; j <= repeatY; j++) {
                float rx1 = x1;
                float ry1 = y1;
                float rx2 = x2;
                float ry2 = y2;

                // Use (i -2)/(j-2) to begin placing our repeating images outside the camera.
                // In case the image is offset, we must negate this using + (x1% imageBounds.width)
                // It's a way to get the remainder of how many images would fit between its starting position and 0
                rx1 = startX + ((i - 2) * imageBounds.width) + (x1 % imageBounds.width);
                rx2 = rx1 + imageBounds.width;

                repeatedImageBounds.set(rx1, ry1, rx2 - rx1, ry2 - ry1);

                if (viewBounds.contains(repeatedImageBounds) || viewBounds.overlaps(repeatedImageBounds)) {
                    final float ru1 = region.getU();
                    final float rv1 = region.getV2();
                    final float ru2 = region.getU2();
                    final float rv2 = region.getV();

                    vertices[X1] = rx1;
                    vertices[Y1] = ry1;
                    vertices[C1] = color;
                    vertices[U1] = ru1;
                    vertices[V1] = rv1;

                    vertices[X2] = rx1;
                    vertices[Y2] = ry2;
                    vertices[C2] = color;
                    vertices[U2] = ru1;
                    vertices[V2] = rv2;

                    vertices[X3] = rx2;
                    vertices[Y3] = ry2;
                    vertices[C3] = color;
                    vertices[U3] = ru2;
                    vertices[V3] = rv2;

                    vertices[X4] = rx2;
                    vertices[Y4] = ry1;
                    vertices[C4] = color;
                    vertices[U4] = ru2;
                    vertices[V4] = rv1;

                    batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
                }
            }
        }
    }
}
