package com.example.trinetraai.drawer_activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.example.trinetraai.drawer_activities.AllZoneWork.ZoneData
import com.example.trinetraai.drawer_activities.AllZoneWork.ZoneDataAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AllZones : AppCompatActivity() {

    private lateinit var sortSpinner: Spinner
    private var currentSortOption = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var zoneAdapter: ZoneDataAdapter
    private val zoneList = mutableListOf<ZoneData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_zones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        recyclerView = findViewById(R.id.displayFIRs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        zoneAdapter = ZoneDataAdapter(zoneList)
        recyclerView.adapter = zoneAdapter

        sortSpinner = findViewById(R.id.sortSpinner)

        val adapter = ArrayAdapter.createFromResource(this, R.array.sort_options, android.R.layout.simple_spinner_item)
            .apply {
                setDropDownViewResource(R.layout.spinner_item_white)

            }
        sortSpinner.adapter =adapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentSortOption = position

                sortAndDisplayData()
            }


            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val exportIcon = findViewById<ImageView>(R.id.exportZoneList)
        exportIcon.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_DayNight_Dialog_Alert)


            dialogBuilder
                .setTitle("Download Zone Report")
                .setMessage("Do you want to download the current Zone FIR list as PDF?")
                .setPositiveButton("Download") { dialog, _ ->
                    val sortedZoneList = when (currentSortOption) {
                        0 -> zoneList.sortedBy { it.zoneId.removePrefix("Zone ").toIntOrNull() ?: 0 }
                        1 -> zoneList.sortedByDescending { it.firCount }
                        else -> zoneList
                    }
                    exportZoneListToPDF(sortedZoneList, this)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }


        fetchZoneData()

    }

    @SuppressLint("ServiceCast")
    private fun showDownloadNotification(context: Context, file: File) {
        val channelId = "zone_pdf_download_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Zone PDF Downloads",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for completed zone FIR PDF downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Download complete")
            .setContentText("Zone FIR Report saved to Downloads")
            .setSmallIcon(R.drawable.logo) // Replace with valid small icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }



    private fun exportZoneListToPDF(zoneList: List<ZoneData>, context: Context) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 1800, 1).create()
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
            val text = "${zone.zoneId} (${zone.areaName}) - ${zone.firCount} FIRs"
            canvas.drawText(text, 10f, y.toFloat(), paint)
        }

        pdfDocument.finishPage(page)

        // Save to Downloads
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "AllZone_Data.pdf"
        )
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
            showDownloadNotification(context, file)
            saveNotificationToFirestore(
                title = "Zone Report Downloaded",
                message = "Your PDF report was successfully downloaded.",
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

    private fun fetchZoneData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("zone_data")
            .get()
            .addOnSuccessListener { result ->
                zoneList.clear()
                for (document in result) {
                    val zone = document.toObject(ZoneData::class.java)
                    zoneList.add(zone)
                }
                sortAndDisplayData()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save notification: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun sortAndDisplayData() {
        when (currentSortOption) {
            0 -> zoneList.sortBy { it.zoneId.removePrefix("Zone ").toIntOrNull() ?: 0 } // Zone ID
            1 -> zoneList.sortByDescending { it.firCount } // FIR count
        }
        zoneAdapter.notifyDataSetChanged()
    }


}