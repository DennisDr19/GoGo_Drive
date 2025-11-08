// Archivo: StaffAdapter.kt
package com.example.gogo_drive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial

class StaffAdapter(
    private val onSwitchChanged: (staff: Staff, newState: Boolean) -> Unit,
    private val onEditClicked: (staff: Staff) -> Unit,
    private val onDeleteClicked: (staff: Staff) -> Unit
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    private var staffList: List<Staff> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staffMember = staffList[position]
        holder.bind(staffMember, onSwitchChanged, onEditClicked, onDeleteClicked)
    }

    override fun getItemCount(): Int = staffList.size

    fun updateList(newList: List<Staff>) {
        this.staffList = newList
        notifyDataSetChanged()
    }

    class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referencias a todos los componentes de la UI en item_staff.xml
        private val nombreCompleto: TextView = itemView.findViewById(R.id.textView_nombre_staff)
        private val correo: TextView = itemView.findViewById(R.id.textView_correo_staff)
        private val telefono: TextView = itemView.findViewById(R.id.textView_telefono_staff)
        private val carnet: TextView = itemView.findViewById(R.id.textView_carnet_staff)
        private val turno: TextView = itemView.findViewById(R.id.textView_turno_staff)
        private val rolChip: Chip = itemView.findViewById(R.id.chip_rol_staff)
        private val switchAcceso: SwitchMaterial = itemView.findViewById(R.id.switch_acceso_staff)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.button_edit_staff)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.button_delete_staff)

        fun bind(
            staff: Staff,
            switchListener: (Staff, Boolean) -> Unit,
            editListener: (Staff) -> Unit,
            deleteListener: (Staff) -> Unit
        ) {
            nombreCompleto.text = "${staff.nombres} ${staff.primerApellido}".trim()
            correo.text = staff.correo
            telefono.text = staff.telefono ?: "No disponible"
            carnet.text = "${staff.carnet ?: ""} ${staff.complemento ?: ""}".trim()
            turno.text = "Turno: ${staff.turno.replaceFirstChar { it.uppercase() }}"
            rolChip.text = staff.rol.uppercase()

            // Configurar el switch de acceso
            switchAcceso.setOnCheckedChangeListener(null) // Evitar disparos accidentales
            switchAcceso.isChecked = staff.acceso
            switchAcceso.setOnCheckedChangeListener { _, isChecked ->
                switchListener(staff, isChecked)
            }

            // Configurar los listeners para los botones de acción
            btnEdit.setOnClickListener { editListener(staff) }
            btnDelete.setOnClickListener { deleteListener(staff) }
        }
    }
}
