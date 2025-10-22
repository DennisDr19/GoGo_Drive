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
        supportActionBar?.title = "Crear cuenta de Estudiante"

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
        val direccionEditText = findViewById<EditText>(R.id.direccionEditText)
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
            val direccion = direccionEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validaciones básicas (La dirección puede ser opcional, así que no la incluimos aquí)
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
            registerUser(nombres, primerApellido, segundoApellido, telefono, carnet, complemento, direccion, email, password)
        }
    }

    private fun registerUser(nombres: String, primerApellido: String, segundoApellido: String, telefono: String, carnet: String, complemento: String, direccion: String, email: String, password: String) {
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

                    // --- INICIO DE LA TRANSACCIÓN POR LOTES (BATCH) ---
                    val batch = firestore.batch()

                    // OPERACIÓN 1: Guardar en la colección "personas"
                    val personRef = firestore.collection("personas").document(userId)
                    val personData = hashMapOf(
                        "nombres" to nombres,
                        "primerApellido" to primerApellido,
                        "segundoApellido" to segundoApellido,
                        "telefono" to telefono,
                        "carnet" to carnet,
                        "complemento" to complemento,
                        "direccion" to direccion,
                        "correo" to email,
                        "acceso" to true,
                        "fechaCreacion" to Timestamp.now()
                    )
                    batch.set(personRef, personData)

                    // OPERACIÓN 2: Guardar en la colección "estudiantes"
                    val studentRef = firestore.collection("estudiantes").document(userId)
                    val studentData = hashMapOf(
                        "activo" to true,
                        "direccion" to direccion, // Tomado del formulario
                        "telefono" to telefono,   // Tomado del formulario
                        "fechaInscripcion" to Timestamp.now()
                    )
                    batch.set(studentRef, studentData)

                    //      ¡NUEVO! OPERACIÓN 3: Asignar el rol en la colección "roles"
                    val roleRef = firestore.collection("roles").document(userId)
                    val roleData = hashMapOf(
                        "rol" to "estudiante" // Rol fijo para esta pantalla de registro
                    )
                    // Añadimos esta tercera operación al mismo lote
                    batch.set(roleRef, roleData)


                    // PASO FINAL: Ejecutar todas las operaciones a la vez
                    batch.commit()
                        .addOnSuccessListener {
                            // ¡Éxito total! Las 3 escrituras se completaron.
                            progressBar.visibility = View.GONE
                            Log.d("RegisterActivity", "Usuario, perfil y rol creados exitosamente.")
                            showSuccessDialogAndReturnToLogin()
                        }
                        .addOnFailureListener { batchException ->
                            // Si algo falló, NINGUNA de las 3 escrituras se realizó.
                            progressBar.visibility = View.GONE
                            Log.e("RegisterActivity", "FALLO en el batch. No se escribió nada en Firestore.", batchException)
                            Toast.makeText(this, "Error al guardar los datos de registro. Contacta a soporte.", Toast.LENGTH_LONG).show()

                            // Borrar el usuario de Auth para que pueda reintentar limpiamente.
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


    // Muestra un diálogo de confirmación y regresa al Login (sin cambios aquí).
    private fun showSuccessDialogAndReturnToLogin() {
        AlertDialog.Builder(this)
            .setTitle("¡Registro Exitoso!")
            .setMessage("Cuenta de estudiante creada exitosamente.")
            .setPositiveButton("Aceptar") { _, _ ->
                finish() // Cierra esta actividad y regresa a LoginActivity.
            }
            .setCancelable(false)
            .show()
    }
}
