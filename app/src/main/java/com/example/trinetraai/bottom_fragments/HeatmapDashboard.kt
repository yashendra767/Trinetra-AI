package com.example.trinetraai.bottom_fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.trinetraai.R
import com.example.trinetraai.bottom_fragments.expandMap.FullScreenMap
import com.example.trinetraai.firdataclass.FIR
import com.example.trinetraai.presetData.CrimeTypesData
import com.example.trinetraai.presetData.DateRangeData
import com.example.trinetraai.presetData.TimePeriodData
import com.example.trinetraai.presetData.ZoneData.delhiZones
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.common.reflect.TypeToken

class HeatmapDashboard : Fragment(), OnMapReadyCallback {

    private lateinit var db: FirebaseFirestore
    private var mGoogleMap: GoogleMap? = null
    private lateinit var expandMap: ImageView

    private val zoneMarkers = mutableListOf<Marker>()
    private val zoneCenterMap = mutableMapOf<String, LatLng>()

    private lateinit var crimeTypeSpinner: Spinner
    private lateinit var dateRangeSpinner: Spinner
    private lateinit var timePeriodSpinner: Spinner
    private lateinit var applyFilterButton: MaterialButton

    private lateinit var activeFIR : TextView
    private lateinit var hotspots : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_heatmap_dashboard, container, false)

        activeFIR = view.findViewById(R.id.tVActiveFir)
        getFIRCount(activeFIR)
        
        hotspots = view.findViewById(R.id.tVHotspots)
        getHotspotCount(hotspots)
        
        






        db = FirebaseFirestore.getInstance()
        uploadFIRDataOnce()

        crimeTypeSpinner = view.findViewById(R.id.crimeTypeSpinner)
        dateRangeSpinner = view.findViewById(R.id.dateRangeSpinner)
        timePeriodSpinner = view.findViewById(R.id.timePeriodSpinner)
        applyFilterButton = view.findViewById(R.id.btnApplyFilter)

        val crimeList = CrimeTypesData.crimeTypeMap
        val dateRangeList = DateRangeData.dateRanges
        val timePeriodList = TimePeriodData.timePeriods

        setCrimeTypeSpinner(crimeList, crimeTypeSpinner)
        setDateSpinner(dateRangeList, dateRangeSpinner)
        setTimePeriodSpinner(timePeriodList, timePeriodSpinner)

        // Load map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Expand map click
        expandMap = view.findViewById(R.id.ExpandMapHotspot)
        expandMap.setOnClickListener {
            val currentPosition = mGoogleMap?.cameraPosition
            val dialog = FullScreenMap()
            val bundle = Bundle()
            bundle.putDouble("lat", currentPosition?.target?.latitude ?: 28.6139)
            bundle.putDouble("lng", currentPosition?.target?.longitude ?: 77.2090)
            bundle.putFloat("zoom", currentPosition?.zoom ?: 13f)
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "map_fullscreen")
        }

        return view
    }

    private fun getHotspotCount(view: TextView) {
        val db = FirebaseFirestore.getInstance()

        db.collection("FIR_Records")
            .get()
            .addOnSuccessListener { documents ->
                val zoneCountMap = mutableMapOf<String, Int>()

                for (doc in documents) {
                    val zone = doc.getString("zone")?.trim() ?: continue
                    zoneCountMap[zone] = zoneCountMap.getOrDefault(zone, 0) + 1
                }


                val hotspotZones = zoneCountMap.filter { it.value > 10 }.keys.toList()
                view.text = hotspotZones.size.toString()

            }
            .addOnFailureListener {
                view.text = "Failed to fetch FIRs"
            }
    }



    private fun getFIRCount(activeFIR: TextView) {
        val db = FirebaseFirestore.getInstance()
        db.collection("FIR_Records")
            .get()
            .addOnSuccessListener { documents ->
                var openFIRCount = 0

                for (doc in documents) {
                    val status = doc.getString("status")
                    if (status.equals("Open", ignoreCase = true) || status.equals("Under Investigation", ignoreCase = true)) {
                        openFIRCount++
                    }
                }
                activeFIR.text = openFIRCount.toString()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(requireContext(), "Error fetching FIRs", Toast.LENGTH_SHORT).show()
            }

    }


    // === FIR UPLOADER ===
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadFIRDataOnce() {
        val prefs = requireContext().getSharedPreferences("fir_upload_status", Context.MODE_PRIVATE)
        val alreadyUploaded = prefs.getBoolean("is_uploaded", false)

        if (alreadyUploaded) {
            Log.d("FIRUploader", "Already uploaded FIR data. Skipping.")
            return
        }

        val json = requireContext().assets.open("fir_data.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val firList: List<FIR> = gson.fromJson(json, object : TypeToken<List<FIR>>() {}.type)

        var successCount = 0

        for (fir in firList) {
            val firMap = hashMapOf(
                "fir_id" to fir.fir_id,
                "crime_type" to fir.crime_type,
                "ipc_sections" to fir.ipc_sections,
                "act_category" to fir.act_category,
                "location" to hashMapOf(
                    "lat" to fir.location.lat,
                    "lng" to fir.location.lng,
                    "area" to fir.location.area
                ),
                "timestamp" to fir.timestamp.toString(),
                "zone" to fir.zone,
                "status" to fir.status,
                "reporting_station" to fir.reporting_station
            )

            db.collection("FIR_Records").document(fir.fir_id)
                .set(firMap)
                .addOnSuccessListener {
                    successCount++
                    Log.d("FIRUploader", "Uploaded FIR: ${fir.fir_id}")
                    if (successCount == firList.size) {
                        prefs.edit().putBoolean("is_uploaded", true).apply()
                        Toast.makeText(requireContext(), "✅ All FIRs uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FIRUploader", "❌ Failed to upload FIR: ${fir.fir_id}", e)
                }
        }
    }



    private fun setTimePeriodSpinner(timeList: List<String>, spinner: Spinner) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeList)
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        spinner.adapter = adapter
    }

    private fun setDateSpinner(dateList: List<String>, spinner: Spinner) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateList)
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        spinner.adapter = adapter
    }

    private fun setCrimeTypeSpinner(crimeList: Map<String, List<String>>, spinner: Spinner) {
        val crimeTypes = listOf("All Crimes") + crimeList.keys.sorted()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, crimeTypes)
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        spinner.adapter = adapter
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        val delhi = LatLng(28.6139, 77.2090)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 10f))

        try {
            val styled = mGoogleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (styled == false) Log.e("MapStyle", "Style parsing failed")
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        drawZoneBoxesOnMap(googleMap, false)
    }

    private fun drawZoneBoxesOnMap(googleMap: GoogleMap?, showMarkers: Boolean) {
        val boxSize = 0.011
        zoneMarkers.clear()

        delhiZones.forEach { zone ->
            val lat = zone.lat
            val lng = zone.lng
            val box = listOf(
                LatLng(lat, lng),
                LatLng(lat, lng + boxSize),
                LatLng(lat - boxSize, lng + boxSize),
                LatLng(lat - boxSize, lng),
            )

            googleMap?.addPolygon(
                PolygonOptions()
                    .addAll(box)
                    .strokeColor(Color.GRAY)
                    .fillColor(0x3300FF00)
                    .strokeWidth(2f)
            )

            zoneCenterMap["Zone ${zone.id}"] = LatLng(lat - boxSize / 2, lng + boxSize / 2)

            if (showMarkers) {
                val marker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(zoneCenterMap["Zone ${zone.id}"]!!)
                        .title("Zone ${zone.id}")
                        .snippet(zone.name)
                )
                marker?.let { zoneMarkers.add(it) }
            }
        }

        googleMap?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }
}
