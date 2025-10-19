// ✅ Top-level build.gradle
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}