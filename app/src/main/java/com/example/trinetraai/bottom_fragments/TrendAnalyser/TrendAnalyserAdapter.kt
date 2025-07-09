package com.example.trinetraai.bottom_fragments.TrendAnalyser

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.example.trinetraai.api.ApiClient
import com.example.trinetraai.api.RequestData
import com.example.trinetraai.firdataclass.FIR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class TrendAnalyserAdapter(
    private var firList: List<FIR>,
    private val coroutineScope: CoroutineScope,
    private val predictionCallback: PredictionCallback,
    private val context: Context
) : RecyclerView.Adapter<TrendAnalyserAdapter.FIRViewHolder>() {

    inner class FIRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firId: TextView = itemView.findViewById(R.id.FIRidTV1)
        val crimeType: TextView = itemView.findViewById(R.id.crimeTypeTV1)
        val timestamp: TextView = itemView.findViewById(R.id.timestampTV1)
        val areaZone: TextView = itemView.findViewById(R.id.areaZoneTV1)
        val reportingStation: TextView = itemView.findViewById(R.id.reportingStationTV1)
        val viewDetails: TextView = itemView.findViewById(R.id.viewDetailsTV1)
        val predict:TextView = itemView.findViewById(R.id.firPredictTV1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FIRViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fir_number, parent, false)
        return FIRViewHolder(view)
    }

    override fun onBindViewHolder(holder: FIRViewHolder, position: Int) {
        val fir = firList[position]
        holder.firId.text = fir.fir_id
        holder.crimeType.text = fir.crime_type
        holder.timestamp.text = fir.timestamp
        holder.areaZone.text = "${fir.location.area} "
        holder.reportingStation.text = fir.reporting_station
        holder.predict.setOnClickListener {
            val requestData = RequestData(
                timestamp = fir.timestamp,
                lat = fir.location.lat,
                lng = fir.location.lng,
                ipc_sections = fir.ipc_sections,
                area = fir.location.area,
                act_category = fir.act_category,
                status = fir.status,
                reporting_station = fir.reporting_station
            )

            coroutineScope.launch {
                try {
                    val response = ApiClient.service.predict(requestData)
                    val Zone = response.zone
                    val Type=response.type
                    predictionCallback.onPredictionResult(Zone,Type)
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Prediction failed", e)
                    predictionCallback.onPredictionResult("Zone prediction Error."," APi Type Error")
                }
            }
        }

        holder.viewDetails.setOnClickListener {
            Toast.makeText(context, "Viewing FIR: ${fir.fir_id}", Toast.LENGTH_SHORT).show()

        }
    }

    override fun getItemCount(): Int = firList.size

    // Update adapter with new data
    fun updateData(newFIRs: List<FIR>) {
        firList = newFIRs
        notifyDataSetChanged()
    }

}
interface PredictionCallback {
    fun onPredictionResult(Zone: String,Type: String)
}



