plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // cần cho Firebase
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
    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.lifecycle.process)

    // Google map
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.cardview)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ===== Firebase (dùng BoM để đồng bộ version) =====
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // Auth (không ghi version khi dùng BoM)
    implementation("com.google.firebase:firebase-auth")

    // Firestore (để map username -> email và login bằng username)
    implementation("com.google.firebase:firebase-firestore")


    implementation(fileTree(
        mapOf(
            "dir" to "libs",
            "include" to listOf("*.aar", "*.jar")
        )
    ))



    // Add this line for Firebase Realtime Database
    implementation("com.google.firebase:firebase-database")
    // (Tuỳ chọn) Realtime Database – chỉ giữ nếu bạn thật sự dùng
    // implementation("com.google.firebase:firebase-database")
}