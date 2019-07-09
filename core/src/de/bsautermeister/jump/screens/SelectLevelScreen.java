package de.bsautermeister.jump.screens;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.utils.GdxUtils;

public class SelectLevelScreen extends ScreenBase {
    private final Viewport viewport;
    private final Stage stage;

    public SelectLevelScreen(GameApp game) {
        super(game);
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        this.stage = new Stage(viewport, game.getBatch());
    }

    @Override
    public void show() {
        initialize();
    }

    private void initialize() {
        TextureAtlas atlas = getAsset(AssetDescriptors.Atlas.UI); // TODO load a background image
        Skin skin = getAsset(AssetDescriptors.Skins.UI);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        table.add(createLevelButton(skin, 1));
        table.add(createLevelButton(skin, 2));
        //table.row();
        //table.add(createLevelButton(skin, 3));
        //table.add(createLevelButton(skin, 4));

        table.pack();
        stage.addActor(table);
    }

    private Button createLevelButton(Skin skin, final int level) {
        Button playButton = new Button(skin, Styles.Button.PLAY);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playLevel(level);
            }
        });
        return playButton;
    }

    private void playLevel(int level) {
        setScreen(new GameScreen(getGame(), level));
    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen(Color.BLACK);

        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return stage;
    }
}
