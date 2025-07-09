package com.example.trinetraai.bottom_fragments.TrendAnalyser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class TrendAnalyser : Fragment() {

    data class predictionresult(val type: String, val zone: String)

    private lateinit var chart: BarChart
    private var gifProgressDialog: AlertDialog? = null
    private lateinit var dialogProgressText: TextView
    private var cancelRequested = false

    private var zoneCrimeMap = mutableMapOf<String, MutableMap<String, Int>>()
    private lateinit var btnAnalyseTrend: Button

    private lateinit var lastUpdate : TextView

    private lateinit var CT_PR1 : TextView
    private lateinit var ZN_PR1 : TextView
    private lateinit var CT_PR2 : TextView
    private lateinit var ZN_PR2 : TextView
    private lateinit var CT_PR3 : TextView
    private lateinit var ZN_PR3 : TextView


    private lateinit var zoneClassSpinner: Spinner

    private lateinit var btnShowLabels: MaterialButton

    private var legendColors: List<Int> = emptyList()
    private var allCrimeTypes: List<String> = emptyList()






    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trend_analyser, container, false)
        chart = view.findViewById(R.id.trendChart)

        btnAnalyseTrend = view.findViewById(R.id.btnAnalyseTrend)
        lastUpdate = view.findViewById(R.id.lastUpdate)


        loadLastUpdateTimestamp()

        btnAnalyseTrend.setOnClickListener {
            simulatePredictionFromFirestore()
            saveLastUpdateTimestamp()
            loadLastUpdateTimestamp()

        }

        zoneClassSpinner = view.findViewById(R.id.zoneClassSpinner)

        val zoneClassOptions = listOf("All", "Low Crime Activity", "Medium Crime Activity", "High Crime Activity")
        val adapter= ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            zoneClassOptions
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        zoneClassSpinner.adapter = adapter

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                loadPreviousPredictions()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        zoneClassSpinner.onItemSelectedListener = filterListener




        loadPreviousPredictions()

        CT_PR1 = view.findViewById(R.id.CT_PR1)
        ZN_PR1 = view.findViewById(R.id.ZN_PR1)
        CT_PR2 = view.findViewById(R.id.CT_PR2)
        ZN_PR2 = view.findViewById(R.id.ZN_PR2)
        CT_PR3 = view.findViewById(R.id.CT_PR3)
        ZN_PR3 = view.findViewById(R.id.ZN_PR3)


        loadLastThreePredictions()


        btnShowLabels = view.findViewById(R.id.btnShowLabels)
        btnShowLabels.setOnClickListener {
            showLegendDialog(allCrimeTypes, legendColors)
        }



        return view
    }


    private fun showLegendDialog(crimeTypes: List<String>, colorList: List<Int>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.legend_dialog, null)
        val legendContainer = dialogView.findViewById<LinearLayout>(R.id.legendContainer)

        for ((index, crime) in crimeTypes.withIndex()) {
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val colorBox = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                    setMargins(0, 0, 24, 0)
                }
                setBackgroundColor(colorList[index % colorList.size])
            }

            val label = TextView(requireContext()).apply {
                text = crime
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
            }

            itemLayout.addView(colorBox)
            itemLayout.addView(label)
            legendContainer.addView(itemLayout)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()


    }



    private fun loadLastThreePredictions() {
        val db = FirebaseFirestore.getInstance()
        db.collection("PredictionResult")
            .get()
            .addOnSuccessListener { result ->
                val sortedDocs = result.documents.sortedBy {
                    it.id.removePrefix("P").toIntOrNull() ?: 0
                }

                val lastThree = sortedDocs.takeLast(3).reversed()


                if (lastThree.size > 0) {
                    ZN_PR1.text = lastThree[0].getString("zone") ?: "N/A"
                    CT_PR1.text = lastThree[0].getString("type") ?: "N/A"
                }

                if (lastThree.size > 1) {
                    ZN_PR2.text = lastThree[1].getString("zone") ?: "N/A"
                    CT_PR2.text = lastThree[1].getString("type") ?: "N/A"
                }

                if (lastThree.size > 2) {
                    ZN_PR3.text = lastThree[2].getString("zone") ?: "N/A"
                    CT_PR3.text = lastThree[2].getString("type") ?: "N/A"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error fetching predictions: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }



    private fun loadLastUpdateTimestamp() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Metadata").document("LastPredictionTime")
            .get()
            .addOnSuccessListener { document ->
                val timestamp = document.getString("last_update")
                if (timestamp != null) {
                    lastUpdate.text = "Last Analysed\n$timestamp"
                }
            }
            .addOnFailureListener {
            }
    }


    private  fun saveLastUpdateTimestamp() {
        val db = FirebaseFirestore.getInstance()
        val timestamp = getCurrentTimestamp()

        val data = hashMapOf("last_update" to timestamp)

        try {
            db.collection("Metadata").document("LastPredictionTime")
                .set(data)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save update time: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadPreviousPredictions() {
        val db = FirebaseFirestore.getInstance()
        db.collection("PredictionResult")
            .get()
            .addOnSuccessListener { documents ->
                val predictions = documents.mapNotNull { doc ->
                    val zone = doc.getString("zone")
                    val type = doc.getString("type")
                    if (zone != null && type != null) {
                        predictionresult(type, zone)
                    } else null
                }

                if (predictions.isNotEmpty()) {
                    updateChartFromPredictionList(predictions)
                } else {
                    Toast.makeText(requireContext(), "No previous prediction results", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load previous predictions: ${it.message}", Toast.LENGTH_SHORT).show()
            }
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
                    Toast.makeText(requireContext(), "No FIR records found", Toast.LENGTH_SHORT)
                        .show()
                    return@addOnSuccessListener
                }

                val firList = documents.mapNotNull { doc ->
                    val timestamp = doc.getString("timestamp") ?: getCurrentTimestamp()
                    val actCategory = doc.getString("act_category") ?: "IPC"
                    val status = doc.getString("status") ?: "Pending"
                    val reportingStation = doc.getString("reporting_station") ?: "Unknown"
                    val ipcSections =
                        (doc.get("ipc_sections") as? List<*>)?.mapNotNull { it?.toString() }
                            ?: emptyList()
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
                    }
                    if (!cancelRequested) {
                        savePredictionsToFirestore(predictionList)
                        withContext(Dispatchers.Main) {
                            updateChartFromPredictionList(predictionList)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch FIR data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun savePredictionsToFirestore(predictions: List<predictionresult>) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("PredictionResult")

        try {

            withContext(Dispatchers.IO) {
                val existingDocs = collectionRef.get().await()
                for (doc in existingDocs.documents) {
                    doc.reference.delete().await()
                }
            }

            val batch = db.batch()
            predictions.forEachIndexed { index, prediction ->
                val docId = "P${index + 1}"
                val docRef = collectionRef.document(docId)

                val data = hashMapOf(
                    "zone" to prediction.zone,
                    "type" to prediction.type,
                )

                batch.set(docRef, data)
            }

            withContext(Dispatchers.IO) {
                batch.commit().await()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Failed to save predictions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

        val selectedZoneClass = zoneClassSpinner.selectedItem.toString()


        for (prediction in predictions) {
            val zone = prediction.zone
            val type = prediction.type
            val crimeMap = zoneCrimeMap.getOrPut(zone) { mutableMapOf() }
            crimeMap[type] = crimeMap.getOrDefault(type, 0) + 1
        }


        val filteredMap = zoneCrimeMap.filter { (_, crimes) ->
            val total = crimes.values.sum()
            when (selectedZoneClass) {
                "Low Crime Activity" -> total < 10
                "Medium Crime Activity" -> total in 10..<50
                "High Crime Activity" -> total >= 50
                else -> true
            }
        }

        updateChart(filteredMap, "Classified Zones")
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
        barData.barWidth = 0.25f
        barData.setValueTextSize(14f)

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
        xAxis.textSize = 15f
        xAxis.labelRotationAngle = -60f

        chart.axisLeft.textSize = 14f


        val legend = chart.legend
        legend.isEnabled = false
        legend.textSize = 16f
        legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
        legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.setWordWrapEnabled(true)
        legend.yOffset = 10f

        chart.invalidate()
        chart.setScaleEnabled(true)
        chart.setDragEnabled(true)
        chart.setPinchZoom(true)
        this.allCrimeTypes = allCrimeTypes
        this.legendColors = allCrimeTypes.mapIndexed { index, _ ->
            android.graphics.Color.parseColor(colors[index % colors.size])
        }

    }

}
