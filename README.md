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

### iOS simulator run configuration

1. Install the **MobiVM** plugin into Android Studio
2. Install Xcode
3. Create a run configuration
   1. Select _Edit configurations..._
   2. Add a new _RoboVM iOS_ configuration
   3. Select the project's _Module_
   4. Select _Simulator_ toggle (which does not need a provisioning profile)
4. Launch the run configuration

### Running on a physical iOS device

1. Run Xcode
2. Create an empty iOS app, using the bundle identifier as specified in `robovm.properties`
   as `app.id`
3. Under _Settings > Accounts_, sign in with your Apple ID
4. Connect your device (iPhone) using a cable
5. In Xcode, select your device in the top toolbar as destination target
6. In your app projects **Signing & Capabilities**, use the following settings:
   - Enable **Automatically manage signing**
   - Select your Apple ID (Personal Team) as **Team**
   - As **Bundle Identifier**, ensure it is correct as described in step 2
   - Xcode should indicate the **Provisioning Profile** and **Signing Certificate** below
7. Create a run configuration similar to described above. But use **Attached Device** and select
   the created **Signing Identity** (equal to the Xcode _Signing Certificate_ from step 6)


## Troubleshooting

### Using texture wrapping for non-power-of-two textures on iOS

This is unfortunately not supported, and in line with the OpenGL ES spec. While this works fine on
Desktop or Android, a render operation using e.g. `GLTexture#setWrap(TextureWrap u, TextureWrap v)`
with `Texture.TextureWrap.Repeat` using a non-power-of-two texture, is simply ignored.
See more details in [LibGDX Issue #3178](https://github.com/libgdx/libgdx/issues/3178)

### Changes in the logo or icon are not reflected in the installed iOS app during development

At least when changes in the Logo (`Logo.imageset`) are made, these are not directly reflected after
reinstalling or even deleting the app. These might be cached by the phone internally.
Simply rebooting the iPhone resolves this and makes these changes visible.

## License

This work is published under [MIT][mit] License.

[mit]: https://github.com/b3nk4n/jump-game/blob/main/LICENSE