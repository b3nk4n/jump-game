package de.bsautermeister.jump.screens.game.level;

public class LevelMetadata {
    private static final LevelInfo[] LEVELS = new LevelInfo[]{
            LevelInfo.tutorial(),

            new LevelInfo(150, 5000, 7500, 9000, 0),
            new LevelInfo(240, 9000, 12000, 14000, 1),
            new LevelInfo(240, 9000, 12000, 13500, 3),
            new LevelInfo(180, 8000, 11000, 13000, 6),
            new LevelInfo(210, 9000, 12000, 14000, 9),

            new LevelInfo(330, 11000, 14000, 16000, 12),
            new LevelInfo(180, 8000, 10000, 12000, 15), // make this level 1?
            new LevelInfo(240, 9000, 11000, 13000, 18),
            new LevelInfo(999, 9999, 99999, 999999, 99),
            new LevelInfo(999, 9999, 99999, 999999, 99),
            new LevelInfo(999, 9999, 99999, 999999, 99)
    };

    private LevelMetadata() {}

    public static LevelInfo getLevelInfo(int level) {
        return LEVELS[level];
    }
}