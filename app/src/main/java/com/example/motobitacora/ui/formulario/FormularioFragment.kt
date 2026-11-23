package com.example.motobitacora.ui.formulario

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.motobitacora.R
import com.example.motobitacora.data.DbHelper
import com.example.motobitacora.data.Registro
import com.example.motobitacora.databinding.FragmentFormularioBinding
import com.example.motobitacora.util.Formato
import com.example.motobitacora.util.Recordatorio
import java.util.Calendar
import java.util.Locale

/** Pantalla 2: formulario para crear o editar un registro, con validación. */
class FormularioFragment : Fragment() {

    private var _binding: FragmentFormularioBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DbHelper
    private lateinit var tipos: Array<String>
    private var registroId = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFormularioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DbHelper.obtener(requireContext())
        registroId = arguments?.getLong("registroId", 0L) ?: 0L
        tipos = resources.getStringArray(R.array.tipos_registro)

        binding.actTipo.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tipos)
        )

        // Solo se llenan los campos la primera vez (al rotar, Android los conserva).
        if (savedInstanceState == null) {
            if (registroId != 0L) cargarRegistro() else prepararRegistroNuevo()
        }

        binding.etFecha.setOnClickListener { mostrarCalendario() }
        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnCancelar.setOnClickListener { findNavController().navigateUp() }

        // Quita el mensaje de error en cuanto el usuario corrige el campo.
        binding.etDescripcion.doAfterTextChanged { binding.tilDescripcion.error = null }
        binding.etKm.doAfterTextChanged { binding.tilKm.error = null }
        binding.etCosto.doAfterTextChanged { binding.tilCosto.error = null }
        binding.actTipo.doAfterTextChanged { binding.tilTipo.error = null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun prepararRegistroNuevo() {
        binding.actTipo.setText(tipos[0], false)
        val hoy = Calendar.getInstance()
        binding.etFecha.setText(
            Formato.fechaLegible(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH) + 1, hoy.get(Calendar.DAY_OF_MONTH))
        )
    }

    private fun cargarRegistro() {
        val registro = db.obtenerPorId(registroId)
        if (registro == null) {
            Toast.makeText(requireContext(), R.string.registro_no_encontrado, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }
        binding.actTipo.setText(registro.tipo, false)
        binding.etFecha.setText(Formato.isoALegible(registro.fecha))
        binding.etKm.setText(registro.km.toString())
        binding.etCosto.setText(String.format(Locale.US, "%.2f", registro.costo))
        binding.etDescripcion.setText(registro.descripcion)
    }

    private fun mostrarCalendario() {
        val actual = Calendar.getInstance()
        val (anio, mes, dia) = Formato.partesDeLegible(binding.etFecha.text.toString())
            ?: Triple(actual.get(Calendar.YEAR), actual.get(Calendar.MONTH) + 1, actual.get(Calendar.DAY_OF_MONTH))

        val dialogo = DatePickerDialog(requireContext(), { _, a, m, d ->
            binding.etFecha.setText(Formato.fechaLegible(a, m + 1, d))
            binding.tilFecha.error = null
        }, anio, mes - 1, dia)
        dialogo.datePicker.maxDate = System.currentTimeMillis() // no se permiten fechas futuras
        dialogo.show()
    }

    /** Valida los campos. Devuelve el Registro listo para guardar, o null si hay errores. */
    private fun leerFormulario(): Registro? {
        var correcto = true

        val tipo = binding.actTipo.text.toString()
        if (tipo !in tipos) {
            binding.tilTipo.error = getString(R.string.error_tipo)
            correcto = false
        }

        val fecha = binding.etFecha.text.toString().trim()
        val fechaIso = Formato.legibleAIso(fecha)
        if (fecha.isEmpty() || fechaIso == null) {
            binding.tilFecha.error = getString(R.string.error_fecha)
            correcto = false
        }

        val descripcion = binding.etDescripcion.text.toString().trim()
        if (descripcion.length < 3) {
            binding.tilDescripcion.error = getString(R.string.error_descripcion)
            correcto = false
        }

        val km = binding.etKm.text.toString().trim().toIntOrNull()
        if (km == null || km < 0 || km > 999_999) {
            binding.tilKm.error = getString(R.string.error_km)
            correcto = false
        }

        val costo = binding.etCosto.text.toString().trim().replace(',', '.').toDoubleOrNull()
        if (costo == null || costo < 0 || costo > 999_999.99) {
            binding.tilCosto.error = getString(R.string.error_costo)
            correcto = false
        }

        if (!correcto) return null
        return Registro(registroId, tipo, descripcion, fechaIso!!, km!!, costo!!)
    }

    private fun guardar() {
        val registro = leerFormulario() ?: return
        if (registroId == 0L) db.insertar(registro) else db.actualizar(registro)
        Recordatorio.revisar(requireContext()) // avisa si el próximo servicio está cerca o vencido
        Toast.makeText(requireContext(), R.string.registro_guardado, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}