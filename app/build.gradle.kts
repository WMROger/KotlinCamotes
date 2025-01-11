plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
    id ("kotlin-android")
    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.kotlinactivities"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kotlinactivities" // Ensure it matches `namespace`
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
        viewBinding = true // This is the correct way to enable ViewBinding
    }

    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {
    // Firebase dependencies
    implementation(libs.firebase.auth.v2130)
    implementation(libs.firebase.bom)
    implementation(libs.firebase.database.ktx)
    implementation ("com.google.firebase:firebase-auth:21.3.0")
    implementation ("com.google.firebase:firebase-database:20.0.5")

    // Android libraries
    implementation(libs.android.sdk)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.monitor)

    // Google Play Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.auth)

    // Material Design
    implementation(libs.material)

    // BubbleTabBar
    implementation(libs.bubbletabbar)

    // Networking libraries
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.gson)
    implementation(libs.okhttp.v4120)
    implementation(libs.okhttp3.logging)
    implementation(libs.gson.v2101)

    // Email utilities
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    // Glide for image loading
    implementation(libs.glide)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.firebase.vertexai)
    implementation(libs.androidx.room.ktx)
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    debugImplementation("com.github.bumptech.glide:okhttp3-integration:4.15.1")
    implementation ("com.google.android.material:material:1.11.0") // Update to the latest version

    // Unit testing
    testImplementation(libs.junit.junit)

    // Android UI testing
    androidTestImplementation(libs.junit.junit)

    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

}
