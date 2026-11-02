plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    `maven-publish`
}

group = "com.dailyprojects.shimmer"
version = "1.0.0"

android {
    namespace = "com.dailyprojects.shimmer"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    api("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    testImplementation("junit:junit:4.13.2")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.dailyprojects.shimmer"
                artifactId = "shimmer"
                version = "1.0.0"
            }
        }
    }
}
