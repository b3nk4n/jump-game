package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.commons.JumpGameStats;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.finish.FinishScreen;
import de.bsautermeister.jump.screens.game.level.LevelInfo;
import de.bsautermeister.jump.screens.game.level.LevelMetadata;
import de.bsautermeister.jump.screens.menu.MenuScreen;
import de.bsautermeister.jump.screens.transition.ScaleScreenTransition;
import de.bsautermeister.jump.services.AdService;

public class GameScreen extends ScreenBase {

    private GameController controller;
    private GameRenderer renderer;
    private GameSoundEffects soundEffects;
    private final AdService adService;

    private final int level;
    private final FileHandle gameToResume;

    private final GameScreenCallbacks callbacks = new GameScreenCallbacks() {
        @Override
        public void start() {
            if (adService.isSupported()) {
                adService.load();
            }
        }

        @Override
        public void success(int level, Vector2 goalCenterPosition) {
            int score = controller.getScore();
            int ttl = controller.getTimeToLive();
            int totalScore = score + ttl * Cfg.SCORE_PER_SEC;
            LevelInfo levelInfo = LevelMetadata.getLevelInfo(level);
            int stars = levelInfo.getStarsForScore(totalScore);

            JumpGameStats.INSTANCE.updateHighestFinishedLevel(level);
            JumpGameStats.INSTANCE.updateLevelStars(level, stars);

            if (adService.isSupported()) {
                adService.show();
            }

            setScreen(new FinishScreen(getGame(), level, score, ttl, totalScore, stars),
                    new ScaleScreenTransition(Cfg.SCREEN_TRANSITION_TIME, Interpolation.smooth,
                            true, goalCenterPosition));
        }

        @Override
        public void backToMenu(Vector2 clickScreenPosition) {
            getGame().getForegroundMusic().fadeOutStop();
            setScreen(new MenuScreen(getGame(), true), new ScaleScreenTransition(
                    Cfg.SCREEN_TRANSITION_TIME, Interpolation.smooth, true,
                    clickScreenPosition));
        }

        @Override
        public void reportKillSequelFinished(int count) {
            if (level == 0) {
                // ignore stats in tutorial
                return;
            }

            JumpGame.getGameServiceManager().checkAndUnlockKillSequelAchievement(count);
        }

        @Override
        public void reportDrunkBeer() {
            if (level == 0) {
                // ignore stats in tutorial
                return;
            }

            int count = JumpGameStats.INSTANCE.incrementTotalBeers();
            JumpGame.getGameServiceManager().checkAndUnlockBeerAchievement(count);
        }

        @Override
        public void gameOver() {
            if (adService.isSupported()
                    // not always show an ad after game over to make it a little less annoying
                    && MathUtils.random() < 0.33f) {
                adService.show();
            }
        }
    };

    /**
     * Start new game at given level.
     */
    public GameScreen(GameApp game, int level) {
        super(game);
        this.level = level;
        this.gameToResume = null;
        this.adService = ((JumpGame) game).getAdService();
        JumpGameStats.INSTANCE.updateLastStartedLevel(level);
    }

    /**
     * Resume game of saved level.
     */
    public GameScreen(GameApp game) {
        super(game);
        this.level = JumpGameStats.INSTANCE.getLastStartedLevel();
        this.gameToResume = JumpGame.getSavedDataHandle();
        this.adService = ((JumpGame) game).getAdService();
    }

    @Override
    public void show() {
        super.show();

        soundEffects = new GameSoundEffects(getAssetManager());
        controller = new GameController(callbacks, getGame(), soundEffects,
                level, gameToResume);
        renderer = new GameRenderer(getBatch(), getAssetManager(), controller,
                getGame().getFrameBufferManager());

        JumpGame.deleteSavedData();

        // enable phones BACK button
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void pause() {
        controller.save();
    }

    @Override
    public void render(float delta) {
        render(delta, false);
    }

    @Override
    public void render(float delta, boolean usedInFbo) {
        controller.update(delta);
        renderer.render(delta, usedInFbo);
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        controller.dispose();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return renderer.getInputProcessor();
    }
}
