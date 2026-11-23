package com.example.motobitacora.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/** Preferencias de la app: tema claro/oscuro y datos de la moto. */
object Ajustes {
    private const val ARCHIVO = "ajustes"
    private const val CLAVE_MODO = "modo_noche"
    private const val CLAVE_NOMBRE = "nombre_moto"
    private const val CLAVE_INTERVALO = "intervalo_servicio"
    private const val CLAVE_RECORDATORIOS = "recordatorios"

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    // ---- Tema ----
    fun aplicarTema(context: Context) {
        val modo = prefs(context).getInt(CLAVE_MODO, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(modo)
    }

    /** Cambia entre claro y oscuro y lo recuerda para la próxima vez. */
    fun alternarTema(context: Context, esOscuroAhora: Boolean) {
        val nuevo = if (esOscuroAhora) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        prefs(context).edit().putInt(CLAVE_MODO, nuevo).apply()
        AppCompatDelegate.setDefaultNightMode(nuevo)
    }

    // ---- Datos de la moto ----
    fun nombreMoto(context: Context): String =
        prefs(context).getString(CLAVE_NOMBRE, "") ?: ""

    fun intervaloServicio(context: Context): Int =
        prefs(context).getInt(CLAVE_INTERVALO, 0)

    fun recordatoriosActivados(context: Context): Boolean =
        prefs(context).getBoolean(CLAVE_RECORDATORIOS, false)

    fun guardarDatosMoto(context: Context, nombre: String, intervalo: Int, recordatorios: Boolean) {
        prefs(context).edit()
            .putString(CLAVE_NOMBRE, nombre)
            .putInt(CLAVE_INTERVALO, intervalo)
            .putBoolean(CLAVE_RECORDATORIOS, recordatorios)
            .apply()
    }
}