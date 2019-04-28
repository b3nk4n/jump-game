package de.bsautermeister.jump.screens.transition;

import com.badlogic.gdx.math.Interpolation;

public final class ScreenTransitions {
    public static final ScreenTransition FADE = new FadeScreenTransition(0.75f, Interpolation.fade);
    public static final ScreenTransition SCLAE = new ScaleScreenTransition(0.75f, Interpolation.elastic, true);
    public static final ScreenTransition SLIDE = new SlideScreenTransition(0.75f, Interpolation.circleIn, true, Direction.LEFT);

    private ScreenTransitions() {
    }
}
