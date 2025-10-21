package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Paso 1: Cargar el layout del panel de administrador
        setContentView(R.layout.activity_main)

        // Paso 2: Encontrar la tarjeta "Registrar Estudiante" por su ID
        val registerStudentCard = findViewById<MaterialCardView>(R.id.btn_register_student)

        // Paso 3: Asignar el listener para que reaccione al clic
        registerStudentCard.setOnClickListener {
            // Informar al usuario de la acción (opcional, pero buena práctica)
            Toast.makeText(this, "Abriendo registro de estudiante...", Toast.LENGTH_SHORT).show()

            // Paso 4: Crear un Intent para abrir RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)

            // Paso 5: Lanzar la nueva actividad
            startActivity(intent)
        }

        // Aquí puedes añadir los listeners para las otras tarjetas del panel...
        // findViewById<MaterialCardView>(R.id.btn_register_staff).setOnClickListener { ... }
        // findViewById<MaterialCardView>(R.id.btn_manage_roles).setOnClickListener { ... }
    }
}
