package de.bsautermeister.jump.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.services.NoopGameServices;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("October Bro");
		config.setWindowedMode(Cfg.WINDOW_WIDTH, Cfg.WINDOW_HEIGHT);
		new Lwjgl3Application(new JumpGame(new NoopGameServices(), () -> "Desktop version"), config);
	}
}
