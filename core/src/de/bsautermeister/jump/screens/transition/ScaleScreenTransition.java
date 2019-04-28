package de.bsautermeister.jump.screens.transition;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

import de.bsautermeister.jump.utils.GdxUtils;

public class ScaleScreenTransition extends ScreenTransitionBase {

    private boolean scaleOut;

    public ScaleScreenTransition(float duration, Interpolation interpolation, boolean scaleOut) {
        super(duration, interpolation);

        if (interpolation == null) {
            throw new IllegalArgumentException("Interpolation function is required");
        }

        this.scaleOut = scaleOut;
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

        // drawing
        GdxUtils.clearScreen();
        batch.begin();

        batch.draw(bottomTexture,
                0, 0,
                0, 0,
                bottomTextureWidth, bottomTextureHeight,
                1, 1,
                0,
                0, 0,
                bottomTextureWidth, bottomTextureHeight,
                false, true);

        batch.draw(topTexture,
                0, 0,
                topTextureWidth / 2f, topTextureHeight / 2f,
                topTextureWidth, topTextureHeight,
                scale, scale,
                0,
                0, 0,
                topTextureWidth, topTextureHeight,
                false, true);

        batch.end();
    }
}
