package com.example.trinetraai.drawer_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.LandingDashboard
import com.example.trinetraai.R
import com.example.trinetraai.drawer_fragments.AllFIRsAdapter.AllFIRsAdapter
import com.example.trinetraai.firdataclass.FIR
import com.example.trinetraai.firdataclass.LocationData
import com.example.trinetraai.presetData.CrimeTypesData
import com.example.trinetraai.presetData.DateRangeData
import com.example.trinetraai.presetData.TimePeriodData
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query


class AllFIRsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllFIRsAdapter
    private val allFIRs = mutableListOf<FIR>()
    private val filteredFIRs = mutableListOf<FIR>()

    private lateinit var crimeTypeSpinner: Spinner
    private lateinit var dateRangeSpinner: Spinner
    private lateinit var timePeriodSpinner: Spinner


    private lateinit var btnApplyFilter: MaterialButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_f_i_rs, container, false)

        setupViews(view)

        return view
    }

    private fun setupViews(view: android.view.View) {
        recyclerView = view.findViewById(R.id.displayFIRs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AllFIRsAdapter(filteredFIRs , requireContext())
        recyclerView.adapter = adapter

        crimeTypeSpinner = view.findViewById(R.id.crimeTypeSpinner)
        dateRangeSpinner = view.findViewById(R.id.dateRangeSpinner)
        timePeriodSpinner = view.findViewById(R.id.timePeriodSpinner)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)

        setupCrimeTypeSpinner(crimeTypeSpinner)
        setupdateRangeSpinner(dateRangeSpinner)
        setupTimePeriodSpinner(timePeriodSpinner)

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
                for (doc in documents) {
                    try {
                        val fir_id = doc.getString("fir_id") ?: ""
                        val crime_type = doc.getString("crime_type") ?: ""
                        val ipc_sections = doc.get("ipc_sections") as? List<String> ?: listOf()
                        val act_category = doc.getString("act_category") ?: ""
                        val location = doc.get("location") as? Map<String, Any> ?: emptyMap()
                        val timestamp = doc.getString("timestamp") ?: ""
                        val zone = doc.getString("zone") ?: ""
                        val status = doc.getString("status") ?: ""
                        val reporting_station = doc.getString("reporting_station") ?: ""

                        val fir = FIR(
                            fir_id = fir_id,
                            crime_type = crime_type,
                            ipc_sections = ipc_sections,
                            act_category = act_category,
                            location = LocationData(
                                lat = (location["lat"] as? Double) ?: 0.0,
                                lng = (location["lng"] as? Double) ?: 0.0,
                                area = (location["area"] as? String) ?: ""
                            ),
                            timestamp = timestamp.toString(),
                            zone = zone,
                            status = status,
                            reporting_station = reporting_station
                        )

                        allFIRs.add(fir)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                applyFilters()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    private fun applyFilters() {
        val selectedCategory = crimeTypeSpinner.selectedItem.toString()
        val selectedDateRange = dateRangeSpinner.selectedItem.toString()
        val selectedTimePeriod = timePeriodSpinner.selectedItem.toString()

        val now = System.currentTimeMillis()
        filteredFIRs.clear()

        for (fir in allFIRs) {
            val firTimestamp = extractTimestamp(fir) ?: continue

            val matchCategory = selectedCategory == "All" ||
                    CrimeTypesData.crimeTypeMap[selectedCategory]?.contains(fir.crime_type) == true

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

            if (matchCategory && matchDate && matchTime) {
                filteredFIRs.add(fir)
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun extractTimestamp(fir: FIR): Long? {
        val tsRaw = fir.timestamp ?: return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",  // most accurate
            "yyyy-MM-dd'T'HH:mm:ss.SSS",     // common fallback
            "yyyy-MM-dd'T'HH:mm:ss"          // basic fallback
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




    private fun setupTimePeriodSpinner(timePeriodSpinner:Spinner) {
        val timePeriods = TimePeriodData.timePeriods
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timePeriods
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        timePeriodSpinner.adapter = adapter

    }

    private fun setupdateRangeSpinner(dateRangeSpinner: Spinner) {
        val dateRanges = DateRangeData.dateRanges
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dateRanges
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        dateRangeSpinner.adapter = adapter

    }

    private fun setupCrimeTypeSpinner(crimeTypeSpinner: Spinner) {
        val categories = mutableListOf("All")
        categories.addAll(CrimeTypesData.crimeTypeMap.keys)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        crimeTypeSpinner.adapter = adapter
    }
}