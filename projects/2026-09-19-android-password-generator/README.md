# Password Generator

A single-screen Android app that generates a random password from adjustable length and character-set options, rates its strength, and keeps a running history of what it generated.

![UI mockup](preview.svg)

*UI mockup — hand-drawn, not a screenshot. This sandbox has no Android emulator, so the app was built and packaged but never launched on a device.*

## How to run

Requires a JDK 17+ and the Android SDK (`ANDROID_HOME` set, platform 35 and build-tools installed).

```bash
cd projects/2026-09-19-android-password-generator
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it on a device or emulator with:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Stack

- Kotlin, Jetpack Compose, Material 3
- `ViewModel` + `StateFlow` for generator state
- `SecureRandom` for character selection, log2-entropy estimate for the strength label
- Gradle Kotlin DSL, Gradle wrapper included (8.9)
- Default length/character-set options and starting history read from `mock/password_history.json` (bundled as an Android asset) through a `PasswordHistoryRepository` interface — swap `LocalPasswordHistoryRepository` for a networked/synced implementation later without touching the ViewModel or UI

## Limitations

- No emulator or physical device was available in this environment, so the app was never actually launched or tapped through. What *was* verified: `./gradlew assembleDebug` runs clean on this repo's Android SDK (compile, resource merge, dex, package all succeed) and the packaged APK contains `assets/mock/password_history.json`.
- `preview.svg` is a hand-drawn mockup of the intended layout, not a real screenshot.
- No persistence: closing the app resets history to the fixture's starting entries. New passwords generated during a session are prepended with a fresh ID and the current device time, capped at 8 entries, but nothing is written back to disk.
- "Copy" writes to the system clipboard via `ClipboardManager`; this could not be exercised without a device.
