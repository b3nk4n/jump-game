package de.bsautermeister.jump.screens.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.I18NBundle;

import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.assets.Language;
import de.bsautermeister.jump.assets.Styles;
import de.bsautermeister.jump.screens.menu.controls.AnimatedLabel;

public class AboutContent extends Table {

    public static final String TYPE = AboutContent.class.getSimpleName();

    public static final float TEXT_ANIMATION_DURATION = 7.5f;

    private final Skin skin;
    private final Table creditContainer = new Table();

    private final CreditEntry[] credits;

    private int currentCreditIndex = 0;


    public AboutContent(AssetManager assetManager) {
        skin = assetManager.get(AssetDescriptors.Skins.UI);
        I18NBundle i18n = assetManager.get(AssetDescriptors.I18n.LANGUAGE);

        credits = new CreditEntry[] {
                new CreditEntry(i18n.get(Language.DEVELOPER), "Benjamin Kan"),
                // https://hdst.itch.io/fox (Copyright/Attribution Notice: HDST or link to this page)
                new CreditEntry(i18n.get(Language.GRAPHICS), "Benjamin Kan", "Vanessa Kan", "vnitti", "sanctumpixel", "Elthen's Pixel Art Shop", "HDST"),
                new CreditEntry(i18n.get(Language.ANIMATIONS), "Benjamin Kan"),
                new CreditEntry(i18n.get(Language.VOICE), "Lukas Woehrl"),
                new CreditEntry(i18n.get(Language.SFX), "Benjamin Kan"),
                new CreditEntry(i18n.get(Language.MUSIC), "Dee Yan-Key"),
        };

        initialize();
    }

    private void initialize() {
        center();
        setFillParent(true);

        addActor(creditContainer);
        creditContainer.center().setFillParent(true);
        updateLabels(credits[currentCreditIndex]);
    }

    private void updateLabels(CreditEntry entry) {
        creditContainer.clearChildren();
        creditContainer.add(
                new AnimatedLabel(skin, Styles.Label.XXLARGE, TEXT_ANIMATION_DURATION, 64)
                        .typeText(entry.title))
                .pad(32f)
                .row();
        int i = 0;
        for (String line : entry.lines) {
            creditContainer.add(
                    new AnimatedLabel(skin, Styles.Label.LARGE, TEXT_ANIMATION_DURATION, 64)
                            .typeText(line, 0.75f + i++ * 0.25f))
                    .row();
        }
        creditContainer.pack();
        creditContainer.addAction(
                Actions.sequence(
                        Actions.show(),
                        Actions.delay(TEXT_ANIMATION_DURATION),
                        Actions.hide(),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                currentCreditIndex = ++currentCreditIndex % credits.length;
                                updateLabels(credits[currentCreditIndex]);
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
