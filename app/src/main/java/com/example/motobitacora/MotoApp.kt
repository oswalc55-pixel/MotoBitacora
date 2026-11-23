package com.example.motobitacora

import android.app.Application
import com.example.motobitacora.util.Ajustes

/** Se ejecuta al abrir la app: aplica el tema (claro/oscuro) guardado por el usuario. */
class MotoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Ajustes.aplicarTema(this)
    }
}
