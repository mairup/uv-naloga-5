package com.example.naloga5

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import java.util.UUID

class DosageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PERSON = "EXTRA_PERSON"
        const val EXTRA_ALL_MEDICINES = "EXTRA_ALL_MEDICINES"
        const val EXTRA_UPDATED_PERSON = "EXTRA_UPDATED_PERSON"
    }

    private lateinit var selectedPerson: Person
    private val allMedicines = mutableListOf<Medicine>()
    private val assignedItems = mutableListOf<PrescribedMedicineItem>()
    private val filteredPickerItems = mutableListOf<PrescribedMedicineItem>()

    private lateinit var assignedAdapter: PrescribedMedicineAdapter
    private lateinit var pickerAdapter: PrescribedMedicineAdapter

    private lateinit var textPersonName: TextView
    private lateinit var textPersonDetails: TextView
    private lateinit var textNoAssignedMedicines: TextView
    private lateinit var recyclerAssignedMedicines: RecyclerView
    private lateinit var recyclerAllMedicines: RecyclerView
    private lateinit var btnCancelExit: MaterialButton
    private lateinit var btnCreateAssignedMedicine: MaterialButton
    private lateinit var btnSavePrescriptions: MaterialButton
    private lateinit var btnClosePicker: MaterialButton
    private lateinit var editSearchMedicine: TextInputEditText
    private lateinit var pickerOverlay: View
    private lateinit var pickerScrim: View
    private lateinit var pickerCard: View

    // Calculator overlay views
    private lateinit var calculatorOverlay: View
    private lateinit var calculatorScrim: View
    private lateinit var calculatorCard: View
    private lateinit var textCalcTitle: TextView
    private lateinit var textCalcSubtitle: TextView
    private lateinit var layoutRelativeCalc: LinearLayout
    private lateinit var textCalcRange: TextView
    private lateinit var editCalcMgKg: TextInputEditText
    private lateinit var btnCalculateDose: MaterialButton
    private lateinit var editCalcFinalDose: TextInputEditText
    private lateinit var editCalcNotes: TextInputEditText
    private lateinit var btnCancelCalc: MaterialButton
    private lateinit var btnSaveCalc: MaterialButton

    private var currentCalcItem: PrescribedMedicineItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dosage)

        if (!readInputData()) {
            finish()
            return
        }

        setupWindowInsets()
        initViews()
        populatePersonHeader()
        setupLists()
        setupBackNavigationHandling()
        setupListeners()
        setupToolbar()
        updateEmptyAssignedState()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { handleExit() }
    }

    private fun setupWindowInsets() {
        val mainLayout = findViewById<android.view.View>(R.id.mainLayout)
        val appBarLayout = findViewById<android.view.View>(R.id.appBarLayout)
        val bottomBar = findViewById<android.view.View>(R.id.bottomActionBar)

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            appBarLayout.setPadding(0, systemBars.top, 0, 0)

            val density = resources.displayMetrics.density
            val buttonBottomMargin = systemBars.bottom + (12 * density).toInt()
            val params = bottomBar.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.bottomMargin = buttonBottomMargin
            bottomBar.layoutParams = params

            val assignedRecycler = findViewById<RecyclerView>(R.id.recyclerAssignedMedicines)
            assignedRecycler.setPadding(
                assignedRecycler.paddingLeft,
                assignedRecycler.paddingTop,
                assignedRecycler.paddingRight,
                systemBars.bottom + (90 * density).toInt()
            )

            insets
        }
    }

    private fun initViews() {
        textPersonName = findViewById(R.id.textPersonName)
        textPersonDetails = findViewById(R.id.textPersonDetails)
        textNoAssignedMedicines = findViewById(R.id.textNoAssignedMedicines)
        recyclerAssignedMedicines = findViewById(R.id.recyclerAssignedMedicines)
        recyclerAllMedicines = findViewById(R.id.recyclerAllMedicines)
        btnCancelExit = findViewById(R.id.btnCancelExit)
        btnCreateAssignedMedicine = findViewById(R.id.btnCreateAssignedMedicine)
        btnSavePrescriptions = findViewById(R.id.btnSavePrescriptions)
        editSearchMedicine = findViewById(R.id.editSearchMedicine)
        btnClosePicker = findViewById(R.id.btnClosePicker)
        pickerOverlay = findViewById(R.id.pickerOverlay)
        pickerScrim = findViewById(R.id.pickerScrim)
        pickerCard = findViewById(R.id.pickerCard)

        pickerOverlay.visibility = View.GONE
        pickerScrim.alpha = 0f
        pickerCard.alpha = 0f

        // Calculator overlay views
        calculatorOverlay = findViewById(R.id.calculatorOverlay)
        calculatorScrim = findViewById(R.id.calculatorScrim)
        calculatorCard = findViewById(R.id.calculatorCard)
        textCalcTitle = findViewById(R.id.textCalcTitle)
        textCalcSubtitle = findViewById(R.id.textCalcSubtitle)
        layoutRelativeCalc = findViewById(R.id.layoutRelativeCalc)
        textCalcRange = findViewById(R.id.textCalcRange)
        editCalcMgKg = findViewById(R.id.editCalcMgKg)
        btnCalculateDose = findViewById(R.id.btnCalculateDose)
        editCalcFinalDose = findViewById(R.id.editCalcFinalDose)
        editCalcNotes = findViewById(R.id.editCalcNotes)
        btnCancelCalc = findViewById(R.id.btnCancelCalc)
        btnSaveCalc = findViewById(R.id.btnSaveCalc)

        calculatorOverlay.visibility = View.GONE
        calculatorScrim.alpha = 0f
        calculatorCard.alpha = 0f
    }

    private fun populatePersonHeader() {
        textPersonName.text = "${selectedPerson.firstName} ${selectedPerson.lastName}"
        val genderText = when (selectedPerson.gender) {
            "M" -> "Moški"
            "Ž", "F" -> "Ženska"
            "D", "O" -> "Drugo"
            else -> selectedPerson.gender
        }
        textPersonDetails.text = "Spol: $genderText | Datum rojstva: ${selectedPerson.dateOfBirth} | Teža: ${selectedPerson.weightKg} kg"
    }

    private fun setupLists() {
        // Build assigned items from prescriptions
        assignedItems.clear()
        selectedPerson.prescribedMedicines.forEach { rx ->
            val med = allMedicines.firstOrNull { it.id == rx.medicineId }
            if (med != null) {
                assignedItems.add(PrescribedMedicineItem(med, rx.dose, rx.notes))
            }
        }

        assignedAdapter = PrescribedMedicineAdapter(
            items = assignedItems,
            showRemoveAction = true,
            onRemoveClick = { item -> removeAssignedItem(item) },
            onItemClick = { item -> showCalculator(item) }
        )

        // Build picker items from all medicines
        filteredPickerItems.clear()
        filteredPickerItems.addAll(allMedicines.map { PrescribedMedicineItem(it) })

        pickerAdapter = PrescribedMedicineAdapter(
            items = filteredPickerItems,
            showRemoveAction = false,
            onItemClick = { item ->
                if (assignedItems.any { it.medicine.id == item.medicine.id }) {
                    showSnackbar("Zdravilo je že dodano")
                    return@PrescribedMedicineAdapter
                }
                assignedItems.add(PrescribedMedicineItem(item.medicine))
                assignedAdapter.notifyItemInserted(assignedItems.size - 1)
                updateEmptyAssignedState()
                hidePicker()
            }
        )

        recyclerAssignedMedicines.layoutManager = LinearLayoutManager(this)
        recyclerAssignedMedicines.adapter = assignedAdapter

        recyclerAllMedicines.layoutManager = LinearLayoutManager(this)
        recyclerAllMedicines.adapter = pickerAdapter
    }

    private fun setupListeners() {
        btnCreateAssignedMedicine.setOnClickListener { showPicker() }
        btnCancelExit.setOnClickListener { handleExit() }
        btnSavePrescriptions.setOnClickListener { finishWithResult() }
        pickerScrim.setOnClickListener { hidePicker() }
        btnClosePicker.setOnClickListener { hidePicker() }
        pickerCard.setOnClickListener { /* consume click */ }

        editSearchMedicine.doAfterTextChanged { text ->
            val query = text?.toString()?.trim() ?: ""
            filteredPickerItems.clear()
            if (query.isEmpty()) {
                filteredPickerItems.addAll(allMedicines.map { PrescribedMedicineItem(it) })
            } else {
                filteredPickerItems.addAll(allMedicines.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.activeIngredient.contains(query, ignoreCase = true)
                }.map { PrescribedMedicineItem(it) })
            }
            pickerAdapter.notifyDataSetChanged()
        }

        // Calculator listeners
        calculatorScrim.setOnClickListener { hideCalculator() }
        btnCancelCalc.setOnClickListener { hideCalculator() }
        calculatorCard.setOnClickListener { /* consume click */ }

        btnCalculateDose.setOnClickListener {
            val item = currentCalcItem ?: return@setOnClickListener
            val mgKg = editCalcMgKg.text?.toString()?.toDoubleOrNull()
            if (mgKg == null) {
                showSnackbar("Vnesite veljavno vrednost mg/kg")
                return@setOnClickListener
            }
            val med = item.medicine
            val weight = selectedPerson.weightKg
            val totalMg = mgKg * weight

            if (med.perMl > 0) {
                // Syrup: convert mg to ml
                val ml = totalMg * med.perMl / med.mgPerUnit
                editCalcFinalDose.setText(String.format(Locale.US, "%.2f ml", ml))
            } else {
                // Pills/tablets: convert mg to units
                val units = totalMg / med.mgPerUnit
                editCalcFinalDose.setText(String.format(Locale.US, "%.2f enot", units))
            }

            // Close keyboard
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(editCalcMgKg.windowToken, 0)
        }

        btnSaveCalc.setOnClickListener {
            val item = currentCalcItem ?: return@setOnClickListener
            val dose = editCalcFinalDose.text?.toString()?.trim() ?: ""
            val notes = editCalcNotes.text?.toString()?.trim() ?: ""
            item.dose = dose
            item.notes = notes
            val index = assignedItems.indexOfFirst { it.medicine.id == item.medicine.id }
            if (index != -1) {
                assignedAdapter.notifyItemChanged(index)
            }
            hideCalculator()
        }
    }

    private fun setupBackNavigationHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (calculatorOverlay.visibility == View.VISIBLE) {
                    hideCalculator()
                } else if (pickerOverlay.visibility == View.VISIBLE) {
                    hidePicker()
                } else {
                    handleExit()
                }
            }
        })
    }

    private fun showPicker() {
        if (pickerOverlay.visibility == View.VISIBLE) return

        pickerOverlay.visibility = View.VISIBLE
        pickerOverlay.post {
            val density = resources.displayMetrics.density
            val startOffsetY = 36f * density
            pickerScrim.alpha = 0f
            pickerCard.alpha = 0f
            pickerCard.translationY = startOffsetY

            pickerScrim.animate()
                .alpha(1f)
                .setDuration(180)
                .start()

            pickerCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(240)
                .setInterpolator(OvershootInterpolator(0.9f))
                .start()
        }
    }

    private fun hidePicker() {
        if (pickerOverlay.visibility != View.VISIBLE) return

        val density = resources.displayMetrics.density
        val endOffsetY = 24f * density

        pickerScrim.animate()
            .alpha(0f)
            .setDuration(150)
            .start()

        pickerCard.animate()
            .alpha(0f)
            .translationY(endOffsetY)
            .setDuration(170)
            .withEndAction {
                pickerOverlay.visibility = View.GONE
            }
            .start()
    }

    private fun showCalculator(item: PrescribedMedicineItem) {
        currentCalcItem = item
        val med = item.medicine

        textCalcTitle.text = med.name
        textCalcSubtitle.text = "${med.activeIngredient} • ${selectedPerson.firstName} ${selectedPerson.lastName} (${selectedPerson.weightKg} kg)"

        val isRelative = med.minDoseMgKg != 0.0 || med.maxDoseMgKg != 0.0

        if (isRelative) {
            layoutRelativeCalc.visibility = View.VISIBLE
            val weight = selectedPerson.weightKg
            val minMg = med.minDoseMgKg * weight
            val maxMg = med.maxDoseMgKg * weight

            val rangeText = StringBuilder().apply {
                append("Priporočen odmerek: ${String.format(Locale.US, "%.2f", med.minDoseMgKg)} – ${String.format(Locale.US, "%.2f", med.maxDoseMgKg)} mg/kg\n")
                append("Odmerek snovi: ${String.format(Locale.US, "%.2f", minMg)} – ${String.format(Locale.US, "%.2f", maxMg)} mg\n")
                if (med.perMl > 0) {
                    val minMl = minMg * med.perMl / med.mgPerUnit
                    val maxMl = maxMg * med.perMl / med.mgPerUnit
                    append("Koncentracija: ${String.format(Locale.US, "%.1f", med.mgPerUnit)} mg / ${String.format(Locale.US, "%.1f", med.perMl)} ml\n")
                    append("Priporočena količina: ${String.format(Locale.US, "%.2f", minMl)} – ${String.format(Locale.US, "%.2f", maxMl)} ml")
                } else {
                    val minUnits = minMg / med.mgPerUnit
                    val maxUnits = maxMg / med.mgPerUnit
                    append("Jakost: ${String.format(Locale.US, "%.1f", med.mgPerUnit)} mg / enoto\n")
                    append("Priporočena količina: ${String.format(Locale.US, "%.2f", minUnits)} – ${String.format(Locale.US, "%.2f", maxUnits)} enot")
                }
            }.toString()

            textCalcRange.text = rangeText
            editCalcMgKg.setText("")
        } else {
            layoutRelativeCalc.visibility = View.GONE
        }

        editCalcFinalDose.setText(item.dose)
        editCalcNotes.setText(item.notes)

        if (calculatorOverlay.visibility == View.VISIBLE) return

        calculatorOverlay.visibility = View.VISIBLE
        calculatorOverlay.post {
            val density = resources.displayMetrics.density
            val startOffsetY = 36f * density
            calculatorScrim.alpha = 0f
            calculatorCard.alpha = 0f
            calculatorCard.translationY = startOffsetY

            calculatorScrim.animate()
                .alpha(1f)
                .setDuration(180)
                .start()

            calculatorCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(240)
                .setInterpolator(OvershootInterpolator(0.9f))
                .start()
        }
    }

    private fun hideCalculator() {
        if (calculatorOverlay.visibility != View.VISIBLE) return
        currentCalcItem = null

        val density = resources.displayMetrics.density
        val endOffsetY = 24f * density

        calculatorScrim.animate()
            .alpha(0f)
            .setDuration(150)
            .start()

        calculatorCard.animate()
            .alpha(0f)
            .translationY(endOffsetY)
            .setDuration(170)
            .withEndAction {
                calculatorOverlay.visibility = View.GONE
            }
            .start()
    }

    private fun removeAssignedItem(item: PrescribedMedicineItem) {
        val index = assignedItems.indexOfFirst { it.medicine.id == item.medicine.id }
        if (index == -1) return

        assignedItems.removeAt(index)
        assignedAdapter.notifyItemRemoved(index)
        updateEmptyAssignedState()
    }

    private fun handleExit() {
        val currentPrescriptions = assignedItems.map { Prescription(it.medicine.id, it.dose, it.notes) }
        if (currentPrescriptions != selectedPerson.prescribedMedicines) {
            showDiscardConfirmationDialog()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun showDiscardConfirmationDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Želite zapustiti brez shranjevanja?")
            .setMessage("Spremembe ne bodo shranjene.")
            .setPositiveButton("Da") { _, _ ->
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun updateEmptyAssignedState() {
        textNoAssignedMedicines.visibility = if (assignedItems.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun finishWithResult() {
        val prescriptions = assignedItems.map { Prescription(it.medicine.id, it.dose, it.notes) }
        val updatedPerson = selectedPerson.copy(
            prescribedMedicines = prescriptions
        )
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(EXTRA_UPDATED_PERSON, updatedPerson)
        )
        finish()
    }

    private fun readInputData(): Boolean {
        val personFromIntent = intent.getSerializableExtra(EXTRA_PERSON) as? Person
        if (personFromIntent == null) {
            return false
        }

        val medicinesFromIntent = intent.getSerializableExtra(EXTRA_ALL_MEDICINES) as? ArrayList<*>
        val parsedMedicines = medicinesFromIntent
            ?.filterIsInstance<Medicine>()
            ?.sortedBy { it.name }
            ?: emptyList()

        selectedPerson = personFromIntent
        allMedicines.clear()
        allMedicines.addAll(parsedMedicines)
        return true
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.bottomActionBar)
            .show()
    }
}