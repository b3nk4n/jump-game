package de.bsautermeister.jump.utils;

import de.bsautermeister.jump.Cfg;

public final class LevelUtils {
    private LevelUtils() {}

    public static int levelToPage(int level) {
        int page = level / Cfg.LEVELS_PER_PAGE + 1;
        return Math.min(page, Cfg.LEVEL_PAGES);
    }
}
