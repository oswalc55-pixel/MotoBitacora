package com.example.motobitacora.util

/**
 * Guarda en memoria si el usuario ya se autenticó con huella.
 * Al cerrar sesión, salir de la app o morir el proceso, vuelve a false,
 * así que siempre hay que pasar por el login.
 */
object Sesion {
    var autenticada: Boolean = false
        private set

    fun iniciar() {
        autenticada = true
    }

    fun cerrar() {
        autenticada = false
    }
}
