package com.example.gogo_drive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.Locale

class StaffAdapter(
    private val listenerAccion: (uid: String, nuevoEstado: Boolean) -> Unit
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    private var staffList: List<Staff> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(staffList[position], listenerAccion)
    }

    override fun getItemCount(): Int = staffList.size

    fun updateList(newList: List<Staff>) {
        this.staffList = newList
        notifyDataSetChanged()
    }

    class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreCompleto: TextView = itemView.findViewById(R.id.textView_nombre_staff)
        private val correo: TextView = itemView.findViewById(R.id.textView_email_staff)
        private val telefono: TextView = itemView.findViewById(R.id.textView_telefono_staff)
        private val turno: TextView = itemView.findViewById(R.id.textView_turno_staff)
        private val rol: TextView = itemView.findViewById(R.id.textView_rol_staff)
        private val switchAcceso: SwitchMaterial = itemView.findViewById(R.id.switch_acceso_staff)

        fun bind(staff: Staff, listener: (String, Boolean) -> Unit) {
            nombreCompleto.text = "${staff.nombres} ${staff.primerApellido}".trim()
            correo.text = staff.correo
            telefono.text = staff.telefono
            turno.text = "Turno: ${staff.turno.uppercase()}"
            rol.text = staff.rol.uppercase()

            switchAcceso.setOnCheckedChangeListener(null)
            switchAcceso.isChecked = staff.acceso
            switchAcceso.setOnCheckedChangeListener { _, isChecked ->
                listener(staff.uid, isChecked)
            }
        }
    }
}
