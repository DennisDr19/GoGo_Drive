package com.example.gogo_drive

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Configuración de la barra de acción para tener un botón de "atrás"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Crear Cuenta de Estudiante"

        // Inicializar Firebase y Vistas
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)

        // --- Referencias a las vistas del formulario ---
        val nombresEditText = findViewById<EditText>(R.id.nombresEditText)
        val primerApellidoEditText = findViewById<EditText>(R.id.primerApellidoEditText)
        val segundoApellidoEditText = findViewById<EditText>(R.id.segundoApellidoEditText)
        val telefonoEditText = findViewById<EditText>(R.id.telefonoEditText)
        val carnetEditText = findViewById<EditText>(R.id.carnetEditText)
        val complementoEditText = findViewById<EditText>(R.id.complementoEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // --- Lógica del Botón de Registro ---
        registerButton.setOnClickListener {
            // Recoger y validar los datos
            val nombres = nombresEditText.text.toString().trim()
            val primerApellido = primerApellidoEditText.text.toString().trim()
            val segundoApellido = segundoApellidoEditText.text.toString().trim()
            val telefono = telefonoEditText.text.toString().trim()
            val carnet = carnetEditText.text.toString().trim()
            val complemento = complementoEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validaciones básicas
            if (listOf(nombres, primerApellido, email, password, telefono, carnet).any { it.isEmpty() }) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Iniciar el proceso de registro
            progressBar.visibility = View.VISIBLE
            registerUser(nombres, primerApellido, segundoApellido, telefono, carnet, complemento, email, password)
        }
    }

    private fun registerUser(nombres: String, primerApellido: String, segundoApellido: String, telefono: String, carnet: String, complemento: String, email: String, password: String) {
        // PASO 1: Crear el usuario en Firebase Authentication.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { authTask ->
                if (authTask.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid

                    if (userId == null) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Error crítico: No se pudo obtener el ID de usuario.", Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }

                    // --- INICIO DE LA TRANSACCIÓN POR LOTES ---
                    val batch = firestore.batch()

                    // PASO 2: Preparar la creación del documento en "personas"
                    val personRef = firestore.collection("personas").document(userId)
                    val personData = hashMapOf(
                        "nombres" to nombres,
                        "primerApellido" to primerApellido,
                        "segundoApellido" to segundoApellido,
                        "telefono" to telefono,
                        "carnet" to carnet,
                        "complemento" to complemento,
                        "correo" to email,
                        "acceso" to true,
                        "fechaCreacion" to Timestamp.now()
                    )
                    batch.set(personRef, personData)

                    // ¡NUEVO! PASO 3: Preparar la creación del documento en "roles"
                    val roleRef = firestore.collection("roles").document(userId)
                    val roleData = hashMapOf(
                        "rol" to "estudiante" // Asignación del rol único
                    )
                    batch.set(roleRef, roleData)

                    // PASO 4: Ejecutar todas las operaciones a la vez
                    batch.commit()
                        .addOnSuccessListener {
                            // ¡Éxito total! Ambas escrituras se completaron.
                            progressBar.visibility = View.GONE
                            Log.d("RegisterActivity", "Usuario, perfil y rol creados exitosamente con batch.")
                            showSuccessDialogAndReturnToLogin()
                        }
                        .addOnFailureListener { batchException ->
                            // Si algo falló, ninguna escritura se realizó. La base de datos está limpia.
                            progressBar.visibility = View.GONE
                            Log.e("RegisterActivity", "FALLO en el batch. No se escribió nada en Firestore.", batchException)
                            Toast.makeText(this, "Error al guardar el perfil. Contacta a soporte.", Toast.LENGTH_LONG).show()

                            // Consideración avanzada: borrar el usuario de Auth para que pueda reintentar limpiamente.
                            firebaseUser.delete().addOnCompleteListener { deleteTask ->
                                if(deleteTask.isSuccessful) {
                                    Log.d("RegisterActivity", "Usuario de Auth eliminado tras fallo de batch.")
                                }
                            }
                        }

                } else {
                    // La creación en Auth falló
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error de registro: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


    // Maneja el clic en el botón de "atrás" de la barra de acción.
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Muestra un diálogo de confirmación y regresa al Login.
    private fun showSuccessDialogAndReturnToLogin() {
        AlertDialog.Builder(this)
            .setTitle("¡Registro Exitoso!")
            .setMessage("Tu cuenta ha sido creada. Ahora serás redirigido para iniciar sesión.")
            .setPositiveButton("Aceptar") { _, _ ->
                finish() // Cierra esta actividad y regresa a LoginActivity.
            }
            .setCancelable(false)
            .show()
    }
}
