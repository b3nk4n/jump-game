package de.bsautermeister.jump.screens.transition;

import com.badlogic.gdx.math.Interpolation;

public interface ScreenTransitions {
    ScreenTransition FADE = new FadeScreenTransition(0.75f, Interpolation.fade);
    ScreenTransition SCLAE = new ScaleScreenTransition(0.75f, Interpolation.elastic, true);
    ScreenTransition SLIDE_LEFT = new SlideScreenTransition(0.75f, Interpolation.fade, true, Direction.LEFT);
    ScreenTransition SLIDE_RIGHT = new SlideScreenTransition(0.75f, Interpolation.fade, true, Direction.RIGHT);
}
