package com.example.naloga5

import java.io.Serializable

data class Prescription(
    val medicineId: String,
    val dose: String = "",
    val notes: String = ""
) : Serializable
