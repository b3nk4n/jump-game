package de.bsautermeister.jump.screens.finish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.menu.MenuScreen;
import de.bsautermeister.jump.screens.menu.controls.AnimatedLabel;
import de.bsautermeister.jump.screens.transition.ScaleScreenTransition;
import de.bsautermeister.jump.utils.GdxUtils;

public class FinishScreen extends ScreenBase {

    private final SpriteBatch batch;
    private final Viewport uiViewport;
    private InsideTentRenderer insideTentRenderer;

    private final int score;
    private final int time;
    private final int totalScore;

    private Sound ploppSound;

    private Stage stage;

    public FinishScreen(GameApp game, int score, int time, int totalScore) {
        super(game);
        batch = game.getBatch();
        this.uiViewport = new FitViewport(Cfg.UI_WIDTH, Cfg.UI_HEIGHT);

        this.score = score;
        this.time = time;
        this.totalScore = totalScore;
    }

    @Override
    public void show() {
        insideTentRenderer = new InsideTentRenderer(getAssetManager());
        ploppSound = getAssetManager().get(AssetDescriptors.Sounds.PLOPP);

        stage = new Stage(uiViewport, batch);
        stage.setDebugAll(Cfg.DEBUG_MODE);
        stage.addActor(builtId(getAssetManager()));
    }

    private Table builtId(AssetManager assetManager) {
        Skin skin = assetManager.get(AssetDescriptors.Skins.UI);
        TextureAtlas atlas = assetManager.get(AssetDescriptors.Atlas.UI);

        Table content = new Table();
        content.center();
        content.setFillParent(true);

        AnimatedLabel title = new AnimatedLabel(skin, Styles.Label.TITLE, Float.MAX_VALUE, 11)
                .typeText("O'zapft is!");
        content.add(title)
                .colspan(2)
                .pad(Cfg.TITLE_PAD)
                .padBottom(Cfg.TITLE_PAD / 2)
                .row();

        final float sidePad = 64f;
        final float topBottom = 8f;
        Label scoreLabel = new Label("Score", skin, Styles.Label.LARGE);
        scoreLabel.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(1f),
                Actions.show()
        ));
        content.add(scoreLabel).left().padLeft(sidePad*2).padBottom(topBottom);

        Label scoreValueLabel = new Label(String.format("%06d", score), skin, Styles.Label.LARGE);
        scoreValueLabel.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(1f),
                Actions.show()
        ));
        content.add(scoreValueLabel).right().padRight(sidePad*2).padBottom(topBottom).row();

        Label timeLabel = new Label("Time", skin, Styles.Label.LARGE);
        timeLabel.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(1.5f),
                Actions.show()
        ));
        content.add(timeLabel).left().padLeft(sidePad*2).padBottom(topBottom);

        Label timeValueLabel = new Label(String.format("%03d", time), skin, Styles.Label.LARGE);
        timeValueLabel.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(1.5f),
                Actions.show()
        ));
        content.add(timeValueLabel).right().padRight(sidePad*2).padBottom(topBottom).row();

        Label totalLabel = new Label("Total", skin, Styles.Label.XXLARGE);
        Container<Label> totalLabelContainer = wrapTransformContainer(totalLabel);
        totalLabelContainer.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(2.0f),
                Actions.show(),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.swingOut)
        ));
        content.add(totalLabelContainer).left().padLeft(sidePad).padBottom(topBottom);

        Label totalValueLabel = new Label(String.format("%06d", totalScore), skin, Styles.Label.XXLARGE);
        Container<Label> totalValueLabelContainer = wrapTransformContainer(totalValueLabel);
        totalValueLabelContainer.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(2.0f),
                Actions.show(),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.swingOut)
        ));
        content.add(totalValueLabelContainer).right().padRight(sidePad).padBottom(topBottom).row();

        final int numStars = MathUtils.random(1, 3);
        final float starPad = 8f;
        TextureRegion fullStartRegion = atlas.findRegion(RegionNames.UI_STAR_FULL);
        TextureRegion emptyStartRegion = atlas.findRegion(RegionNames.UI_STAR_EMPTY);
        Table starTable = new Table();
        Image star1 = new Image(numStars >= 1 ? fullStartRegion : emptyStartRegion);
        Container<Image> star1Container = wrapTransformContainer(star1);
        star1Container.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(3.0f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        if (numStars >= 1) {
                            ploppSound.play(0.66f, 0.8f, 0f);
                        }
                    }
                }),
                Actions.show(),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.swingOut)
        ));
        starTable.add(star1Container).padRight(starPad);
        Image star2 = new Image(numStars >= 2 ? fullStartRegion : emptyStartRegion);
        Container<Image> star2Container = wrapTransformContainer(star2);
        star2Container.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(3.25f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        if (numStars >= 2) {
                            ploppSound.play(0.66f, 0.9f, 0f);
                        }
                    }
                }),
                Actions.show(),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.swingOut)
        ));
        starTable.add(star2Container).padTop(2 * starPad).padLeft(starPad).padRight(starPad);
        Image star3 = new Image(numStars >= 3 ? fullStartRegion : emptyStartRegion);
        Container<Image> star3Container = wrapTransformContainer(star3);
        star3Container.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(3.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        if (numStars >= 3) {
                            ploppSound.play(0.66f, 1.0f, 0f);
                        }
                    }
                }),
                Actions.show(),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.swingOut)
        ));
        starTable.add(star3Container).padLeft(starPad);
        content.add(starTable).colspan(2).row();

        Label pushLabel = new Label("Tap to continue", skin, Styles.Label.DEFAULT);
        pushLabel.addAction(Actions.sequence(
                Actions.hide(),
                Actions.delay(4.0f),
                Actions.repeat(Integer.MAX_VALUE, Actions.sequence(
                        Actions.show(),
                        Actions.delay(1.0f),
                        Actions.hide(),
                        Actions.delay(1.0f)
                ))
        ));
        content.add(pushLabel).center().colspan(2).padTop(36f).padBottom(36f);

        content.pack();
        return content;
    }

    private <T extends Actor> Container<T> wrapTransformContainer(T actor) {
        Container<T> container = new Container<>(actor);
        container.pack();
        container.setScale(0f);
        container.setOrigin(Align.center);
        container.setTransform(true);
        return container;
    }

    @Override
    public void render(float delta) {
        insideTentRenderer.update(delta);

        if (Gdx.input.isTouched()) {
            getGame().getForegroundMusic().fadeOutStop();
            setScreen(new MenuScreen(getGame(), true), new ScaleScreenTransition(
                    Cfg.SCREEN_TRANSITION_TIME, Interpolation.smooth, true));
        }

        GdxUtils.clearScreen(Color.BLACK);

        insideTentRenderer.render(batch);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        insideTentRenderer.resize(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        stage.dispose();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return stage;
    }
}
