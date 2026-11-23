package com.example.motobitacora.data

/**
 * Un renglón de la bitácora: servicio, carga de combustible, pieza u otro gasto.
 * La fecha se guarda en formato ISO (yyyy-MM-dd) para poder ordenarla fácilmente.
 */
data class Registro(
    val id: Long = 0,
    val tipo: String,
    val descripcion: String,
    val fecha: String,
    val km: Int,
    val costo: Double
)
