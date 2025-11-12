package com.example.gogo_drive

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Inicializa la app de Firebase principal
        Firebase.initialize(this)

        // 2. ¡CLAVE! Inicializa la app secundaria para el registro temporal
        try {
            // --- LÍNEA CORREGIDA ---
            // Se hace la llamada más explícita para evitar ambigüedades.
            val options = FirebaseApp.getInstance().options

            FirebaseApp.initializeApp(this, options, "secondary")
            Log.d("MyApp", "Firebase secondary app initialized successfully.")
        } catch (e: IllegalStateException) {
            // Esto puede pasar si la app ya fue inicializada, lo cual está bien.
            Log.w("MyApp", "Secondary Firebase app might already exist.", e)
        }
    }
}
