package com.example.trinetraai.drawer_activities.newFIR

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.example.trinetraai.R
import com.example.trinetraai.presetData.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddNewFIRBottomSheet : BottomSheetDialogFragment() {

    private var selectedCrimeType: String? = null
    private var selectedActCategory: String? = null
    private var selectedIPCSection: String? = null
    private var selectedZone: String? = null
    private var selectedStatus: String? = null
    private var selectedReportingStation: String? = null
    private var fullZoneText: String? = null

    private lateinit var crimeTypeAuto: AutoCompleteTextView
    private lateinit var ipcSectionsAuto: AutoCompleteTextView
    private lateinit var actCategoryText: TextView
    private lateinit var reportingStationAuto: AutoCompleteTextView
    private lateinit var zoneSpinner: Spinner
    private lateinit var zoneAuto: AutoCompleteTextView

    private lateinit var statusSpinner: Spinner
    private lateinit var btnSubmitFIR: Button

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_add_new_fir, container, false)
        initializeViews(view)
        setupCrimeTypeAutoComplete()
        //setupZoneSpinner()
        setupZoneAutoComplete()

        setupStatusSpinner()
        btnSubmitFIR.setOnClickListener { generateNextFIRIDAndSave() }
        return view
    }

    private fun initializeViews(view: View) {
        crimeTypeAuto = view.findViewById(R.id.crimeTypeAuto)
        ipcSectionsAuto = view.findViewById(R.id.ipcSectionsAuto)
        actCategoryText = view.findViewById(R.id.actCategoryAuto)
        reportingStationAuto = view.findViewById(R.id.reportingStationAuto)
        //zoneSpinner = view.findViewById(R.id.zoneSpinner)
        zoneAuto = view.findViewById(R.id.zoneAuto)

        statusSpinner = view.findViewById(R.id.statusSpinner)
        btnSubmitFIR = view.findViewById(R.id.btnSubmitFIR)
    }

    private fun setupCrimeTypeAutoComplete() {
        val allCrimeTypes = CrimeTypesData.crimeTypeMap.values.flatten().distinct().sorted()
        val crimeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, allCrimeTypes)
        crimeTypeAuto.setAdapter(crimeAdapter)
        crimeTypeAuto.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) crimeTypeAuto.showDropDown() }

        crimeTypeAuto.addTextChangedListener {
            selectedCrimeType = crimeTypeAuto.text.toString().trim()
            selectedActCategory = ActCategoryMap.actCategoryMap[selectedCrimeType] ?: ""
            actCategoryText.text = selectedActCategory

            val ipcList = IPC_SectionData.ipcSectionMap[selectedCrimeType] ?: emptyList()
            ipcSectionsAuto.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ipcList))
            if (ipcList.isNotEmpty()) ipcSectionsAuto.showDropDown()
        }

        ipcSectionsAuto.setOnClickListener { ipcSectionsAuto.showDropDown() }
        ipcSectionsAuto.addTextChangedListener { selectedIPCSection = ipcSectionsAuto.text.toString().trim() }
    }

    private fun setupZoneAutoComplete() {
        val zoneList = ZoneData.zoneList.sorted()
        val zoneAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, zoneList)
        zoneAuto.setAdapter(zoneAdapter)

        zoneAuto.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) zoneAuto.showDropDown() }

        zoneAuto.addTextChangedListener {
            fullZoneText = zoneAuto.text.toString().trim()
            selectedZone = fullZoneText?.substringAfter(" - ")?.trim()
            val ps = ReportingPS.reportingStationMap[fullZoneText] ?: "Unknown PS"
            reportingStationAuto.setText(ps)
            selectedReportingStation = ps
        }
    }


    private fun setupStatusSpinner() {
        val list = listOf("Open", "Closed", "Pending", "Under Investigation")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        statusSpinner.adapter = adapter
        statusSpinner.setSelection(0)
        selectedStatus = list[0]

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedStatus = list[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedStatus = null
            }
        }
    }

    private fun generateNextFIRIDAndSave() {
        val db = FirebaseFirestore.getInstance()
        db.collection("FIR_Records").get().addOnSuccessListener { documents ->
            val firIds = documents.mapNotNull { it.id.removePrefix("FIR").toIntOrNull() }
            val nextFIRNumber = (firIds.maxOrNull() ?: 0) + 1
            val newFIRId = "FIR%04d".format(nextFIRNumber)
            saveFIRToFirestore(newFIRId)
        }
    }

    private fun saveFIRToFirestore(firId: String) {
        val db = FirebaseFirestore.getInstance()
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).format(Date())
        val areaName = fullZoneText?.substringBefore(" - ")?.trim() ?: ""
        val zoneInfo = ZoneData.delhiZones.find { it.name == areaName }
        val lat = zoneInfo?.lat ?: 0.0
        val lng = zoneInfo?.lng ?: 0.0

        val locationMap = mapOf("area" to areaName, "lat" to lat, "lng" to lng)
        val firData = hashMapOf(
            "fir_id" to firId,
            "crime_type" to selectedCrimeType,
            "act_category" to selectedActCategory,
            "ipc_sections" to listOfNotNull(selectedIPCSection),
            "zone" to selectedZone,
            "reporting_station" to selectedReportingStation,
            "status" to selectedStatus,
            "timestamp" to timestamp,
            "location" to locationMap
        )

        db.collection("FIR_Records").document(firId).set(firData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "FIR filed successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
