package de.bsautermeister.jump.screens.game.level;

public class LevelMetadata {
    private static final LevelInfo[] LEVELS = new LevelInfo[]{
            new LevelInfo(120, 0, 4000, 6000, 0),
            new LevelInfo(60, 0, 1000, 2000, 1)
    };

    private LevelMetadata() {}

    public static LevelInfo getLevelInfo(int level) {
        return LEVELS[level - 1];
    }
}
