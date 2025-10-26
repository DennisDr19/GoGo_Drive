// Archivo: ManageLicensesActivity.kt
package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageLicensesActivity : AppCompatActivity() {

    private val TAG = "ManageLicensesActivity"
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: LicenseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_license)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadLicenses()
    }

    private fun initializeViews() {
        firestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.licensesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = LicenseAdapter(
            licenses = emptyList(),
            onAvailabilityChanged = { license, isAvailable ->
                updateLicenseAvailability(license, isAvailable)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun loadLicenses() {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("licencias").get().await()
                val licenses = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<License>()?.copy(id = doc.id)
                }

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (licenses.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateList(licenses)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e(TAG, "Error al cargar las licencias", e)
                    Toast.makeText(this@ManageLicensesActivity, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateLicenseAvailability(license: License, isAvailable: Boolean) {
        if (license.id.isEmpty()) return

        val licenseRef = firestore.collection("licencias").document(license.id)

        licenseRef.update("disponible", isAvailable)
            .addOnSuccessListener {
                Log.d(TAG, "Licencia ${license.id} actualizada a disponible=$isAvailable")
                // Opcional: mostrar un Toast de éxito
                // Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al actualizar la licencia", e)
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                // Para revertir el switch visualmente si falla la actualización
                loadLicenses()
            }
    }
}
