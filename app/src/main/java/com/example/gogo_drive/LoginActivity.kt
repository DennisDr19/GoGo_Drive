package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // Constantes para logging y nombres de colecciones
    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialización de Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Si ya hay una sesión activa, redirigir directamente sin pedir login
        if (auth.currentUser != null) {
            checkUserRoleAndRedirect(auth.currentUser!!.uid)
            return // Evita mostrar el layout de login si ya está autenticado
        }

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

            // --- PASO 1: AUTENTICAR CON FIREBASE AUTH ---
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Autenticación exitosa para: $email")
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // Si la autenticación es correcta, procedemos a verificar el rol
                            checkUserRoleAndRedirect(userId)
                        } else {
                            Toast.makeText(this, "Error: No se pudo obtener el ID de usuario.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Si la autenticación falla, mostramos un error claro.
                        handleLoginFailure(task.exception)
                    }
                }
        }
    }

    /**
     * Función centralizada que busca el rol del usuario en la colección 'roles' y lo redirige.
     */
    private fun checkUserRoleAndRedirect(userId: String) {
        // --- PASO 2: BUSCAR EL DOCUMENTO DEL USUARIO EN LA COLECCIÓN 'roles' ---
        // El ID del documento debe ser igual al UID del usuario en Firebase Auth.
        val roleDocRef = firestore.collection("roles").document(userId)

        roleDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // El usuario tiene un documento de rol, procedemos a leerlo.
                Log.d(TAG, "Documento de rol encontrado para el usuario: $userId")

                // Obtener el valor del campo 'rol' del documento
                val userRole = documentSnapshot.getString("rol")

                // --- PASO 3: REDIRIGIR SEGÚN EL ROL ---
                when (userRole) {
                    "estudiante" -> {
                        // Asumiendo que EstudentActivity es la pantalla para estudiantes
                        redirectToActivity(EstudentActivity::class.java, "Bienvenido, Estudiante.")
                    }
                    "administrativo" -> {
                        redirectToActivity(AdminDashboardActivity::class.java, "Bienvenido, Administrador.")
                    }
                    "instructor" -> {
                        // TODO: Crear y enlazar la pantalla para instructores (e.g., InstructorDashboardActivity)
                        // Por ahora, se redirige a una pantalla de administrador como placeholder.
                        Log.d(TAG, "Redirigiendo a instructor (usando layout de Admin como placeholder).")
                        redirectToActivity(AdminDashboardActivity::class.java, "Bienvenido, Instructor.")
                    }
                    else -> {
                        // El campo 'rol' tiene un valor no reconocido o está vacío.
                        Log.e(TAG, "Rol '$userRole' no reconocido para el usuario $userId.")
                        showRoleErrorMessageAndSignOut()
                    }
                }
            } else {
                // ESTADO INCONSISTENTE: El usuario está en Auth, pero no tiene un documento en 'roles'.
                Log.e(TAG, "Error de consistencia: Usuario $userId autenticado pero sin documento en la colección 'roles'.")
                showRoleErrorMessageAndSignOut()
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error al buscar en la colección 'roles'", e)
            Toast.makeText(this, "Error al verificar el rol del usuario.", Toast.LENGTH_LONG).show()
        }
    }

    // --- FUNCIONES AUXILIARES (Sin cambios) ---

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
