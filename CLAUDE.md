# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android Ludo game app built with Kotlin and Jetpack Compose, targeting Android 8.0+ (SDK 26–36). Single-module Gradle project using Material Design 3 and dynamic theming.

## Build & Development Commands

On Windows, use `gradlew.bat` instead of `./gradlew`.

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install debug APK on connected device/emulator
./gradlew assembleRelease        # Build release APK (minify currently disabled)
```

## Testing

```bash
./gradlew testDebugUnitTest              # Run unit tests
./gradlew connectedAndroidTest           # Run instrumented tests (requires device/emulator)
```

Unit tests: `app/src/test/java/com/shaeed/ludo/`
Instrumented tests: `app/src/androidTest/java/com/shaeed/ludo/`

## Linting

```bash
./gradlew lintDebug              # Run Android Lint
```

## Architecture

- **Entry point:** `MainActivity.kt` — single-activity Compose app using `ComponentActivity`
- **UI layer:** Jetpack Compose with Material 3 (`LudoTheme` in `ui/theme/`)
- **Theming:** Supports dark/light mode and Android 12+ dynamic colors
- **Package:** `com.shaeed.ludo`

## Key Configuration

- **Dependency versions:** Centralized in `gradle/libs.versions.toml`
- **Kotlin:** 2.0.21 with Compose compiler plugin
- **Java compatibility:** 11
- **Compose BOM:** 2024.09.00
- **Gradle:** 8.13, AGP 8.11.2
