package com.example.trinetraai.bottom_fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trinetraai.R
import com.example.trinetraai.firdataclass.LocationData
import com.example.trinetraai.presetData.DateRangeData
import com.example.trinetraai.presetData.TimePeriodData
import com.example.trinetraai.presetData.WomenCrime
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.component1
import kotlin.collections.component2

class WomenSafety : Fragment() {

    private lateinit var activeFIRwomen : TextView
    private lateinit var highRiskZone : TextView

    private lateinit var zoneName_1 : TextView
    private lateinit var zoneName_2 : TextView
    private lateinit var zone1_cases : TextView
    private lateinit var zone2_cases : TextView

    private lateinit var grid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_women_safety, container, false)

        activeFIRwomen = view.findViewById(R.id.tVActiveFirWomen)
        getFIRCountWomen(activeFIRwomen)

        highRiskZone = view.findViewById(R.id.tVHighRiskZoneWomen)
        gethighRiskZone(highRiskZone)

        grid = view.findViewById(R.id.crimeIntensityGrid)
        
        fetchWomenFIRPerZone { firMap ->
            populateZoneHeatmap(grid,firMap)
        }
        return view
    }

    private fun populateZoneHeatmap(grid: GridLayout, firMap: Map<String, Int>) {
        grid.removeAllViews()
        val context = grid.context
        val inflater = LayoutInflater.from(context)
        val totalZones = firMap.size

        val maxFir = firMap.values.maxOrNull() ?: 1

        val columns = 4
        val rows = (totalZones  + columns - 1) / columns

        grid.columnCount = columns
        grid.rowCount = rows


        for ((zoneId, firCount) in firMap.toSortedMap()) {
            val view = inflater.inflate(R.layout.item_heatmap_cell, grid, false)
            val firText = view.findViewById<TextView>(R.id.firCount)
            val zoneText = view.findViewById<TextView>(R.id.zoneLabel)

            firText.text = firCount.toString()
            zoneText.text = zoneId

            val background = ContextCompat.getDrawable(context, R.drawable.cell_outline)?.mutate()
            background?.setTint(getHeatColor(firCount, maxFir , context))
            view.background = background

            grid.addView(view)
        }
    }

    private fun getHeatColor(count: Int, max: Int, context: Context): Int {
        val ratio = count.toFloat() / max
        return when {
            ratio > 0.8 -> Color.parseColor("#FF0000") // Deep red
            ratio > 0.6 -> Color.parseColor("#FF4D4D")
            ratio > 0.4 -> Color.parseColor("#FF9999")
            ratio > 0.2 -> Color.parseColor("#FFDADA")
            else -> Color.parseColor("#FFE5E5") // White or low risk
        }
    }

    fun fetchWomenFIRPerZone(onResult: (Map<String, Int>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("WomenFIR")
            .get()
            .addOnSuccessListener { documents ->
                val zoneCountMap = mutableMapOf<String, Int>()

                for (doc in documents) {
                    val zone = doc.getString("zone")?.trim() ?: continue
                    zoneCountMap[zone] = zoneCountMap.getOrDefault(zone, 0) + 1
                }

                onResult(zoneCountMap)
            }
            .addOnFailureListener {
                onResult(emptyMap())
            }
    }

    private fun gethighRiskZone(highRiskZone: TextView) {
        val db = FirebaseFirestore.getInstance()
        db.collection("WomenFIR")
            .get()
            .addOnSuccessListener { document ->
                val zoneMap = mutableMapOf<String,Pair<Int,String>>() // zone -> (count ,area)

                for (doc in document){
                    val zone = doc.getString("zone")?.trim()?: continue
                    val location = LocationData(
                        lat = (doc.get("location") as? Map<String, Any>)?.get("lat") as? Double
                            ?: 0.0,
                        lng = (doc.get("location") as? Map<String, Any>)?.get("lng") as? Double
                            ?: 0.0,
                        area = (doc.get("location") as? Map<String, Any>)?.get("area") as? String
                            ?: ""
                    )

                    val currentCount = zoneMap[zone]?.first ?: 0
                    zoneMap[zone] = Pair(currentCount + 1,location.area)


                }
                val allwomenCrimeZone = zoneMap
                val riskZone = zoneMap.filter{it.value.first > 5}
                highRiskZone.text = riskZone.size.toString()

                // push allwomen crime data to firebase
                for ((zoneId, pair) in allwomenCrimeZone) {
                    val (count, area) = pair

                    val matchingDoc = document.find { it.getString("zone")?.trim() == zoneId }
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

                    db.collection("allWomenCrimeZone")
                        .document(zoneId)
                        .set(zoneData)
                        .addOnSuccessListener {}
                        .addOnSuccessListener {}
                }

                //push data of risk zone
                for ((zoneId, pair) in riskZone) {
                    val (count, area) = pair

                    val matchingDoc = document.find { it.getString("zone")?.trim() == zoneId }
                    val locationMap = matchingDoc?.get("location") as? Map<String, Any>
                    val lat = locationMap?.get("lat") as? Double ?: 0.0
                    val lng = locationMap?.get("lng") as? Double ?: 0.0

                    val zonedata = mapOf(
                        "zoneId" to zoneId,
                        "areaName" to area,
                        "firCount" to count,
                        "location" to mapOf(
                            "lat" to lat,
                            "lng" to lng
                        )
                    )
                    db.collection("WomenRiskZone")
                        .document(zoneId)
                        .set(zonedata)
                        .addOnSuccessListener {  }
                        .addOnFailureListener {  }
                }

                val sortedRiskList = riskZone.entries.sortedByDescending { it.value.first }

                val rootedview = view ?: return@addOnSuccessListener
                sortedRiskList.getOrNull(0)?.let{(zoneId, pair) ->
                    val (count, area) = pair
                    zoneName_1 = rootedview.findViewById(R.id.zoneName_1)
                    zone1_cases = rootedview.findViewById(R.id.zone1_cases)
                    zoneName_1.text = "${zoneId} - ${area}"
                    zone1_cases.text = "${count} FIRs"
                }
                sortedRiskList.getOrNull(1)?.let{(zoneId, pair) ->
                    val (count, area) = pair
                    zoneName_2 = rootedview.findViewById(R.id.zoneName_2)
                    zone2_cases = rootedview.findViewById(R.id.zone2_cases)
                    zoneName_2.text = "${zoneId} - ${area}"
                    zone2_cases.text = "${count} FIRs"
                }


            }

    }

    private fun getFIRCountWomen(activeFIRwomen: TextView) {

        val db = FirebaseFirestore.getInstance()
        val womenCrimes = WomenCrime.womenCrimeList
        val womenFIRCollection = db.collection("WomenFIR")

        db.collection("FIR_Records")
            .get()
            .addOnSuccessListener { document ->
                var count = 0
                for (doc in document){
                    val status = doc.getString("status")
                    val crimeType = doc.getString("crime_type")

                    val isActive = status.equals("Open",ignoreCase = true) || status.equals("Under Investigation" , ignoreCase = true)
                    val isWomenCrime = crimeType != null && womenCrimes.contains(crimeType)

                    if (isActive && isWomenCrime){
                        count++

                        val dataToCopy = doc.data.toMutableMap()
                        dataToCopy["originalFIRId"] = doc.id
                        womenFIRCollection.document(doc.id).set(dataToCopy)
                            .addOnSuccessListener {
                                Log.d("WomenFIR", "FIR copied to WomenFIR: ${doc.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("WomenFIR", "Error copying FIR: ${e.message}")
                                e.printStackTrace()
                            }
                    }
                }
                activeFIRwomen.text = count.toString()
            }
            .addOnFailureListener {  exception ->
                exception.printStackTrace()
                Toast.makeText(requireContext(), "Error fetching FIRs", Toast.LENGTH_SHORT).show()
            }
    }

}