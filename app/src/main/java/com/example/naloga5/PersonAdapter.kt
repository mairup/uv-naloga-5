package com.example.naloga5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class PersonAdapter(
    private val persons: List<Person>,
    private val medicines: List<Medicine>,
    private val onEditClick: (Person) -> Unit,
    private val onDeleteClick: (Person) -> Unit,
    private val onManageMedicinesClick: (Person) -> Unit
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    private var expandedPosition: Int = -1

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textDetails: TextView = itemView.findViewById(R.id.textDetails)
        val textStats: TextView = itemView.findViewById(R.id.textStats)
        val btnDelete: View = itemView.findViewById(R.id.btnDelete)
        val btnManagePersonMedicines: View = itemView.findViewById(R.id.btnManagePersonMedicines)
        val expandedActions: View = itemView.findViewById(R.id.expandedActions)
        val textPrescribedMeds: TextView = itemView.findViewById(R.id.textPrescribedMeds)
        val textBmi: TextView = itemView.findViewById(R.id.textBmi)

        fun bind(person: Person, position: Int) {
            textName.text = "${person.firstName} ${person.lastName}"
            val genderText = when (person.gender) {
                "M" -> "Moški"
                "Ž", "F" -> "Ženska"
                "D", "O" -> "Drugo"
                else -> person.gender
            }
            textDetails.text = "Spol: $genderText | Datum rojstva: ${person.dateOfBirth}"
            textStats.text = "Teža: ${person.weightKg} kg | Višina: ${person.heightCm} cm"

            val heightM = person.heightCm / 100.0
            val bmi = if (heightM > 0) person.weightKg / (heightM * heightM) else 0.0
            textBmi.text = String.format(Locale.US, "ITM: %.1f", bmi)

            if (person.prescribedMedicines.isNotEmpty()) {
                textPrescribedMeds.text = person.prescribedMedicines.joinToString("\n") { rx ->
                    val medName = medicines.firstOrNull { it.id == rx.medicineId }?.name ?: rx.medicineId
                    if (rx.dose.isNotBlank()) "• $medName (${rx.dose})" else "• $medName"
                }
            } else {
                textPrescribedMeds.text = "Brez predpisanih zdravil"
            }

            val isExpanded = position == expandedPosition
            expandedActions.visibility = if (isExpanded) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val currentPos = adapterPosition
                if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener

                val previousExpanded = expandedPosition
                val isCurrentlyExpanded = currentPos == expandedPosition
                expandedPosition = if (isCurrentlyExpanded) -1 else currentPos

                if (previousExpanded != -1) notifyItemChanged(previousExpanded)
                if (expandedPosition != -1) notifyItemChanged(expandedPosition)
            }

            itemView.setOnLongClickListener {
                onEditClick(person)
                true
            }

            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle("Izbris osebe")
                    .setMessage("Ali ste prepričani, da želite izbrisati osebo ${person.firstName} ${person.lastName}?")
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

            btnManagePersonMedicines.setOnClickListener {
                onManageMedicinesClick(person)
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
