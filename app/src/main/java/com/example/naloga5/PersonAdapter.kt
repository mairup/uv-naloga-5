package com.example.naloga5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PersonAdapter(
    private val persons: List<Person>,
    private val onEditClick: (Person) -> Unit,
    private val onDeleteClick: (Person) -> Unit
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    private var expandedPosition: Int = -1

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textDetails: TextView = itemView.findViewById(R.id.textDetails)
        val textStats: TextView = itemView.findViewById(R.id.textStats)
        val btnDelete: View = itemView.findViewById(R.id.btnDelete)
        val expandedActions: View = itemView.findViewById(R.id.expandedActions)
        val textPrescribedMeds: TextView = itemView.findViewById(R.id.textPrescribedMeds)
        val textBmi: TextView = itemView.findViewById(R.id.textBmi)

        fun bind(person: Person, position: Int) {
            textName.text = "${person.firstName} ${person.lastName}"
            textDetails.text = "Spol: ${person.gender} | Rojstvo: ${person.dateOfBirth}"
            textStats.text = "Teža: ${person.weightKg}kg | Višina: ${person.heightCm}cm"

            val heightM = person.heightCm / 100.0
            val bmi = if (heightM > 0) person.weightKg / (heightM * heightM) else 0.0
            textBmi.text = String.format("ITM: %.1f", bmi)

            if (person.prescribedMedicines.isNotEmpty()) {
                textPrescribedMeds.text = person.prescribedMedicines.joinToString("\n") { "• $it" }
            } else {
                textPrescribedMeds.text = "Brez predpisanih zdravil"
            }

            val isExpanded = position == expandedPosition
            expandedActions.visibility = if (isExpanded) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val previousExpanded = expandedPosition
                expandedPosition = if (isExpanded) -1 else position

                if (previousExpanded != -1) notifyItemChanged(previousExpanded)
                if (expandedPosition != -1) notifyItemChanged(expandedPosition)
            }

            itemView.setOnLongClickListener {
                onEditClick(person)
                true
            }

            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle("Izbriši osebo")
                    .setMessage("Ali ste prepričani, da želite izbrisati ${person.firstName} ${person.lastName}?")
                    .setNegativeButton("Prekliči", null)
                    .setPositiveButton("Izbriši") { _, _ ->
                        val pos = adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            if (expandedPosition == pos) expandedPosition = -1
                            else if (expandedPosition > pos) expandedPosition--
                            onDeleteClick(person)
                        }
                    }
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(persons[position], position)
    }

    override fun getItemCount(): Int = persons.size
}
