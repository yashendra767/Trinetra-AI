package com.example.trinetraai.bottom_fragments.TrendAnalyser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.trinetraai.R
import com.example.trinetraai.api.ApiClient
import com.example.trinetraai.api.RequestData
import com.example.trinetraai.api.ResponseData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class predictionresult(val type: String, val zone: String)

class TrendAnalyser : Fragment() {

    private lateinit var chart: BarChart
    private var gifProgressDialog: AlertDialog? = null
    private lateinit var dialogProgressText: TextView
    private var cancelRequested = false

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

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun showGifProgressDialog() {
        cancelRequested = false
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)

        dialogProgressText = dialogView.findViewById(R.id.dailogprogressText)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            cancelRequested = true
            dismissGifProgressDialog()
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        gifProgressDialog = builder.create()
        gifProgressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        gifProgressDialog?.show()
    }

    private fun updateGifProgressText(progress: Int) {
        dialogProgressText.text = "$progress% done"
    }

    private fun dismissGifProgressDialog() {
        gifProgressDialog?.dismiss()
        gifProgressDialog = null
    }

    private fun simulatePredictionFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val predictionList = mutableListOf<predictionresult>()

        db.collection("FIR_Records")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "No FIR records found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val firList = documents.mapNotNull { doc ->
                    val timestamp = doc.getString("timestamp") ?: getCurrentTimestamp()
                    val actCategory = doc.getString("act_category") ?: "IPC"
                    val status = doc.getString("status") ?: "Pending"
                    val reportingStation = doc.getString("reporting_station") ?: "Unknown"
                    val ipcSections = (doc.get("ipc_sections") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val location = doc.get("location") as? Map<*, *>
                    val lat = location?.get("lat") as? Double ?: 0.0
                    val lng = location?.get("lng") as? Double ?: 0.0
                    val area = location?.get("area") as? String ?: "Unknown"

                    RequestData(
                        timestamp = timestamp,
                        lat = lat,
                        lng = lng,
                        ipc_sections = ipcSections,
                        area = area,
                        act_category = actCategory,
                        status = status,
                        reporting_station = reportingStation
                    )
                }

                val total = firList.size
                showGifProgressDialog()

                lifecycleScope.launch {
                    var completed = 0
                    for (request in firList) {
                        if (cancelRequested) break

                        val result = predictAndGet(request)
                        if (result != null) {
                            predictionList.add(result)
                        }
                        completed++
                        val percentage = (completed * 100) / total
                        withContext(Dispatchers.Main) {
                            updateGifProgressText(percentage)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        dismissGifProgressDialog()
                        if (!cancelRequested) {
                            updateChartFromPredictionList(predictionList)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch FIR data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun predictAndGet(requestData: RequestData): predictionresult? {
        return withContext(Dispatchers.IO) {
            try {
                val response: ResponseData = ApiClient.service.predict(requestData)
                predictionresult(response.type, response.zone)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun updateChartFromPredictionList(predictions: List<predictionresult>) {
        zoneCrimeMap.clear()

        for (prediction in predictions) {
            val zone = prediction.zone
            val type = prediction.type

            val crimeMap = zoneCrimeMap.getOrPut(zone) { mutableMapOf() }
            crimeMap[type] = crimeMap.getOrDefault(type, 0) + 1
        }

        updateChart(zoneCrimeMap, "Predicted Crimes")
    }

    private fun updateChart(data: Map<String, Map<String, Int>>, type: String) {
        val allCrimeTypes = data.values.flatMap { it.keys }.toSet().toList().sorted()
        val zoneList = data.keys.sorted()

        val entries = zoneList.mapIndexed { index, zone ->
            val crimeCounts = allCrimeTypes.map { crime ->
                data[zone]?.get(crime)?.toFloat() ?: 0f
            }.toFloatArray()

            BarEntry(index.toFloat(), crimeCounts)
        }

        val dataSet = BarDataSet(entries, type)
        val colors = listOf(
            "#67B7A4", "#F5B041", "#E74C3C", "#A569BD",
            "#2980B9", "#D35400", "#16A085", "#7D3C98"
        )
        dataSet.setColors((0 until allCrimeTypes.size).map {
            android.graphics.Color.parseColor(colors[it % colors.size])
        })
        dataSet.stackLabels = allCrimeTypes.toTypedArray()

        val barData = BarData(dataSet)
        barData.barWidth = 0.3f

        chart.data = barData
        chart.setFitBars(true)
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(zoneList)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelCount = zoneList.size
        xAxis.setDrawGridLines(false)

        chart.axisLeft.axisMinimum = 0f
        chart.invalidate()
    }
}
