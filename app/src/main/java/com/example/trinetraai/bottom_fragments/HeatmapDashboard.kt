package com.example.trinetraai.bottom_fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.example.trinetraai.drawer_activities.AllFIRsActivity
import com.example.trinetraai.firdataclass.FIR
import com.example.trinetraai.firdataclass.LocationData
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
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

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

    private lateinit var hp_zone1name : TextView
    private lateinit var hp_zone1cases : TextView
    private lateinit var hp_zone2name : TextView
    private lateinit var hp_zone2cases : TextView



    private lateinit var case1_FIRid : TextView
    private lateinit var case1_Crime : TextView
    private lateinit var case1_Time : TextView
    private lateinit var case1_Location : TextView

    private lateinit var case2_FIRid : TextView
    private lateinit var case2_Crime : TextView
    private lateinit var case2_Time : TextView
    private lateinit var case2_Location : TextView

    private lateinit var case3_FIRid : TextView
    private lateinit var case3_Crime : TextView
    private lateinit var case3_Time : TextView
    private lateinit var case3_Location : TextView

    private lateinit var viewAllFIR : MaterialButton

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

        activeFIR = view.findViewById(R.id.tVActiveFir)
        getFIRCount(activeFIR)

        hp_zone1name = view.findViewById(R.id.hp_Zone1)
        hp_zone1cases = view.findViewById(R.id.hp_Zone1Cases)
        hp_zone2name = view.findViewById(R.id.hp_Zone2)
        hp_zone2cases = view.findViewById(R.id.hp_Zone2Cases)


        hotspots = view.findViewById(R.id.tVHotspots)
        getHotspotCount(hotspots , hp_zone1name , hp_zone1cases , hp_zone2name , hp_zone2cases)

        case1_Time =view.findViewById(R.id.case1_Timestamp)
        case1_FIRid = view.findViewById(R.id.case1_FIRid)
        case1_Crime = view.findViewById(R.id.case1_Crime)
        case1_Location = view.findViewById(R.id.case1_AreaZone)

        case2_Time = view.findViewById(R.id.case2_Timestamp)
        case2_FIRid = view.findViewById(R.id.case2_FIRid)
        case2_Crime = view.findViewById(R.id.case2_Crime)
        case2_Location = view.findViewById(R.id.case2_AreaZone)

        case3_Time = view.findViewById(R.id.case3_Timestamp)
        case3_FIRid = view.findViewById(R.id.case3_FIRid)
        case3_Crime = view.findViewById(R.id.case3_Crime)
        case3_Location = view.findViewById(R.id.case3_AreaZone)

        loadRecentFIR(
            case1_Time, case1_FIRid, case1_Crime, case1_Location,
            case2_Time,
            case2_FIRid,
            case2_Crime,
            case2_Location,
            case3_Time,
            case3_FIRid,
            case3_Crime,
            case3_Location,
        )
        viewAllFIR = view.findViewById(R.id.btnViewAllFirs)
        viewAllFIR.setOnClickListener {
            startActivity(Intent(requireContext(), AllFIRsActivity ::class.java))
        }




        return view
    }

    private fun loadRecentFIR(
        case1_Time: TextView,
        case1_FIRid: TextView,
        case1_Crime: TextView,
        case1_Location: TextView,
        case2_Time: TextView,
        case2_FIRid: TextView,
        case2_Crime: TextView,
        case2_Location: TextView,
        case3_Time: TextView,
        case3_FIRid: TextView,
        case3_Crime: TextView,
        case3_Location: TextView,
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("FIR_Records")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                val firList = documents.mapNotNull { doc ->
                        try {
                            FIR(
                                fir_id = doc.getString("fir_id") ?: "",
                                crime_type = doc.getString("crime_type") ?: "",
                                ipc_sections = doc.get("ipc_sections") as? List<String> ?: listOf(),
                                act_category = doc.getString("act_category") ?: "",
                                location = LocationData(
                                    lat = (doc.get("location") as? Map<String, Any>)?.get("lat") as? Double
                                        ?: 0.0,
                                    lng = (doc.get("location") as? Map<String, Any>)?.get("lng") as? Double
                                        ?: 0.0,
                                    area = (doc.get("location") as? Map<String, Any>)?.get("area") as? String
                                        ?: ""
                                ),
                                timestamp = doc.getString("timestamp") ?: "",
                                zone = doc.getString("zone") ?: "",
                                status = doc.getString("status") ?: "",
                                reporting_station = doc.getString("reporting_station") ?: ""
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                case1_Time.text = formatTimestampReadable(firList[0].timestamp)
                case1_FIRid.text = firList[0].fir_id
                case1_Crime.text = firList[0].crime_type
                case1_Location.text = "${firList[0].zone} - ${firList[0].location.area}"

                case2_Time.text = formatTimestampReadable(firList[1].timestamp)
                case2_FIRid.text = firList[1].fir_id
                case2_Crime.text = firList[1].crime_type
                case2_Location.text = "${firList[1].zone} - ${firList[1].location.area}"

                case3_Time.text = formatTimestampReadable(firList[2].timestamp)
                case3_FIRid.text = firList[2].fir_id
                case3_Crime.text = firList[2].crime_type
                case3_Location.text = "${firList[2].zone} - ${firList[2].location.area}"
            }

            .addOnFailureListener {
                it.printStackTrace()
            }
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


    private fun getHotspotCount(
        hp_count: TextView,
        hp_zone1name: TextView,
        hp_zone1cases: TextView,
        hp_zone2name: TextView,
        hp_zone2cases: TextView
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("FIR_Records")
            .get()
            .addOnSuccessListener { documents ->
                val zoneInfoMap = mutableMapOf<String, Pair<Int, String>>()

                for (doc in documents) {
                    val zone = doc.getString("zone")?.trim() ?: continue
                    val location = LocationData(
                        lat = (doc.get("location") as? Map<String, Any>)?.get("lat") as? Double ?: 0.0,
                        lng = (doc.get("location") as? Map<String, Any>)?.get("lng") as? Double ?: 0.0,
                        area = (doc.get("location") as? Map<String, Any>)?.get("area") as? String ?: ""
                    )
                    val currentCount = zoneInfoMap[zone]?.first?:0
                    zoneInfoMap[zone] = Pair(currentCount + 1, location.area)
                    val hotspotZones = zoneInfoMap.filter { it.value.first > 8 }.toMap()
                    hp_count.text = hotspotZones.size.toString()


                    val sortedHotspotList = hotspotZones.entries.sortedByDescending { it.value.first }

                    sortedHotspotList.getOrNull(0)?.let { (zoneId, pair) ->
                        val (count, area) = pair
                        hp_zone1name.text = "$zoneId - $area"
                        hp_zone1cases.text = "${count.toString()} cases"
                    }

                    sortedHotspotList.getOrNull(1)?.let { (zoneId, pair) ->
                        val (count, area) = pair
                        hp_zone2name.text = "$zoneId - $area"
                        hp_zone2cases.text = "${count.toString()} cases"
                    }
                }
            }
            .addOnFailureListener {
                hp_count.text = "Failed to fetch FIRs"
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
