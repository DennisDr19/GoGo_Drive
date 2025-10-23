// Contenido para el archivo: RegisterCarActivity.kt

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
    // Si contiene un auto, estamos en modo EDICIÓN. Si es null, estamos en modo REGISTRO.
    private var carToEdit: Car? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_car)

        // --- Flujo de inicialización CORRECTO ---

        // 1. Inicializar Firebase y TODAS las vistas primero para evitar errores de objeto nulo.
        firestore = FirebaseFirestore.getInstance()
        initializeViews()

        // 2. Configurar los listeners y componentes básicos.
        setupEstadoDropdown()
        toolbar.setNavigationOnClickListener { finish() } // Botón de atrás en la toolbar
        registerCarButton.setOnClickListener {
            // Este método ahora decidirá si registrar o actualizar
            handleRegistrationOrUpdate()
        }

        // 3. Comprobar si estamos en modo edición y actualizar la UI.
        //    Esto se hace al final, cuando todas las vistas ya existen y son seguras de usar.
        checkIfEditMode()
    }

    private fun initializeViews() {
        // Asegúrate de que todos estos IDs existen en tu activity_register_car.xml
        toolbar = findViewById(R.id.toolbar)
        placaEditText = findViewById(R.id.placaEditText)
        marcaEditText = findViewById(R.id.marcaEditText)
        modeloEditText = findViewById(R.id.modeloEditText)
        numeroTallerEditText = findViewById(R.id.numeroTallerEditText)
        estadoAutoCompleteTextView = findViewById(R.id.estadoAutoCompleteTextView)
        registerCarButton = findViewById(R.id.registerCarButton)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * Comprueba el Intent para ver si se le pasó un objeto 'Car'.
     * Si es así, configura la UI para el modo de edición.
     */
    private fun checkIfEditMode() {
        // Recuperamos el objeto Car que ManageCarsActivity nos envió
        carToEdit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_CAR_TO_EDIT", Car::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("EXTRA_CAR_TO_EDIT") as? Car
        }

        // Si carToEdit no es nulo, entramos en MODO EDICIÓN
        if (carToEdit != null) {
            Log.d(TAG, "Modo Edición activado para el auto: ${carToEdit!!.placa}")

            // Cambiamos los textos de la UI para reflejar la acción de editar
            toolbar.title = "Editar Vehículo"
            registerCarButton.text = "Actualizar Vehículo"

            // Llenamos los campos del formulario con los datos del auto existente
            populateFieldsForEdit()
        } else {
            // Si carToEdit es nulo, estamos en MODO REGISTRO
            Log.d(TAG, "Modo Registro: Creando un nuevo vehículo.")
            toolbar.title = "Registrar Vehículo"
        }
    }

    /**
     * Rellena los campos del formulario con los datos del 'carToEdit'.
     */
    private fun populateFieldsForEdit() {
        carToEdit?.let { car ->
            placaEditText.setText(car.placa)
            marcaEditText.setText(car.marca)
            modeloEditText.setText(car.modelo.toString())
            numeroTallerEditText.setText(car.numeroTaller.toString())
            // El 'false' es importante para que el AutoCompleteTextView no filtre la lista
            estadoAutoCompleteTextView.setText(car.estado, false)
        }
    }

    private fun setupEstadoDropdown() {
        val estados = arrayOf("Habilitado", "En Taller", "Deshabilitado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados)
        estadoAutoCompleteTextView.setAdapter(adapter)
    }

    /**
     * Valida los campos y decide si crear (save) o actualizar (update) el vehículo.
     */
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

        // --- Decisión clave: ¿Actualizar o Crear? ---
        if (carToEdit != null) {
            // Modo Edición: Llama a la función de ACTUALIZAR
            updateCarInFirestore(carToEdit!!.id, placa, marca, modelo, numeroTaller, estado)
        } else {
            // Modo Registro: Llama a la función de CREAR
            saveNewCarToFirestore(placa, marca, modelo, numeroTaller, estado)
        }
    }

    private fun validateFields(placa: String, marca: String, modelo: String, numeroTaller: String, estado: String): Boolean {
        if (listOf(placa, marca, modelo, numeroTaller, estado).any { it.isEmpty() }) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_LONG).show()
            return false
        }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val modeloAnio = modelo.toIntOrNull()
        if (modeloAnio == null || modeloAnio < 1980 || modeloAnio > currentYear + 1) {
            modeloEditText.error = "Ingresa un año válido (entre 1980 y ${currentYear + 1})."
            return false
        }
        return true
    }

    private fun saveNewCarToFirestore(placa: String, marca: String, modelo: Int, numeroTaller: Int, estado: String) {
        showLoading(true)
        val carData = hashMapOf(
            "placa" to placa, "marca" to marca, "modelo" to modelo,
            "numeroTaller" to numeroTaller, "estado" to estado,
            "fechaRegistro" to Timestamp.now() // La fecha solo se guarda al crear
        )
        firestore.collection("autos").add(carData).addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Vehículo registrado con éxito.", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad y vuelve a la lista
        }.addOnFailureListener { e ->
            handleFailure(e)
        }
    }

    /**
     * Actualiza un documento existente en Firestore usando su ID.
     */
    private fun updateCarInFirestore(id: String, placa: String, marca: String, modelo: Int, numeroTaller: Int, estado: String) {
        showLoading(true)
        val carData = mapOf(
            "placa" to placa, "marca" to marca, "modelo" to modelo,
            "numeroTaller" to numeroTaller, "estado" to estado
            // No actualizamos la fecha de registro para que se mantenga la original
        )
        firestore.collection("autos").document(id).update(carData).addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Vehículo actualizado con éxito.", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad y vuelve a la lista
        }.addOnFailureListener { e ->
            handleFailure(e)
        }
    }

    private fun handleFailure(e: Exception) {
        showLoading(false)
        Log.e(TAG, "Error en la operación de Firestore", e)
        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        registerCarButton.isEnabled = !isLoading
    }
}
