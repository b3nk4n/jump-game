package de.bsautermeister.jump;

import de.bsautermeister.jump.game.BuildConfig;

public class AndroidGameEnv implements GameEnv {
    @Override
    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
