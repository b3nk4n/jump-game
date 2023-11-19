package de.bsautermeister.jump.services;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.badlogic.gdx.Gdx;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.example.games.basegameutils.GameHelper;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.bsautermeister.jump.Cfg;

public class GooglePlayGameServices implements GameServices {
    private final GameHelper gameHelper;
    private final static int requestCode = 1;

    private final Activity activity;

    public GooglePlayGameServices(Activity activity) {
        this.activity = activity;

        gameHelper = new GameHelper(activity, GameHelper.CLIENT_GAMES);
        gameHelper.enableDebugLog(Cfg.DEBUG_MODE);

        GameHelper.GameHelperListener gameHelperListener = new GameHelper.GameHelperListener()
        {
            @Override
            public void onSignInFailed(){ }

            @Override
            public void onSignInSucceeded(){ }
        };

        gameHelper.setup(gameHelperListener);
    }

    public void start() {
        gameHelper.onStart(activity);
    }

    public void stop() {
        gameHelper.onStop();
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data)
    {
        gameHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void signIn()
    {
        try
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    gameHelper.beginUserInitiatedSignIn();
                }
            });
        }
        catch (Exception e)
        {
            Gdx.app.log("MainActivity", "Log in failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void signOut()
    {
        try
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    gameHelper.signOut();
                }
            });
        }
        catch (Exception e)
        {
            Gdx.app.log("MainActivity", "Log out failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void rateGame()
    {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market back-stack, in order to get back to our application
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    @Override
    public void unlockAchievement(String key)
    {
        Games.Achievements.unlock(gameHelper.getApiClient(), key);
    }

    @Override
    public void showAchievements()
    {
        if (isSignedIn()) {
            activity.startActivityForResult(
                    Games.Achievements.getAchievementsIntent(
                            gameHelper.getApiClient()), requestCode);
        } else {
            signIn();
        }
    }

    @Override
    public void loadAchievementsAsync(final boolean forceReload, final LoadAchievementsCallback callback)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadAchievementsInternal(forceReload, callback);
            }
        }).start();
    }

    private Map<String, Boolean> loadAchievementsInternal(boolean forceReload, final GameServices.LoadAchievementsCallback callback) {
        final Map<String,Boolean> achievementMap = new HashMap<>();

        String errorMessage = "Unknown error";

        if (isSignedIn()) {
            Achievements.LoadAchievementsResult result = Games.Achievements.load(
                    gameHelper.getApiClient(),
                    forceReload)
                    .await(5, TimeUnit.SECONDS);
            if (result != null) {
                if (result.getStatus().isSuccess() && result.getAchievements() != null) {
                    AchievementBuffer achievementBuffer = result.getAchievements();

                    for(Achievement achievement : achievementBuffer) {
                        achievementMap.put(achievement.getAchievementId(), achievement.getState() == Achievement.STATE_UNLOCKED);
                    }
                    achievementBuffer.release();

                    if (callback != null) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                callback.success(achievementMap);
                            }
                        });
                    }

                    return achievementMap;
                } else {
                    errorMessage = result.getStatus().getStatusMessage();
                }
            }
        }

        if (callback != null) {
            final String finalErrorMessage = errorMessage;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    callback.error(finalErrorMessage);
                }
            });
        }

        return achievementMap;
    }

    @Override
    public boolean isSignedIn()
    {
        return gameHelper.isSignedIn();
    }
}
