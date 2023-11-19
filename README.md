# October Bro ![GitHub](https://img.shields.io/github/license/b3nk4n/jump-game)

_October Bro_ is a jump 'n' run game for Android using [Box2D physics](https://box2d.org/).

<p align="center">
    <img alt="App Logo" src="android/ic_launcher-playstore.png">
</p>

Once upon a time, deep in the Bavarian Forest, a little bro takes the adventurous journey to meet his lass at Munich's Beerfest. Help him to get there safe and sound!

You can download the game from [Google Play Store](https://play.google.com/store/apps/details?id=de.bsautermeister.jump), or watch the [October Bro video](https://youtu.be/P4tZJIlQ_64) on YouTube.

### Features
- 12 challenging levels
- Online leaderboards and achievements
- Shader effects

### Reviews

What did users think about this app?

> "Outstanding! Just right for waiting for the bus. Bottom up!"
>
> _Woflgang, Germany_

> "Sauguads Spuil!"
>
> _Martina, Germany_

## Acknowledgements

Thanks to Dee Yan-Key for allowing me to use [his songs](https://freemusicarchive.org/music/Dee_Yan-Key/Bavarian_Symphony) in this non-commercial game. The song is free under [CC BY-NC-SA 4.0 license](https://creativecommons.org/licenses/by-nc-sa/4.0/).

## Technical Setup

Use Java 11 to build and run the project.

## IntelliJ

### Desktop run configuration

On MacOS, the VM argument `-XstartOnFirstThread` is required to launch the project on desktop.
Setting this flag is already defined the in the `desktop:run` Gradle task. However, if you simply
run the main method of the `DesktopLaumcher` class, the auto-created IntelliJ run configuration does
not actually use that Gradle task. Instead, simply create this run configuration yourself:

1. Select _Edit configurations..._
2. Add a new _Gradle_ configuration
3. Use `desktop:run` as the command to _Run_
4. Launch the created run configuration

While this might only be strictly necessary for MacOS, it does not harm to do that for any platform,
to ensure the proper Gradle task to run the desktop project is used.

### iOS run configuration

1. Install the **MobiVM** plugin into Android Studio
2. Install Xcode
3. Create a run configuration
   1. Select _Edit configurations..._
   2. Add a new _RoboVM iOS_ configuration
   3. Select the project's _Module_
   4. Select _Simulator_ toggle (which does not need a provisioning profile)
4. Launch the run configuration

## License

This work is published under [MIT][mit] License.

[mit]: https://github.com/b3nk4n/jump-game/blob/main/LICENSE