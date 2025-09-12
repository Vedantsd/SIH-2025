plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // ✅ Add serialization plugin
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.example.smartcropadvisory"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartcropadvisory"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {

    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.2.0")
        // TFLite runtime
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

        // (Optional) TFLite GPU Delegate for acceleration
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

        // (Optional) TFLite Support Library (helps with pre/post-processing)
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Retrofit core
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// Retrofit Gson converter (if you prefer Gson instead of kotlinx serialization)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// OkHttp (network client)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // Retrofit core
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// Retrofit + Kotlinx Serialization Converter
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
// OkHttp (network client)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
// Kotlinx Serialization JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    // For Location Services
    implementation("com.google.android.gms:play-services-location:21.2.0") // Check for the latest version

// For Networking (example with Retrofit and Kotlinx.serialization)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Or your preferred version
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0") // Check for latest
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // Retrofit usually brings this
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // For debugging network calls

    dependencies {
        implementation("androidx.compose.material:material-icons-core:...") // You likely have this
        implementation("androidx.compose.material:material-icons-extended:...") // <<< MAKE SURE THIS IS PRESENT
        // Ensure the version matches your other compose material libraries
    }
        // ... other dependencies
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Or the latest version compatible
        implementation("com.google.android.gms:play-services-location:21.2.0") // You should already have this
        // ... other dependencies

    implementation("com.google.accompanist:accompanist-flowlayout:0.32.0")
// For ViewModel and LiveData/StateFlow
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0") // Check latest
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0") // For collectAsStateWithLifecycle
// In your dependencies { ... } block
    implementation("io.coil-kt:coil-compose:2.6.0") // Check for the latest version of Coil
    // Core + lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Material icons
    implementation("androidx.compose.material:material-icons-extended")

    // ✅ Ktor HTTP client + JSON serialization
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")

    // ✅ Kotlinx Serialization (needed for @Serializable)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
