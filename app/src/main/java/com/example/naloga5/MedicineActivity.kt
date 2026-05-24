package com.example.naloga5

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import java.util.UUID

class MedicineActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEDICINE = "extra_medicine"
    }

    private lateinit var editName: EditText
    private lateinit var editActiveIngredient: EditText
    private lateinit var editMinDose: EditText
    private lateinit var editMaxDose: EditText
    private lateinit var editMgPerUnit: EditText
    private lateinit var editPerMl: EditText
    private lateinit var editNotes: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button

    private lateinit var layoutName: TextInputLayout
    private lateinit var layoutActiveIngredient: TextInputLayout
    private lateinit var layoutMinDose: TextInputLayout
    private lateinit var layoutMaxDose: TextInputLayout
    private lateinit var layoutMgPerUnit: TextInputLayout
    private lateinit var layoutPerMl: TextInputLayout

    private var currentMedicineId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_medicine)

        setupWindowInsets()
        initViews()
        setupToolbar()

        val existingMedicine = intent.getSerializableExtra(EXTRA_MEDICINE) as? Medicine

        if (existingMedicine != null) {
            currentMedicineId = existingMedicine.id
            editName.setText(existingMedicine.name)
            editActiveIngredient.setText(existingMedicine.activeIngredient)
            editMinDose.setText(existingMedicine.minDoseMgKg.toString())
            editMaxDose.setText(existingMedicine.maxDoseMgKg.toString())
            editMgPerUnit.setText(existingMedicine.mgPerUnit.toString())
            editPerMl.setText(existingMedicine.perMl.toString())
            editNotes.setText(existingMedicine.notes)
        }

        buttonSave.setOnClickListener { saveMedicine() }
        buttonCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupWindowInsets() {
        val mainLayout = findViewById<android.view.View>(R.id.main_layout)
        val appBarLayout = findViewById<android.view.View>(R.id.appBarLayout)
        val contentLayout = findViewById<android.view.View>(R.id.content_layout)

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            appBarLayout.setPadding(0, systemBars.top, 0, 0)

            val density = resources.displayMetrics.density
            val bottomPaddingPx = systemBars.bottom + (24 * density).toInt()
            contentLayout.setPadding(
                contentLayout.paddingLeft,
                contentLayout.paddingTop,
                contentLayout.paddingRight,
                bottomPaddingPx
            )
            insets
        }
    }

    private fun initViews() {
        editName = findViewById(R.id.editName)
        editActiveIngredient = findViewById(R.id.editActiveIngredient)
        editMinDose = findViewById(R.id.editMinDose)
        editMaxDose = findViewById(R.id.editMaxDose)
        editMgPerUnit = findViewById(R.id.editMgPerUnit)
        editPerMl = findViewById(R.id.editPerMl)
        editNotes = findViewById(R.id.editNotes)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)

        layoutName = findViewById(R.id.layoutName)
        layoutActiveIngredient = findViewById(R.id.layoutActiveIngredient)
        layoutMinDose = findViewById(R.id.layoutMinDose)
        layoutMaxDose = findViewById(R.id.layoutMaxDose)
        layoutMgPerUnit = findViewById(R.id.layoutMgPerUnit)
        layoutPerMl = findViewById(R.id.layoutPerMl)

        // Clear error states when text changes
        editName.doAfterTextChanged { layoutName.error = null }
        editActiveIngredient.doAfterTextChanged { layoutActiveIngredient.error = null }
        editMinDose.doAfterTextChanged { layoutMinDose.error = null }
        editMaxDose.doAfterTextChanged { layoutMaxDose.error = null }
        editMgPerUnit.doAfterTextChanged { layoutMgPerUnit.error = null }
        editPerMl.doAfterTextChanged { layoutPerMl.error = null }
    }

    private fun saveMedicine() {
        val name = editName.text.toString().trim()
        val activeIngredient = editActiveIngredient.text.toString().trim()
        val minDoseStr = editMinDose.text.toString().trim()
        val maxDoseStr = editMaxDose.text.toString().trim()
        val mgPerUnitStr = editMgPerUnit.text.toString().trim()
        val perMlStr = editPerMl.text.toString().trim()
        val notes = editNotes.text.toString().trim()

        // Clear existing errors
        layoutName.error = null
        layoutActiveIngredient.error = null
        layoutMinDose.error = null
        layoutMaxDose.error = null
        layoutMgPerUnit.error = null
        layoutPerMl.error = null

        var hasErrors = false

        if (name.isEmpty()) {
            layoutName.error = "Ime ne sme biti prazno"
            hasErrors = true
        }
        if (activeIngredient.isEmpty()) {
            layoutActiveIngredient.error = "Učinkovina ne sme biti prazna"
            hasErrors = true
        }

        val minDose = minDoseStr.toDoubleOrNull()
        if (minDose == null || minDose < 0) {
            layoutMinDose.error = "Neveljaven vnos minimalnega odmerka"
            hasErrors = true
        }

        val maxDose = maxDoseStr.toDoubleOrNull()
        if (maxDose == null || maxDose < 0) {
            layoutMaxDose.error = "Neveljaven vnos maksimalnega odmerka"
            hasErrors = true
        }

        if (minDose != null && maxDose != null && maxDose < minDose) {
            layoutMaxDose.error = "Maksimalni odmerek mora biti večji ali enak minimalnemu"
            hasErrors = true
        }

        val mgPerUnit = mgPerUnitStr.toDoubleOrNull()
        if (mgPerUnit == null || mgPerUnit <= 0) {
            layoutMgPerUnit.error = "Neveljaven vnos vsebnosti (> 0)"
            hasErrors = true
        }

        val perMl = perMlStr.toDoubleOrNull()
        if (perMl == null || perMl <= 0) {
            layoutPerMl.error = "Neveljaven vnos volumna (> 0)"
            hasErrors = true
        }

        if (hasErrors) {
            Snackbar.make(findViewById(android.R.id.content), "Preverite pravilnost vnesenih podatkov", Snackbar.LENGTH_SHORT).show()
            return
        }

        val id = currentMedicineId ?: UUID.randomUUID().toString()

        val medicine = Medicine(
            id = id,
            name = name,
            activeIngredient = activeIngredient,
            minDoseMgKg = minDose!!,
            maxDoseMgKg = maxDose!!,
            mgPerUnit = mgPerUnit!!,
            perMl = perMl!!,
            notes = notes
        )

        val resultIntent = Intent().apply {
            putExtra(EXTRA_MEDICINE, medicine)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
