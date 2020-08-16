package de.bsautermeister.jump.screens.game.level;

public class LevelInfo {
    private final int time;
    private final int requiredScoreOneStar;
    private final int requiredScoreTwoStars;
    private final int requiredScoreThreeStars;

    private final int requiredStarsToUnlock;

    private final boolean tutorial;

    public LevelInfo(int time, int requiredScoreOneStar, int requiredScoreTwoStars, int requiredScoreThreeStars, int requiredStarsToUnlock) {
        this(time, requiredScoreOneStar, requiredScoreTwoStars, requiredScoreThreeStars, requiredStarsToUnlock, false);
    }

    public LevelInfo(int time, int requiredScoreOneStar, int requiredScoreTwoStars, int requiredScoreThreeStars, int requiredStarsToUnlock, boolean tutorial) {
        this.time = time;
        this.requiredScoreOneStar = requiredScoreOneStar;
        this.requiredScoreTwoStars = requiredScoreTwoStars;
        this.requiredScoreThreeStars = requiredScoreThreeStars;
        this.requiredStarsToUnlock = requiredStarsToUnlock;
        this.tutorial = tutorial;
    }

    public static LevelInfo tutorial() {
        return new LevelInfo(999, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, true);
    }

    public int getTime() {
        return time;
    }

    public int getRequiredScoreOneStar() {
        return requiredScoreOneStar;
    }

    public int getRequiredScoreTwoStars() {
        return requiredScoreTwoStars;
    }

    public int getRequiredScoreThreeStars() {
        return requiredScoreThreeStars;
    }

    public int getRequiredScoreNStars(int stars) {
        switch (stars) {
            case 0:
                return 0;
            case 1:
                return getRequiredScoreOneStar();
            case 2:
                return getRequiredScoreTwoStars();
            case 3:
            default:
                return getRequiredScoreThreeStars();
        }
    }

    public boolean isTutorial() {
        return tutorial;
    }

    public int getStarsForScore(int score) {
        for (int stars = 3; stars >= 0; --stars) {
            if (score >= getRequiredScoreNStars(stars)) {
                return stars;
            }
        }
        return 0;
    }

    public int getRequiredStarsToUnlock() {
        return requiredStarsToUnlock;
    }
}
