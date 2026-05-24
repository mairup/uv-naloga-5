package com.example.naloga5

import java.io.Serializable

data class Person(
    val id: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dateOfBirth: String, 
    val weightKg: Double,
    val heightCm: Double,
    val prescribedMedicines: List<Prescription> = emptyList()
) : Serializable {
    // Used by ArrayAdapter inside Spinner to display the name
    override fun toString(): String = "$firstName $lastName"
}
