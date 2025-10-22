package com.example.gogo_drive

import android.content.Intent
import android.os.Bundle
import android.widget.Button // ¡Importación necesaria!
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // ¡Importación necesaria!

class EstudentActivity : AppCompatActivity() {

    // Declara la variable para Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estudent)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 1. Encuentra el botón en el layout por su ID
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // 2. Asigna la acción que se ejecutará al hacer clic
        logoutButton.setOnClickListener {
            // Llama a la función para cerrar la sesión
            signOut()
        }
    }

    /**
     * Cierra la sesión activa del usuario y lo redirige a la pantalla de Login.
     */
    private fun signOut() {
        // Cierra la sesión en Firebase
        auth.signOut()

        // Prepara el Intent para ir a LoginActivity
        val intent = Intent(this, LoginActivity::class.java)

        // Estas "flags" son muy importantes:
        // FLAG_ACTIVITY_NEW_TASK: La nueva actividad será la primera en una nueva pila.
        // FLAG_ACTIVITY_CLEAR_TASK: Limpia todas las actividades anteriores.
        // Esto evita que el usuario pueda presionar "atrás" y volver a EstudentActivity
        // después de cerrar sesión.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Inicia LoginActivity
        startActivity(intent)

        // Cierra la actividad actual (EstudentActivity)
        finish()
    }
}
