package de.bsautermeister.jump.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = GameConfig.WORLD_WIDTH * 2;
		config.height = GameConfig.WORLD_HEIGHT * 2;
		new LwjglApplication(new JumpGame(), config);
	}
}
