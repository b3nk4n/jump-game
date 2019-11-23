package de.bsautermeister.jump.screens.game;

public enum GameState {
    UNDEFINED,
    PLAYING,
    PAUSED,
    GAME_OVER;

    public boolean isPlaying() {
        return this == PLAYING;
    }

    public boolean isPaused() {
        return this == PAUSED;
    }

    public boolean isGameOver() {
        return this == GAME_OVER;
    }
}
