package de.bsautermeister.jump;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSBundle;
import org.robovm.apple.foundation.NSDictionary;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.uikit.UIApplication;

import de.golfgl.gdxgamesvcs.NoGameServiceClient;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.orientationLandscape = true;
        config.orientationPortrait = false;

        return new IOSApplication(new JumpGame(new GameEnv() {
            @Override
            public String getVersion() {
                NSDictionary<NSString, ?> infoDictionary = NSBundle.getMainBundle().getInfoDictionary();
                return infoDictionary.get(new NSString("CFBundleShortVersionString")).toString();
            }
        }, new NoGameServiceClient()), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
