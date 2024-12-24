plugins {
    id("com.android.application") version "8.5.1" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}
buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7") // BNRG Listing 13.9
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.10-1.0.13") // BNRG Listing 12.9
        classpath("com.android.tools.build:gradle:8.1.4")
    }
}