// Contenido para el archivo: Car.kt

package com.example.gogo_drive

import java.io.Serializable

data class Car(
    val id: String = "",
    val placa: String = "",
    val marca: String = "",
    val modelo: Int = 0,
    val numeroTaller: Int = 0,
    val estado: String = ""
) : Serializable
