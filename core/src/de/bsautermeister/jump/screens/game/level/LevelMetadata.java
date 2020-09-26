package de.bsautermeister.jump.screens.game.level;

public class LevelMetadata {
    private static final LevelInfo[] LEVELS = new LevelInfo[]{
            LevelInfo.tutorial(),

            new LevelInfo(180, 8000, 10000, 12000, 0),
            new LevelInfo(150, 5000, 7500, 9000, 1),
            new LevelInfo(180, 8000, 11000, 13000, 3),
            new LevelInfo(210, 8000, 11000, 12500, 6),
            new LevelInfo(240, 9000, 12000, 14000, 9),

            new LevelInfo(240, 9000, 12000, 13500, 11),
            new LevelInfo(210, 9000, 12000, 14000, 14),
            new LevelInfo(240, 8000, 11500, 13000, 17),
            new LevelInfo(240, 9000, 11000, 13000, 20),
            new LevelInfo(270, 10000, 13500, 15000, 22),
            new LevelInfo(330, 11000, 14000, 16000, 25),
    };

    private LevelMetadata() {}

    public static LevelInfo getLevelInfo(int level) {
        return LEVELS[level];
    }
}