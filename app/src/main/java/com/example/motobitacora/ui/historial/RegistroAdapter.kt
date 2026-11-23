package com.example.motobitacora.ui.historial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.motobitacora.R
import com.example.motobitacora.data.Registro
import com.example.motobitacora.databinding.ItemRegistroBinding
import com.example.motobitacora.util.Formato

/** Adaptador del RecyclerView: dibuja cada registro de la bitácora. */
class RegistroAdapter(
    private val alEditar: (Registro) -> Unit,
    private val alEliminar: (Registro) -> Unit
) : RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder>() {

    private var registros: List<Registro> = emptyList()

    fun actualizar(nuevos: List<Registro>) {
        registros = nuevos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {
        val binding = ItemRegistroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegistroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        holder.enlazar(registros[position])
    }

    override fun getItemCount(): Int = registros.size

    inner class RegistroViewHolder(private val binding: ItemRegistroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun enlazar(registro: Registro) {
            val contexto = binding.root.context
            binding.tvTipo.text = registro.tipo
            binding.tvCosto.text = Formato.dinero(registro.costo)
            binding.tvDescripcion.text = registro.descripcion
            binding.tvDetalle.text = contexto.getString(
                R.string.detalle_registro,
                Formato.isoALegible(registro.fecha),
                Formato.numero(registro.km)
            )
            binding.root.setOnClickListener { alEditar(registro) }
            binding.btnEliminar.setOnClickListener { alEliminar(registro) }
        }
    }
}
