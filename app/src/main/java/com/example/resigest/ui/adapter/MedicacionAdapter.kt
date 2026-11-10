package com.example.resigest.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.resigest.data.model.MedicacionItem
import com.example.resigest.databinding.ItemMedicacionTablaBinding

class MedicacionAdapter(
    private val onFirmarToma: (MedicacionItem, String) -> Unit
) : RecyclerView.Adapter<MedicacionAdapter.MedicacionViewHolder>() {

    private var items = listOf<MedicacionItem>()

    fun setData(nuevos: List<MedicacionItem>) {
        this.items = nuevos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicacionViewHolder {
        val binding = ItemMedicacionTablaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MedicacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicacionViewHolder, position: Int) {
        val med = items[position]
        holder.binding.tvNombreFarmaco.text = "${med.farmaco} ${med.dosis}"
        holder.binding.tvViaDosis.text = "Vía ${med.via}\n${med.observaciones ?: "Pautado"}"

        // Ahora solo pasamos el evento de clic al MainActivity, sin forzar booleanos
        configurarCelda(holder.binding.cellDesayuno, holder.binding.txtDesayuno, med.pauta_desayuno, med.firmado_desayuno, "09:00") {
            onFirmarToma(med, "DES")
        }

        configurarCelda(holder.binding.cellComida, holder.binding.txtComida, med.pauta_comida, med.firmado_comida, "14:00") {
            onFirmarToma(med, "COM")
        }

        configurarCelda(holder.binding.cellMerienda, holder.binding.txtMerienda, med.pauta_merienda, med.firmado_merienda, "17:00") {
            onFirmarToma(med, "MER")
        }

        configurarCelda(holder.binding.cellCena, holder.binding.txtCena, med.pauta_cena, med.firmado_cena, "21:00") {
            onFirmarToma(med, "CEN")
        }

        configurarCelda(holder.binding.cellNoche, holder.binding.txtNoche, med.pauta_noche, med.firmado_noche, "00:00") {
            onFirmarToma(med, "NOC")
        }
    }

    private fun configurarCelda(
        container: FrameLayout,
        txt: TextView,
        pautada: Boolean,
        firmada: Boolean,
        hora: String,
        onClick: () -> Unit
    ) {
        if (!pautada) {
            container.alpha = 0.2f
            container.setBackgroundColor(Color.TRANSPARENT)
            txt.text = "-"
            container.setOnClickListener(null)
            return
        }

        container.alpha = 1.0f

        // Pintamos los colores según si viene firmada de MySQL o no
        if (firmada) {
            container.setBackgroundColor(Color.parseColor("#2E7D32"))
            txt.text = "✓ $hora"
            txt.setTextColor(Color.WHITE)
        } else {
            container.setBackgroundColor(Color.parseColor("#ECEFF1"))
            txt.text = hora
            txt.setTextColor(Color.parseColor("#0B4F8A"))
        }

        // EL CAMBIO CLAVE: El botón SIEMPRE reacciona al clic, sea verde o gris
        container.setOnClickListener { onClick() }
    }

    override fun getItemCount(): Int = items.size

    inner class MedicacionViewHolder(val binding: ItemMedicacionTablaBinding) :
        RecyclerView.ViewHolder(binding.root)
}