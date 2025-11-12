package com.example.gogo_drive

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegisterCarActivity : AppCompatActivity() {

    private val TAG = "RegisterCarActivity"
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    // --- Vistas del formulario ---
    private lateinit var placaEditText: TextInputEditText
    private lateinit var marcaEditText: TextInputEditText
    private lateinit var modeloEditText: TextInputEditText
    private lateinit var numeroTallerEditText: TextInputEditText
    private lateinit var estadoAutoCompleteTextView: AutoCompleteTextView
    private lateinit var registerCarButton: Button
    private lateinit var toolbar: MaterialToolbar

    // --- Variable CLAVE para la lógica "inteligente" ---
    private var carToEdit: Car? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_car)

        firestore = FirebaseFirestore.getInstance()
        initializeViews()

        setupEstadoDropdown()
        setSupportActionBar(toolbar) // ¡Importante para que la toolbar funcione como ActionBar!
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        registerCarButton.setOnClickListener {
            handleRegistrationOrUpdate()
        }
        checkIfEditMode()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        placaEditText = findViewById(R.id.placaEditText)
        marcaEditText = findViewById(R.id.marcaEditText)
        modeloEditText = findViewById(R.id.modeloEditText)
        numeroTallerEditText = findViewById(R.id.numeroTallerEditText)
        estadoAutoCompleteTextView = findViewById(R.id.estadoAutoCompleteTextView)
        registerCarButton = findViewById(R.id.registerCarButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun checkIfEditMode() {
        carToEdit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_CAR_TO_EDIT", Car::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("EXTRA_CAR_TO_EDIT") as? Car
        }

        if (carToEdit != null) {
            Log.d(TAG, "Modo Edición activado para el auto: ${carToEdit!!.placa}")
            toolbar.title = "Editar Vehículo"
            registerCarButton.text = "Actualizar Vehículo"
            placaEditText.isEnabled = false // No se puede editar la placa (que es el ID)
            populateFieldsForEdit()
        } else {
            Log.d(TAG, "Modo Registro: Creando un nuevo vehículo.")
            toolbar.title = "Registrar Vehículo"
        }
    }

    private fun populateFieldsForEdit() {
        carToEdit?.let { car ->
            placaEditText.setText(car.placa)
            marcaEditText.setText(car.marca)
            modeloEditText.setText(car.modelo.toString())
            numeroTallerEditText.setText(car.numeroTaller.toString())
            estadoAutoCompleteTextView.setText(car.estado, false)
        }
    }

    private fun setupEstadoDropdown() {
        val estados = arrayOf("Habilitado", "En Taller", "Deshabilitado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados)
        estadoAutoCompleteTextView.setAdapter(adapter)
    }

    private fun handleRegistrationOrUpdate() {
        val placa = placaEditText.text.toString().trim().uppercase()
        val marca = marcaEditText.text.toString().trim()
        val modeloStr = modeloEditText.text.toString().trim()
        val numeroTallerStr = numeroTallerEditText.text.toString().trim()
        val estado = estadoAutoCompleteTextView.text.toString().trim()

        if (!validateFields(placa, marca, modeloStr, numeroTallerStr, estado)) {
            return
        }

        val modelo = modeloStr.toInt()
        val numeroTaller = numeroTallerStr.toInt()

        if (carToEdit != null) {
            // Usa el ID del auto a editar (que es la placa original)
            updateCarInFirestore(carToEdit!!.id, placa, marca, modelo, numeroTaller, estado)
        } else {
            // Usa la nueva placa como ID del documento
            saveNewCarToFirestore(placa, marca, modelo, numeroTaller, estado)
        }
    }

    // --- FUNCIÓN DE VALIDACIÓN ---
    private fun validateFields(placa: String, marca: String, modeloStr: String, numeroTallerStr: String, estado: String): Boolean {
        var isValid = true
        // Limpiar errores previos
        placaEditText.error = null
        marcaEditText.error = null
        modeloEditText.error = null
        numeroTallerEditText.error = null
        estadoAutoCompleteTextView.error = null

        // 1. Validar que ningún campo esencial esté vacío
        if (listOf(placa, marca, modeloStr, numeroTallerStr, estado).any { it.isEmpty() }) {
            if(placa.isEmpty()) placaEditText.error = "La placa es requerida."
            if(marca.isEmpty()) marcaEditText.error = "La marca es requerida."
            if(modeloStr.isEmpty()) modeloEditText.error = "El modelo es requerido."
            if(numeroTallerStr.isEmpty()) numeroTallerEditText.error = "El número de taller es requerido."
            if(estado.isEmpty()) estadoAutoCompleteTextView.error = "Debe seleccionar un estado."
            isValid = false
        }

        // 2. Validación de Placa (mezcla de letras y números, entre 6 y 7 caracteres)
        // Solo valida si el campo no está vacío
        if (placa.isNotEmpty() && !placa.matches(Regex("^[A-Z0-9]{6,7}$"))) {
            placaEditText.error = "La placa debe tener 6 o 7 caracteres (letras y números)."
            isValid = false
        }

        // 3. Validación de Marca (solo letras, espacios y límite de 20 caracteres)
        if (marca.isNotEmpty()) {
            if (!marca.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
                marcaEditText.error = "La marca solo debe contener letras y espacios."
                isValid = false
            }
            if (marca.length > 20) {
                marcaEditText.error = "La marca no debe exceder los 20 caracteres."
                isValid = false
            }
        }

        // 4. Validación de Modelo (año lógico)
        if (modeloStr.isNotEmpty()) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val baseYear = 1950 // Año base para la validación
            val modeloAnio = modeloStr.toIntOrNull()
            if (modeloAnio == null || modeloAnio !in baseYear..currentYear) {
                modeloEditText.error = "Debe ser un año válido entre $baseYear y $currentYear."
                isValid = false
            }
        }

        // 5. Validación de número de taller (debe ser un número entero)
        if (numeroTallerStr.isNotEmpty() && numeroTallerStr.toIntOrNull() == null) {
            numeroTallerEditText.error = "Este campo solo admite números enteros."
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, "Por favor, corrige los errores marcados.", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    // --- LÓGICA DE GUARDADO ---
    private fun saveNewCarToFirestore(placa: String, marca: String, modelo: Int, numeroTaller: Int, estado: String) {
        showLoading(true)
        val carData = hashMapOf(
            "placa" to placa, "marca" to marca, "modelo" to modelo,
            "numeroTaller" to numeroTaller, "estado" to estado,
            "fechaRegistro" to Timestamp.now()
        )

        // Usar la placa como ID previene duplicados. Usamos .set() en lugar de .add()
        firestore.collection("autos").document(placa).set(carData).addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Vehículo registrado con éxito.", Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener { e ->
            handleFailure(e, isCreatingNew = true)
        }
    }

    private fun updateCarInFirestore(id: String, placa: String, marca: String, modelo: Int, numeroTaller: Int, estado: String) {
        showLoading(true)
        val carData = mapOf(
            "placa" to placa, "marca" to marca, "modelo" to modelo,
            "numeroTaller" to numeroTaller, "estado" to estado
        )
        // El ID es la placa original y no cambia.
        firestore.collection("autos").document(id).update(carData).addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Vehículo actualizado con éxito.", Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener { e ->
            handleFailure(e)
        }
    }

    private fun handleFailure(e: Exception, isCreatingNew: Boolean = false) {
        showLoading(false)
        Log.e(TAG, "Error en la operación de Firestore", e)
        if (isCreatingNew) {
            placaEditText.error = "Esta placa ya está registrada"
            Toast.makeText(this, "Error: La placa ya existe en la base de datos.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        registerCarButton.isEnabled = !isLoading
    }
}
