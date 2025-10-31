// Archivo: app/src/main/java/com/example/gogo_drive/AdminDashboardActivity.kt
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

        // --- SECCIÓN: Gestión de Usuarios ---

        // Botón: Registrar Estudiante
        findViewById<MaterialCardView>(R.id.btn_register_student).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Botón: Registrar Personal
        findViewById<MaterialCardView>(R.id.btn_register_staff).setOnClickListener {
            startActivity(Intent(this, RegisterStaffActivity::class.java))
        }

        // Botón: Gestion de Estudiantes
        findViewById<MaterialCardView>(R.id.btn_manage_students).setOnClickListener {
            startActivity(Intent(this, ManageStudentActivity::class.java))
        }

        // Botón: Gestion de Personal
        findViewById<MaterialCardView>(R.id.btn_manage_staff).setOnClickListener {
            startActivity(Intent(this, ManageStudentActivity::class.java))
        }

        // --- SECCIÓN: Gestión de Clases y Vehículos ---

        // Botón: Gestionar Autos
        findViewById<MaterialCardView>(R.id.btn_register_car).setOnClickListener {
            startActivity(Intent(this, ManageCarsActivity::class.java))
        }

        // --- SECCIÓN: Administración General ---

        // Botón: Gestionar Licencias
        findViewById<MaterialCardView>(R.id.btn_manage_licenses).setOnClickListener {
            startActivity(Intent(this, ManageLicensesActivity::class.java))
        }

        // Botón: Registrar Pagos
        findViewById<MaterialCardView>(R.id.btn_register_payment).setOnClickListener {
            Toast.makeText(this, "Registrar Pagos (en construcción)", Toast.LENGTH_SHORT).show()
        }


        // --- Botón de Cierre de Sesión ---
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
