import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.0.20" apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20" apply false
}

// The runner already has Node and Yarn on PATH; use those instead of having the
// Kotlin/JS plugin fetch its own (its default download repo trips the settings'
// FAIL_ON_PROJECT_REPOS-adjacent repository rules in this build).
plugins.withType<NodeJsRootPlugin> {
    extensions.configure<NodeJsRootExtension> {
        download = false
    }
}
plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootExtension> {
        download = false
    }
}
