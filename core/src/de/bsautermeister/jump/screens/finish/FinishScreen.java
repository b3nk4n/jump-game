package de.bsautermeister.jump.screens.finish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

    private Stage stage;

    public FinishScreen(GameApp game) {
        super(game);
        batch = game.getBatch();
        this.uiViewport = new FitViewport(Cfg.UI_WIDTH, Cfg.UI_HEIGHT);
    }

    @Override
    public void show() {
        insideTentRenderer = new InsideTentRenderer(getGame().getAssetManager());

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
        content.add(scoreLabel).left().padLeft(sidePad*2).padBottom(topBottom);

        Label scoreValueLabel = new Label("001234", skin, Styles.Label.LARGE);
        content.add(scoreValueLabel).right().padRight(sidePad*2).padBottom(topBottom).row();

        Label timeLabel = new Label("Time", skin, Styles.Label.LARGE);
        content.add(timeLabel).left().padLeft(sidePad*2).padBottom(topBottom);

        Label timeValueLabel = new Label("012", skin, Styles.Label.LARGE);
        content.add(timeValueLabel).right().padRight(sidePad*2).padBottom(topBottom).row();

        Label totalLabel = new Label("Total", skin, Styles.Label.XXLARGE);
        content.add(totalLabel).left().padLeft(sidePad).padBottom(topBottom);

        Label totalValueLabel = new Label("123456", skin, Styles.Label.XXLARGE);
        content.add(totalValueLabel).right().padRight(sidePad).padBottom(topBottom).row();

        final float starPad = 8f;
        TextureRegion fullStartRegion = atlas.findRegion(RegionNames.UI_STAR_FULL);
        TextureRegion emptyStartRegion = atlas.findRegion(RegionNames.UI_STAR_EMPTY);
        Table starTable = new Table();
        Image star1 = new Image(fullStartRegion);
        starTable.add(star1).padRight(starPad);
        Image star2 = new Image(fullStartRegion);
        starTable.add(star2).padTop(2 * starPad).padLeft(starPad).padRight(starPad);
        Image star3 = new Image(emptyStartRegion);
        starTable.add(star3).padLeft(starPad);
        content.add(starTable).colspan(2).padBottom(148f);

        content.pack();
        return content;
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
