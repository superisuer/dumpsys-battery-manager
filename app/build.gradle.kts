plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.huker667.dumpsysmanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.huker667.dumpsysmanager"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "101"

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
}

dependencies {
    implementation("dev.rikka.shizuku:api:13.1.0")
    implementation("dev.rikka.shizuku:provider:13.1.0")
    //compileOnly("dev.rikka.shizuku:api-stub:13.1.5")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}