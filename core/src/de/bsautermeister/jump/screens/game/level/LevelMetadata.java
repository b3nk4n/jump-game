package de.bsautermeister.jump.screens.game.level;

public class LevelMetadata {
    private static final LevelInfo[] LEVELS = new LevelInfo[]{
            LevelInfo.tutorial(),

            new LevelInfo(120, 0, 4000, 6000, 0),
            new LevelInfo(60, 0, 1000, 2000, 1),
            new LevelInfo(99, 0, 999, 9999, 3),
            new LevelInfo(99, 0, 999, 9999, 6),
            new LevelInfo(99, 0, 999, 9999, 9),

            new LevelInfo(99, 0, 999, 9999, 12),
            new LevelInfo(99, 0, 999, 9999, 15),
            new LevelInfo(99, 0, 999, 9999, 18),
            new LevelInfo(99, 0, 999, 9999, 21),
            new LevelInfo(99, 0, 999, 9999, 24),
            new LevelInfo(99, 0, 999, 9999, 27)
    };

    private LevelMetadata() {}

    public static LevelInfo getLevelInfo(int level) {
        return LEVELS[level];
    }
}
