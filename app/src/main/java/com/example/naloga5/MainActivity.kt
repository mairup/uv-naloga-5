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

class MainActivity : AppCompatActivity() {

    private val persons = mutableListOf<Person>()
    private val medicines = mutableListOf<Medicine>()

    private lateinit var personAdapter: PersonAdapter
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textScreenTitle: TextView

    private var activeTab: Int = R.id.tabPeople

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
                    medicineAdapter.notifyItemChanged(index)
                    Snackbar.make(findViewById(android.R.id.content), "Zdravilo uspešno posodobljeno", Snackbar.LENGTH_SHORT).show()
                } else {
                    medicines.add(returnedMedicine)
                    medicineAdapter.notifyItemInserted(medicines.size - 1)
                    Snackbar.make(findViewById(android.R.id.content), "Zdravilo uspešno dodano", Snackbar.LENGTH_SHORT).show()
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
            onEditClick = { person -> handleEditPerson(person) },
            onDeleteClick = { person -> handleDeletePerson(person) }
        )

        medicineAdapter = MedicineAdapter(
            medicines,
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

        val colorOnBackground = getColorFromAttr(com.google.android.material.R.attr.colorOnBackground)
        val pillIconActive = androidx.core.content.ContextCompat.getColor(this, R.color.nav_pill_icon_active)
        val tabTextActive = androidx.core.content.ContextCompat.getColor(this, R.color.nav_tab_text_active)

        if (tabId == R.id.tabPeople) {
            pillPeople.setBackgroundResource(R.drawable.nav_item_pill_selected)
            iconPeople.imageTintList = android.content.res.ColorStateList.valueOf(pillIconActive)
            textPeople.setTextColor(tabTextActive)

            pillMedicines.setBackgroundResource(android.R.color.transparent)
            iconMedicines.imageTintList = android.content.res.ColorStateList.valueOf(colorOnBackground)
            textMedicines.setTextColor(colorOnBackground)

            textScreenTitle.text = "Seznam oseb"
            recyclerView.adapter = personAdapter
        } else if (tabId == R.id.tabMedicines) {
            pillMedicines.setBackgroundResource(R.drawable.nav_item_pill_selected)
            iconMedicines.imageTintList = android.content.res.ColorStateList.valueOf(pillIconActive)
            textMedicines.setTextColor(tabTextActive)

            pillPeople.setBackgroundResource(android.R.color.transparent)
            iconPeople.imageTintList = android.content.res.ColorStateList.valueOf(colorOnBackground)
            textPeople.setTextColor(colorOnBackground)

            textScreenTitle.text = "Zdravila"
            recyclerView.adapter = medicineAdapter
        }
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
            Snackbar.make(findViewById(android.R.id.content), "Izbrisana oseba: ${person.firstName}", Snackbar.LENGTH_SHORT).show()
        }
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
            medicineAdapter.notifyItemRemoved(position)
            Snackbar.make(findViewById(android.R.id.content), "Izbrisano zdravilo: ${medicine.name}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupDummyData() {
        persons.add(Person(UUID.randomUUID().toString(), "Janez", "Novak", "M", "1990-01-01", 85.0, 180.0, emptyList()))
        persons.add(Person(UUID.randomUUID().toString(), "Luka", "Kovač", "M", "1995-07-20", 78.0, 185.0, listOf("Amoksiklav", "Lekadol")))
        persons.add(Person(UUID.randomUUID().toString(), "Marija", "Horvat", "Ž", "1985-05-15", 65.0, 170.0, listOf("Mirzaten", "Controloc")))
        persons.add(Person(UUID.randomUUID().toString(), "Ana", "Zupan", "Ž", "1992-11-03", 58.0, 165.0, listOf("Aquipta", "Nalgesin", "Wellbutrin")))
        persons.add(Person(UUID.randomUUID().toString(), "Aleks", "Car", "D", "2000-03-25", 70.0, 175.0, listOf("Lekadol", "Claritin")))
        persons.add(Person(UUID.randomUUID().toString(), "Robin", "Drago", "D", "1998-09-12", 62.0, 172.0, listOf("Aspirin", "Wellbutrin")))
        persons.add(Person(UUID.randomUUID().toString(), "Peter", "Majcen", "M", "1978-04-10", 92.0, 188.0, listOf("Concor", "Atoris")))
        persons.add(Person(UUID.randomUUID().toString(), "Klara", "Zver", "Ž", "2003-12-05", 52.0, 160.0, listOf("Claritin", "Ventolin")))
        persons.add(Person(UUID.randomUUID().toString(), "Tomaž", "Rupnik", "M", "1965-08-30", 80.0, 174.0, listOf("Jentadueto", "Controloc", "Euthyrox")))
        persons.add(Person(UUID.randomUUID().toString(), "Neja", "Bizjak", "Ž", "1994-02-18", 68.0, 168.0, listOf("Xanax")))

        medicines.add(Medicine(UUID.randomUUID().toString(), "Amoksiklav", "amoksicilin", 25.0, 45.0, 400.0, 5.0, "Peroralna suspenzija"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Aquipta", "atogepant", 0.5, 1.0, 60.0, 1.0, "Za preventivo migrene"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Mirzaten", "mirtazapin", 0.3, 0.6, 30.0, 1.0, "Antidepresiv"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Wellbutrin", "bupropion klorid", 3.0, 6.0, 150.0, 1.0, "Antidepresiv / odvajanje od kajenja"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Lekadol", "paracetamol", 10.0, 15.0, 120.0, 5.0, "Za lajšanje bolečin in zniževanje telesne temperature"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Nalgesin", "naproksen natrij", 5.0, 10.0, 275.0, 1.0, "Proti bolečinam in vnetjem"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Aspirin", "acetilsalicilna kislina", 10.0, 20.0, 500.0, 1.0, "Protibolečinsko in protivnetno zdravilo"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Concor", "bisoprolol", 0.05, 0.15, 5.0, 1.0, "Za zniževanje krvnega tlaka (beta blokator)"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Jentadueto", "linagliptin / metformin", 10.0, 20.0, 850.0, 1.0, "Za zdravljenje sladkorne bolezni tipa 2"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Ventolin", "salbutamol", 0.1, 0.2, 2.0, 5.0, "Za širjenje dihalnih poti (lajšanje astme)"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Atoris", "atorvastatin", 0.15, 0.3, 20.0, 1.0, "Za zniževanje povišanega holesterola"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Claritin", "loratadin", 0.1, 0.2, 10.0, 10.0, "Protialergijsko zdravilo (antihistaminik)"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Euthyrox", "levotiroksin natrij", 0.001, 0.002, 0.1, 1.0, "Hormonsko zdravljenje zmanjšanega delovanja ščitnice"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Controloc", "pantoprazol", 0.4, 0.8, 40.0, 1.0, "Zaviranje želodčne kisline (zgaga in refluks)"))
        medicines.add(Medicine(UUID.randomUUID().toString(), "Xanax", "alprazolam", 0.01, 0.03, 0.5, 1.0, "Za lajšanje hudih tesnobnih stanj (anksiolitik)"))
    }
}
