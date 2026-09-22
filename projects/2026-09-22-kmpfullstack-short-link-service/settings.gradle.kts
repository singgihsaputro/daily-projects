pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_SETTINGS (not FAIL_ON_PROJECT_REPOS): the Kotlin/JS plugin registers its
    // own Node.js distribution repository at configuration time for :webApp, which
    // FAIL_ON_PROJECT_REPOS rejects.
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ShortLinkService"
include(":shared")
include(":server")
include(":androidApp")
include(":webApp")
