package com.example.resigest.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.resigest.data.model.ResidenteItem
import com.example.resigest.databinding.ItemResidenteCamaBinding

class ResidenteAdapter(
    private val onResidenteSelected: (ResidenteItem) -> Unit
) : RecyclerView.Adapter<ResidenteAdapter.ResidenteViewHolder>() {

    private var items = listOf<ResidenteItem>()
    private var selectedIndex = 0

    fun setData(newItems: List<ResidenteItem>) {
        this.items = newItems
        this.selectedIndex = 0
        notifyDataSetChanged()
        if (items.isNotEmpty()) {
            onResidenteSelected(items[0])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidenteViewHolder {
        val binding = ItemResidenteCamaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ResidenteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResidenteViewHolder, position: Int) {
        holder.bind(items[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class ResidenteViewHolder(val binding: ItemResidenteCamaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(residente: ResidenteItem, isSelected: Boolean) {
            // Mapeo con los IDs exactos de item_residente_cama.xml
            binding.tvItemCama.text = "${residente.habitacion}-${residente.cama}"
            binding.tvItemNombre.text = "${residente.apellidos}, ${residente.nombre}"
            binding.tvItemDetalle.text = "NHC: ${residente.nhc} | Dieta: ${residente.dieta}"

            // Alerta roja: ÚNICA Y EXCLUSIVAMENTE para pacientes con alergias reales registradas
            val alergiasTexto = residente.alergias?.trim() ?: ""
            val tieneAlergia = alergiasTexto.isNotBlank() &&
                    !alergiasTexto.equals("Ninguna", ignoreCase = true) &&
                    !alergiasTexto.equals("Sin alergias", ignoreCase = true) &&
                    !alergiasTexto.equals("Sin alergias conocidas", ignoreCase = true) &&
                    !alergiasTexto.equals("No constan", ignoreCase = true)

            binding.viewAlerta.visibility = if (tieneAlergia) View.VISIBLE else View.GONE

            // Resaltado visual de la tarjeta seleccionada en el censo
            if (isSelected) {
                binding.cardCama.strokeColor = Color.parseColor("#0B4F8A")
                binding.cardCama.strokeWidth = 4
                binding.cardCama.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
            } else {
                binding.cardCama.strokeColor = Color.parseColor("#CFD8DC")
                binding.cardCama.strokeWidth = 2
                binding.cardCama.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            binding.root.setOnClickListener {
                val anterior = selectedIndex
                val posActual = adapterPosition
                if (posActual != RecyclerView.NO_POSITION) {
                    selectedIndex = posActual
                    notifyItemChanged(anterior)
                    notifyItemChanged(selectedIndex)
                    onResidenteSelected(residente)
                }
            }
        }
    }
}