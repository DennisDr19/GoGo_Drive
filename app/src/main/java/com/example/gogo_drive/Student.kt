package com.example.gogo_drive

import com.google.firebase.Timestamp

data class Student(
    val uid: String,
    val nombres: String,
    val primerApellido: String,
    val segundoApellido: String,
    val correo: String,
    val telefono: String,
    val carnet: String,
    val complemento: String,
    val direccion: String,
    val acceso: Boolean,
    val fechaCreacion: Timestamp?
)
