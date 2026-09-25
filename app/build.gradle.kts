plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.demowallet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.demowallet"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Must be HTTPS — AccountVerificationClient enforces this at runtime
        buildConfigField(
            "String",
            "BACKEND_BASE_URL",
            "\"https://renmonie-backend-production.up.railway.app\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BACKEND_BASE_URL",
                "\"https://renmonie-backend-production.up.railway.app\""
            )
        }

        release {
            isMinifyEnabled = false

            buildConfigField(
                "String",
                "BACKEND_BASE_URL",
                "\"https://renmonie-backend-production.up.railway.app\""
            )

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")

    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Required by AccountVerificationClient (withContext / Dispatchers.IO)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
