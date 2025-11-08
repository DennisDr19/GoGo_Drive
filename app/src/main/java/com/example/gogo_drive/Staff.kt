// Archivo: Staff.kt
package com.example.gogo_drive

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize // Permite pasar este objeto entre actividades
data class Staff(
    val uid: String = "",
    var rol: String = "", // Es 'var' para que ManageStaffActivity pueda asignarlo después
    val nombres: String = "",
    val primerApellido: String = "",
    val segundoApellido: String? = null,
    val carnet: String? = null,
    val complemento: String? = null,
    val correo: String = "",
    val telefono: String? = null,
    val direccion: String? = null,
    val turno: String = "",
    val acceso: Boolean = false,
    val fechaCreacion: Timestamp = Timestamp.now()
) : Parcelable
