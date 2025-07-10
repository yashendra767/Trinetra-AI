package com.example.trinetraai.drawer_activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trinetraai.R
import com.example.trinetraai.drawer_activities.AllFIRsAdapter.AllFIRsAdapter
import com.example.trinetraai.drawer_activities.newFIR.AddNewFIRBottomSheet
import com.example.trinetraai.firdataclass.FIR
import com.example.trinetraai.firdataclass.LocationData
import com.example.trinetraai.presetData.CrimeTypesData
import com.example.trinetraai.presetData.DateRangeData
import com.example.trinetraai.presetData.TimePeriodData
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import pl.droidsonroids.gif.GifImageView
import java.text.SimpleDateFormat
import java.util.*

class AllFIRsActivity : AppCompatActivity() {

    private lateinit var newFIR : FloatingActionButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllFIRsAdapter
    private val allFIRs = mutableListOf<FIR>()
    private val filteredFIRs = mutableListOf<FIR>()

    private lateinit var crimeTypeSpinner: Spinner
    private lateinit var dateRangeSpinner: Spinner
    private lateinit var timePeriodSpinner: Spinner
    private lateinit var statusSpinner: Spinner

    private lateinit var matchedFIRs: TextView
    private lateinit var plsselectFIR: TextView
    private lateinit var noFIRsgif: GifImageView

    private lateinit var btnApplyFilter: MaterialButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_all_f_i_rs)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.surface)
        window.statusBarColor = ContextCompat.getColor(this, R.color.surface)


        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        val rootFrame = findViewById<FrameLayout>(R.id.rootFrameAllFirs)

        ViewCompat.setOnApplyWindowInsetsListener(rootFrame) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            view.updatePadding(
                top = statusBarInsets.top,
                bottom = navBarInsets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        newFIR = findViewById(R.id.addNewFIR)
        newFIR.setOnClickListener {
            val bottomSheet = AddNewFIRBottomSheet()
            bottomSheet.show(supportFragmentManager, "AddNewFIRBottomSheet")
        }
        setupViews()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.displayFIRs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AllFIRsAdapter(filteredFIRs, this)
        recyclerView.adapter = adapter

        crimeTypeSpinner = findViewById(R.id.crimeTypeSpinner)
        dateRangeSpinner = findViewById(R.id.dateRangeSpinner)
        timePeriodSpinner = findViewById(R.id.timePeriodSpinner)
        statusSpinner = findViewById(R.id.statusSpinner)
        matchedFIRs = findViewById(R.id.countMatchedTV)
        plsselectFIR=findViewById(R.id.nofir_showtxt)
        noFIRsgif = findViewById(R.id.noDataGif)

        matchedFIRs.visibility = TextView.GONE


        btnApplyFilter = findViewById(R.id.btnApplyFilter)


        setupCrimeTypeSpinner()
        setupdateRangeSpinner()
        setupTimePeriodSpinner()
        setupStatusSpinner()

        btnApplyFilter.setOnClickListener {
            fetchAndFilterFIRs()
        }
    }

    private fun fetchAndFilterFIRs() {
        val db = FirebaseFirestore.getInstance()
        db.collection("FIR_Records")
            .get()
            .addOnSuccessListener { documents ->
                allFIRs.clear()
          if (documents.size()==0){
              plsselectFIR.visibility =View.GONE
              noFIRsgif.visibility =View.VISIBLE
          }else{
              plsselectFIR.visibility =View.GONE
                for (doc in documents) {
                    try {
                        val fir = FIR(
                            fir_id = doc.getString("fir_id") ?: "",
                            crime_type = doc.getString("crime_type") ?: "",
                            ipc_sections = doc.get("ipc_sections") as? List<String> ?: listOf(),
                            act_category = doc.getString("act_category") ?: "",
                            location = LocationData(
                                lat = (doc.get("location") as? Map<String, Any>)?.get("lat") as? Double ?: 0.0,
                                lng = (doc.get("location") as? Map<String, Any>)?.get("lng") as? Double ?: 0.0,
                                area = (doc.get("location") as? Map<String, Any>)?.get("area") as? String ?: ""
                            ),
                            timestamp =doc.getString("timestamp") ?: "",
                            zone = doc.getString("zone") ?: "",
                            status = doc.getString("status") ?: "",
                            reporting_station = doc.getString("reporting_station") ?: ""
                        )

                        allFIRs.add(fir)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                applyFilters()
            }}
            .addOnFailureListener { it.printStackTrace()
                noFIRsgif.visibility =View.VISIBLE
                Toast.makeText(this," Exception Error ",Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilters() {
        val selectedCategory = crimeTypeSpinner.selectedItem.toString()
        val selectedDateRange = dateRangeSpinner.selectedItem.toString()
        val selectedTimePeriod = timePeriodSpinner.selectedItem.toString()
        val selectedStatus = statusSpinner.selectedItem.toString()

        val now = System.currentTimeMillis()
        filteredFIRs.clear()

        for (fir in allFIRs) {
            val firTimestamp = extractTimestamp(fir) ?: continue

            val matchCategory = selectedCategory == "All" ||
                    CrimeTypesData.crimeTypeMap[selectedCategory]?.contains(fir.crime_type) == true

            val matchStatus = selectedStatus == "All" || selectedStatus == fir.status

            val matchDate = when (selectedDateRange) {
                "Last 7 Days" -> now - firTimestamp <= 7L * 24 * 60 * 60 * 1000
                "Last 15 Days" -> now - firTimestamp <= 15L * 24 * 60 * 60 * 1000
                "Last 3 Months" -> now - firTimestamp <= 90L * 24 * 60 * 60 * 1000
                "Last Year" -> now - firTimestamp <= 365L * 24 * 60 * 60 * 1000
                "All Time" -> true
                else -> true
            }

            val hour = Calendar.getInstance().apply { timeInMillis = firTimestamp }.get(Calendar.HOUR_OF_DAY)
            val matchTime = when (selectedTimePeriod) {
                "All Hours" -> true
                "00:00 – 03:00" -> hour in 0..2
                "03:00 – 09:00" -> hour in 3..8
                "09:00 – 12:00" -> hour in 9..11
                "12:00 – 18:00" -> hour in 12..17
                "18:00 – 00:00" -> hour in 18..23
                else -> true
            }

            if (matchCategory && matchDate && matchTime && matchStatus) {
                filteredFIRs.add(fir)
            }
        }

        adapter.notifyDataSetChanged()
        if (filteredFIRs.isEmpty()) {

            matchedFIRs.text = "Matched FIRs: 0"
        } else {

            matchedFIRs.text = "Matched FIRs: ${filteredFIRs.size}"
        }
        matchedFIRs.alpha = 0f
        matchedFIRs.visibility = View.VISIBLE
        matchedFIRs.animate().alpha(1f).setDuration(400).start()

    }

    private fun formatTimestampReadable(rawTimestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(rawTimestamp)
            if (date != null) outputFormat.format(date) else rawTimestamp
        } catch (e: Exception) {
            rawTimestamp
        }
    }

    private fun extractTimestamp(fir: FIR): Long? {
        val tsRaw = fir.timestamp ?: return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                return sdf.parse(tsRaw)?.time
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun setupTimePeriodSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            TimePeriodData.timePeriods
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        timePeriodSpinner.adapter = adapter
    }

    private fun setupdateRangeSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            DateRangeData.dateRanges
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        dateRangeSpinner.adapter = adapter
    }

    private fun setupCrimeTypeSpinner() {
        val categories = mutableListOf("All")
        categories.addAll(CrimeTypesData.crimeTypeMap.keys)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        crimeTypeSpinner.adapter = adapter
    }

    private fun setupStatusSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("All", "Open", "Closed", "Pending", "Under Investigation")
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        statusSpinner.adapter = adapter
    }
}
