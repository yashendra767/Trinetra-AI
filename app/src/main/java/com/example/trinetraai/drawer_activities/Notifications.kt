package com.example.trinetraai.drawer_activities

import android.Manifest
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.example.trinetraai.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*

class Notifications : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var clearAllButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val loadedIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        container = findViewById(R.id.notificationContainer)
        clearAllButton = findViewById(R.id.clearAllButton)

        clearAllButton.setOnClickListener {
            clearAllNotifications()
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        db.collection("UserNotifications")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                container.removeAllViews()

                for (doc in snapshot.documents) {
                    val id = doc.id
                    val title = doc.getString("title") ?: ""
                    val message = doc.getString("message") ?: ""
                    val isRead = doc.getBoolean("isRead") ?: false


                    if (!loadedIds.contains(id)) {

                        loadedIds.add(id)
                    }
                    val itemView = layoutInflater.inflate(R.layout.item_notification, null)

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 0, 0, 24) // Ensures vertical spacing
                    itemView.layoutParams = layoutParams


                    val titleText = itemView.findViewById<TextView>(R.id.titleText)
                    val messageText = itemView.findViewById<TextView>(R.id.messageText)
                    val deleteBtn = itemView.findViewById<Button>(R.id.deleteButton)

                    titleText.text = title
                    messageText.text = message

                    itemView.alpha = if (isRead) 0.5f else 1.0f

                    itemView.setOnClickListener {
                        if (!isRead) {
                            db.collection("UserNotifications")
                                .document(userId)
                                .collection("notifications")
                                .document(id)
                                .update("isRead", true)
                        }
                    }

                    deleteBtn.setOnClickListener {
                        db.collection("UserNotifications")
                            .document(userId)
                            .collection("notifications")
                            .document(id)
                            .delete()
                    }

                    container.addView(itemView)
                }
            }
    }

    private fun clearAllNotifications() {
        db.collection("UserNotifications")
            .document(userId)
            .collection("notifications")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /*    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun playNotificationSound() {
        // Optional vibration
    *//*    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(150)
        }*//*

        // Notification sound
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
        ringtone.play()
    }
*/
}
