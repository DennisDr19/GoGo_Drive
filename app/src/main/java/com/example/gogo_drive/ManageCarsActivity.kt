package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageCarsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var carsRecyclerView: RecyclerView
    private lateinit var carAdapter: CarAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    private val TAG = "ManageCarsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_cars)

        initializeViews()
        setupToolbar()
        setupRecyclerView() // <-- Aquí se configura la lógica de Edición
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        fetchCarsData()
    }

    private fun initializeViews() {
        firestore = FirebaseFirestore.getInstance()
        carsRecyclerView = findViewById(R.id.carsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(
            carList = emptyList(),

            // ================================================================
            // ===== LÓGICA PARA EL BOTÓN EDITAR =====
            // ================================================================
            // Esto se ejecuta cuando CarAdapter invoca 'onEditClick'.
            onEditClick = { car ->
                // 1. (Opcional) Registra un mensaje para depuración.
                Log.d(TAG, "Iniciando edición para el auto con ID: ${car.id}")

                // 2. Crea una "intención" (Intent) para abrir la pantalla de registro/edición.
                val intent = Intent(this, RegisterCarActivity::class.java).apply {
                    // 3. Adjunta el objeto 'Car' completo al Intent.
                    //    RegisterCarActivity usará este dato para saber que está
                    //    en modo edición y para rellenar los campos.
                    putExtra("EXTRA_CAR_TO_EDIT", car)
                }

                // 4. Lanza la nueva actividad, pasándole el Intent con los datos.
                startActivity(intent)
            },
            // ================================================================

            onDeleteClick = { car ->
                showDeleteConfirmationDialog(car)
            }
        )
        carsRecyclerView.adapter = carAdapter
    }

    private fun setupFab() {
        val addCarFab: FloatingActionButton = findViewById(R.id.addCarFab)
        addCarFab.setOnClickListener {
            val intent = Intent(this, RegisterCarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchCarsData() {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("autos")
                    .orderBy("marca", Query.Direction.ASCENDING)
                    .get()
                    .await()
                val carList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id)
                }
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (carList.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        carsRecyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        carsRecyclerView.visibility = View.VISIBLE
                        carAdapter.updateList(carList)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e(TAG, "Error al cargar los vehículos", e)
                    Toast.makeText(this@ManageCarsActivity, "Error al cargar datos.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(car: Car) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Borrado")
            .setMessage("¿Estás seguro de que deseas eliminar el vehículo con placa ${car.placa}?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Sí, eliminar") { _, _ ->
                deleteCarFromFirestore(car)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCarFromFirestore(car: Car) {
        if (car.id.isEmpty()) {
            Toast.makeText(this, "Error: ID de vehículo inválido.", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        firestore.collection("autos").document(car.id)
            .delete()
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Vehículo eliminado con éxito.", Toast.LENGTH_SHORT).show()
                fetchCarsData()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e(TAG, "Error al eliminar vehículo", e)
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
