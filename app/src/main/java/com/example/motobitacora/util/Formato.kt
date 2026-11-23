package com.example.motobitacora.util

import java.util.Locale

/** Funciones para mostrar dinero, kilómetros y fechas. */
object Formato {
    private val LOCAL = Locale.forLanguageTag("es-MX")

    fun dinero(valor: Double): String = String.format(LOCAL, "\$%,.2f", valor)

    fun numero(valor: Int): String = String.format(LOCAL, "%,d", valor)

    /** Arma una fecha dd/MM/yyyy. */
    fun fechaLegible(anio: Int, mes: Int, dia: Int): String =
        String.format(LOCAL, "%02d/%02d/%04d", dia, mes, anio)

    /** yyyy-MM-dd  ->  dd/MM/yyyy */
    fun isoALegible(iso: String): String {
        val p = iso.split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
    }

    /** dd/MM/yyyy  ->  yyyy-MM-dd (null si el texto no es válido) */
    fun legibleAIso(legible: String): String? {
        val p = legible.split("/")
        return if (p.size == 3) "${p[2]}-${p[1]}-${p[0]}" else null
    }

    /** dd/MM/yyyy  ->  Triple(año, mes, día) (null si el texto no es válido) */
    fun partesDeLegible(legible: String): Triple<Int, Int, Int>? {
        val p = legible.split("/")
        if (p.size != 3) return null
        val dia = p[0].toIntOrNull() ?: return null
        val mes = p[1].toIntOrNull() ?: return null
        val anio = p[2].toIntOrNull() ?: return null
        return Triple(anio, mes, dia)
    }
}
