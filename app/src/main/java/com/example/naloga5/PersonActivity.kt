package com.example.naloga5

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import androidx.core.widget.doAfterTextChanged
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class PersonActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PERSON = "extra_person"
    }

    private lateinit var editFirstName: EditText
    private lateinit var editLastName: EditText
    private lateinit var editDateOfBirth: EditText
    private lateinit var editWeight: EditText
    private lateinit var editHeight: EditText
    private lateinit var btnMale: MaterialButton
    private lateinit var btnFemale: MaterialButton
    private lateinit var btnOther: MaterialButton
    private var selectedGenderId: Int = android.view.View.NO_ID
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button

    private lateinit var layoutFirstName: TextInputLayout
    private lateinit var layoutLastName: TextInputLayout
    private lateinit var layoutDateOfBirth: TextInputLayout
    private lateinit var layoutWeight: TextInputLayout
    private lateinit var layoutHeight: TextInputLayout

    private var currentPersonId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_person)

        setupWindowInsets()
        initViews()
        setupToolbar()

        val existingPerson = intent.getSerializableExtra(EXTRA_PERSON) as? Person

        if (existingPerson != null) {
            currentPersonId = existingPerson.id
            editFirstName.setText(existingPerson.firstName)
            editLastName.setText(existingPerson.lastName)
            editDateOfBirth.setText(existingPerson.dateOfBirth)
            editWeight.setText(existingPerson.weightKg.toString())
            editHeight.setText(existingPerson.heightCm.toString())

            if (existingPerson.gender.equals("M", ignoreCase = true)) {
                selectGender(R.id.btnMale)
            } else if (existingPerson.gender.equals("F", ignoreCase = true) || existingPerson.gender.equals("Ž", ignoreCase = true)) {
                selectGender(R.id.btnFemale)
            } else if (existingPerson.gender.equals("D", ignoreCase = true) || existingPerson.gender.equals("O", ignoreCase = true)) {
                selectGender(R.id.btnOther)
            }
        } else {
            selectGender(R.id.btnMale)
        }

        buttonSave.setOnClickListener {
            savePerson()
        }

        buttonCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        editDateOfBirth.setOnClickListener {
            showDatePicker()
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
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            // Pad the transparent AppBarLayout down so it doesn't overlap the status bar / cutout
            appBarLayout.setPadding(0, systemBars.top, 0, 0)
            
            // Pad bottom of scrolling content layout to clear navigation bar and keyboard
            val density = resources.displayMetrics.density
            val bottomPaddingPx = Math.max(systemBars.bottom, ime.bottom) + (24 * density).toInt()
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
        editFirstName = findViewById(R.id.editFirstName)
        editLastName = findViewById(R.id.editLastName)
        editDateOfBirth = findViewById(R.id.editDateOfBirth)
        editWeight = findViewById(R.id.editWeight)
        editHeight = findViewById(R.id.editHeight)
        btnMale = findViewById(R.id.btnMale)
        btnFemale = findViewById(R.id.btnFemale)
        btnOther = findViewById(R.id.btnOther)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)

        layoutFirstName = findViewById(R.id.layoutFirstName)
        layoutLastName = findViewById(R.id.layoutLastName)
        layoutDateOfBirth = findViewById(R.id.layoutDateOfBirth)
        layoutWeight = findViewById(R.id.layoutWeight)
        layoutHeight = findViewById(R.id.layoutHeight)

        // Setup gender selection listeners
        btnMale.setOnClickListener { selectGender(R.id.btnMale) }
        btnFemale.setOnClickListener { selectGender(R.id.btnFemale) }
        btnOther.setOnClickListener { selectGender(R.id.btnOther) }

        // Clear error states when text changes
        editFirstName.doAfterTextChanged { layoutFirstName.error = null }
        editLastName.doAfterTextChanged { layoutLastName.error = null }
        editDateOfBirth.doAfterTextChanged { layoutDateOfBirth.error = null }
        editWeight.doAfterTextChanged { layoutWeight.error = null }
        editHeight.doAfterTextChanged { layoutHeight.error = null }
    }

    private fun savePerson() {
        val firstName = editFirstName.text.toString().trim()
        val lastName = editLastName.text.toString().trim()
        val dateOfBirth = editDateOfBirth.text.toString().trim()
        val weightStr = editWeight.text.toString().trim()
        val heightStr = editHeight.text.toString().trim()

        val gender = when (selectedGenderId) {
            R.id.btnMale -> "M"
            R.id.btnFemale -> "Ž"
            R.id.btnOther -> "D"
            else -> ""
        }

        // Clear existing errors
        layoutFirstName.error = null
        layoutLastName.error = null
        layoutDateOfBirth.error = null
        layoutWeight.error = null
        layoutHeight.error = null

        // 1. Check for empty fields
        val hasEmptyFields = firstName.isEmpty() || lastName.isEmpty() || dateOfBirth.isEmpty() ||
                weightStr.isEmpty() || heightStr.isEmpty() || gender.isEmpty()

        if (hasEmptyFields) {
            if (firstName.isEmpty()) layoutFirstName.error = "Ime ne sme biti prazno"
            if (lastName.isEmpty()) layoutLastName.error = "Priimek ne sme biti prazen"
            if (dateOfBirth.isEmpty()) layoutDateOfBirth.error = "Datum rojstva ne sme biti prazen"
            if (weightStr.isEmpty()) layoutWeight.error = "Teža ne sme biti prazna"
            if (heightStr.isEmpty()) layoutHeight.error = "Višina ne sme biti prazna"
            
            Snackbar.make(findViewById(android.R.id.content), "Izpolnite vsa polja", Snackbar.LENGTH_SHORT).show()
            return
        }

        // 2. All fields are filled. Now validate formats using regex / parsing
        val isFirstNameValid = firstName.matches(Regex("^[A-ZČŠŽĆĐa-zčšžćđ\\s-]+$"))
        val isLastNameValid = lastName.matches(Regex("^[A-ZČŠŽĆĐa-zčšžćđ\\s-]+$"))
        val isDateOfBirthValid = dateOfBirth.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
        
        val weightKg = weightStr.toDoubleOrNull()
        val isWeightValid = weightKg != null && weightKg > 0

        val heightCm = heightStr.toDoubleOrNull()
        val isHeightValid = heightCm != null && heightCm > 0

        val hasFormatErrors = !isFirstNameValid || !isLastNameValid || !isDateOfBirthValid || !isWeightValid || !isHeightValid

        if (hasFormatErrors) {
            if (!isFirstNameValid) layoutFirstName.error = "Neveljaven format imena"
            if (!isLastNameValid) layoutLastName.error = "Neveljaven format priimka"
            if (!isDateOfBirthValid) layoutDateOfBirth.error = "Neveljaven format datuma (LLLL-MM-DD)"
            if (!isWeightValid) layoutWeight.error = "Neveljaven format teže"
            if (!isHeightValid) layoutHeight.error = "Neveljaven format višine"

            Snackbar.make(findViewById(android.R.id.content), "Preverite pravilnost vnesenih podatkov", Snackbar.LENGTH_SHORT).show()
            return
        }

        val id = currentPersonId ?: UUID.randomUUID().toString()
        val existingPrescribedMeds = (intent.getSerializableExtra(EXTRA_PERSON) as? Person)?.prescribedMedicines ?: emptyList<Prescription>()

        val person = Person(
            id = id,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            dateOfBirth = dateOfBirth,
            weightKg = weightKg!!,
            heightCm = heightCm!!,
            prescribedMedicines = existingPrescribedMeds
        )

        val resultIntent = Intent().apply {
            putExtra(EXTRA_PERSON, person)
        }

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Izberite datum rojstva")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = Date(selection)
            editDateOfBirth.setText(sdf.format(date))
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun selectGender(buttonId: Int) {
        if (selectedGenderId == buttonId) return

        // Smooth transition for backgrounds and text colors
        androidx.transition.TransitionManager.beginDelayedTransition(
            findViewById(R.id.layoutGenderContainer),
            androidx.transition.AutoTransition().apply {
                duration = 200
            }
        )

        selectedGenderId = buttonId
        val buttons = listOf(btnMale, btnFemale, btnOther)
        for (btn in buttons) {
            val isSelected = btn.id == buttonId
            btn.isSelected = isSelected
            if (isSelected) {
                // Pop scale-up animation
                btn.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).withEndAction {
                    btn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start()
                }.start()
            } else {
                btn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
            }
        }
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (ev.action == android.view.MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = android.graphics.Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}