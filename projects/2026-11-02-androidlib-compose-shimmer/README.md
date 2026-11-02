# Compose Shimmer

An Android library of Jetpack Compose skeleton loaders: a `shimmer()` modifier, a `skeleton()` placeholder modifier, and ready-made list-row placeholders.

![Preview: UI mockup of the sample app's loading state](preview.svg)

*UI mockup, hand-drawn. It is not a screenshot; no emulator was available.*

## Dependency

The library is a Gradle module published through `maven-publish` (coordinates below). It is not on Maven Central; publish it locally first.

```kotlin
dependencies {
    implementation("com.dailyprojects.shimmer:shimmer:1.0.0")
}
```

## Usage

```kotlin
// 1. Ready-made placeholders
if (articles == null) SkeletonList(count = 5) else ArticleList(articles)

// 2. Turn any element into a skeleton while loading
Text(
    text = title,
    modifier = Modifier.skeleton(visible = isLoading, shape = RoundedCornerShape(8.dp)),
)

// 3. Shimmer over real content
Image(painter, null, Modifier.shimmer(enabled = isRefreshing))

// 4. Dark theme
SkeletonList(count = 3, spec = ShimmerSpec.Dark)
```

`ShimmerSpec` controls base and highlight colours, sweep duration, band width and tilt. Its invalid values throw at construction.

## How to run

Needs JDK 17 and the Android SDK (`ANDROID_HOME` set, or `local.properties` with `sdk.dir`).

```bash
cd projects/2026-11-02-androidlib-compose-shimmer
./gradlew build :sample:assembleDebug     # compiles library, runs unit tests, builds the sample APK
./gradlew :shimmer:publishToMavenLocal    # makes the coordinates above resolvable locally
```

The sample APK lands in `sample/build/outputs/apk/debug/`. Its `ArticleSource` interface reads `mock/articles.json` (copied to the sample's assets); swapping in a real client means replacing `AssetArticleSource` only.

## Stack

Kotlin 2.0, Jetpack Compose (BOM 2024.09.02), Gradle KTS, AGP 8.6, minSdk 24.

## Limitations

- Verified at authoring time: `./gradlew build :sample:assembleDebug` finished with BUILD SUCCESSFUL (library compile, lint, 5 unit tests, sample APK).
- Never run on a device or emulator: none was available, so the animation's look is untested. Only the band geometry is unit-tested.
- `skeleton()` hides the content's drawing, but the content is still measured, so it needs a size (text should have placeholder text or a width).
- No reduced-motion handling yet, and no shared sweep across items: each element animates its own transition.
