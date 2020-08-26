package de.bsautermeister.jump.screens.game.level;

public class LevelMetadata {
    private static final LevelInfo[] LEVELS = new LevelInfo[]{
            LevelInfo.tutorial(),

            new LevelInfo(150, 2000, 4000, 5000, 0),
            new LevelInfo(240, 6000, 7500, 9000, 1),
            new LevelInfo(240, 5000, 7500, 9000, 3),
            new LevelInfo(210, 0, 999, 9999, 6),
            new LevelInfo(99, 0, 999, 9999, 9)
    };

    private LevelMetadata() {}

    public static LevelInfo getLevelInfo(int level) {
        return LEVELS[level];
    }
}