package com.example.trinetraai.bottom_fragments

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.trinetraai.R
import com.example.trinetraai.firdataclass.LocationData
import com.example.trinetraai.presetData.CrimeTypesData
import com.example.trinetraai.presetData.DateRangeData
import com.example.trinetraai.presetData.WomenCrime
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

    private lateinit var crimeSpinner: Spinner
    private lateinit var dateRangeSpinner: Spinner


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

        crimeSpinner = view.findViewById(R.id.crimeTypeSpinnerWomen)
        setCrimeTypeSpinner(CrimeTypesData.crimeTypeMap,crimeSpinner)

        dateRangeSpinner = view.findViewById(R.id.dateRangeSpinnerWomen)
        setDateSpinner(DateRangeData.dateRanges , dateRangeSpinner)





        return view
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

                sortedRiskList.getOrNull(0)?.let{(zoneId, pair) ->
                    val (count, area) = pair
                    zoneName_1 = view?.findViewById(R.id.zoneName_1)!!
                    zone1_cases = view?.findViewById(R.id.zone1_cases)!!
                    zoneName_1.text = "${zoneId} - ${area}"
                    zone1_cases.text = "${count} FIRs"
                }
                sortedRiskList.getOrNull(1)?.let{(zoneId, pair) ->
                    val (count, area) = pair
                    zoneName_2 = view?.findViewById(R.id.zoneName_2)!!
                    zone2_cases = view?.findViewById(R.id.zone2_cases)!!
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