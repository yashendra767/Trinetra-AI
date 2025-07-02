package com.example.trinetraai.bottom_fragments.TrendAnalyser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.example.trinetraai.firdataclass.FIR
import com.example.trinetraai.firdataclass.LocationData
import com.google.firebase.firestore.FirebaseFirestore

class TrendAnalyser : Fragment(), PredictionCallback {
    private lateinit var db: FirebaseFirestore
    private val allFIRs = mutableListOf<FIR>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrendAnalyserAdapter
    private lateinit var Zonetv: TextView
    private lateinit var CrimeTypetv: TextView

    private val filteredFIRs = mutableListOf<FIR>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trend_analyser, container, false)
//        db = FirebaseFirestore.getInstance()
//
//        Zonetv = view.findViewById(R.id.tvZone)
//        CrimeTypetv=view.findViewById(R.id.tvCrimeType)
//        recyclerView = view.findViewById(R.id.recyclerViewFIR)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//        adapter = TrendAnalyserAdapter(
//            firList = filteredFIRs,
//            coroutineScope = viewLifecycleOwner.lifecycleScope,
//            predictionCallback = this,
//            context = requireContext()
//        )
//
//        recyclerView.adapter = adapter
//        fetchAndFilterFIRs()
//        return view
//    }
//
//    private fun fetchAndFilterFIRs() {
//        db.collection("FIR_Records")
//            .get()
//            .addOnSuccessListener { documents ->
//                allFIRs.clear()
//                filteredFIRs.clear()
//                Toast.makeText(requireContext(), "FIR data loaded", Toast.LENGTH_SHORT).show()
//                for (doc in documents) {
//                    try {
//                        val fir = FIR(
//                            fir_id = doc.getString("fir_id") ?: "",
//                            crime_type = doc.getString("crime_type") ?: "",
//                            ipc_sections = doc.get("ipc_sections") as? List<String> ?: listOf(),
//                            act_category = doc.getString("act_category") ?: "",
//                            location = LocationData(
//                                lat = (doc.get("location") as? Map<String, Any>)?.get("lat") as? Double ?: 0.0,
//                                lng = (doc.get("location") as? Map<String, Any>)?.get("lng") as? Double ?: 0.0,
//                                area = (doc.get("location") as? Map<String, Any>)?.get("area") as? String ?: ""
//                            ),
//                            timestamp = doc.getString("timestamp") ?: "",
//                            zone = doc.getString("zone") ?: "",
//                            status = doc.getString("status") ?: "",
//                            reporting_station = doc.getString("reporting_station") ?: ""
//                        )
//                        allFIRs.add(fir)
//                        filteredFIRs.add(fir)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//                adapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener { it.printStackTrace() }
//    }
//
//
//
//    override fun onPredictionResult(Zone: String, Type: String) {
//       CrimeTypetv.text=Type
//        Zonetv.text =Zone
//
        return view
    }

    override fun onPredictionResult(Zone: String, Type: String) {

    }
}


