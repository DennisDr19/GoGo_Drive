package com.example.gogo_drive

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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

class RegisterStaffActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private val TAG = "RegisterStaffActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_staff)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Registrar Personal"

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Referencias a las vistas del formulario
        progressBar = findViewById(R.id.progressBar)
        val nombresEditText = findViewById<EditText>(R.id.nombresEditText)
        val primerApellidoEditText = findViewById<EditText>(R.id.primerApellidoEditText)
        val segundoApellidoEditText = findViewById<EditText>(R.id.segundoApellidoEditText)
        val carnetEditText = findViewById<EditText>(R.id.carnetEditText)
        val complementoEditText = findViewById<EditText>(R.id.complementoEditText)
        val telefonoEditText = findViewById<EditText>(R.id.telefonoEditText)
        val direccionEditText = findViewById<EditText>(R.id.direccionEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // --- LÓGICA PARA EL MENÚ "CARGO" ---
        // 1. Opciones en mayúscula para que se vea mejor en el UI
        val staffRoles = arrayOf("ADMINISTRADOR", "INSTRUCTOR")
        val cargoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, staffRoles)
        val cargoAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.cargoAutoCompleteTextView)
        cargoAutoCompleteTextView.setAdapter(cargoAdapter)

        // --- LÓGICA PARA EL MENÚ "TURNO" ---
        // 1. Definir las opciones que aparecerán en el menú
        val turnos = arrayOf("MAÑANA", "TARDE", "COMPLETO")

        // 2. Crear un "adaptador" que conecta las opciones con el menú
        val turnoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, turnos)

        // 3. Encontrar el AutoCompleteTextView por su ID
        val turnoAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.turnoAutoCompleteTextView)

        // 4. Asignar el adaptador al menú para que muestre las opciones
        turnoAutoCompleteTextView.setAdapter(turnoAdapter)
        // =================================================================


        registerButton.setOnClickListener {
            // Recoger todos los datos del formulario
            val nombres = nombresEditText.text.toString().trim()
            val primerApellido = primerApellidoEditText.text.toString().trim()
            val segundoApellido = segundoApellidoEditText.text.toString().trim()
            val carnet = carnetEditText.text.toString().trim()
            val complemento = complementoEditText.text.toString().trim()
            val telefono = telefonoEditText.text.toString().trim()
            val direccion = direccionEditText.text.toString().trim()
            val cargo = cargoAutoCompleteTextView.text.toString().trim()
            val turno = turnoAutoCompleteTextView.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // ---- Se añade validación ----
            if (listOf(nombres, primerApellido, email, password, telefono, cargo, carnet, turno).any { it.isEmpty() }) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE
            registerUser(nombres, primerApellido, segundoApellido, carnet, complemento, telefono, direccion, cargo, turno, email, password)
        }
    }

    private fun registerUser(nombres: String, primerApellido: String, segundoApellido: String, carnet: String, complemento: String, telefono: String, direccion: String, cargo: String, turno: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { authTask ->
                if (authTask.isSuccessful) {
                    val newUser = authTask.result?.user
                    val newUserId = newUser?.uid

                    if (newUserId != null) {
                        saveUserDataToFirestore(newUserId, nombres, primerApellido, segundoApellido, carnet, complemento, telefono, direccion, cargo, turno, email)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Error crítico: No se pudo obtener el ID del nuevo usuario.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Error en createUserWithEmailAndPassword", authTask.exception)
                    Toast.makeText(this, "Error al crear usuario en Auth: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // ---- La función ahora acepta el parámetro "turno" ----
    private fun saveUserDataToFirestore(userId: String, nombres: String, primerApellido: String, segundoApellido: String, carnet: String, complemento: String, telefono: String, direccion: String, cargo: String, turno: String, email: String) {
        val batch = firestore.batch()
        val rolParaDb = cargo.lowercase() // Se convierte a minúscula solo para la colección 'roles'

        // 1. Documento en 'personas'
        val personRef = firestore.collection("personas").document(userId)
        val personData = hashMapOf(
            "nombres" to nombres,
            "primerApellido" to primerApellido,
            "segundoApellido" to segundoApellido,
            "carnet" to carnet,
            "complemento" to complemento,
            "telefono" to telefono,
            "direccion" to direccion,
            "correo" to email,
            "acceso" to true,
            "fechaCreacion" to Timestamp.now(),
            "turno" to turno
        )
        batch.set(personRef, personData)

        // 2. Documento en 'roles'
        val roleRef = firestore.collection("roles").document(userId)
        val roleData = hashMapOf("rol" to rolParaDb) // Se guarda "administrador" o "instructor"
        batch.set(roleRef, roleData)

        batch.commit()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                showSuccessDialogAndFinish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error al ejecutar el batch de Firestore.", e)
                Toast.makeText(this, "Error al guardar los datos. Contacta a soporte.", Toast.LENGTH_LONG).show()
                auth.currentUser?.delete()
            }
    }

    private fun showSuccessDialogAndFinish() {
        AlertDialog.Builder(this)
            .setTitle("¡Registro Exitoso!")
            .setMessage("La cuenta del nuevo personal ha sido creada.")
            .setPositiveButton("Aceptar") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
