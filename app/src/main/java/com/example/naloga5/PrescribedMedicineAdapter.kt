package com.example.naloga5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrescribedMedicineAdapter(
    private val items: MutableList<PrescribedMedicineItem>,
    private val showRemoveAction: Boolean,
    private val onItemClick: ((PrescribedMedicineItem) -> Unit)? = null,
    private val onRemoveClick: ((PrescribedMedicineItem) -> Unit)? = null,
    private val onItemLongClick: ((PrescribedMedicineItem) -> Unit)? = null
) : RecyclerView.Adapter<PrescribedMedicineAdapter.PrescribedMedicineViewHolder>() {

    inner class PrescribedMedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textPrescribedMedicineName)
        private val textActiveIngredient: TextView = itemView.findViewById(R.id.textPrescribedMedicineActiveIngredient)
        private val textNotes: TextView = itemView.findViewById(R.id.textPrescribedMedicineNotes)
        private val btnDelete: View = itemView.findViewById(R.id.btnRemovePrescribedMedicine)

        fun bind(item: PrescribedMedicineItem) {
            textName.text = item.medicine.name

            val parts = mutableListOf<String>()
            if (item.medicine.activeIngredient.isNotBlank()) parts.add(item.medicine.activeIngredient)
            if (item.dose.isNotBlank()) parts.add(item.dose)
            if (item.notes.isNotBlank()) parts.add(item.notes)
            textActiveIngredient.text = if (parts.isNotEmpty()) parts.joinToString(" • ") else ""

            btnDelete.visibility = if (showRemoveAction) View.VISIBLE else View.GONE
            btnDelete.setOnClickListener { onRemoveClick?.invoke(item) }

            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }

            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(item)
                onItemLongClick != null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescribedMedicineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prescribed_medicine, parent, false)
        return PrescribedMedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrescribedMedicineViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
