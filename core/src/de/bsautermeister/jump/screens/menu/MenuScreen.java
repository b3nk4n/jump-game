package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.Gdx;
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
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.audio.MusicPlayer;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.game.GameScreen;
import de.bsautermeister.jump.utils.GdxUtils;

public class MenuScreen extends ScreenBase {
    private final Viewport viewport;
    private final Stage stage;

    public MenuScreen(GameApp game) {
        super(game);
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        this.stage = new Stage(viewport, game.getBatch());
        this.stage.setDebugAll(Cfg.DEBUG_MODE);
    }

    @Override
    public void show() {
        initialize();

        // use default BACK button handling (exit game)
        Gdx.input.setCatchBackKey(false);

        getGame().getMusicPlayer().selectMusic(AssetPaths.Music.MENU_AUDIO);
        getGame().getMusicPlayer().setVolume(MusicPlayer.MAX_VOLUME, true);
        getGame().getMusicPlayer().playFromBeginning();
    }

    private void initialize() {
        TextureAtlas atlas = getAsset(AssetDescriptors.Atlas.UI); // TODO load a background image
        Skin skin = getAsset(AssetDescriptors.Skins.UI);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        Button playButton = new Button(skin, Styles.Button.PLAY);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playNewGame();
            }
        });
        table.add(playButton).pad(8f);

        if (JumpGame.hasSavedData()) {
            Button continueButton = new Button(skin, Styles.Button.CONTINUE);
            continueButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    continueGame();
                }
            });
            table.add(continueButton).pad(8f);
        }

        table.pack();
        stage.addActor(table);
    }

    private void playNewGame() {
        setScreen(new SelectLevelScreen(getGame(), 1));
    }

    private void continueGame() {
        setScreen(new GameScreen(getGame()));
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
