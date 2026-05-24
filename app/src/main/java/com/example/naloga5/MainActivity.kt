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
                    showSnackbar("Oseba uspešno posodobljena")
                } else {
                    persons.add(returnedPerson)
                    personAdapter.notifyItemInserted(persons.size - 1)
                    showSnackbar("Oseba uspešno dodana")
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
                    showSnackbar("Zdravilo uspešno posodobljeno")
                } else {
                    medicines.add(returnedMedicine)
                    filterMedicines()
                    showSnackbar("Zdravilo uspešno dodano")
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
                    showSnackbar("Predpisana zdravila posodobljena")
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
            personAdapter.notifyItemRangeChanged(position, persons.size - position)
            showSnackbar("Oseba ${person.firstName} je bila izbrisana")
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

            // Remove this medicine from all persons' prescriptions
            for (i in persons.indices) {
                val person = persons[i]
                val updatedRx = person.prescribedMedicines.filter { it.medicineId != medicine.id }
                if (updatedRx.size != person.prescribedMedicines.size) {
                    persons[i] = person.copy(prescribedMedicines = updatedRx)
                    personAdapter.notifyItemChanged(i)
                }
            }

            showSnackbar("Izbrisano zdravilo: ${medicine.name}")
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.bottomNavBar)
            .show()
    }

    private fun setupDummyData() {
        // Create medicines first so we can reference their IDs
        val kventiax = Medicine(UUID.randomUUID().toString(), "Kventiax", "kvetiapin", 0.0, 0.0, 0.0, 0.0, "Antipsihotik / v malih dozah za spanje")
        val asentra = Medicine(UUID.randomUUID().toString(), "Asentra", "sertralin", 0.0, 0.0, 0.0, 0.0, "Antidepresiv (SSRI)")
        val helex = Medicine(UUID.randomUUID().toString(), "Helex", "alprazolam", 0.0, 0.0, 0.0, 0.0, "Za lajšanje hudih tesnobnih stanj (anksiolitik)")
        val cipralex = Medicine(UUID.randomUUID().toString(), "Cipralex", "escitalopram", 0.0, 0.0, 0.0, 0.0, "Antidepresiv (SSRI)")
        val ritalin = Medicine(UUID.randomUUID().toString(), "Ritalin", "metilfenidat", 0.0, 0.0, 0.0, 0.0, "Za zdravljenje ADHD")
        val strattera = Medicine(UUID.randomUUID().toString(), "Strattera", "atomoksetin", 0.0, 0.0, 0.0, 0.0, "Ne-stimulativno zdravilo za ADHD")
        val zyprexa = Medicine(UUID.randomUUID().toString(), "Zyprexa", "olanzapin", 0.0, 0.0, 0.0, 0.0, "Antipsihotik")
        val calpol = Medicine(UUID.randomUUID().toString(), "Calpol", "paracetamol", 10.0, 15.0, 120.0, 5.0, "Sirup za lajšanje bolečin in zniževanje vročine")
        val brufen = Medicine(UUID.randomUUID().toString(), "Brufen sirup", "ibuprofen", 20.0, 30.0, 100.0, 5.0, "Sirup proti bolečinam in vnetjem")
        val hiconcil = Medicine(UUID.randomUUID().toString(), "Hiconcil", "amoksicilin", 20.0, 40.0, 250.0, 5.0, "Antibiotik (penicilin) v sirupu")
        val zinnat = Medicine(UUID.randomUUID().toString(), "Zinnat", "cefuroksim", 10.0, 15.0, 125.0, 5.0, "Antibiotik v sirupu")
        val claritine = Medicine(UUID.randomUUID().toString(), "Claritine sirup", "loratadin", 0.1, 0.2, 5.0, 5.0, "Protialergijski sirup")
        val aquipta = Medicine(UUID.randomUUID().toString(), "Aquipta", "atogepant", 0.0, 0.0, 0.0, 0.0, "Za preventivo migrene")
        val controloc = Medicine(UUID.randomUUID().toString(), "Controloc", "pantoprazol", 0.0, 0.0, 0.0, 0.0, "Zaviranje želodčne kisline (zgaga in refluks)")
        val atoris = Medicine(UUID.randomUUID().toString(), "Atoris", "atorvastatin", 0.0, 0.0, 0.0, 0.0, "Za zniževanje povišanega holesterola")
        val euthyrox = Medicine(UUID.randomUUID().toString(), "Euthyrox", "levotiroksin natrij", 0.0, 0.0, 0.0, 0.0, "Hormonsko zdravljenje zmanjšanega delovanja ščitnice")
        val wellbutrin = Medicine(UUID.randomUUID().toString(), "Wellbutrin", "bupropion", 0.0, 0.0, 0.0, 0.0, "Antidepresiv (NDRI)")
        val remeron = Medicine(UUID.randomUUID().toString(), "Mirzaten", "mirtazapin", 0.0, 0.0, 0.0, 0.0, "Antidepresiv (NaSSA)")

        // 5 new psych meds
        val lekotam = Medicine(UUID.randomUUID().toString(), "Lekotam", "bromazepam", 0.0, 0.0, 0.0, 0.0, "Anksiolitik (benzodiazepin)")
        val sanval = Medicine(UUID.randomUUID().toString(), "Sanval", "zolpidem", 0.0, 0.0, 0.0, 0.0, "Sedativ in hipnotik za kratkotrajno zdravljenje nespečnosti")
        val trittico = Medicine(UUID.randomUUID().toString(), "Trittico", "trazodon", 0.0, 0.0, 0.0, 0.0, "Antidepresiv s sedativnim učinkom")
        val cymbalta = Medicine(UUID.randomUUID().toString(), "Cymbalta", "duloksetin", 0.0, 0.0, 0.0, 0.0, "Antidepresiv in anksiolitik (SNRI)")
        val eglonyl = Medicine(UUID.randomUUID().toString(), "Eglonyl", "sulpirid", 0.0, 0.0, 0.0, 0.0, "Antipsihotik z antidepresivnim in anksiolitičnim delovanjem")

        // 5 new relative dose meds (mg/kg)
        val daleron = Medicine(UUID.randomUUID().toString(), "Daleron sirup", "paracetamol", 10.0, 15.0, 120.0, 5.0, "Sirup za otroke proti vročini in bolečinam")
        val sumamed = Medicine(UUID.randomUUID().toString(), "Sumamed sirup", "azitromicin", 10.0, 10.0, 100.0, 5.0, "Širokospektralni antibiotik za otroke, enkrat dnevno")
        val flonidan = Medicine(UUID.randomUUID().toString(), "Flonidan sirup", "loratadin", 0.1, 0.2, 5.0, 5.0, "Protialergijski sirup za otroke")
        val ospen = Medicine(UUID.randomUUID().toString(), "Ospen sirup", "fenoksimetilpenicilin kalij", 25.0, 50.0, 250.0, 5.0, "Penicilinski antibiotik za zdravljenje angine pri otrocih")
        val keppra = Medicine(UUID.randomUUID().toString(), "Keppra sirup", "levetiracetam", 10.0, 30.0, 100.0, 1.0, "Sirup za zdravljenje epilepsije (protiepileptik)")

        medicines.addAll(listOf(
            kventiax, asentra, helex, cipralex, ritalin, strattera, zyprexa, calpol, brufen, hiconcil, 
            zinnat, claritine, aquipta, controloc, atoris, euthyrox, wellbutrin, remeron,
            lekotam, sanval, trittico, cymbalta, eglonyl,
            daleron, sumamed, flonidan, ospen, keppra
        ))
        medicines.sortBy { it.name.lowercase() }

        // Create persons with Prescription references by medicine ID and ensure all have doses, and some have notes
        persons.add(Person(UUID.randomUUID().toString(), "Janez", "Novak", "M", "1990-01-01", 85.0, 180.0, listOf(
            Prescription(atoris.id, "20 mg", "Vzeti zvečer ob hrani"), 
            Prescription(controloc.id, "40 mg", "30 minut pred zajtrkom"),
            Prescription(sanval.id, "5 mg", "Po potrebi zvečer pred spanjem ob hudi nespečnosti")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Luka", "Kovač", "M", "1995-07-20", 78.0, 185.0, listOf(
            Prescription(wellbutrin.id, "150 mg", "Zjutraj s kozarcem vode"), 
            Prescription(lekotam.id, "1.5 mg", "Po potrebi ob napadu hude anksioznosti (največ dvakrat dnevno)")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Marija", "Horvat", "Ž", "1985-05-15", 65.0, 170.0, listOf(
            Prescription(kventiax.id, "25 mg", "Zvečer, 30 minut pred spanjem"), 
            Prescription(cipralex.id, "10 mg", "Zjutraj po jedi"),
            Prescription(controloc.id, "20 mg", "Zjutraj na tešče")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Ana", "Zupan", "Ž", "1992-11-03", 58.0, 165.0, listOf(
            Prescription(aquipta.id, "60 mg", "Za preventivo migrenskih napadov enkrat dnevno"), 
            Prescription(asentra.id, "100 mg", "Zjutraj po zajtrku"), 
            Prescription(helex.id, "0.25 mg", "Po potrebi ob napadih panike")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Aleks", "Car", "D", "2015-03-25", 30.0, 135.0, listOf(
            Prescription(daleron.id, "12.50 ml", "Po potrebi na 6 ur ob povišani temperaturi"), 
            Prescription(sumamed.id, "7.50 ml", "Enkrat dnevno 3 dni za pljučno okužbo")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Robin", "Drago", "D", "2018-09-12", 22.0, 115.0, listOf(
            Prescription(brufen.id, "5.50 ml", "Po potrebi ob bolečinah"), 
            Prescription(zinnat.id, "4.40 ml", "Na 12 ur za vnetje ušesa, izprazniti celo stekleničko"),
            Prescription(flonidan.id, "4.40 ml", "Enkrat dnevno ob pojavu sezonskih alergij")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Peter", "Majcen", "M", "1978-04-10", 92.0, 188.0, listOf(
            Prescription(cipralex.id, "20 mg", "Zjutraj ob isti uri"), 
            Prescription(trittico.id, "75 mg", "Zvečer pred spanjem za izboljšanje spanja"),
            Prescription(atoris.id, "20 mg", "Zvečer")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Klara", "Zver", "Ž", "2003-12-05", 52.0, 160.0, listOf(
            Prescription(strattera.id, "40 mg", "Zjutraj ob hrani za zdravljenje ADHD"), 
            Prescription(ritalin.id, "10 mg", "Zjutraj pred šolo")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Maja", "Kranjc", "Ž", "1991-04-12", 62.0, 168.0, listOf(
            Prescription(zyprexa.id, "5 mg", "Zvečer pred spanjem"), 
            Prescription(euthyrox.id, "75 mcg", "Zjutraj 30 minut pred zajtrkom na prazen želodec"),
            Prescription(controloc.id, "20 mg", "Zjutraj 30 minut pred zajtrkom na tešče")
        )))
        
        persons.add(Person(UUID.randomUUID().toString(), "Neja", "Bizjak", "Ž", "1994-02-18", 68.0, 168.0, listOf(
            Prescription(cymbalta.id, "60 mg", "Zjutraj po jedi"),
            Prescription(helex.id, "0.5 mg", "Samo ob izraziti tesnobi po potrebi")
        )))

        // 3 new persons
        val marko = Person(UUID.randomUUID().toString(), "Marko", "Turk", "M", "1980-11-12", 82.0, 179.0, listOf(
            Prescription(eglonyl.id, "200 mg", "Zjutraj in opoldne pred jedjo"),
            Prescription(wellbutrin.id, "150 mg", "Zjutraj")
        ))
        
        val tjasa = Person(UUID.randomUUID().toString(), "Tjaša", "Oblak", "Ž", "1998-06-25", 54.0, 164.0, listOf(
            Prescription(remeron.id, "30 mg", "Zvečer tik pred spanjem"),
            Prescription(sanval.id, "10 mg", "Po potrebi tik pred spanjem, ko ne more zaspati"),
            Prescription(ospen.id, "10.80 ml", "Na 8 ur za streptokokno angino (10 dni)")
        ))
        
        val matej = Person(UUID.randomUUID().toString(), "Matej", "Pirc", "M", "2012-02-14", 42.0, 148.0, listOf(
            Prescription(ritalin.id, "10 mg", "Zjutraj pred odhodom v šolo"),
            Prescription(keppra.id, "8.40 ml", "Na 12 ur zjutraj in zvečer za preprečevanje epileptičnih napadov")
        ))

        persons.addAll(listOf(marko, tjasa, matej))
    }
}
