package de.bsautermeister.jump.commons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class JumpGameStats {
    /**
     * The preferences under Linux are stored under the ~/.prefs/JumpGameStats directory
     */
    private final Preferences prefs;

    private static final String KEY_PREFIX = "de.bsautermeister.jump.";

    private static final String KEY_HIGHEST_FINISHED_LEVEL = KEY_PREFIX + "highestFinishedLevel";
    private static final String KEY_LAST_STARTED_LEVEL = KEY_PREFIX + "lastStartedLevel";
    private static final String KEY_LEVEL_STARS_PREFIX = KEY_PREFIX + "levelStars";
    private static final String KEY_TOTAL_STARS_PREFIX = KEY_PREFIX + "totalStars";

    // achievement related
    private static final String KEY_TOTAL_BEERS = KEY_PREFIX + "totalBeers";

    private Integer cachedHighestLevel;
    private Integer cachedTotalStars;

    private Integer cachedTotalBeers;

    public static final JumpGameStats INSTANCE = new JumpGameStats();

    private JumpGameStats() {
        this.prefs = Gdx.app.getPreferences(getClass().getSimpleName());
    }

    public void updateHighestFinishedLevel(int level) {
        int prevHighestLevel = getHighestFinishedLevel();
        if (level > prevHighestLevel) {
            cachedHighestLevel = level;
            prefs.putInteger(KEY_HIGHEST_FINISHED_LEVEL, level);
            prefs.flush();
        }
    }

    public int getHighestFinishedLevel() {
        if (cachedHighestLevel == null) {
            cachedHighestLevel = prefs.getInteger(KEY_HIGHEST_FINISHED_LEVEL);
        }
        return cachedHighestLevel;
    }

    public void updateLastStartedLevel(int level) {
        prefs.putInteger(KEY_LAST_STARTED_LEVEL, level);
        prefs.flush();
    }

    public int getLastStartedLevel() {
        return prefs.getInteger(KEY_LAST_STARTED_LEVEL, 1);
    }

    public void updateLevelStars(int level, int stars) {
        int currentStars = getLevelStars(level);
        if (stars > currentStars) {
            prefs.putInteger(KEY_LEVEL_STARS_PREFIX + level, stars);
            int oldTotalStars = getTotalStars();
            cachedTotalStars = oldTotalStars + stars - currentStars;
            prefs.putInteger(KEY_TOTAL_STARS_PREFIX, cachedTotalStars);
            prefs.flush();
        }
    }

    public int getLevelStars(int level) {
        return prefs.getInteger(KEY_LEVEL_STARS_PREFIX + level, 0);
    }

    public int getTotalStars() {
        if (cachedTotalStars == null) {
            cachedTotalStars = prefs.getInteger(KEY_TOTAL_STARS_PREFIX);
        }
        return cachedTotalStars;
    }

    public int incrementTotalBeers() {
        int count = getTotalBeers();
        int newCount = count + 1;
        cachedTotalBeers = newCount;
        prefs.putInteger(KEY_TOTAL_BEERS, newCount);
        prefs.flush();
        return newCount;
    }

    public int getTotalBeers() {
        if (cachedTotalBeers == null) {
            cachedTotalBeers = prefs.getInteger(KEY_TOTAL_BEERS, 0);
        }
        return cachedTotalBeers;
    }
}
