package de.bsautermeister.jump.services;

import java.util.Map;

public interface GameServices extends OnlineServices, PlatformDependentService {
    void start();
    void stop();

    void unlockAchievement(String key);
    void loadAchievementsAsync(boolean forceReload, LoadAchievementsCallback callback);

    interface LoadAchievementsCallback {
        void success(Map<String, Boolean> achievementsResult);
        void error(String message);
    }
}
