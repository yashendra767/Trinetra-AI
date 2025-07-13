package com.example.trinetraai.drawer_activities.AllFIRsAdapter



import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.example.trinetraai.firdataclass.FIR
import java.text.SimpleDateFormat
import java.util.Locale

class AllFIRsAdapter(
    private var firList: MutableList<FIR>,
    private val context: Context,

) : RecyclerView.Adapter<AllFIRsAdapter.FIRViewHolder>() {





    inner class FIRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firId: TextView = itemView.findViewById(R.id.FIRidTV)
        val crimeType: TextView = itemView.findViewById(R.id.crimeTypeTV)
        val timestamp: TextView = itemView.findViewById(R.id.timestampTV)
        val areaZone: TextView = itemView.findViewById(R.id.areaZoneTV)
        val firStatus : TextView = itemView.findViewById(R.id.firStatusTV)
        val reportingStation : TextView = itemView.findViewById(R.id.reportingStationTV)
        val viewDetails: TextView = itemView.findViewById(R.id.viewDetailsTV)

        val editicon : ImageView = itemView.findViewById(R.id.editicon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FIRViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fir_card, parent, false)
        return FIRViewHolder(view)
    }

    override fun onBindViewHolder(holder: FIRViewHolder, position: Int) {
        val fir = firList[position]

        holder.firId.text = "${fir.fir_id}"
        holder.crimeType.text = "${fir.crime_type}"
        holder.timestamp.text = "${formatTimestampReadable(fir.timestamp)}"
        holder.areaZone.text = "${fir.location.area} - ${fir.zone}"
        holder.firStatus.text = "${fir.status}"
        holder.reportingStation.text = "${fir.reporting_station}"

        holder.editicon.setOnClickListener { view ->
            val popupMenu = android.widget.PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.edit_fir_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                if (isAdminUser()) {
                    when (item.itemId) {
                        R.id.edit_fir_option -> {
                            val bottomsheet = bottomsheet_modify_fir(fir)
                            bottomsheet.show((context as AppCompatActivity).supportFragmentManager, "EditFIRBottomSheet")
                            true
                        }
                        R.id.delete_fir_option -> {
                            showDeleteConfirmation(fir, context)
                            true
                        }
                        else -> false
                    }
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Only admins can modify FIRs",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            }
            popupMenu.show()
        }
        holder.viewDetails.setOnClickListener {
            showDetailsDialog(fir)
        }
    }

    override fun getItemCount(): Int = firList.size

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

    private fun showDeleteConfirmation(fir: FIR , context : Context) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete FIR ID: ${fir.fir_id}?")
            .setPositiveButton("Delete") { dialog, _ ->
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                firestore.collection("FIR_Records")
                    .document(fir.fir_id)
                    .delete()
                    .addOnSuccessListener {
                        android.widget.Toast.makeText(context, "Deleted FIR ${fir.fir_id}", android.widget.Toast.LENGTH_SHORT).show()

                        // Update RecyclerView
                        val position = firList.indexOfFirst { it.fir_id == fir.fir_id }
                        if (position != -1) {
                            firList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                    }
                    .addOnFailureListener { e ->
                        android.widget.Toast.makeText(context, "Failed to delete FIR: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDetailsDialog(fir: FIR) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fir_details, null)

        dialogView.findViewById<TextView>(R.id.detailFirId).text = "FIR ID: ${fir.fir_id}"
        dialogView.findViewById<TextView>(R.id.detailCrimeType).text = "Crime Type: ${fir.crime_type}"
        dialogView.findViewById<TextView>(R.id.detailTimestamp).text = "Timestamp: ${formatTimestampReadable(fir.timestamp)}"
        dialogView.findViewById<TextView>(R.id.detailLocation).text =
            "Location: ${fir.location.area} (Lat: ${fir.location.lat}, Lng: ${fir.location.lng})"
        dialogView.findViewById<TextView>(R.id.detailZone).text = "Zone: ${fir.zone}"
        dialogView.findViewById<TextView>(R.id.detailStatus).text = "Status: ${fir.status}"
        dialogView.findViewById<TextView>(R.id.detailActCategory).text = "Act Category: ${fir.act_category}"
        dialogView.findViewById<TextView>(R.id.detailReportingStation).text = "Reporting Station: ${fir.reporting_station}"
        dialogView.findViewById<TextView>(R.id.detailIPCSections).text =
            "IPC Sections: ${fir.ipc_sections.joinToString(", ")}"

        val alertDialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.closeDialogBtn).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

}

    private fun isAdminUser(): Boolean {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val adminUID = "ZMPRnLmH0ce5oeNQkwVWcCPgaCY2" //aryan180906@gmail.com is admin
        return currentUserId == adminUID
    }








