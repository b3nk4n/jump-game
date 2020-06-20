package de.bsautermeister.jump.screens.menu.controls;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

public class AnimatedLabel extends Table {
    public static final float TEXT_ANIMATION_DURATION = 7.5f;
    private static final float CHAR_ANIMATION_TIME = 1.0f;

    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private Array<Label> charContainers;

    public AnimatedLabel(Skin skin, String labelStyle, int maxTextLength) {
        super();
        Label.LabelStyle labelStyleInstance = skin.get(labelStyle, Label.LabelStyle.class);
        this.font = labelStyleInstance.font;

        charContainers = new Array<>(maxTextLength);
        glyphLayout = new GlyphLayout();

        for (int i = 0; i < maxTextLength; i++) {
            Label label = new Label("", labelStyleInstance);
            add(label);
            charContainers.add(label);
        }

        center();
    }

    private FloatArray getPositions(String text, GlyphLayout.GlyphRun run) {
        float[] positions = new float[text.length()];

        for (int i = 0; i < positions.length; i++) {
            if (i == 0) {
                positions[0] = run.xAdvances.get(0);
            } else {
                positions[i] = positions[i - 1] + run.xAdvances.get(i);
            }
        }

        return FloatArray.with(positions);
    }

    public AnimatedLabel typeText(String text, float delay) {
        prepareText(text);

        float characterDelay = CHAR_ANIMATION_TIME / text.length();

        for (int i = 0; i < text.length(); i++) {
            Label character = charContainers.get(i);
            character.addAction(
                    Actions.parallel(
                            Actions.hide(),
                            Actions.delay(delay + characterDelay * i,
                                    Actions.show()),
                            Actions.delay(TEXT_ANIMATION_DURATION - CHAR_ANIMATION_TIME,
                                    Actions.hide())
                    ));
        }
        return this;
    }

    private void prepareText(String text) {
        glyphLayout.setText(font, text);
        GlyphLayout.GlyphRun run = glyphLayout.runs.get(0);
        FloatArray advances = run.xAdvances;
        FloatArray positions = getPositions(text, run);

        float leftX = -glyphLayout.width / 2;

        for (int i = 0; i < text.length(); i++) {
            Label character = charContainers.get(i);
            character.setText(String.valueOf(text.charAt(i)));
            character.setPosition(leftX + positions.get(i), 0);
            character.setOrigin(advances.get(i) / 2, character.getHeight() / 8);
        }
    }
}
