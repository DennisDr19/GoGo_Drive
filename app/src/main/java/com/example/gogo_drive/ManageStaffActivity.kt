// Archivo: ManageStaffActivity.kt
package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageStaffActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var staffAdapter: StaffAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddStaff: FloatingActionButton
    private lateinit var staffFilterGroup: MaterialButtonToggleGroup

    private val allStaff = mutableListOf<Staff>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_staff)
        setupUI()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadStaffFromFirestore()
    }

    private fun setupUI() {
        firestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView_staff)
        progressBar = findViewById(R.id.progressBar_staff)
        fabAddStaff = findViewById(R.id.fab_add_staff)
        staffFilterGroup = findViewById(R.id.staff_filter_group)

        recyclerView.layoutManager = LinearLayoutManager(this)
        staffAdapter = StaffAdapter(
            onSwitchChanged = { staff, newState -> updateAccessInFirestore(staff, newState) },
            onEditClicked = { staff -> handleEditStaff(staff) },
            onDeleteClicked = { staff -> handleDeleteStaff(staff) }
        )
        recyclerView.adapter = staffAdapter
        staffFilterGroup.check(R.id.button_filter_all)
    }

    private fun setupListeners() {
        fabAddStaff.setOnClickListener {
            startActivity(Intent(this, RegisterStaffActivity::class.java))
        }

        staffFilterGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_filter_all -> applyFilter("todos")
                    R.id.button_filter_instructors -> applyFilter("instructor")
                    R.id.button_filter_secretaries -> applyFilter("administrador")
                }
            }
        }
    }

    private fun applyFilter(role: String) {
        val filteredList = if (role == "todos") {
            allStaff
        } else {
            allStaff.filter { it.rol.equals(role, ignoreCase = true) }
        }
        staffAdapter.updateList(filteredList.sortedBy { it.nombres })
    }

    private fun loadStaffFromFirestore() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rolesToFetch = listOf("instructor", "administrador")
                val rolesSnapshot = firestore.collection("roles").whereIn("rol", rolesToFetch).get().await()
                val staffRolesMap = rolesSnapshot.documents.associate { it.id to it.getString("rol")!! }

                if (staffRolesMap.keys.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        allStaff.clear()
                        applyFilter("todos")
                        progressBar.visibility = View.GONE
                    }
                    return@launch
                }

                val peopleSnapshot = firestore.collection("personas").whereIn(FieldPath.documentId(), staffRolesMap.keys.toList()).get().await()

                allStaff.clear()
                for (doc in peopleSnapshot.documents) {
                    doc.toObject(Staff::class.java)?.copy(
                        uid = doc.id,
                        rol = staffRolesMap[doc.id] ?: "Desconocido"
                    )?.let { allStaff.add(it) }
                }

                withContext(Dispatchers.Main) {
                    staffFilterGroup.check(R.id.button_filter_all)
                    applyFilter("todos")
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ManageStaffActivity", "Error al cargar personal", e)
                    Toast.makeText(this@ManageStaffActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateAccessInFirestore(staff: Staff, newState: Boolean) {
        firestore.collection("personas").document(staff.uid).update("acceso", newState)
            .addOnSuccessListener {
                val index = allStaff.indexOfFirst { it.uid == staff.uid }
                if (index != -1) {
                    allStaff[index] = allStaff[index].copy(acceso = newState)
                }
                Toast.makeText(this, "Acceso actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                val indexToRevert = allStaff.indexOfFirst { it.uid == staff.uid }
                if (indexToRevert != -1) staffAdapter.notifyItemChanged(indexToRevert)
            }
    }

    private fun handleEditStaff(staff: Staff) {
        val intent = Intent(this, RegisterStaffActivity::class.java)
        intent.putExtra("EXTRA_STAFF_TO_EDIT", staff)
        startActivity(intent)
    }

    private fun handleDeleteStaff(staff: Staff) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar a ${staff.nombres} ${staff.primerApellido}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> deleteStaffFromFirestore(staff) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteStaffFromFirestore(staff: Staff) {
        progressBar.visibility = View.VISIBLE
        val batch = firestore.batch()
        batch.delete(firestore.collection("personas").document(staff.uid))
        batch.delete(firestore.collection("roles").document(staff.uid))

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Personal eliminado", Toast.LENGTH_SHORT).show()
                loadStaffFromFirestore() // Recargar la lista para reflejar la eliminación
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener { progressBar.visibility = View.GONE }
    }
}
