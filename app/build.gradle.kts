plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    //Firebase
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
    // Versiones estables y compatibles de AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.9.0") // Usar activity-ktx si usas viewModels
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Versión estable
    implementation("com.google.android.material:material:1.12.0") // Versión estable

    // Credenciales (las versiones estaban bien)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // Firebase - ¡IMPORTANTE! Usar el BoM correctamente
    // Importa la lista de materiales (BoM). Esto asegura que todas las librerías
    // de Firebase sean compatibles entre sí.
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    // Ahora, añade las dependencias de Firebase que necesites SIN especificar la versión.
    // El BoM se encargará de seleccionar la versión correcta.
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth-ktx") // Añadida para funciones de extensión

    // Google Identity (estaba bien)
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    testImplementation(libs.junit.junit) // Versión estable

    // La dependencia de kotlin-stdlib ya no es necesaria, el plugin la incluye.
}
