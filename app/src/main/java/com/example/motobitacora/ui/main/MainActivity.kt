package com.example.motobitacora.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.motobitacora.R
import com.example.motobitacora.databinding.ActivityMainBinding
import com.example.motobitacora.ui.login.LoginActivity
import com.example.motobitacora.util.Ajustes
import com.example.motobitacora.util.Sesion
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Contenedor de las pantallas protegidas (Historial, Registro, Resumen y Datos de mi moto).
 * Si no hay sesión abierta, manda al login.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acceso protegido: sin huella no se muestra nada.
        if (!Sesion.autenticada) {
            irAlLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        setupActionBarWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        if (!Sesion.autenticada) irAlLogin()
    }

    override fun onStop() {
        super.onStop()
        // Si la app pasa a segundo plano (no por rotar o cambiar de tema), se vuelve a bloquear.
        if (!isChangingConfigurations && !isFinishing) Sesion.cerrar()
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.accion_resumen -> {
            if (navController.currentDestination?.id != R.id.resumenFragment) {
                navController.navigate(R.id.resumenFragment)
            }
            true
        }
        R.id.accion_moto -> {
            if (navController.currentDestination?.id != R.id.motoFragment) {
                navController.navigate(R.id.motoFragment)
            }
            true
        }
        R.id.accion_tema -> {
            val esOscuro = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
            Ajustes.alternarTema(this, esOscuro)
            true
        }
        R.id.accion_cerrar_sesion -> {
            confirmarCierreDeSesion()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun confirmarCierreDeSesion() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cerrar_sesion_titulo)
            .setMessage(R.string.cerrar_sesion_mensaje)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.cerrar_sesion) { _, _ ->
                Sesion.cerrar()
                irAlLogin()
            }
            .show()
    }

    /** Abre el login y borra la pila de pantallas para que el botón atrás no regrese aquí. */
    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}