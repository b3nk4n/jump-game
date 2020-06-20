package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Styles;

public class AboutContent extends Table {

    private final Skin skin;
    private final Table creditContainer = new Table();

    private static final CreditEntry[] CREDITS = new CreditEntry[] {
            new CreditEntry("Developer", "Benjamin Sautermeister"),
            new CreditEntry("Graphics", "Benjamin Sautermeister", "Vanessa Kan", "Foo Bar", "Lorem Ipsum"),
            new CreditEntry("SFX", "Benjamin Sautermeister"),
            new CreditEntry("Music", "Dee Yan-Key"),
    };

    private int currentCreditIndex = 0;


    public AboutContent(AssetManager assetManager) {
        skin = assetManager.get(AssetDescriptors.Skins.UI);
        initialize();
    }

    private void initialize() {
        center();
        setFillParent(true);


        addActor(creditContainer);
        creditContainer.center().setFillParent(true);
        updateLabels(CREDITS[currentCreditIndex]);
    }

    private void updateLabels(CreditEntry entry) {
        creditContainer.clearChildren();
        creditContainer.add(new Label(entry.title, skin, Styles.Label.XXLARGE)).row();
        for (String line : entry.lines) {
            creditContainer.add(new Label(line, skin, Styles.Label.LARGE)).row();
        }
        creditContainer.pack();
        creditContainer.addAction(
                Actions.sequence(
                        Actions.show(),
                        Actions.fadeIn(0.5f, Interpolation.smooth),
                        Actions.delay(3f),
                        Actions.fadeOut(1f, Interpolation.smooth),
                        Actions.hide(),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                currentCreditIndex = ++currentCreditIndex % CREDITS.length;
                                updateLabels(CREDITS[currentCreditIndex]);
                            }
                        })
                )
        );
        pack();
    }

    private static class CreditEntry {
        public final String title;
        public final String[] lines;

        CreditEntry(String title, String... lines){
            this.title = title;
            this.lines = lines;
        }
    }
}
