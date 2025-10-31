package com.example.gogo_drive

import com.google.firebase.Timestamp

data class Staff(
    val uid: String,
    val nombres: String,
    val primerApellido: String,
    val segundoApellido: String,
    val correo: String,
    val telefono: String,
    val turno: String,
    val rol: String,
    val acceso: Boolean,
    val fechaCreacion: Timestamp?
)
