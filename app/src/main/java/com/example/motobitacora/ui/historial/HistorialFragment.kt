package com.example.motobitacora.ui.historial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.motobitacora.R
import com.example.motobitacora.data.DbHelper
import com.example.motobitacora.data.Registro
import com.example.motobitacora.databinding.FragmentHistorialBinding
import com.example.motobitacora.util.Ajustes
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/** Pantalla 1: lista de registros con búsqueda por descripción y filtro por tipo. */
class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DbHelper
    private lateinit var adapter: RegistroAdapter

    private var todos: List<Registro> = emptyList()
    private var tipoFiltro: String? = null // null = mostrar todos los tipos

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DbHelper.obtener(requireContext())
        tipoFiltro = savedInstanceState?.getString(CLAVE_FILTRO)

        adapter = RegistroAdapter(
            alEditar = { registro -> abrirFormulario(registro.id) },
            alEliminar = { registro -> confirmarEliminacion(registro) }
        )
        binding.rvRegistros.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRegistros.adapter = adapter

        crearChips()
        binding.etBuscar.doAfterTextChanged { aplicarFiltros() }
        binding.fabAgregar.setOnClickListener { abrirFormulario(0L) }
    }

    override fun onResume() {
        super.onResume()
        // Muestra el nombre de la moto debajo del título.
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle =
            Ajustes.nombreMoto(requireContext()).ifEmpty { null }
        cargarRegistros()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLAVE_FILTRO, tipoFiltro)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvRegistros.adapter = null
        _binding = null // evita fugas de memoria
    }

    /** Crea un chip "Todos" y uno por cada tipo de registro. */
    private fun crearChips() {
        val inflador = LayoutInflater.from(requireContext())
        val opciones: List<String?> = listOf<String?>(null) + resources.getStringArray(R.array.tipos_registro).toList()
        for (tipo in opciones) {
            val chip = inflador.inflate(R.layout.chip_filtro, binding.chipGroup, false) as Chip
            chip.id = View.generateViewId()
            chip.text = tipo ?: getString(R.string.chip_todos)
            chip.isChecked = (tipo == tipoFiltro)
            chip.setOnClickListener {
                tipoFiltro = tipo
                aplicarFiltros()
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun cargarRegistros() {
        todos = db.obtenerTodos()
        aplicarFiltros()
    }

    /** Aplica a la vez el filtro por tipo y el texto de búsqueda. */
    private fun aplicarFiltros() {
        val b = _binding ?: return
        val texto = b.etBuscar.text.toString().trim()
        val filtrados = todos.filter { registro ->
            (tipoFiltro == null || registro.tipo == tipoFiltro) &&
                    registro.descripcion.contains(texto, ignoreCase = true)
        }
        adapter.actualizar(filtrados)

        when {
            todos.isEmpty() -> {
                b.tvVacio.setText(R.string.historial_vacio)
                b.tvVacio.visibility = View.VISIBLE
            }
            filtrados.isEmpty() -> {
                b.tvVacio.setText(R.string.historial_sin_resultados)
                b.tvVacio.visibility = View.VISIBLE
            }
            else -> b.tvVacio.visibility = View.GONE
        }
    }

    /** id = 0 significa "registro nuevo". */
    private fun abrirFormulario(id: Long) {
        findNavController().navigate(R.id.formularioFragment, bundleOf("registroId" to id))
    }

    private fun confirmarEliminacion(registro: Registro) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.eliminar_titulo)
            .setMessage(getString(R.string.eliminar_mensaje, registro.descripcion))
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                db.eliminar(registro.id)
                cargarRegistros()
                Snackbar.make(binding.root, R.string.registro_eliminado, Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    companion object {
        private const val CLAVE_FILTRO = "tipoFiltro"
    }
}