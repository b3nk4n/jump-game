package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.math.Vector2;

public interface GameScreenCallbacks {
    void success(int level, Vector2 goalCenterPosition);
    void backToMenu(Vector2 clickScreenPosition);
    void reportKillSequelFinished(int count);
    void reportDrunkBeer();
}
