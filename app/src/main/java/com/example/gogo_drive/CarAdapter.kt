// Contenido para el archivo: CarAdapter.kt

package com.example.gogo_drive

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarAdapter(
    private var carList: List<Car>,
    private val onEditClick: (Car) -> Unit,
    private val onDeleteClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        holder.bind(car)
    }

    override fun getItemCount(): Int = carList.size

    fun updateList(newList: List<Car>) {
        carList = newList
        notifyDataSetChanged()
    }

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placaTextView: TextView = itemView.findViewById(R.id.placaTextView)
        private val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        private val marcaModeloTextView: TextView = itemView.findViewById(R.id.marcaModeloTextView)
        private val numeroTallerTextView: TextView = itemView.findViewById(R.id.numeroTallerTextView)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(car: Car) {
            placaTextView.text = car.placa
            estadoTextView.text = car.estado
            marcaModeloTextView.text = "${car.marca} (${car.modelo})"
            numeroTallerTextView.text = "Nº de veces en taller: ${car.numeroTaller}"

            val estadoColor = when (car.estado) {
                "Habilitado" -> Color.parseColor("#388E3C") // Verde
                "En Taller" -> Color.parseColor("#FBC02D") // Amarillo
                "Deshabilitado" -> Color.parseColor("#D32F2F") // Rojo
                else -> Color.GRAY
            }
            estadoTextView.setTextColor(estadoColor)

            editButton.setOnClickListener { onEditClick(car) }
            deleteButton.setOnClickListener { onDeleteClick(car) }
        }
    }
}
