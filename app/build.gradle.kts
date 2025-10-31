plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gogo_drive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.gogo_drive"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Dependencias de AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.9.0")

    // Credenciales de Google
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

    // --- MANEJO DE FIREBASE (CORREGIDO) ---
    // 1. Usa la BoM (Bill of Materials) para que Firebase gestione las versiones.
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    // 2. Declara solo las dependencias que necesitas, SIN versión.
    implementation("com.google.firebase:firebase-auth")      // Para autenticación
    implementation("com.google.firebase:firebase-firestore") // Para la base de datos Firestore

    // --- DEPENDENCIA DE COMPATIBILIDAD (CLAVE) ---
    // 3. Forzamos una versión compatible de Play Services Auth para evitar errores silenciosos.
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Dependencias de Test
    testImplementation(libs.junit.junit)
}
