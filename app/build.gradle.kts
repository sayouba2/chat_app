plugins {
    // On utilise les ID classiques (plus sûr que les alias)
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {    namespace = "com.example.chat_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chat_app"
        minSdk = 24
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

    // C'est ici qu'on dit qu'on utilise JAVA 8
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // On retire le bloc "kotlinOptions" car votre projet est en Java

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- FIREBASE (Vos dépendances fonctionnelles) ---
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database") // Ajouté par sécurité

    // --- ANDROID STANDARD (Correction des erreurs 'libs') ---
    // On met les versions en DUR pour éviter les erreurs "Unresolved reference"
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")

    // --- IMAGES ---
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // --- TESTS ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
