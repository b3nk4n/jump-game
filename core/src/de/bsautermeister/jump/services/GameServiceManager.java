package de.bsautermeister.jump.services;

import com.badlogic.gdx.utils.Logger;

import java.util.HashMap;
import java.util.Map;

import de.bsautermeister.jump.Cfg;

public class GameServiceManager implements OnlineServices, PlatformDependentService {
    private static final Logger LOG = new Logger(GameServiceManager.class.getSimpleName(), Cfg.LOG_LEVEL);

    private final GameServices gameServices;

    private Map<String, Boolean> onlineAchievements = new HashMap<String, Boolean>();

    public GameServiceManager(GameServices gameServices) {
        this.gameServices = gameServices;
    }

    public void refresh() {
        refreshAchievements();
    }

    private void refreshAchievements() {
        gameServices.loadAchievementsAsync(false, new GameServices.LoadAchievementsCallback() {
            @Override
            public void success(Map<String, Boolean> achievementsResult) {
                onlineAchievements = achievementsResult;
            }

            @Override
            public void error(String message) {
                LOG.error("Failed to load online achievements: " + message);
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
        gameServices.unlockAchievement(achievementKey);
    }

    public boolean hasOnlineAchievements () {
        return onlineAchievements != null ? onlineAchievements.size() > 0 : false;
    }

    @Override
    public boolean isSupported() {
        return gameServices.isSupported();
    }


    @Override
    public void signIn() {
        gameServices.signIn();
    }

    @Override
    public boolean isSignedIn() {
        return gameServices.isSignedIn();
    }

    @Override
    public void signOut() {
        gameServices.signOut();
    }

    @Override
    public void rateGame() {
        gameServices.rateGame();
    }

    @Override
    public void showAchievements() {
        gameServices.showAchievements();
    }
}
