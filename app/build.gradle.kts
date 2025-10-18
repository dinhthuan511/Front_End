plugins {
    alias(libs.plugins.android.application)
    // Add the google-services plugin here
//    id("com.google.gms.google-services") // This line is crucial for Firebase setup
    // TO:
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.book_store_mobileapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.book_store_mobileapp"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // For making API calls (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // For loading images from a URL (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // --- Firebase Additions Start Here ---

    // Import the Firebase BoM
    // This ensures all Firebase libraries you use are compatible
    implementation(platform("com.google.firebase:firebase-bom:32.0.0")) // Use the latest BoM version if available

    // Add the dependency for the Realtime Database
    // When using the BoM, you don't specify versions for individual Firebase libraries
    implementation("com.google.firebase:firebase-database-ktx") // For Kotlin extensions

    // Optionally, if you also want to use Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    // Or other Firebase products, you'd add them here without versions
    // implementation("com.google.firebase:firebase-storage-ktx")
    // implementation("com.google.firebase:firebase-firestore-ktx")

    // --- Firebase Additions End Here ---
}
