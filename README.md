# Ludo

A classic Ludo board game for Android, built with Kotlin and Jetpack Compose.

## Features

- **2-4 Players** - Play with friends or against AI opponents
- **AI Opponents** - Three difficulty levels: Easy, Medium, and Hard
- **Configurable Rules** - Customize house rules to match your play style:
  - Enter board on 6 only (or any roll)
  - Safe zones enabled/disabled
  - Maximum consecutive 6s before losing turn
- **Shake to Roll** - Use your device's accelerometer to roll the dice
- **Material Design 3** - Modern UI with dynamic theming on Android 12+
- **Dark Mode** - Full support for light and dark themes

## Screenshots

*Coming soon*

## Requirements

- Android 8.0 (API 26) or higher

## Building

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Release build
./gradlew assembleRelease
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Testing

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM with ViewModel
- **Navigation:** Jetpack Navigation Compose
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 36

## Project Structure

```
app/src/main/java/com/shaeed/ludo/
├── ai/           # AI strategies (Easy, Medium, Hard)
├── engine/       # Game engine, move validation, rules
├── model/        # Data models (GameState, Player, Token, etc.)
├── navigation/   # Navigation graph
├── sensor/       # Shake detection for dice rolling
└── ui/
    ├── components/   # Reusable UI components
    ├── screen/       # App screens (Home, Setup, Game, Settings)
    └── theme/        # Material 3 theming
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
