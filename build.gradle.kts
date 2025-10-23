// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Updated to a modern, compatible version
        classpath("com.android.tools.build:gradle:8.4.1")
        // Updated to a modern, compatible version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
