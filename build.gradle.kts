// Fichier: build.gradle.kts (Racine du projet)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false    // ON RETIRE L'ALIAS KOTLIN QUI POSE PROBLÈME
    // alias(libs.plugins.kotlin.android) apply false
}
