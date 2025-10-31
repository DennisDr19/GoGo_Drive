// Suponiendo que tu archivo de Login se llama LoginActivity.kt
// Reemplaza el contenido de ese archivo con esto.

package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Asegúrate de que el nombre de tu layout sea correcto

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginActivity", "Inicio de sesión exitoso.")
                        checkUserRoleAndRedirect()
                    } else {
                        Log.w("LoginActivity", "Fallo en el inicio de sesión", task.exception)
                        Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // --- ¡AQUÍ ESTÁ LA LÓGICA CLAVE! ---
    // Este método se ejecuta cada vez que la actividad se inicia o vuelve a primer plano.
    override fun onStart() {
        super.onStart()
        // Comprueba si un usuario ya ha iniciado sesión.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si hay un usuario, no necesitamos que se loguee de nuevo.
            // Lo mandamos directamente a la pantalla principal.
            Log.d("LoginActivity", "Sesión activa encontrada para ${currentUser.email}. Redirigiendo...")
            checkUserRoleAndRedirect()
        } else {
            // No hay sesión activa, el usuario debe iniciar sesión.
            Log.d("LoginActivity", "No hay sesión activa. Se requiere inicio de sesión.")
        }
    }

    private fun checkUserRoleAndRedirect() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("roles").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val rol = document.getString("rol")
                    when (rol) {
                        "administrador" -> {
                            // Cambia AdminDashboardActivity::class por el nombre de tu actividad de admin
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Bienvenido administrador.", Toast.LENGTH_LONG).show()
                        }
                        "estudiante" -> {
                            // Cambia StudentDashboardActivity::class por el nombre de tu actividad de estudiante
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Bienvenido estudiante.", Toast.LENGTH_LONG).show()
                        }
                        "instructor" -> {
                            // Cambia StudentDashboardActivity::class por el nombre de tu actividad de estudiante
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Bienvenido instructor.", Toast.LENGTH_LONG).show()
                        }
                        // Añade más roles si los tienes (ej. "instructor")
                        else -> {
                            Toast.makeText(this, "Rol de usuario desconocido.", Toast.LENGTH_LONG).show()
                        }
                    }
                    // Cierra LoginActivity para que el usuario no pueda volver atrás con el botón de retroceso.
                    finish()
                } else {
                    Toast.makeText(this, "No se encontró el rol del usuario.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al obtener el rol: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
