package com.example.naloga5

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.core.widget.doAfterTextChanged

class DosageActivity : AppCompatActivity() {

    private lateinit var editWeight: TextInputEditText
    private lateinit var editMinDose: TextInputEditText
    private lateinit var editMaxDose: TextInputEditText
    private lateinit var editMgPerUnit: TextInputEditText
    private lateinit var editPerMl: TextInputEditText
    private lateinit var textResultValues: TextView
    private lateinit var btnCalculate: MaterialButton
    private lateinit var btnConfirm: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private lateinit var layoutWeight: TextInputLayout
    private lateinit var layoutMinDose: TextInputLayout
    private lateinit var layoutMaxDose: TextInputLayout
    private lateinit var layoutMgPerUnit: TextInputLayout
    private lateinit var layoutPerMl: TextInputLayout

    private lateinit var cardResult: View

    private var lastCalculatedResult: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dosage)

        setupWindowInsets()
        initViews()
        setupListeners()
        setupToolbar()

        // Pre-fill weight if provided
        val weight = intent.getDoubleExtra("EXTRA_WEIGHT", 0.0)
        if (weight > 0) {
            editWeight.setText(weight.toString())
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
            
            // Pad the transparent AppBarLayout down so it doesn't overlap the status bar / cutout
            appBarLayout.setPadding(0, systemBars.top, 0, 0)
            
            // Pad bottom of scrolling content layout to clear navigation bar
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
        editWeight = findViewById(R.id.editWeight)
        editMinDose = findViewById(R.id.editMinDose)
        editMaxDose = findViewById(R.id.editMaxDose)
        editMgPerUnit = findViewById(R.id.editMgPerUnit)
        editPerMl = findViewById(R.id.editPerMl)
        textResultValues = findViewById(R.id.textResultValues)
        cardResult = findViewById(R.id.cardResult)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnCancel = findViewById(R.id.btnCancel)

        layoutWeight = findViewById(R.id.layoutWeight)
        layoutMinDose = findViewById(R.id.layoutMinDose)
        layoutMaxDose = findViewById(R.id.layoutMaxDose)
        layoutMgPerUnit = findViewById(R.id.layoutMgPerUnit)
        layoutPerMl = findViewById(R.id.layoutPerMl)

        // Clear error states when text changes
        editWeight.doAfterTextChanged { layoutWeight.error = null }
        editMinDose.doAfterTextChanged { layoutMinDose.error = null }
        editMaxDose.doAfterTextChanged { layoutMaxDose.error = null }
        editMgPerUnit.doAfterTextChanged { layoutMgPerUnit.error = null }
        editPerMl.doAfterTextChanged { layoutPerMl.error = null }
    }

    private fun setupListeners() {
        btnCalculate.setOnClickListener { calculateDosage() }
        btnConfirm.setOnClickListener { confirmResult() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun calculateDosage() {
        var isValid = true

        layoutWeight.error = null
        layoutMinDose.error = null
        layoutMaxDose.error = null
        layoutMgPerUnit.error = null
        layoutPerMl.error = null

        val weight = editWeight.text.toString().toDoubleOrNull()
        if (weight == null || weight <= 0) {
            layoutWeight.error = "Vnesite veljavno težo"
            isValid = false
        }

        val minDoseMgKg = editMinDose.text.toString().toDoubleOrNull()
        if (minDoseMgKg == null || minDoseMgKg < 0) {
            layoutMinDose.error = "Vnesite odmerek"
            isValid = false
        }

        val maxDoseMgKg = editMaxDose.text.toString().toDoubleOrNull()
        if (maxDoseMgKg == null || maxDoseMgKg < 0) {
            layoutMaxDose.error = "Vnesite odmerek"
            isValid = false
        }

        val mgPerUnit = editMgPerUnit.text.toString().toDoubleOrNull()
        if (mgPerUnit == null || mgPerUnit <= 0) {
            layoutMgPerUnit.error = "Vnesite koncentracijo (> 0)"
            isValid = false
        }

        val perMl = editPerMl.text.toString().toDoubleOrNull()
        if (perMl == null || perMl <= 0) {
            layoutPerMl.error = "Vnesite volumen"
            isValid = false
        }

        if (!isValid || weight == null || minDoseMgKg == null || maxDoseMgKg == null || mgPerUnit == null || perMl == null) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_invalid_input), Snackbar.LENGTH_SHORT).show()
            return
        }

        // Hide soft keyboard on successful validation pass
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val focusedView = currentFocus ?: btnCalculate
        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)

        // Calculation:
        // Dose range in mg: (minDose * weight) to (maxDose * weight)
        val minMg = minDoseMgKg * weight
        val maxMg = maxDoseMgKg * weight

        // Dose range in ml: (mg / concentration) * perMl
        val minMl = (minMg / mgPerUnit) * perMl
        val maxMl = (maxMg / mgPerUnit) * perMl

        val resultStr = getString(R.string.dosage_result_prefix) + "\n" +
                String.format("%.1f mg - %.1f mg\n", minMg, maxMg) +
                String.format("%.2f ml - %.2f ml", minMl, maxMl)

        val valuesStr = String.format("%.1f mg - %.1f mg\n", minMg, maxMg) +
                String.format("%.2f ml - %.2f ml", minMl, maxMl)

        lastCalculatedResult = resultStr
        textResultValues.text = valuesStr
        cardResult.visibility = View.VISIBLE
    }

    private fun confirmResult() {
        if (lastCalculatedResult == null) {
            Snackbar.make(findViewById(android.R.id.content), "Najprej izvedite izračun", Snackbar.LENGTH_SHORT).show()
            return
        }
        val resultIntent = Intent()
        resultIntent.putExtra("EXTRA_RESULT", lastCalculatedResult)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}