package com.example.gogo_drive

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageStudentActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    // Lista que contendrá solo a los estudiantes
    private val studentList = mutableListOf<Student>()
    private var currentFilter: Filter = Filter.ALL

    private enum class Filter { ALL, WITH_ACCESS, NO_ACCESS }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apuntando al layout correcto que ya tienes
        setContentView(R.layout.activity_gestion_student)

        setupUI()
        setupListeners()
        // El nombre de la función ahora es más específico
        loadStudentsFromFirestore()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Estudiantes" // Título más específico

        firestore = Firebase.firestore
        recyclerView = findViewById(R.id.recyclerView_students)
        progressBar = findViewById(R.id.progressBar_gestion)
        toggleGroup = findViewById(R.id.toggleButton_filter)

        studentAdapter = StudentAdapter { uid, newState ->
            updateAccessInFirestore(uid, newState)
        }
        recyclerView.adapter = studentAdapter
    }

    private fun setupListeners() {
        toggleGroup.check(R.id.button_todos)
        currentFilter = Filter.ALL

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentFilter = when (checkedId) {
                    R.id.button_con_acceso -> Filter.WITH_ACCESS
                    R.id.button_sin_acceso -> Filter.NO_ACCESS
                    else -> Filter.ALL
                }
                applyFilterAndUpdateUI()
            }
        }
    }

    private fun loadStudentsFromFirestore() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // PASO 1: Obtener los UIDs de la colección 'roles'
                val rolesSnapshot = firestore.collection("roles")
                    .whereEqualTo("rol", "estudiante")
                    .get().await()

                val studentUids = rolesSnapshot.documents.map { it.id }

                if (studentUids.isEmpty()) {
                    // Si no hay UIDs de estudiantes, no hay nada que mostrar
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ManageStudentActivity, "No se encontraron estudiantes.", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        studentList.clear()
                        applyFilterAndUpdateUI()
                    }
                    return@launch
                }

                // PASO 2: Obtener datos de 'personas' solo para los UIDs de estudiantes
                val peopleSnapshot = firestore.collection("personas")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), studentUids)
                    .get().await()

                studentList.clear()
                peopleSnapshot.documents.forEach { doc ->
                    studentList.add(
                        Student(
                            uid = doc.id,
                            nombres = doc.getString("nombres") ?: "",
                            primerApellido = doc.getString("primerApellido") ?: "",
                            segundoApellido = doc.getString("segundoApellido") ?: "",
                            correo = doc.getString("correo") ?: "",
                            telefono = doc.getString("telefono") ?: "",
                            carnet = doc.getString("carnet") ?: "",
                            complemento = doc.getString("complemento") ?: "",
                            direccion = doc.getString("direccion") ?: "",
                            acceso = doc.getBoolean("acceso") ?: false,
                            fechaCreacion = doc.getTimestamp("fechaCreacion")
                        )
                    )
                }

                // PASO 3: Actualizar la UI en el hilo principal
                withContext(Dispatchers.Main) {
                    applyFilterAndUpdateUI()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ManageStudentActivity", "Error al cargar estudiantes", e)
                    Toast.makeText(this@ManageStudentActivity, "Error al cargar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun applyFilterAndUpdateUI() {
        val filteredList = when (currentFilter) {
            Filter.WITH_ACCESS -> studentList.filter { it.acceso }
            Filter.NO_ACCESS -> studentList.filter { !it.acceso }
            Filter.ALL -> studentList
        }
        // Ordenamos por fecha de creación, los más nuevos primero
        studentAdapter.updateList(filteredList.sortedByDescending { it.fechaCreacion })
    }

    private fun updateAccessInFirestore(uid: String, newState: Boolean) {
        progressBar.visibility = View.VISIBLE
        recyclerView.suppressLayout(true)

        firestore.collection("personas").document(uid)
            .update("acceso", newState)
            .addOnSuccessListener {
                Log.d("ManageStudentActivity", "Acceso actualizado para $uid a $newState")

                // Optimización: actualiza el estado en la lista local sin recargar todo de Firestore
                val studentIndex = studentList.indexOfFirst { it.uid == uid }
                if (studentIndex != -1) {
                    studentList[studentIndex] = studentList[studentIndex].copy(acceso = newState)
                }
                // Vuelve a aplicar el filtro, el ítem puede desaparecer si ya no cumple la condición
                applyFilterAndUpdateUI()
            }
            .addOnFailureListener { e ->
                Log.e("ManageStudentActivity", "Error al actualizar acceso para $uid", e)
                Toast.makeText(this, "Error al guardar el cambio.", Toast.LENGTH_SHORT).show()
                // Si falla, revierte el cambio en la UI
                applyFilterAndUpdateUI()
            }
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
                recyclerView.suppressLayout(false)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
