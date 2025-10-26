// Archivo: License.kt
package com.example.gogo_drive

import com.google.firebase.firestore.Exclude

data class License(
    @get:Exclude
    var id: String = "",

    val tipoLicencia: String = "",
    val disponible: Boolean = false
)
