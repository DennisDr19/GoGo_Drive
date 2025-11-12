package com.example.gogo_drive

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class RegisterActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private val TAG = "RegisterActivity"

    // Referencias a los TextInputLayout para mostrar errores
    private lateinit var nombresLayout: TextInputLayout
    private lateinit var primerApellidoLayout: TextInputLayout
    private lateinit var segundoApellidoLayout: TextInputLayout
    private lateinit var carnetLayout: TextInputLayout
    private lateinit var complementoLayout: TextInputLayout
    private lateinit var telefonoLayout: TextInputLayout
    private lateinit var direccionLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Registrar Estudiante"

        firestore = FirebaseFirestore.getInstance()

        initializeViews()

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            if (validateForm()) {
                progressBar.visibility = View.VISIBLE
                registerStudent()
            }
        }
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        nombresLayout = findViewById(R.id.nombresInputLayout)
        primerApellidoLayout = findViewById(R.id.primerApellidoInputLayout)
        segundoApellidoLayout = findViewById(R.id.segundoApellidoInputLayout)
        carnetLayout = findViewById(R.id.carnetInputLayout)
        complementoLayout = findViewById(R.id.complementoInputLayout)
        telefonoLayout = findViewById(R.id.telefonoInputLayout)
        direccionLayout = findViewById(R.id.direccionInputLayout)
        emailLayout = findViewById(R.id.emailInputLayout)
        passwordLayout = findViewById(R.id.passwordInputLayout)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        listOf(
            nombresLayout, primerApellidoLayout, segundoApellidoLayout,
            carnetLayout, complementoLayout, telefonoLayout, direccionLayout,
            emailLayout, passwordLayout
        ).forEach { it.error = null }

        val nombre = nombresLayout.editText?.text.toString().trim()
        val apellido1 = primerApellidoLayout.editText?.text.toString().trim()
        val apellido2 = segundoApellidoLayout.editText?.text.toString().trim()

        // --- LÍNEA CORREGIDA: Se añade ñ y Ñ a la Regex ---
        if (nombre.isEmpty() || !nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) || nombre.length > 50) {
            nombresLayout.error = "Nombre inválido (solo texto, máx. 50)"
            isValid = false
        }
        // --- LÍNEA CORREGIDA: Se añade ñ y Ñ a la Regex ---
        if (apellido1.isEmpty() || !apellido1.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) || apellido1.length > 50) {
            primerApellidoLayout.error = "Apellido inválido (solo texto, máx. 50)"
            isValid = false
        }
        // --- LÍNEA CORREGIDA: Se añade ñ y Ñ a la Regex ---
        if (apellido2.isNotEmpty() && (!apellido2.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) || apellido2.length > 50)) {
            segundoApellidoLayout.error = "Apellido inválido (solo texto, máx. 50)"
            isValid = false
        }

        val carnet = carnetLayout.editText?.text.toString().trim()
        if (!carnet.matches(Regex("^[0-9]{7}$"))) {
            carnetLayout.error = "Carnet inválido (debe tener 7 números)"
            isValid = false
        }

        val complemento = complementoLayout.editText?.text.toString().trim()
        if (complemento.length > 2) {
            complementoLayout.error = "Máx. 2 caracteres"
            isValid = false
        }

        val telefono = telefonoLayout.editText?.text.toString().trim()
        if (!telefono.matches(Regex("^[0-9]{8}$"))) {
            telefonoLayout.error = "Teléfono inválido (debe tener 8 números)"
            isValid = false
        }

        val email = emailLayout.editText?.text.toString().trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Formato de correo inválido"
            isValid = false
        }

        val password = passwordLayout.editText?.text.toString()
        if (password.length < 8) {
            passwordLayout.error = "La contraseña debe tener al menos 8 caracteres"
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, "Por favor, corrige los errores marcados", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun registerStudent() {
        val email = emailLayout.editText?.text.toString().trim()
        val password = passwordLayout.editText?.text.toString()

        val firebaseAppSecondary = Firebase.app("secondary")
        val tempAuth = FirebaseAuth.getInstance(firebaseAppSecondary)

        tempAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { authTask ->
                if (authTask.isSuccessful) {
                    val newUser = authTask.result?.user
                    if (newUser != null) {
                        saveStudentDataToFirestore(newUser.uid)
                    } else {
                        handleRegistrationError("Error crítico: No se pudo obtener el ID del nuevo usuario.")
                    }
                    tempAuth.signOut()
                } else {
                    handleRegistrationError("Error al crear usuario: ${authTask.exception?.message}")
                    emailLayout.error = "Este correo ya está en uso o es inválido."
                }
            }
    }

    private fun saveStudentDataToFirestore(userId: String) {
        val batch = firestore.batch()

        val personRef = firestore.collection("personas").document(userId)
        val personData = hashMapOf(
            "nombres" to nombresLayout.editText?.text.toString().trim(),
            "primerApellido" to primerApellidoLayout.editText?.text.toString().trim(),
            "segundoApellido" to segundoApellidoLayout.editText?.text.toString().trim(),
            "carnet" to carnetLayout.editText?.text.toString().trim(),
            "complemento" to complementoLayout.editText?.text.toString().trim(),
            "telefono" to telefonoLayout.editText?.text.toString().trim(),
            "direccion" to direccionLayout.editText?.text.toString().trim(),
            "correo" to emailLayout.editText?.text.toString().trim(),
            "acceso" to true,
            "fechaCreacion" to Timestamp.now()
        )
        batch.set(personRef, personData)

        val roleRef = firestore.collection("roles").document(userId)
        val roleData = hashMapOf("rol" to "estudiante")
        batch.set(roleRef, roleData)

        batch.commit()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                showSuccessDialogAndFinish()
            }
            .addOnFailureListener { e ->
                handleRegistrationError("Error al guardar los datos. Contacta a soporte.", e)
                Firebase.app("secondary").let { FirebaseAuth.getInstance(it).currentUser?.delete() }
            }
    }

    private fun handleRegistrationError(message: String, exception: Exception? = null) {
        progressBar.visibility = View.GONE
        Log.e(TAG, message, exception)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessDialogAndFinish() {
        AlertDialog.Builder(this)
            .setTitle("¡Registro Exitoso!")
            .setMessage("La cuenta del nuevo estudiante ha sido creada.")
            .setPositiveButton("Aceptar") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
