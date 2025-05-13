plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.emil_z.ultimate_tic_tac_toe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.emil_z.ultimate_tic_tac_toe"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
    implementation(libs.circleimageview)
    implementation(project(":VIEWMODEL"))
    implementation(project(":MODEL"))
    implementation(project(":HELPER"))
    implementation(libs.gridlayout)
    implementation(project(":REPOSITORY"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}