package com.example.naloga5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MedicineAdapter(
    private val medicines: List<Medicine>,
    private val onEditClick: (Medicine) -> Unit,
    private val onDeleteClick: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    private var expandedPosition: Int = -1


    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textMedicineName)
        val textActiveIngredient: TextView = itemView.findViewById(R.id.textActiveIngredient)
        val textStats: TextView = itemView.findViewById(R.id.textMedicineStats)
        val textNotes: TextView = itemView.findViewById(R.id.textMedicineNotes)
        val textNotesTitle: TextView = itemView.findViewById(R.id.textMedicineNotesTitle)
        val btnDelete: View = itemView.findViewById(R.id.btnDeleteMedicine)
        val expandedActions: View = itemView.findViewById(R.id.expandedMedicineActions)

        fun bind(medicine: Medicine, position: Int) {
            textName.text = medicine.name
            textActiveIngredient.text = medicine.activeIngredient
            textStats.text = "Doza: ${medicine.minDoseMgKg}-${medicine.maxDoseMgKg} mg/kg | ${medicine.mgPerUnit}mg/${medicine.perMl}ml"

            val isExpanded = position == expandedPosition
            expandedActions.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Show notes if present and expanded
            if (isExpanded && medicine.notes.isNotBlank()) {
                textNotes.text = medicine.notes
                textNotes.visibility = View.VISIBLE
                textNotesTitle.visibility = View.VISIBLE
            } else {
                textNotes.visibility = View.GONE
                textNotesTitle.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val previousExpanded = expandedPosition
                expandedPosition = if (isExpanded) -1 else position

                if (previousExpanded != -1) notifyItemChanged(previousExpanded)
                if (expandedPosition != -1) notifyItemChanged(expandedPosition)
            }

            itemView.setOnLongClickListener {
                onEditClick(medicine)
                true
            }

            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle("Izbriši zdravilo")
                    .setMessage("Ali ste prepričani, da želite izbrisati ${medicine.name}?")
                    .setNegativeButton("Prekliči", null)
                    .setPositiveButton("Izbriši") { _, _ ->
                        val pos = adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            if (expandedPosition == pos) expandedPosition = -1
                            else if (expandedPosition > pos) expandedPosition--
                            onDeleteClick(medicine)
                        }
                    }
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(medicines[position], position)
    }

    override fun getItemCount(): Int = medicines.size
}
