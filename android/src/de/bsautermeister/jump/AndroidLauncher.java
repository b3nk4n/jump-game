package de.bsautermeister.jump;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import de.bsautermeister.jump.service.GpgsAchievementMapper;
import de.golfgl.gdxgamesvcs.GpgsClient;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;

		GpgsClient gpgsClient = new GpgsClient()
				.setGpgsAchievementIdMapper(new GpgsAchievementMapper())
				.initialize(this, false);

		initialize(new JumpGame(new AndroidGameEnv(), gpgsClient), config);
	}
}
