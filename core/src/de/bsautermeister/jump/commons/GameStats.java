package de.bsautermeister.jump.commons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameStats {
    private final Preferences prefs;

    private static final String KEY_HIGHEST_FINISHED_LEVEL = "highestFinishedLevel";
    private static final String KEY_LAST_STARTED_LEVEL = "lastStartedLevel";
    private static final String KEY_LEVEL_STARTS_PREFIX = "levelStars";

    private Integer cachedHighestLevel;

    public static final GameStats INSTANCE = new GameStats();

    private GameStats() {
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
            prefs.putInteger(KEY_LEVEL_STARTS_PREFIX + level, stars);
            prefs.flush();
        }
    }

    public int getLevelStars(int level) {
        return prefs.getInteger(KEY_LEVEL_STARTS_PREFIX + level, 0);
    }
}
