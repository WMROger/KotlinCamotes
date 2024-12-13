plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.kotlinactivites"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kotlinactivites"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xjvm-default=all"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.firebase.auth.v2130)
    implementation(libs.firebase.bom)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation(libs.retrofit2)
    implementation(libs.gson.v2101)
    implementation(libs.maplibre.sdk)
    implementation(libs.android.sdk)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.bubbletabbar)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
}
