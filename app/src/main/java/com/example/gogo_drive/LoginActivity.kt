package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View // ¡Importación añadida!
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar // ¡Importación añadida!
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // 1. Declara la variable para el ProgressBar
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 2. Conecta la variable con el ProgressBar del layout
        progressBar = findViewById(R.id.loginProgressBar)

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

            // 3. MUESTRA el ProgressBar justo antes de iniciar la operación
            progressBar.visibility = View.VISIBLE
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Autenticación exitosa para: $email")
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        checkUserRoleAndRedirect(userId)
                    } else {
                        // OCULTA el ProgressBar si hay un error
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Error: No se pudo obtener el ID de usuario.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // OCULTA el ProgressBar si la autenticación falla
                    progressBar.visibility = View.GONE
                    handleLoginFailure(task.exception)
                }
            }
    }

    private fun checkUserRoleAndRedirect(userId: String) {
        val roleDocRef = firestore.collection("roles").document(userId)

        roleDocRef.get().addOnSuccessListener { documentSnapshot ->
            // OCULTA el ProgressBar ANTES de redirigir
            progressBar.visibility = View.GONE
            if (documentSnapshot.exists()) {
                val userRole = documentSnapshot.getString("rol")
                when (userRole) {
                    "estudiante" -> redirectToActivity(EstudentActivity::class.java, "Bienvenido, Estudiante.")
                    "administrativo" -> redirectToActivity(AdminDashboardActivity::class.java, "Bienvenido, Administrador.")
                    "instructor" -> redirectToActivity(AdminDashboardActivity::class.java, "Bienvenido, Instructor.")
                    else -> showRoleErrorMessageAndSignOut()
                }
            } else {
                showRoleErrorMessageAndSignOut()
            }
        }.addOnFailureListener { e ->
            // OCULTA el ProgressBar si falla la lectura de rol
            progressBar.visibility = View.GONE
            Log.e(TAG, "Error al buscar en la colección 'roles'", e)
            Toast.makeText(this, "Error al verificar el rol del usuario.", Toast.LENGTH_LONG).show()
        }
    }

    // --- El resto de tus funciones auxiliares (sin cambios) ---

    private fun showRoleErrorMessageAndSignOut() {
        Toast.makeText(this, "Tu usuario no tiene un rol asignado. Contacta a administración.", Toast.LENGTH_LONG).show()
        auth.signOut()
    }

    private fun redirectToActivity(activityClass: Class<*>, message: String? = null) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun handleLoginFailure(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "El correo electrónico no está registrado."
            is FirebaseAuthInvalidCredentialsException -> "La contraseña es incorrecta."
            else -> "Error de autenticación. Inténtalo de nuevo."
        }
        Log.w(TAG, "Error de autenticación", exception)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}
