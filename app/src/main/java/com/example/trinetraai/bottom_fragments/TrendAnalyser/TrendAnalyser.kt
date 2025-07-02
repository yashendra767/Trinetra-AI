package com.example.trinetraai.bottom_fragments.TrendAnalyser

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.trinetraai.R
import com.example.trinetraai.api.ApiClient
import com.example.trinetraai.api.RequestData
import com.example.trinetraai.api.ResponseData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrendAnalyser : Fragment(), PredictionCallback {


    private lateinit var chart: BarChart

    private var zoneCrimeMap = mutableMapOf<String, MutableMap<String, Int>>()


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trend_analyser, container, false)

        chart = view.findViewById(R.id.trendChart)

        simulatePredictionFromFirestore()




        return view
    }

    private fun simulatePredictionFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("FIR_Records")
            .limit(1) // pick the first for now (can use random later if needed)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.first()

                    val timestamp = doc.getString("timestamp") ?: getCurrentTimestamp()
                    val actCategory = doc.getString("act_category") ?: "IPC"
                    val status = doc.getString("status") ?: "Pending"
                    val reportingStation = doc.getString("reporting_station") ?: "Unknown"

                    val ipcSections = (doc.get("ipc_sections") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                    val location = doc.get("location") as? Map<*, *>
                    val lat = location?.get("lat") as? Double ?: 0.0
                    val lng = location?.get("lng") as? Double ?: 0.0
                    val area = location?.get("area") as? String ?: "Unknown"

                    val requestData = RequestData(
                        timestamp = timestamp,
                        lat = lat,
                        lng = lng,
                        ipc_sections = ipcSections,
                        area = area,
                        act_category = actCategory,
                        status = status,
                        reporting_station = reportingStation
                    )

                    callPredictionApi(requestData)
                } else {
                    Toast.makeText(requireContext(), "No FIR records found", Toast.LENGTH_SHORT).show()
                }

                fetchPredictionAndPlot()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch FIR data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun callPredictionApi(requestData: RequestData) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response: ResponseData = ApiClient.service.predict(requestData)

                // Save result to Firestore
                val predictionData = hashMapOf(
                    "zone" to response.zone,
                    "type" to response.type,
                    )

                FirebaseFirestore.getInstance()
                    .collection("Prediction")
                    .add(predictionData)
                    .addOnSuccessListener {
                        println("Prediction saved to Firestore")
                    }
                    .addOnFailureListener {
                        println("Failed to save prediction: ${it.message}")
                    }

                withContext(Dispatchers.Main) {
                    onPredictionResult(response.zone, response.type)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Prediction failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onPredictionResult(zone: String, type: String) {
//        val crimeMap = zoneCrimeMap.getOrPut(zone) { mutableMapOf() }
//        crimeMap[type] = crimeMap.getOrDefault(type, 0) + 1
//        updateChart(zoneCrimeMap , type)
  }



    private fun fetchPredictionAndPlot() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Prediction")
            .get()
            .addOnSuccessListener { result ->
                zoneCrimeMap.clear()

                for (document in result) {
                    val zone = document.getString("zone") ?: continue
                    val type = document.getString("type") ?: continue

                    val crimeMap = zoneCrimeMap.getOrPut(zone) { mutableMapOf() }
                    crimeMap[type] = crimeMap.getOrDefault(type, 0) + 1
                }

                // Update chart after all documents are processed
                updateChart(zoneCrimeMap, "Predicted Crimes")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching predictions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun updateChart(data: Map<String, Map<String, Int>>, type: String) {
        val chart = view?.findViewById<com.github.mikephil.charting.charts.BarChart>(R.id.trendChart) ?: return

        val allCrimeTypes = data.values.flatMap { it.keys }.toSet().toList().sorted()
        val zoneList = data.keys.sorted() // zone names for X-axis

        val entries = zoneList.mapIndexed { index, zone ->
            val crimeCounts = allCrimeTypes.map { crime ->
                data[zone]?.get(crime)?.toFloat() ?: 0f
            }.toFloatArray()

            BarEntry(index.toFloat(), crimeCounts) // one bar per zone
        }

        val dataSet = BarDataSet(entries, type)
        val colors = listOf(
            "#67B7A4", "#F5B041", "#E74C3C", "#A569BD",
            "#2980B9", "#D35400", "#16A085", "#7D3C98"
        )
        dataSet.setColors((0 until allCrimeTypes.size).map { Color.parseColor(colors[it % colors.size]) })
        dataSet.stackLabels = allCrimeTypes.toTypedArray()

        val barData = BarData(dataSet)
        barData.barWidth = 0.3f
        chart.data = barData

        chart.setFitBars(true)
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(zoneList) // ✅ ZONE NAMES HERE
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelCount = zoneList.size
        xAxis.setDrawGridLines(false)

        chart.axisLeft.axisMinimum = 0f
        chart.invalidate()
    }



}


