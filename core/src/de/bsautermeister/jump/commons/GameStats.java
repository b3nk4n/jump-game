package de.bsautermeister.jump.commons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameStats {
    private final Preferences prefs;

    public static final String KEY_HIGHEST_FINISHED_LEVEL = "highestFinishedLevel";
    public static final String KEY_LAST_STARTED_LEVEL = "lastStartedLevel";

    private Integer cachedHighestLevel;

    public GameStats() {
        this.prefs = Gdx.app.getPreferences(getClass().getSimpleName());
    }

    public void setHighestFinishedLevel(int level) {
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

    public int getLastStartedLevel() {
        return prefs.getInteger(KEY_LAST_STARTED_LEVEL, 1);
    }

    public void setLastStartedLevel(int level) {
        prefs.putInteger(KEY_LAST_STARTED_LEVEL, level);
        prefs.flush();
    }
}
