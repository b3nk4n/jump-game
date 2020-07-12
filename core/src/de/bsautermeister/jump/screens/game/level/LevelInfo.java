package de.bsautermeister.jump.screens.game.level;

public class LevelInfo {
    private final int time;
    private final int requiredScoreOneStar;
    private final int requiredScoreTwoStars;
    private final int requiredScoreThreeStars;

    private final int requiredStarsToUnlock;

    public LevelInfo(int time, int requiredScoreOneStar, int requiredScoreTwoStars, int requiredScoreThreeStars, int requiredStarsToUnlock) {
        this.time = time;
        this.requiredScoreOneStar = requiredScoreOneStar;
        this.requiredScoreTwoStars = requiredScoreTwoStars;
        this.requiredScoreThreeStars = requiredScoreThreeStars;
        this.requiredStarsToUnlock = requiredStarsToUnlock;
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
