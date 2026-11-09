package com.example.resigest.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.resigest.data.model.MedicacionItem
import com.example.resigest.databinding.ItemMedicacionBinding

class MedicacionAdapter(
    private val onFirmarClick: (MedicacionItem) -> Unit
) : RecyclerView.Adapter<MedicacionAdapter.MedicacionViewHolder>() {

    private var items = listOf<MedicacionItem>()

    fun setData(nuevos: List<MedicacionItem>) {
        this.items = nuevos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicacionViewHolder {
        val binding = ItemMedicacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MedicacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicacionViewHolder, position: Int) {
        val med = items[position]
        holder.binding.tvFarmaco.text = "${med.farmaco} ${med.dosis}"
        holder.binding.tvHorarioVia.text = "${med.horario} • Vía ${med.via}"
        holder.binding.tvIndicacion.text = med.observaciones ?: "Sin indicaciones especiales"

        if (med.administrado) {
            holder.binding.btnFirmar.text = "✓ FIRMADO"
            holder.binding.btnFirmar.setBackgroundColor(Color.parseColor("#2E7D32"))
            holder.binding.btnFirmar.isEnabled = false
        } else {
            holder.binding.btnFirmar.text = "ADMINISTRAR"
            holder.binding.btnFirmar.setBackgroundColor(Color.parseColor("#0B4F8A"))
            holder.binding.btnFirmar.isEnabled = true
            holder.binding.btnFirmar.setOnClickListener {
                onFirmarClick(med)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class MedicacionViewHolder(val binding: ItemMedicacionBinding) :
        RecyclerView.ViewHolder(binding.root)
}