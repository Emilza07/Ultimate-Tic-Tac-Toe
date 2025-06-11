plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.emil_z.viewmodel"
    compileSdk = 35

    defaultConfig {

        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.play.services.tasks.v1821)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation (libs.play.services.tasks)
    implementation(libs.firebase.firestore)
    implementation(project(":MODEL"))
    implementation(project(":REPOSITORY"))
    implementation(project(":HELPER"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}