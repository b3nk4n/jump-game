package de.bsautermeister.jump.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.services.NoopGameServices;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "October Bro";
		config.width = Cfg.WINDOW_WIDTH;
		config.height = Cfg.WINDOW_HEIGHT;
		new LwjglApplication(new JumpGame(new NoopGameServices()), config);
	}
}
