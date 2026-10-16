<p align="center">
  <img src="https://user-images.githubusercontent.com/59665125/182234781-e60b3a3a-1b6f-44e6-84b2-d21a12caa023.png"/>
  <br>
</p>


# About
A crossplatform game based on Emini Physics Engine (Android, J2ME and desktop Java are supported)
- Endless gameplay with procedural world generation (random combination of predefined or custom structures)
- Levels (fully predefined tracks where you need to touch the finish element)
- Touchscreen and keyboard support
- Adaptive to screen resolution
- Built-in level and structure editor
- Custom structures: You can create create your own structures using the built-in editor and load them into the game by clicking "Load Structures" in the game's main menu. The game will then use your custom structures in world generation on a par with the built-in structures. You can also quickly test structures directly from the editor
- Custom levels: The built-in editor also allows to create custom levels, which can be loaded from the "Levels" menu or directly from the editor

![photo](https://github.com/user-attachments/assets/3e4c62eb-3cab-4e38-a75e-28ae485e8d50)
![Scr_174](https://github.com/user-attachments/assets/eda7a77e-dba1-4a03-bd94-5cd6fc36dc88)
![Scr_176](https://github.com/user-attachments/assets/4ff3f507-2c74-4807-9bcb-b4ac9105c8ec)
![Scr_178](https://github.com/user-attachments/assets/31e28575-43cc-470e-a34b-0570e1919819)
![Scr_181](https://github.com/user-attachments/assets/37a9b5fe-5aa7-4150-afec-24bfd4c6343b)

# Build
`git clone https://github.com/vipaoL/mobapp-game.git --recurse-submodules`
## Command-line (Apache Ant)
### Desktop
```
cd desktop
ant jar
```
Produces a runnable fat jar `desktop/build/libs/mobapp-game-desktop-<version>.jar` (Main-Class is set, `java -jar` works). Dependencies (jopt-simple, oshi-core, jna) are downloaded from Maven Central into `desktop/libs/`; use `-Dmaven.repo=<url>` to use a mirror, `ant run` to start the game.
### Android
Requires JDK 17+ and Android SDK with `platforms;android-34` and `build-tools;36.0.0`:
```
sdkmanager "platforms;android-34" "build-tools;36.0.0"
cd android
ant debug     # signed debug APK (applicationId suffix .dev) -> build/outputs/apk/debug/
ant release   # unsigned release APK -> build/outputs/apk/release/
```
The SDK path is taken from `sdk.dir` in `android/local.properties` or from the `ANDROID_HOME`/`ANDROID_SDK_ROOT` environment variable.
### J2ME
See `j2me/build.sh`
## IDE
### Desktop
Open `desktop/` folder in IntelliJ IDEA as a plain Java module (run `ant deps` first to download the libraries into `desktop/libs/`)
### Android
Open `android/app/src/main/java` folder in Android Studio for editing; building is done with Ant (see above)
### J2ME
Use IntelliJ IDEA integration in KEmulator nnmod to open the project (`j2me/` folder is the project root)
## CI
See `.github/workflows/`
