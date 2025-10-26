// Archivo: LicenseAdapter.kt
package com.example.gogo_drive

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial

class LicenseAdapter(
    private var licenses: List<License>,
    private val onAvailabilityChanged: (License, Boolean) -> Unit
) : RecyclerView.Adapter<LicenseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_license, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val license = licenses[position]
        holder.bind(license)
    }

    override fun getItemCount(): Int = licenses.size

    fun updateList(newLicenses: List<License>) {
        licenses = newLicenses
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Vistas del layout
        private val licenseTypeTextView: TextView = itemView.findViewById(R.id.licenseTypeTextView)
        private val availabilitySwitch: SwitchMaterial = itemView.findViewById(R.id.availabilitySwitch)
        // Nueva vista para el texto de estado
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(license: License) {
            licenseTypeTextView.text = license.tipoLicencia

            // Lógica para actualizar el texto y el color del estado
            updateStatusUI(license.disponible)

            // Configurar el listener del switch
            availabilitySwitch.setOnCheckedChangeListener(null) // Previene disparos accidentales
            availabilitySwitch.isChecked = license.disponible
            availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
                // Cuando el usuario interactúa, actualiza la UI y notifica el cambio
                updateStatusUI(isChecked)
                onAvailabilityChanged(license, isChecked)
            }
        }

        // Función auxiliar para mantener la UI consistente
        private fun updateStatusUI(isAvailable: Boolean) {
            if (isAvailable) {
                statusTextView.text = "ACTIVO"
                statusTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_700)) // Un color verde
            } else {
                statusTextView.text = "INACTIVO"
                statusTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.red_700)) // Un color rojo
            }
        }
    }
}
