package com.example.naloga5

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import java.util.UUID
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private val persons = mutableListOf<Person>()
    private val medicines = mutableListOf<Medicine>()

    private lateinit var personAdapter: PersonAdapter
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textScreenTitle: TextView
    private lateinit var layoutSearchMedicine: TextInputLayout
    private lateinit var editSearchMedicineMain: TextInputEditText

    private var activeTab: Int = R.id.tabPeople
    private var currentMedicineQuery: String = ""

    private val personLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnedPerson = result.data?.getSerializableExtra(PersonActivity.EXTRA_PERSON) as? Person
            if (returnedPerson != null) {
                val index = persons.indexOfFirst { it.id == returnedPerson.id }
                if (index != -1) {
                    persons[index] = returnedPerson
                    personAdapter.notifyItemChanged(index)
                    Snackbar.make(findViewById(android.R.id.content), "Oseba uspešno posodobljena", Snackbar.LENGTH_SHORT).show()
                } else {
                    persons.add(returnedPerson)
                    personAdapter.notifyItemInserted(persons.size - 1)
                    Snackbar.make(findViewById(android.R.id.content), "Oseba uspešno dodana", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val medicineLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnedMedicine = result.data?.getSerializableExtra(MedicineActivity.EXTRA_MEDICINE) as? Medicine
            if (returnedMedicine != null) {
                val index = medicines.indexOfFirst { it.id == returnedMedicine.id }
                if (index != -1) {
                    medicines[index] = returnedMedicine
                    filterMedicines()
                    Snackbar.make(findViewById(android.R.id.content), "Zdravilo uspešno posodobljeno", Snackbar.LENGTH_SHORT).show()
                } else {
                    medicines.add(returnedMedicine)
                    filterMedicines()
                    Snackbar.make(findViewById(android.R.id.content), "Zdravilo uspešno dodano", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val prescribedMedicinesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnedPerson = result.data?.getSerializableExtra(DosageActivity.EXTRA_UPDATED_PERSON) as? Person
            if (returnedPerson != null) {
                val index = persons.indexOfFirst { it.id == returnedPerson.id }
                if (index != -1) {
                    persons[index] = returnedPerson
                    personAdapter.notifyItemChanged(index)
                    Snackbar.make(findViewById(android.R.id.content), "Predpisana zdravila posodobljena", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        textScreenTitle = findViewById(R.id.textScreenTitle)
        recyclerView = findViewById(R.id.recyclerView)
        layoutSearchMedicine = findViewById(R.id.layoutSearchMedicine)
        editSearchMedicineMain = findViewById(R.id.editSearchMedicineMain)

        editSearchMedicineMain.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentMedicineQuery = s?.toString() ?: ""
                filterMedicines()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupWindowInsets()
        setupDummyData()
        setupAdapters()
        setupBottomNavigation()

        // Default to People tab
        selectTab(R.id.tabPeople)
    }

    private fun setupWindowInsets() {
        val rootLayout: View = findViewById(R.id.main)
        val titleView: View = findViewById(R.id.textScreenTitle)
        val bottomNavBar: View = findViewById(R.id.bottomNavBar)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val density = resources.displayMetrics.density
            val topPaddingPx = (24 * density).toInt() + systemBars.top
            titleView.setPadding(
                titleView.paddingLeft,
                topPaddingPx,
                titleView.paddingRight,
                titleView.paddingBottom
            )

            bottomNavBar.setPadding(
                bottomNavBar.paddingLeft,
                bottomNavBar.paddingTop,
                bottomNavBar.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }

    private fun setupAdapters() {
        personAdapter = PersonAdapter(
            persons,
            medicines,
            onEditClick = { person -> handleEditPerson(person) },
            onDeleteClick = { person -> handleDeletePerson(person) },
            onManageMedicinesClick = { person -> handleManagePersonMedicines(person) }
        )

        medicineAdapter = MedicineAdapter(
            emptyList(),
            onEditClick = { medicine -> handleEditMedicine(medicine) },
            onDeleteClick = { medicine -> handleDeleteMedicine(medicine) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setItemViewCacheSize(20)
    }

    private fun setupBottomNavigation() {
        val tabPeople: View = findViewById(R.id.tabPeople)
        val tabMedicines: View = findViewById(R.id.tabMedicines)
        val btnNavAdd: View = findViewById(R.id.btnNavAdd)

        tabPeople.setOnClickListener {
            selectTab(R.id.tabPeople)
        }

        tabMedicines.setOnClickListener {
            selectTab(R.id.tabMedicines)
        }

        btnNavAdd.setOnClickListener {
            handleAdd()
        }
    }

    private fun selectTab(tabId: Int) {
        activeTab = tabId

        val pillPeople: View = findViewById(R.id.pillPeople)
        val iconPeople: ImageView = findViewById(R.id.iconPeople)
        val textPeople: TextView = findViewById(R.id.textPeople)

        val pillMedicines: View = findViewById(R.id.pillMedicines)
        val iconMedicines: ImageView = findViewById(R.id.iconMedicines)
        val textMedicines: TextView = findViewById(R.id.textMedicines)

        val pillIconActive = androidx.core.content.ContextCompat.getColor(this, R.color.nav_pill_icon_active)
        val pillIconInactive = androidx.core.content.ContextCompat.getColor(this, R.color.nav_pill_icon_inactive)

        if (tabId == R.id.tabPeople) {
            pillPeople.setBackgroundResource(R.drawable.nav_item_pill_selected)
            iconPeople.imageTintList = android.content.res.ColorStateList.valueOf(pillIconActive)

            pillMedicines.setBackgroundResource(android.R.color.transparent)
            iconMedicines.imageTintList = android.content.res.ColorStateList.valueOf(pillIconInactive)

            textScreenTitle.text = "Seznam oseb"
            layoutSearchMedicine.visibility = View.GONE
            recyclerView.adapter = personAdapter
        } else if (tabId == R.id.tabMedicines) {
            pillMedicines.setBackgroundResource(R.drawable.nav_item_pill_selected)
            iconMedicines.imageTintList = android.content.res.ColorStateList.valueOf(pillIconActive)

            pillPeople.setBackgroundResource(android.R.color.transparent)
            iconPeople.imageTintList = android.content.res.ColorStateList.valueOf(pillIconInactive)

            textScreenTitle.text = "Seznam zdravil"
            layoutSearchMedicine.visibility = View.VISIBLE
            recyclerView.adapter = medicineAdapter
            filterMedicines()
        }
    }

    private fun filterMedicines() {
        val filtered = if (currentMedicineQuery.isBlank()) {
            medicines.sortedBy { it.name.lowercase() }
        } else {
            medicines.filter {
                it.name.contains(currentMedicineQuery, ignoreCase = true) ||
                it.activeIngredient.contains(currentMedicineQuery, ignoreCase = true)
            }.sortedBy { it.name.lowercase() }
        }
        medicineAdapter.updateData(filtered)
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun handleAdd() {
        if (activeTab == R.id.tabPeople) {
            val intent = Intent(this, PersonActivity::class.java)
            personLauncher.launch(intent)
        } else {
            val intent = Intent(this, MedicineActivity::class.java)
            medicineLauncher.launch(intent)
        }
    }

    private fun handleEditPerson(person: Person) {
        val intent = Intent(this, PersonActivity::class.java)
        intent.putExtra(PersonActivity.EXTRA_PERSON, person)
        personLauncher.launch(intent)
    }

    private fun handleDeletePerson(person: Person) {
        val position = persons.indexOf(person)
        if (position != -1) {
            persons.removeAt(position)
            personAdapter.notifyItemRemoved(position)
            Snackbar.make(findViewById(android.R.id.content), "Oseba ${person.firstName} je bila izbrisana", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleManagePersonMedicines(person: Person) {
        val intent = Intent(this, DosageActivity::class.java)
        intent.putExtra(DosageActivity.EXTRA_PERSON, person)
        intent.putExtra(DosageActivity.EXTRA_ALL_MEDICINES, ArrayList(medicines))
        prescribedMedicinesLauncher.launch(intent)
    }

    private fun handleEditMedicine(medicine: Medicine) {
        val intent = Intent(this, MedicineActivity::class.java)
        intent.putExtra(MedicineActivity.EXTRA_MEDICINE, medicine)
        medicineLauncher.launch(intent)
    }

    private fun handleDeleteMedicine(medicine: Medicine) {
        val position = medicines.indexOf(medicine)
        if (position != -1) {
            medicines.removeAt(position)
            filterMedicines()
            Snackbar.make(findViewById(android.R.id.content), "Izbrisano zdravilo: ${medicine.name}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupDummyData() {
        // Create medicines first so we can reference their IDs
        val kventiax = Medicine(UUID.randomUUID().toString(), "Kventiax", "kvetiapin", 0.0, 0.0, 25.0, 1.0, "Antipsihotik / v malih dozah za spanje")
        val asentra = Medicine(UUID.randomUUID().toString(), "Asentra", "sertralin", 0.0, 0.0, 50.0, 1.0, "Antidepresiv (SSRI)")
        val helex = Medicine(UUID.randomUUID().toString(), "Helex", "alprazolam", 0.0, 0.0, 0.25, 1.0, "Za lajšanje hudih tesnobnih stanj (anksiolitik)")
        val cipralex = Medicine(UUID.randomUUID().toString(), "Cipralex", "escitalopram", 0.0, 0.0, 10.0, 1.0, "Antidepresiv (SSRI)")
        val ritalin = Medicine(UUID.randomUUID().toString(), "Ritalin", "metilfenidat", 0.0, 0.0, 10.0, 1.0, "Za zdravljenje ADHD")
        val strattera = Medicine(UUID.randomUUID().toString(), "Strattera", "atomoksetin", 0.0, 0.0, 40.0, 1.0, "Ne-stimulativno zdravilo za ADHD")
        val zyprexa = Medicine(UUID.randomUUID().toString(), "Zyprexa", "olanzapin", 0.0, 0.0, 5.0, 1.0, "Antipsihotik")
        val calpol = Medicine(UUID.randomUUID().toString(), "Calpol", "paracetamol", 10.0, 15.0, 120.0, 5.0, "Sirup za lajšanje bolečin in zniževanje vročine")
        val brufen = Medicine(UUID.randomUUID().toString(), "Brufen sirup", "ibuprofen", 20.0, 30.0, 100.0, 5.0, "Sirup proti bolečinam in vnetjem")
        val hiconcil = Medicine(UUID.randomUUID().toString(), "Hiconcil", "amoksicilin", 20.0, 40.0, 250.0, 5.0, "Antibiotik (penicilin) v sirupu")
        val zinnat = Medicine(UUID.randomUUID().toString(), "Zinnat", "cefuroksim", 10.0, 15.0, 125.0, 5.0, "Antibiotik v sirupu")
        val claritine = Medicine(UUID.randomUUID().toString(), "Claritine sirup", "loratadin", 0.1, 0.2, 5.0, 5.0, "Protialergijski sirup")
        val aquipta = Medicine(UUID.randomUUID().toString(), "Aquipta", "atogepant", 0.0, 0.0, 60.0, 1.0, "Za preventivo migrene")
        val controloc = Medicine(UUID.randomUUID().toString(), "Controloc", "pantoprazol", 0.0, 0.0, 40.0, 1.0, "Zaviranje želodčne kisline (zgaga in refluks)")
        val atoris = Medicine(UUID.randomUUID().toString(), "Atoris", "atorvastatin", 0.0, 0.0, 20.0, 1.0, "Za zniževanje povišanega holesterola")
        val euthyrox = Medicine(UUID.randomUUID().toString(), "Euthyrox", "levotiroksin natrij", 0.0, 0.0, 0.1, 1.0, "Hormonsko zdravljenje zmanjšanega delovanja ščitnice")

        medicines.addAll(listOf(kventiax, asentra, helex, cipralex, ritalin, strattera, zyprexa, calpol, brufen, hiconcil, zinnat, claritine, aquipta, controloc, atoris, euthyrox))
        medicines.sortBy { it.name.lowercase() }

        // Create persons with Prescription references by medicine ID and ensure all have doses, and some have notes
        persons.add(Person(UUID.randomUUID().toString(), "Janez", "Novak", "M", "1990-01-01", 85.0, 180.0, listOf(Prescription(atoris.id, "20 mg", "Vzeti zvečer ob hrani"), Prescription(controloc.id, "40 mg", "30 minut pred zajtrkom"))))
        persons.add(Person(UUID.randomUUID().toString(), "Luka", "Kovač", "M", "1995-07-20", 78.0, 185.0, listOf(Prescription(hiconcil.id, "31.20 ml"), Prescription(calpol.id, "32.50 ml", "Po potrebi ob povišani temperaturi"))))
        persons.add(Person(UUID.randomUUID().toString(), "Marija", "Horvat", "Ž", "1985-05-15", 65.0, 170.0, listOf(Prescription(kventiax.id, "25 mg", "Pred spanjem"), Prescription(controloc.id, "40 mg"))))
        persons.add(Person(UUID.randomUUID().toString(), "Ana", "Zupan", "Ž", "1992-11-03", 58.0, 165.0, listOf(Prescription(aquipta.id, "60 mg"), Prescription(asentra.id, "50 mg", "Zjutraj po jedi"), Prescription(helex.id, "0.25 mg", "Po potrebi ob napadih panike"))))
        persons.add(Person(UUID.randomUUID().toString(), "Aleks", "Car", "D", "2015-03-25", 30.0, 135.0, listOf(Prescription(calpol.id, "12.50 ml"), Prescription(claritine.id, "6.00 ml"))))
        persons.add(Person(UUID.randomUUID().toString(), "Robin", "Drago", "D", "2018-09-12", 22.0, 115.0, listOf(Prescription(brufen.id, "11.00 ml", "Po jedi z obilo tekočine"), Prescription(zinnat.id, "8.80 ml"))))
        persons.add(Person(UUID.randomUUID().toString(), "Peter", "Majcen", "M", "1978-04-10", 92.0, 188.0, listOf(Prescription(cipralex.id, "10 mg"), Prescription(atoris.id, "20 mg"))))
        persons.add(Person(UUID.randomUUID().toString(), "Klara", "Zver", "Ž", "2003-12-05", 52.0, 160.0, listOf(Prescription(strattera.id, "40 mg"), Prescription(ritalin.id, "10 mg", "Vzeti pred šolo"))))
        persons.add(Person(UUID.randomUUID().toString(), "Tomaž", "Rupnik", "M", "1965-08-30", 80.0, 174.0, listOf(Prescription(zyprexa.id, "5 mg"), Prescription(controloc.id, "40 mg"), Prescription(euthyrox.id, "100 mcg", "Zjutraj na tešče"))))
        persons.add(Person(UUID.randomUUID().toString(), "Neja", "Bizjak", "Ž", "1994-02-18", 68.0, 168.0, listOf(Prescription(helex.id, "0.25 mg", "Samo ob izraziti tesnobi"))))
    }
}
