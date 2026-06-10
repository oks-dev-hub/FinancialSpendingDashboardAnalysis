plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.financialspendingdashboardanalysis"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.financialspendingdashboardanalysis"
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.graphics)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.ui:ui:1.5.0") // Use the latest version
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("androidx.compose.material:material-icons-core-android:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.03.00")) // Use the latest BOM version

    // Jetpack Compose UI
    implementation("androidx.compose.ui:ui")  // No version needed due to BOM
    implementation("androidx.compose.ui:ui-tooling-preview")  // No version needed
    implementation("androidx.compose.material3:material3")  // No version needed
    implementation("androidx.compose.material:material-icons-core")  // No version needed

    // Additional dependencies
    implementation("androidx.navigation:navigation-compose:2.5.0")  // Use the latest version
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0")  // Use the latest version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.jakewharton.timber:timber:5.0.1")
}