package com.example.trinetraai.drawer_fragments.AllFIRsAdapter



import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.example.trinetraai.firdataclass.FIR

class AllFIRsAdapter(
    private var firList: List<FIR>,
    private val context: Context
) : RecyclerView.Adapter<AllFIRsAdapter.FIRViewHolder>() {

    inner class FIRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firId: TextView = itemView.findViewById(R.id.FIRidTV)
        val crimeType: TextView = itemView.findViewById(R.id.crimeTypeTV)
        val timestamp: TextView = itemView.findViewById(R.id.timestampTV)
        val areaZone: TextView = itemView.findViewById(R.id.areaZoneTV)
        val firStatus : TextView = itemView.findViewById(R.id.firStatusTV)
        val reportingStation : TextView = itemView.findViewById(R.id.reportingStationTV)
        val viewDetails: TextView = itemView.findViewById(R.id.viewDetailsTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FIRViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fir_card, parent, false)
        return FIRViewHolder(view)
    }

    override fun onBindViewHolder(holder: FIRViewHolder, position: Int) {
        val fir = firList[position]

        holder.firId.text = "${fir.fir_id}"
        holder.crimeType.text = "${fir.crime_type}"
        holder.timestamp.text = "${fir.timestamp}"
        holder.areaZone.text = "${fir.location.area} - ${fir.zone}"
        holder.firStatus.text = "${fir.status}"
        holder.reportingStation.text = "${fir.reporting_station}"


//        holder.viewDetails.setOnClickListener {
//            showDetailsDialog(fir)
//        }
    }

    override fun getItemCount(): Int = firList.size

    fun updateList(newList: List<FIR>) {
        firList = newList
        notifyDataSetChanged()
    }

//    private fun showDetailsDialog(fir: FIR) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fir_details, null)
//
//        dialogView.findViewById<TextView>(R.id.detailFirId).text = "FIR ID: ${fir.fir_id}"
//        dialogView.findViewById<TextView>(R.id.detailCrimeType).text = "Crime Type: ${fir.crime_type}"
//        dialogView.findViewById<TextView>(R.id.detailTimestamp).text = "Timestamp: ${fir.timestamp}"
//        dialogView.findViewById<TextView>(R.id.detailLocation).text =
//            "Location: ${fir.location.area} (Lat: ${fir.location.lat}, Lng: ${fir.location.lng})"
//        dialogView.findViewById<TextView>(R.id.detailZone).text = "Zone: ${fir.zone}"
//        dialogView.findViewById<TextView>(R.id.detailStatus).text = "Status: ${fir.status}"
//        dialogView.findViewById<TextView>(R.id.detailActCategory).text = "Act Category: ${fir.act_category}"
//        dialogView.findViewById<TextView>(R.id.detailReportingStation).text = "Reporting Station: ${fir.reporting_station}"
//        dialogView.findViewById<TextView>(R.id.detailIPCSections).text =
//            "IPC Sections: ${fir.ipc_sections.joinToString(", ")}"
//
//        AlertDialog.Builder(context)
//            .setTitle("FIR Details")
//            .setView(dialogView)
//            .setPositiveButton("Close", null)
//            .show()
//    }
}
