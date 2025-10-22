package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Comprueba si ya hay un usuario con sesión activa.
        if (auth.currentUser != null) {
            // Muestra el ProgressBar mientras se verifica el rol y se redirige.
            progressBar = findViewById(R.id.loginProgressBar)
            progressBar.visibility = View.VISIBLE

            // Si hay un usuario, verifica su rol y redirígelo directamente.
            Log.d(TAG, "Usuario ya autenticado: ${auth.currentUser!!.uid}. Redirigiendo...")
            checkUserRoleAndRedirect(auth.currentUser!!.uid)

            // 'return' evita que se ejecute el resto del código del 'onCreate'.
            // La pantalla de login no llegará a mostrarse completamente.
            return
        }
        // ===============================================================

        // El resto del código solo se ejecutará si NO hay una sesión activa.
        progressBar = findViewById(R.id.loginProgressBar)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // 1. VERIFICAR SI LOS CAMPOS ESTÁN LLENOS
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si los campos están llenos, se procede a la autenticación.
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
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Error: No se pudo obtener el ID de usuario.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    progressBar.visibility = View.GONE
                    handleLoginFailure(task.exception)
                }
            }
    }

    private fun checkUserRoleAndRedirect(userId: String) {
        val roleDocRef = firestore.collection("roles").document(userId)

        roleDocRef.get().addOnSuccessListener { documentSnapshot ->
            progressBar.visibility = View.GONE
            if (documentSnapshot.exists()) {
                val userRole = documentSnapshot.getString("rol")
                when (userRole) {
                    "estudiante" -> redirectToActivity(EstudentActivity::class.java, "Bienvenido, Estudiante.")
                    "administrador" -> redirectToActivity(AdminDashboardActivity::class.java, "Bienvenido, Administrador.")
                    "instructor" -> redirectToActivity(AdminDashboardActivity::class.java, "Bienvenido, Instructor.")
                    else -> showRoleErrorMessageAndSignOut()
                }
            } else {
                showRoleErrorMessageAndSignOut()
            }
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            Log.e(TAG, "Error al buscar en la colección 'roles'", e)
            Toast.makeText(this, "Error al verificar el rol del usuario.", Toast.LENGTH_LONG).show()
        }
    }

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
            is FirebaseAuthInvalidCredentialsException -> "El correo/contraseña incorrecto."
            else -> "Error de autenticación. Inténtalo de nuevo."
        }
        Log.w(TAG, "Error de autenticación", exception)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}
