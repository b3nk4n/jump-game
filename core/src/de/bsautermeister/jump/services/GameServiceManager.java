package de.bsautermeister.jump.services;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;

import java.util.HashMap;
import java.util.Map;

import de.bsautermeister.jump.Cfg;
import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.MockGameServiceClient;
import de.golfgl.gdxgamesvcs.NoGameServiceClient;
import de.golfgl.gdxgamesvcs.achievement.IAchievement;
import de.golfgl.gdxgamesvcs.achievement.IFetchAchievementsResponseListener;

public class GameServiceManager {
    private static final Logger LOG = new Logger(GameServiceManager.class.getSimpleName(), Cfg.LOG_LEVEL);

    private final IGameServiceClient gameServiceClient;

    private final Map<String, Boolean> onlineAchievements = new HashMap<String, Boolean>();

    public GameServiceManager(IGameServiceClient gameServiceClient) {
        this.gameServiceClient = gameServiceClient;
    }

    public void refresh() {
        refreshAchievements();
    }

    private void refreshAchievements() {
        gameServiceClient.fetchAchievements(new IFetchAchievementsResponseListener() {
            @Override
            public void onFetchAchievementsResponse(Array<IAchievement> achievements) {
                for (String achievementKey : AchievementKeys.ALL_KEYS) {
                    for (IAchievement achievement : achievements) {
                        // Check via IAchievement#isAchievementId is needed to respect the key mappings
                        if (achievement.isAchievementId(achievementKey)) {
                            onlineAchievements.put(achievementKey, achievement.isUnlocked());
                        }
                    }
                }
            }
        });
    }

    public void checkAndUnlockBeerAchievement(int totalDrunkenBeers) {
        if (checkAchievementCanBeUnlocked(AchievementKeys.BEERS_25, totalDrunkenBeers, 25)) {
            unlockAchievement(AchievementKeys.BEERS_25);
        } else if (checkAchievementCanBeUnlocked(AchievementKeys.BEERS_50, totalDrunkenBeers, 50)) {
            unlockAchievement(AchievementKeys.BEERS_50);
        } else if (checkAchievementCanBeUnlocked(AchievementKeys.BEERS_100, totalDrunkenBeers, 100)) {
            unlockAchievement(AchievementKeys.BEERS_100);
        }
    }

    public void checkAndUnlockKillSequelAchievement(int currentKillSequel) {
        if (checkAchievementCanBeUnlocked(AchievementKeys.KILL_3, currentKillSequel, 3)) {
            unlockAchievement(AchievementKeys.KILL_3);
        } else if (checkAchievementCanBeUnlocked(AchievementKeys.KILL_5, currentKillSequel, 5)) {
            unlockAchievement(AchievementKeys.KILL_5);
        }
    }

    private boolean checkAchievementCanBeUnlocked(String achievementKey, int currentValue, int targetValue) {
        return currentValue >= targetValue &&
                onlineAchievements.containsKey(achievementKey) &&
                !onlineAchievements.get(achievementKey);
    }

    private void unlockAchievement(String achievementKey) {
        onlineAchievements.put(achievementKey, true);
        gameServiceClient.unlockAchievement(achievementKey);
    }

    public boolean hasOnlineAchievements () {
        return onlineAchievements != null ? onlineAchievements.size() > 0 : false;
    }

    public boolean isSupported() {
        return !(gameServiceClient instanceof MockGameServiceClient || gameServiceClient instanceof NoGameServiceClient);
    }

    public void showAchievements() {
        try {
            gameServiceClient.showAchievements();
        } catch (GameServiceException.NoSessionException nse) {
            LOG.info("Signing in");
            gameServiceClient.logIn();
        } catch (GameServiceException e) {
            LOG.error("Showing achievements failed", e);
        }
    }
}
