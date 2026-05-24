package com.example.naloga5

data class PrescribedMedicineItem(
    val medicine: Medicine,
    var dose: String = "",
    var notes: String = ""
)
