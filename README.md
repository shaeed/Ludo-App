# Ludo

A classic Ludo board game for Android, built with Kotlin and Jetpack Compose.

## Features

- **2-4 Players** - Play with friends or against AI opponents
- **AI Opponents** - Three difficulty levels: Easy, Medium, and Hard
- **Configurable Rules** - Customize house rules to match your play style:
  - Enter board on 6 only (or any roll)
  - Safe zones enabled/disabled
  - Maximum consecutive 6s before losing turn
  - Pass unused dice to next player
- **Rule Presets** - Classic and Casual presets to quickly set up your preferred rules
- **Friend Mode** - Diagonal allies (Red-Yellow, Green-Blue) can't capture each other in 4-player games
- **Sound Effects** - PCM-synthesized game sounds for dice rolls, token moves, captures, reaching home, and winning
- **Shake to Roll** - Use your device's accelerometer to roll the dice (toggleable in settings)
- **Token Styles** - Choose from 5 visual styles: Classic Cone, Flat Disc, Star, Ring, and Pawn
- **Save & Resume** - Save your game in progress and continue later
- **Persistent Settings** - All preferences (sound, shake, token style, rule defaults) are saved across sessions
- **Material Design 3** - Modern UI with dynamic theming on Android 12+
- **Dark Mode** - Full support for light and dark themes
- **100% Ad-Free** - No ads, no tracking, no interruptions

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
├── audio/        # Sound effects (PCM synthesis via AudioTrack)
├── data/         # Persistence (saved games, user preferences)
├── engine/       # Game engine, move validation, rules
├── model/        # Data models (GameState, Player, Token, etc.)
├── navigation/   # Navigation graph
├── sensor/       # Shake detection for dice rolling
└── ui/
    ├── components/   # Reusable UI components (DiceView, TokenPiece, etc.)
    ├── screen/       # App screens (Home, Setup, Game, Settings, About)
    └── theme/        # Material 3 theming
```

## Contributing

Contributions are welcome! Feel free to:

- **Fork** the repository and submit a pull request
- **Open an issue** to suggest a feature or report a bug
- **Star** the repo if you enjoy the game

See [Issues](https://github.com/shaeed/Ludo-App/issues) for open tasks and feature requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
