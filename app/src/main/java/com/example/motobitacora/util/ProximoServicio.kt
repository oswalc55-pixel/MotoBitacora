package com.example.motobitacora.util

import com.example.motobitacora.data.Registro

/** proximoKm = km en que toca el servicio; faltan = km restantes (negativo si ya se pasó). */
data class EstadoServicio(val proximoKm: Int, val faltan: Int)

object ProximoServicio {


    fun calcular(registros: List<Registro>, intervalo: Int, tipoServicio: String): EstadoServicio? {
        if (intervalo <= 0) return null
        val ultimoServicioKm = registros.filter { it.tipo == tipoServicio }.maxOfOrNull { it.km } ?: return null
        val kmActual = registros.maxOf { it.km }
        val proximo = ultimoServicioKm + intervalo
        return EstadoServicio(proximo, proximo - kmActual)
    }
}