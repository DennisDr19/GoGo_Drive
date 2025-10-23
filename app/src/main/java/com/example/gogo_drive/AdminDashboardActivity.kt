package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // --- Lógica para la tarjeta "Registrar Estudiante" ---
        val registerStudentCard = findViewById<MaterialCardView>(R.id.btn_register_student)
        registerStudentCard.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
         // --- Lógica para la tarjeta "Registrar Personal" ---
        val registerStaffCard = findViewById<MaterialCardView>(R.id.btn_register_staff)
        registerStaffCard.setOnClickListener {
            // Navega a la pantalla de registro de personal
            val intent = Intent(this, RegisterStaffActivity::class.java)
            startActivity(intent)
        }

        // --- Lógica para el botón de cierre de sesión ---
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // --- Lógica para la tarjeta "Gestionar Autos" ---
        val registerCarCard = findViewById<MaterialCardView>(R.id.btn_register_car)
        registerCarCard.setOnClickListener {
            // Navega a la pantalla de registro de personal
            val intent = Intent(this, RegisterCarActivity::class.java)
            startActivity(intent)
        }
        val gestionarAutosCard = findViewById<MaterialCardView>(R.id.btn_register_car)

        // 2. Configura un listener para que reaccione al clic
        gestionarAutosCard.setOnClickListener {
            // 3. Crea la "intención" de abrir la nueva pantalla
            val intent = Intent(this, ManageCarsActivity::class.java)

            // 4. Inicia la nueva pantalla
            startActivity(intent)
        }

        // Aquí puedes añadir los listeners para las otras tarjetas del panel...
        // findViewById<MaterialCardView>(R.id.btn_manage_roles).setOnClickListener { ... }
    }
}
