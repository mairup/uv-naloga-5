package com.example.naloga5

import java.io.Serializable

data class Medicine(
    val id: String,
    val name: String,
    val activeIngredient: String,
    val minDoseMgKg: Double,
    val maxDoseMgKg: Double,
    val mgPerUnit: Double,
    val perMl: Double,
    val notes: String = ""
) : Serializable {
    override fun toString(): String = "$name ($activeIngredient)"
}
