package de.bsautermeister.jump.services;

public class NoopGameServices implements GameServices {
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void unlockAchievement(String key) {

    }

    @Override
    public void loadAchievementsAsync(boolean forceReload, LoadAchievementsCallback callback) {
        callback.error("Not implemented");
    }

    @Override
    public void signIn() {

    }

    @Override
    public boolean isSignedIn() {
        return false;
    }

    @Override
    public void signOut() {

    }

    @Override
    public void rateGame() {

    }

    @Override
    public void showAchievements() {

    }

    @Override
    public boolean isSupported() {
        return false;
    }
}
