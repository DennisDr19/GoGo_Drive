package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase // Importación de Firebase

class LoginActivity : AppCompatActivity() {

    // Declara la instancia de Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializa Firebase Auth usando la función de extensión de KTX
        auth = Firebase.auth

        // Referencias a las vistas del XML
        // Asegúrate de que estos IDs existen en archivo R.layout.activity_login
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        // Listener para el botón de Iniciar Sesión
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validaciones básicas
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Iniciar sesión con Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Inicio de sesión exitoso
                        Toast.makeText(this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show()

                        // NAVEGACIÓN: Descomenta estas líneas cuando tengas tu MainActivity
                        // val intent = Intent(this, MainActivity::class.java)
                        // startActivity(intent)
                        // finish() // Cierra la actividad de login para que el usuario no pueda volver
                    } else {
                        // <-- MEJORA: Manejo de errores específicos
                        val exception = task.exception
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException -> "El correo electrónico no está registrado."
                            is FirebaseAuthInvalidCredentialsException -> "La contraseña es incorrecta."
                            else -> "Error de autenticación: ${exception?.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Listener para el texto de registrarse
        registerTextView.setOnClickListener {
            // NAVEGACIÓN: Se activa la navegación a RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

}
