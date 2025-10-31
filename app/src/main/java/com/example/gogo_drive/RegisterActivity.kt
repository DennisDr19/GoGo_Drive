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
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    // Declara todos los EditText
    private lateinit var nombresEditText: EditText
    private lateinit var primerApellidoEditText: EditText
    private lateinit var segundoApellidoEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var carnetEditText: EditText
    private lateinit var complementoEditText: EditText
    private lateinit var direccionEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Crear cuenta de Estudiante"

        // Inicializa Firebase y las Vistas
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)

        nombresEditText = findViewById(R.id.nombresEditText)
        primerApellidoEditText = findViewById(R.id.primerApellidoEditText)
        segundoApellidoEditText = findViewById(R.id.segundoApellidoEditText)
        telefonoEditText = findViewById(R.id.telefonoEditText)
        carnetEditText = findViewById(R.id.carnetEditText)
        complementoEditText = findViewById(R.id.complementoEditText)
        direccionEditText = findViewById(R.id.direccionEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            handleRegistration()
        }
    }

    private fun handleRegistration() {
        val nombres = nombresEditText.text.toString().trim()
        val primerApellido = primerApellidoEditText.text.toString().trim()
        val segundoApellido = segundoApellidoEditText.text.toString().trim()
        val telefono = telefonoEditText.text.toString().trim()
        val carnet = carnetEditText.text.toString().trim()
        val complemento = complementoEditText.text.toString().trim()
        val direccion = direccionEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validación robusta
        if (listOf(nombres, primerApellido, email, password, telefono, carnet).any { it.isBlank() }) {
            Toast.makeText(this, "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_LONG).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres.", Toast.LENGTH_LONG).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        // El proceso de registro
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { authTask ->
            if (authTask.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error crítico: No se pudo obtener el ID de usuario.", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val userData = hashMapOf(
                    "nombres" to nombres,
                    "primerApellido" to primerApellido,
                    "segundoApellido" to segundoApellido,
                    "correo" to email,
                    "telefono" to telefono,
                    "carnet" to carnet,
                    "complemento" to complemento,
                    "direccion" to direccion,
                    "acceso" to true,
                    "fechaCreacion" to Timestamp.now()
                )

                // Escribe el mapa de datos directamente en la colección 'personas'
                firestore.collection("personas").document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Log.d("RegisterActivity", "¡ÉXITO! Usuario creado y perfil guardado en 'personas'.")
                        showSuccessDialogAndFinish()
                    }
                    .addOnFailureListener { firestoreException ->
                        // Si falla la escritura en Firestore, borramos el usuario de Auth para evitar inconsistencias
                        progressBar.visibility = View.GONE
                        Log.e("RegisterActivity", "FALLO al escribir en Firestore. Revirtiendo creación de usuario.", firestoreException)
                        Toast.makeText(this, "Error al guardar los datos. Contacta a soporte.", Toast.LENGTH_LONG).show()

                        auth.currentUser?.delete()
                    }

            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error de registro: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSuccessDialogAndFinish() {
        AlertDialog.Builder(this)
            .setTitle("¡Registro Exitoso!")
            .setMessage("La cuenta ha sido creada correctamente.")
            .setPositiveButton("Aceptar") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
