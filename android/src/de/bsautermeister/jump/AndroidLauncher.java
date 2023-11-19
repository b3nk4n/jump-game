package de.bsautermeister.jump;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import de.bsautermeister.jump.game.BuildConfig;
import de.bsautermeister.jump.services.GameServices;
import de.bsautermeister.jump.services.GooglePlayGameServices;

public class AndroidLauncher extends AndroidApplication {
	private GameServices gameServices;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		this.gameServices = new GooglePlayGameServices(this);
		initialize(new JumpGame(gameServices, new AndroidGameEnv()), config);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		gameServices.start();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		gameServices.stop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		GooglePlayGameServices googlePlayGameServices = (GooglePlayGameServices) gameServices;
		googlePlayGameServices.handleActivityResult(requestCode, resultCode, data);
	}
}
