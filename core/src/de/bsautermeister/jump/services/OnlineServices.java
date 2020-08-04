package de.bsautermeister.jump.services;

public interface OnlineServices {
    void signIn();
    boolean isSignedIn();
    void signOut();
    void rateGame();
    void showAchievements();
}
