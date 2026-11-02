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

    inner class ResidenteViewHolder(private val binding: ItemResidenteCamaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(res: ResidenteItem, isSelected: Boolean) {
            binding.tvItemCama.text = "${res.habitacion}-${res.cama}"
            binding.tvItemNombre.text = "${res.apellidos}, ${res.nombre}"
            binding.tvItemDetalle.text = "${res.nhc} | Dieta: ${res.dieta}"

            binding.viewAlerta.visibility = if (res.aislamiento != "NINGUNO" || res.riesgo_caidas == "ALTO") {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (isSelected) {
                binding.cardCama.setCardBackgroundColor(Color.parseColor("#FFF9C4"))
            } else {
                binding.cardCama.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            binding.root.setOnClickListener {
                val prev = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(prev)
                notifyItemChanged(selectedIndex)
                onResidenteSelected(res)
            }
        }
    }
}