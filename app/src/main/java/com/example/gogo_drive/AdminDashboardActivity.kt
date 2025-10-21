package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.widget.Button // ¡Importación añadida!
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth // ¡Importación añadida!

class AdminDashboardActivity : AppCompatActivity() {

    // 1. Declara la variable para Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cargar el layout del panel de administrador
        setContentView(R.layout.activity_main)

        // 2. Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        // --- Lógica para la tarjeta "Registrar Estudiante" (sin cambios) ---
        val registerStudentCard = findViewById<MaterialCardView>(R.id.btn_register_student)
        registerStudentCard.setOnClickListener {
            Toast.makeText(this, "Abriendo registro de estudiante...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 3. Encuentra el botón de cierre de sesión por su ID
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // 4. Asigna el listener para que reaccione al clic
        logoutButton.setOnClickListener {
            // Informar al usuario (opcional)
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
            // a) Cierra la sesión activa en Firebase
            auth.signOut()
            // b) Crea un Intent para volver a la pantalla de Login
            val intent = Intent(this, LoginActivity::class.java)
            // c) Limpia el historial para que el usuario no pueda volver atrás
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // d) Lanza la actividad de Login
            startActivity(intent)
            // e) Cierra la actividad actual (AdminDashboardActivity)
            finish()
        }

        // Aquí puedes añadir los listeners para las otras tarjetas del panel...
        // findViewById<MaterialCardView>(R.id.btn_register_staff).setOnClickListener { ... }
        // findViewById<MaterialCardView>(R.id.btn_manage_roles).setOnClickListener { ... }
    }
}
