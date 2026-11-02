package com.example.resigest.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.resigest.data.model.HistorialItem
import com.example.resigest.databinding.ItemHistorialBinding

class HistorialAdapter : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    private var lista = listOf<HistorialItem>()

    fun setData(items: List<HistorialItem>) {
        this.lista = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val binding = ItemHistorialBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistorialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.tvHistorialTipo.text = item.tipo_registro
        holder.binding.tvHistorialFecha.text = item.fecha_hora
        holder.binding.tvHistorialProfesional.text = item.profesional
        holder.binding.tvHistorialDetalle.text = "${item.titulo}: ${item.resultado}"
    }

    override fun getItemCount(): Int = lista.size

    inner class HistorialViewHolder(val binding: ItemHistorialBinding) :
        RecyclerView.ViewHolder(binding.root)
}