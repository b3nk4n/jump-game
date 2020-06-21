package de.bsautermeister.jump.screens.transition;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import de.bsautermeister.jump.utils.GdxUtils;

public class ScaleScreenTransition extends ScreenTransitionBase {

    private boolean scaleOut;

    private final Vector2 startPositionInScreenCoordinates;

    public ScaleScreenTransition(float duration, Interpolation interpolation, boolean scaleOut) {
        this(duration, interpolation, scaleOut, null);
    }

    public ScaleScreenTransition(float duration, Interpolation interpolation, boolean scaleOut,
                                 Vector2 startPosition) {
        super(duration, interpolation);

        if (interpolation == null) {
            throw new IllegalArgumentException("Interpolation function is required");
        }

        this.scaleOut = scaleOut;
        this.startPositionInScreenCoordinates = startPosition;
    }

    @Override
    public void render(SpriteBatch batch, Texture currentScreenTexture, Texture nextScreenTexture, float progress) {
        float percentage = getInterpolatedPercentage(progress);

        float scale = scaleOut ? percentage : 1 - percentage;

        // draw order depends on scale type (in / out)
        Texture topTexture = scaleOut ? nextScreenTexture : currentScreenTexture;
        Texture bottomTexture = scaleOut ? currentScreenTexture : nextScreenTexture;

        int topTextureWidth = topTexture.getWidth();
        int topTextureHeight = topTexture.getHeight();

        int bottomTextureWidth = bottomTexture.getWidth();
        int bottomTextureHeight = bottomTexture.getHeight();

        // calculate new center
        float originX = topTextureWidth / 2f;
        float originY = topTextureHeight / 2f;
        if (startPositionInScreenCoordinates != null) {
            originX = Interpolation.smoother.apply(startPositionInScreenCoordinates.x, topTextureWidth / 2f, progress);
            originY = Interpolation.smoother.apply(startPositionInScreenCoordinates.y, topTextureHeight / 2f, progress);
            //originX = startPositionInScreenCoordinates.x + (originX - startPositionInScreenCoordinates.x) * progress;
            //originY = startPositionInScreenCoordinates.y + (originY - startPositionInScreenCoordinates.y) * progress;
        }

        // drawing
        GdxUtils.clearScreen();
        batch.begin();

        float whiteness = 1f - percentage * 0.33f;
        batch.setColor(whiteness, whiteness, whiteness, 1f);
        batch.draw(bottomTexture,
                0, 0,
                0, 0,
                bottomTextureWidth, bottomTextureHeight,
                1, 1,
                0,
                0, 0,
                bottomTextureWidth, bottomTextureHeight,
                false, true);
        batch.setColor(Color.WHITE);

        batch.draw(topTexture,
                0, 0,
                originX, originY,
                topTextureWidth, topTextureHeight,
                scale, scale,
                0,
                0, 0,
                topTextureWidth, topTextureHeight,
                false, true);

        batch.end();
    }
}
