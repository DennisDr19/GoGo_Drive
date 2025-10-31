package com.example.gogo_drive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Adaptador para el RecyclerView que muestra la lista de estudiantes.
 * Conecta los datos de la clase Student con el layout item_student.xml.
 *
 * @param listenerAccion Una función lambda que se invoca cuando el estado de 'acceso' cambia.
 */
class StudentAdapter(
    // La función recibirá el UID del estudiante y el nuevo estado del switch (true/false)
    private val listenerAccion: (uid: String, nuevoEstado: Boolean) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    private var students: List<Student> = emptyList()

    /**
     * Crea un nuevo ViewHolder inflando el layout de la fila.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    /**
     * Vincula los datos de un estudiante específico con un ViewHolder.
     */
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student, listenerAccion)
    }

    /**
     * Devuelve la cantidad total de ítems en la lista.
     */
    override fun getItemCount(): Int = students.size

    /**
     * Actualiza la lista de estudiantes en el adaptador y notifica al RecyclerView
     * para que se redibuje.
     */
    fun updateList(newList: List<Student>) {
        this.students = newList
        notifyDataSetChanged()
    }

    /**
     * ViewHolder que contiene y gestiona las vistas de una única fila (un estudiante).
     */
    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referencias a las vistas dentro de item_student.xml
        private val nombreCompleto: TextView = itemView.findViewById(R.id.textView_nombre)
        private val correo: TextView = itemView.findViewById(R.id.textView_email)
        private val telefono: TextView = itemView.findViewById(R.id.textView_telefono)
        private val carnet: TextView = itemView.findViewById(R.id.textView_carnet)
        private val direccion: TextView = itemView.findViewById(R.id.textView_direccion)
        private val switchAcceso: SwitchMaterial = itemView.findViewById(R.id.switch_acceso)

        /**
         * Rellena las vistas con los datos del objeto Student.
         */
        fun bind(student: Student, listener: (String, Boolean) -> Unit) {
            nombreCompleto.text = "${student.nombres} ${student.primerApellido} ${student.segundoApellido}".trim()
            correo.text = student.correo
            telefono.text = student.telefono
            carnet.text = "CI: ${student.carnet} ${student.complemento}".trim()
            direccion.text = student.direccion

            // Configura el switch de acceso
            // Se quita el listener temporalmente para evitar que se dispare al setear el valor inicial
            switchAcceso.setOnCheckedChangeListener(null)
            switchAcceso.isChecked = student.acceso
            // Se vuelve a asignar el listener para capturar la interacción del usuario
            switchAcceso.setOnCheckedChangeListener { _, isChecked ->
                listener(student.uid, isChecked)
            }
        }
    }
}
