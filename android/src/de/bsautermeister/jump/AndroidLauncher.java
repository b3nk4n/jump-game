package de.bsautermeister.jump;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import de.bsautermeister.jump.service.Ads;
import de.bsautermeister.jump.service.GpgsAchievementMapper;
import de.bsautermeister.jump.service.InterstitialAdMobService;
import de.bsautermeister.jump.service.PlayStoreRateService;
import de.bsautermeister.jump.services.AdService;
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

		AdService adService = new InterstitialAdMobService(
				this, Ads.INTERSTITIAL_LEVEL_COMPLETED_AD_UNIT_ID,
				Cfg.DEBUG_ADS, Cfg.DEBUG_ADS_TEST_DEVICE_HASHED_ID);
		adService.initialize();

		initialize(
				new JumpGame(
						new AndroidGameEnv(),
						gpgsClient,
						new PlayStoreRateService(this),
						adService),
				config);
	}
}
