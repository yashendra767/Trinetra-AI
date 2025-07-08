package com.example.trinetraai.bottom_fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.example.trinetraai.bottom_fragments.AllZonesAdapter.HotspotZoneAdapter
import com.example.trinetraai.bottom_fragments.expandMap.FullScreenMap
import com.example.trinetraai.drawer_activities.AllFIRsActivity
import com.example.trinetraai.firdataclass.FIR
import com.example.trinetraai.firdataclass.LocationData
import com.example.trinetraai.presetData.CrimeTypesData
import com.example.trinetraai.presetData.DateRangeData
import com.example.trinetraai.presetData.TimePeriodData
import com.example.trinetraai.presetData.ZoneData.delhiZones
import com.example.trinetraai.zoneDataClass.ZoneData_hp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.common.reflect.TypeToken
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HeatmapDashboard : Fragment(), OnMapReadyCallback {

    private lateinit var db: FirebaseFirestore
    private var mGoogleMap: GoogleMap? = null
    private lateinit var expandMap: ImageView

    private var topHotspotZone1: String? = null
    private var topHotspotZone2: String? = null

    private lateinit var showmap : ImageView


    private lateinit var toggle : SwitchCompat

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
    private lateinit var viewAllZones :MaterialButton

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

        showmap = view.findViewById(R.id.showmap)
        showmap.setOnClickListener {
            mapdialog()
        }




        db = FirebaseFirestore.getInstance()
        uploadFIRDataOnce()

        crimeTypeSpinner = view.findViewById(R.id.crimeTypeSpinner)
        dateRangeSpinner = view.findViewById(R.id.dateRangeSpinner)
        timePeriodSpinner = view.findViewById(R.id.timePeriodSpinner)
        applyFilterButton = view.findViewById(R.id.btnApplyFilter)

        applyFilterButton.setOnClickListener {
            applyFiltersAndDrawHeatmap()
        }

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

        viewAllZones = view.findViewById(R.id.btnViewAllZones)



        hotspots = view.findViewById(R.id.tVHotspots)
        getHotspotCount(hotspots , hp_zone1name , hp_zone1cases , hp_zone2name , hp_zone2cases , viewAllZones)

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
    private fun mapdialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_map, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialogView.findViewById<Button>(R.id.closeBtn)
        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyFiltersAndDrawHeatmap() {
        val selectedCrime = crimeTypeSpinner.selectedItem.toString()
        val selectedDateRange = dateRangeSpinner.selectedItem.toString()
        val selectedTimePeriod = timePeriodSpinner.selectedItem.toString()

        val db = FirebaseFirestore.getInstance()
        db.collection("FIR_Records").get().addOnSuccessListener { result ->
            val zoneFIRMap = mutableMapOf<String, Int>()
            val zoneLatLngMap = mutableMapOf<String, LatLng>()

            for (doc in result) {
                val crimeType = doc.getString("crime_type")?.trim() ?: continue
                val zone = doc.getString("zone")?.trim() ?: continue
                val timestampStr = doc.getString("timestamp")?.trim() ?: continue

                //  Parse nested location map
                val locationMap = doc.get("location") as? Map<*, *> ?: continue
                val lat = (locationMap["lat"] as? Number)?.toDouble() ?: continue
                val lng = (locationMap["lng"] as? Number)?.toDouble() ?: continue

                // Parse date from timestamp string
                val firTime = parseStringToDate(timestampStr)
                if (firTime == null) {
                    Log.d("FILTER", "Invalid date format for: $timestampStr")
                    continue
                }
                val firTimestamp = firTime.time  // firTime is your parsed Date object
                val now = System.currentTimeMillis()
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
                if (!matchDate || !matchTime) continue

                //Apply Filters
                if (selectedCrime != "All") {
                    val allowedCrimeTypes = CrimeTypesData.crimeTypeMap[selectedCrime]
                    if (allowedCrimeTypes == null) {
                    } else if (!allowedCrimeTypes.contains(crimeType)) {
                        continue
                    } else {
                    }
                } else {

                }

                //Count and map
                zoneFIRMap[zone] = (zoneFIRMap[zone] ?: 0) + 1
                if (!zoneLatLngMap.containsKey(zone)) {
                    zoneLatLngMap[zone] = LatLng(lat, lng)
                }
            }
            // Proceed if FIRs found
            if (zoneFIRMap.isEmpty()) {
                Toast.makeText(requireContext(), "No FIRs found for selected filters", Toast.LENGTH_SHORT).show()
                mGoogleMap?.clear()
            } else {
                drawHeatMap(zoneFIRMap, zoneLatLngMap)
            }
        }

    }

    private fun parseStringToDate(timestampStr: String): Date? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            formatter.parse(timestampStr)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    private fun drawHeatMap(
        zoneFIRMap: Map<String, Int>,
        zoneLatLngMap: Map<String, LatLng>
    ) {
        val maxCount = zoneFIRMap.values.maxOrNull() ?: 1
        val weightedPoints = mutableListOf<WeightedLatLng>()

        for ((zone, count) in zoneFIRMap) {
            val latLng = zoneLatLngMap[zone] ?: continue
            val normalizedIntensity = count.toDouble() / maxCount
            weightedPoints.add(WeightedLatLng(latLng, normalizedIntensity))
        }

        if (weightedPoints.isEmpty()) {
            Toast.makeText(requireContext(), "No data matched your filters.", Toast.LENGTH_SHORT).show()
            mGoogleMap?.clear()
            return
        }

        val heatmapProvider = HeatmapTileProvider.Builder()
            .weightedData(weightedPoints)
            .radius(50)
            .opacity(0.7)
            .build()

        mGoogleMap?.clear()
        mGoogleMap?.addTileOverlay(TileOverlayOptions().tileProvider(heatmapProvider))
    }





    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDateInRange(selectedRange: String, firDate: Date): Boolean {
        val now = Calendar.getInstance().time
        val diffInMillis = now.time - firDate.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

        return when (selectedRange) {
            "All" -> true
            "Last 7 Days" -> diffInDays <= 7
            "Last 30 Days" -> diffInDays <= 30
            "This Year" -> {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val firYear = Calendar.getInstance().apply { time = firDate }.get(Calendar.YEAR)
                firYear == currentYear
            }
            else -> true
        }
    }




    private fun focusOnZone(zoneId: String) {
        val center = zoneCenterMap[zoneId] ?: return
        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 12f))
        popupMarker?.remove()
        popupMarker = mGoogleMap?.addMarker(
            MarkerOptions()
                .position(center)
                .title(zoneId)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        popupMarker?.showInfoWindow()
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
        hp_zone2cases: TextView,
        viewAllZones: MaterialButton,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = FirebaseFirestore.getInstance()

            db.collection("FIR_Records")
                .get()
                .addOnSuccessListener { documents ->
                    val zoneInfoMap = mutableMapOf<String, Pair<Int, String>>()

                    for (doc in documents) {
                        val zone = doc.getString("zone")?.trim() ?: continue
                        val location = LocationData(
                            lat = (doc.get("location") as? Map<String, Any>)?.get("lat") as? Double
                                ?: 0.0,
                            lng = (doc.get("location") as? Map<String, Any>)?.get("lng") as? Double
                                ?: 0.0,
                            area = (doc.get("location") as? Map<String, Any>)?.get("area") as? String
                                ?: ""
                        )
                        val currentCount = zoneInfoMap[zone]?.first ?: 0
                        zoneInfoMap[zone] = Pair(currentCount + 1, location.area)
                    }
                    val allzonedata = zoneInfoMap
                    val hotspotZones = zoneInfoMap.filter { it.value.first > 10 }.toMap()
                    hp_count.text = hotspotZones.size.toString()

                    //push allzone the data to Firbase under "zone data collection"
                    for ((zoneId, pair) in allzonedata) {
                        val (count, area) = pair

                        val matchingDoc = documents.find { it.getString("zone")?.trim() == zoneId }
                        val locationMap = matchingDoc?.get("location") as? Map<String, Any>
                        val lat = locationMap?.get("lat") as? Double ?: 0.0
                        val lng = locationMap?.get("lng") as? Double ?: 0.0

                        val zoneData = mapOf(
                            "zoneId" to zoneId,
                            "areaName" to area,
                            "firCount" to count,
                            "location" to mapOf(
                                "lat" to lat,
                                "lng" to lng
                            )
                        )

                        db.collection("zone_data")
                            .document(zoneId)
                            .set(zoneData)
                            .addOnSuccessListener {
                                Log.d("ZoneDataUpdate", "$zoneId updated with location")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ZonedataUpdate", "Failed to update $zoneId: ${e.message}")
                            }
                    }


                    // Push hotspot data to Firestore under "hotspot_data" collection
                    for ((zoneId, pair) in hotspotZones) {
                        val (count, area) = pair

                        // Find the location for this zone from the FIR data
                        val matchingDoc = documents.find { it.getString("zone")?.trim() == zoneId }
                        val locationMap = matchingDoc?.get("location") as? Map<String, Any>
                        val lat = locationMap?.get("lat") as? Double ?: 0.0
                        val lng = locationMap?.get("lng") as? Double ?: 0.0

                        // Hotspot data with coordinates
                        val hotspotData = mapOf(
                            "zoneId" to zoneId,
                            "areaName" to area,
                            "firCount" to count,
                            "location" to mapOf(
                                "lat" to lat,
                                "lng" to lng
                            )
                        )

                        db.collection("hotspot_data")
                            .document(zoneId) // zoneId as document ID
                            .set(hotspotData) // overwrite existing data
                            .addOnSuccessListener {
                                Log.d("HotspotUpdate", "Zone $zoneId updated with location")
                            }
                            .addOnFailureListener { e ->
                                Log.e("HotspotUpdate", "Failed to update $zoneId: ${e.message}")
                            }
                    }


                    val sortedHotspotList =
                        hotspotZones.entries.sortedByDescending { it.value.first }



                    sortedHotspotList.getOrNull(0)?.let { (zoneId, pair) ->
                        val (count, area) = pair
                        hp_zone1name.text = "$zoneId - $area"
                        hp_zone1cases.text = "${count.toString()} cases"
                        topHotspotZone1 = zoneId
                    }

                    sortedHotspotList.getOrNull(1)?.let { (zoneId, pair) ->
                        val (count, area) = pair
                        hp_zone2name.text = "$zoneId - $area"
                        hp_zone2cases.text = "${count.toString()} cases"
                        topHotspotZone2 = zoneId
                    }

                    val hp_card1 = view?.findViewById<MaterialCardView>(R.id.hp_card1)
                    val hp_card2 = view?.findViewById<MaterialCardView>(R.id.hp_card2)

                    hp_card1?.setOnClickListener {
                        topHotspotZone1?.let { zoneId ->
                            focusOnZone(zoneId)
                        }
                    }
                    hp_card2?.setOnClickListener {
                        topHotspotZone2?.let { zoneId ->
                            focusOnZone(zoneId)
                        }
                    }

                    viewAllZones.setOnClickListener {
                        val zoneList = hotspotZones.map { (zoneId, pair) ->
                            val (count, area) = pair
                            ZoneData_hp(zoneId, area, count.toString())
                        }
                        showAllZonesDailog(requireContext(), zoneList)
                    }

                }
                .addOnFailureListener {
                    hp_count.text = "Failed to fetch FIRs"
                }
        }
    }

    private fun showAllZonesDailog(context: Context, zoneStats: List<ZoneData_hp>) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dilaog_all_zones)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.zonesRecyclerView)
        val closeBtn = dialog.findViewById<Button>(R.id.closeDialogBtn)
        val exportBtn = dialog.findViewById<ImageView>(R.id.exportZone)


        val sortedZoneData = zoneStats.sortedByDescending {
            it.count.toIntOrNull() ?: 0
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = HotspotZoneAdapter(sortedZoneData)

        closeBtn.setOnClickListener { dialog.dismiss() }
        exportBtn.setOnClickListener {
            exportZoneListToPDF(sortedZoneData , context)
        }
        dialog.show()
    }

    private fun exportZoneListToPDF(zoneList: List<ZoneData_hp>, context: Context) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 12f

        var y = 20
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Trinetra AI - Zone FIR Report", 10f, y.toFloat(), paint)

        y += 20
        paint.typeface = Typeface.DEFAULT
        for ((i, zone) in zoneList.withIndex()) {
            y += 20
            val text = "${zone.zoneId} (${zone.area}) - ${zone.count} cases"
            canvas.drawText(text, 10f, y.toFloat(), paint)
        }

        pdfDocument.finishPage(page)

        // Save to Downloads
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HotspotZone_FIR_Report.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
            showDownloadNotification(context, file)
            saveNotificationToFirestore(
                title = "Hotspot Zone Report Downloaded",
                message = "Your PDF report was successfully downloaded.",
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

    private fun saveNotificationToFirestore(title: String, message: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val notification = hashMapOf(
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )

        db.collection("UserNotifications")
            .document(userId)
            .collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                // Optional: Log or toast
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save notification: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showDownloadNotification(context: Context, file: File) {
        val channelId = "trinetra_pdf_download"
        val channelName = "PDF Downloads"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification for PDF download"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Zone FIR Report Downloaded")
            .setContentText("Saved to Downloads as ${file.name}")
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
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

    // === FIR UPLOADER === this to be removed after full app get completed
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadFIRDataOnce() {

        viewLifecycleOwner.lifecycleScope.launch {
            val prefs =
                requireContext().getSharedPreferences("fir_upload_status", Context.MODE_PRIVATE)
            val alreadyUploaded = prefs.getBoolean("is_uploaded", false)

            if (alreadyUploaded) {
                Log.d("FIRUploader", "Already uploaded FIR data. Skipping.")
                return@launch
            }

            val json =
                requireContext().assets.open("fir_data.json").bufferedReader().use { it.readText() }
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
                            if (isAdded) {
                                Toast.makeText(
                                    requireContext(),
                                    "✅ All FIRs uploaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {

                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FIRUploader", "❌ Failed to upload FIR: ${fir.fir_id}", e)
                    }
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

//        try {
//            val styled = mGoogleMap?.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
//            )
//            if (styled == false) Log.e("MapStyle", "Style parsing failed")
//        } catch (e: Resources.NotFoundException) {
//            Log.e("MapStyle", "Can't find style. Error: ", e)
//        }

        googleMap.uiSettings.isZoomControlsEnabled = true


        val drawnPolygons = mutableListOf<Polygon>()
        toggle = view?.findViewById(R.id.markerToggle)!!
        toggle?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                drawnPolygons.clear()
                drawnPolygons.addAll(drawZoneBoxesOnMap(googleMap, false))

                googleMap?.setOnPolygonClickListener { polygon ->
                    val zoneId = polygonZoneMap[polygon] ?: "Unknown Zone"
                    val center = getPolygonCenterPoint(polygon.points)

                    popupMarker?.remove()

                    popupMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(center)
                            .title(zoneId)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )

                    popupMarker?.showInfoWindow()
                }
                
            } else {
                for (polygon in drawnPolygons) {
                    polygon.remove()
                }
                drawnPolygons.clear()
            }
        }
    }

    private fun getPolygonCenterPoint(polygonPoints: List<LatLng>): LatLng {
        var lat = 0.0
        var lng = 0.0
        for (point in polygonPoints) {
            lat += point.latitude
            lng += point.longitude
        }
        val size = polygonPoints.size
        return LatLng(lat / size, lng / size)
    }

    private val polygonZoneMap = mutableMapOf<Polygon, String>() // Global map
    private var popupMarker: Marker? = null

    private fun drawZoneBoxesOnMap(googleMap: GoogleMap?, showMarkers: Boolean): List<Polygon> {
        val boxSize = 0.011
        val polygons = mutableListOf<Polygon>()
        zoneMarkers.clear()

        delhiZones.forEach { zone ->
            val lat = zone.lat
            val lng = zone.lng
            val box = listOf(
                LatLng(lat, lng),
                LatLng(lat, lng + boxSize),
                LatLng(lat - boxSize, lng + boxSize),
                LatLng(lat - boxSize, lng)
            )

            val polygon = googleMap?.addPolygon(
                PolygonOptions()
                    .addAll(box)
                    .strokeColor(Color.GRAY)
                    .fillColor(0x3300FF00)
                    .strokeWidth(2f)
                    .clickable(true)
            )

            polygon?.let { polygons.add(it)
                polygonZoneMap[it] = "Zone ${zone.id}"
            }

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

        return polygons
    }

}
