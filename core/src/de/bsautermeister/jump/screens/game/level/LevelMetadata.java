package de.bsautermeister.jump.screens.game.level;

public class LevelMetadata {
    private static final LevelInfo[] LEVELS = new LevelInfo[]{
            LevelInfo.tutorial(),

            new LevelInfo(180, 5000, 8000, 11000, 0),
            new LevelInfo(150, 4500, 6500, 8000, 1),
            new LevelInfo(180, 6000, 90000, 12000, 3),
            new LevelInfo(210, 6000, 9000, 11500, 6),
            new LevelInfo(240, 7000, 10000, 13000, 9),

            new LevelInfo(240, 7000, 10000, 12500, 11),
            new LevelInfo(210, 7000, 10000, 13000, 14),
            new LevelInfo(240, 7000, 10000, 12000, 17),
            new LevelInfo(240, 7000, 10000, 12000, 20),
            new LevelInfo(270, 8000, 12000, 14000, 22),
            new LevelInfo(330, 8500, 12000, 14500, 25),
    };

    private LevelMetadata() {}

    public static LevelInfo getLevelInfo(int level) {
        return LEVELS[level];
    }
}