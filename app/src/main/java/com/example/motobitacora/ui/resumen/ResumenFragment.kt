package com.example.motobitacora.ui.resumen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.motobitacora.R
import com.example.motobitacora.data.DbHelper
import com.example.motobitacora.data.Registro
import com.example.motobitacora.databinding.FragmentResumenBinding
import com.example.motobitacora.util.Ajustes
import com.example.motobitacora.util.Formato
import com.example.motobitacora.util.ProximoServicio

/** Pantalla 3: totales de gasto y kilómetros, datos de la moto y próximo servicio. */
class ResumenFragment : Fragment() {

    private var _binding: FragmentResumenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResumenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarResumen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun mostrarResumen() {
        val contexto = requireContext()
        val registros = DbHelper.obtener(contexto).obtenerTodos()
        val tipos = resources.getStringArray(R.array.tipos_registro)

        val gastoTotal = registros.sumOf { it.costo }
        val kmActual = registros.maxOfOrNull { it.km } ?: 0
        val kmInicial = registros.minOfOrNull { it.km } ?: 0
        val kmRecorridos = if (registros.size >= 2) kmActual - kmInicial else 0

        val nombreMoto = Ajustes.nombreMoto(contexto)
        binding.tvMoto.text = if (nombreMoto.isEmpty()) {
            getString(R.string.resumen_moto_sin_datos)
        } else {
            getString(R.string.resumen_moto, nombreMoto)
        }

        binding.tvTotalRegistros.text = getString(R.string.resumen_total_registros, registros.size)
        binding.tvGastoTotal.text = getString(R.string.resumen_gasto_total, Formato.dinero(gastoTotal))
        binding.tvKmActual.text = getString(R.string.resumen_km_actual, Formato.numero(kmActual))
        binding.tvKmRecorridos.text = getString(R.string.resumen_km_recorridos, Formato.numero(kmRecorridos))

        // La lista ya viene ordenada del más reciente al más antiguo.
        val ultimoServicio = registros.firstOrNull { it.tipo == tipos[0] }
        binding.tvUltimoServicio.text = if (ultimoServicio == null) {
            getString(R.string.resumen_sin_servicio)
        } else {
            getString(
                R.string.resumen_ultimo_servicio,
                Formato.isoALegible(ultimoServicio.fecha),
                Formato.numero(ultimoServicio.km)
            )
        }

        binding.tvProximoServicio.text = textoProximoServicio(registros, tipos[0])

        binding.tvPorTipo.text = tipos.joinToString("\n") { tipo ->
            val subtotal = registros.filter { it.tipo == tipo }.sumOf { it.costo }
            getString(R.string.resumen_por_tipo_linea, tipo, Formato.dinero(subtotal))
        }
    }

    private fun textoProximoServicio(registros: List<Registro>, tipoServicio: String): String {
        val intervalo = Ajustes.intervaloServicio(requireContext())
        if (intervalo <= 0) return getString(R.string.resumen_proximo_sin_intervalo)

        val estado = ProximoServicio.calcular(registros, intervalo, tipoServicio)
            ?: return getString(R.string.resumen_proximo_sin_servicio)

        return if (estado.faltan >= 0) {
            getString(R.string.resumen_proximo, Formato.numero(estado.proximoKm), Formato.numero(estado.faltan))
        } else {
            getString(R.string.resumen_proximo_vencido, Formato.numero(-estado.faltan))
        }
    }
}