package com.example.motobitacora.ui.moto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.motobitacora.R
import com.example.motobitacora.databinding.FragmentMotoBinding
import com.example.motobitacora.util.Ajustes
import com.example.motobitacora.util.Recordatorio

/** Pantalla "Datos de mi moto": nombre, intervalo de servicio y recordatorios. */
class MotoFragment : Fragment() {

    private var _binding: FragmentMotoBinding? = null
    private val binding get() = _binding!!

    // Pide el permiso de notificaciones (obligatorio desde Android 13).
    private val pedirPermiso = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (!concedido && _binding != null) {
            binding.swRecordatorios.isChecked = false
            Toast.makeText(requireContext(), R.string.permiso_notificaciones_denegado, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contexto = requireContext()

        if (savedInstanceState == null) {
            binding.etNombre.setText(Ajustes.nombreMoto(contexto))
            val intervalo = Ajustes.intervaloServicio(contexto)
            if (intervalo > 0) binding.etIntervalo.setText(intervalo.toString())
            binding.swRecordatorios.isChecked = Ajustes.recordatoriosActivados(contexto)
        }

        binding.swRecordatorios.setOnCheckedChangeListener { _, activado ->
            if (activado && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.etNombre.doAfterTextChanged { binding.tilNombre.error = null }
        binding.etIntervalo.doAfterTextChanged { binding.tilIntervalo.error = null }
        binding.btnGuardar.setOnClickListener { guardar() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun guardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val intervalo = binding.etIntervalo.text.toString().trim().toIntOrNull()
        var correcto = true

        if (nombre.length < 2) {
            binding.tilNombre.error = getString(R.string.error_nombre_moto)
            correcto = false
        }
        if (intervalo == null || intervalo < 100 || intervalo > 50_000) {
            binding.tilIntervalo.error = getString(R.string.error_intervalo)
            correcto = false
        }
        if (!correcto) return

        Ajustes.guardarDatosMoto(requireContext(), nombre, intervalo!!, binding.swRecordatorios.isChecked)
        Recordatorio.revisar(requireContext()) // si ya está cerca, avisa de una vez
        Toast.makeText(requireContext(), R.string.moto_guardada, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}