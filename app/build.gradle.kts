plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("androidx.room") version "2.7.2"
    id("com.google.dagger.hilt.android") version "2.57"

    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "2.2.0"
}

android {
    namespace = "com.github.dictionary"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.dictionary"
        minSdk = 21
        targetSdk = 36
        versionCode = 200
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3:1.4.0-alpha18")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.3")

    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.9.2")

    implementation("androidx.room:room-ktx:2.7.2")
    implementation("androidx.room:room-paging:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    implementation("io.coil-kt.coil3:coil-svg:3.3.0")

    implementation("androidx.paging:paging-compose:3.3.6")

    implementation("com.google.dagger:hilt-android:2.57")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:2.57")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}

room {
    schemaDirectory("$projectDir/schemas")
}
hilt {
    enableAggregatingTask = false
}