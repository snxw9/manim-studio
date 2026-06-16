plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
}

android {
    namespace = "com.manimstudio.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.manimstudio.app"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui:1.11.2")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.compose.foundation:foundation:1.11.2")
    implementation("androidx.compose.animation:animation:1.11.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    
    implementation("androidx.security:security-crypto:1.1.0")
    
    // Media3 / ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("androidx.media3:media3-ui:1.10.1")
    implementation("androidx.media3:media3-common:1.10.1")
    
    // Google Fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.11.2")
    
    // Coil (video thumbnails)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")

    implementation("androidx.work:work-runtime-ktx:2.11.2")
    
    implementation("androidx.compose.ui:ui-tooling-preview:1.11.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.11.2")
    debugImplementation("androidx.compose.ui:ui:1.11.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
