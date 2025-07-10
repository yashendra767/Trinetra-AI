package com.example.trinetraai.drawer_activities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.core.graphics.toColorInt
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.google.android.material.snackbar.Snackbar
import pl.droidsonroids.gif.GifImageView

class Notifications : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var clearAllButton: MaterialButton
    private lateinit var adapter: NotificationAdapter

    private lateinit var noDataGif: GifImageView
    private val notifications = mutableListOf<HashMap<String, Any>>()
    private val docIdMap = mutableListOf<String>()

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        recyclerView = findViewById(R.id.notificationRecyclerView)
        clearAllButton = findViewById(R.id.clearAllButton)

        adapter = NotificationAdapter(notifications)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSwipeGestures()

        noDataGif = findViewById<GifImageView>(R.id.noDataGif)



        clearAllButton.setOnClickListener {
            clearAllNotifications()
        }

        loadNotifications()
    }

    private fun setupSwipeGestures() {
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val docId = docIdMap[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {

                        val deletedItem = notifications[position]
                        val deletedDocId = docIdMap[position]

                        notifications.removeAt(position)
                        docIdMap.removeAt(position)
                        adapter.notifyItemRemoved(position)

                        Snackbar.make(recyclerView, "Notification deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                notifications.add(position, deletedItem)
                                docIdMap.add(position, deletedDocId)
                                adapter.notifyItemInserted(position)
                            }
                            .setActionTextColor(ContextCompat.getColor(this@Notifications, R.color.accentGold))
                            .setBackgroundTint(ContextCompat.getColor(this@Notifications, R.color.surface))
                            .setTextColor(ContextCompat.getColor(this@Notifications, R.color.textPrimary))
                            .addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    if (event != DISMISS_EVENT_ACTION) {
                                        db.collection("UserNotifications")
                                            .document(userId)
                                            .collection("notifications").document(deletedDocId)
                                            .delete()
                                    }
                                }
                            })
                            .show()

                    }

                    ItemTouchHelper.RIGHT -> {
                        db.collection("UserNotifications")
                            .document(userId)
                            .collection("notifications")
                            .document(docId)
                            .update("isRead", true)

                        notifications[position]["isRead"] = true

                        viewHolder.itemView.animate()
                            .alpha(0.5f)
                            .setDuration(300)
                            .start()

                        adapter.notifyItemChanged(position)
                    }
                }
            }


            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconSize = (24 * resources.displayMetrics.density).toInt()
                val iconMargin = (itemView.height - iconSize) / 2

                val paint = Paint()
                val icon: Drawable?

                val swipeDistance = Math.min(Math.abs(dX), itemView.width.toFloat())
                val swipeProgress = swipeDistance / itemView.width
                val alpha = (100 + (155 * swipeProgress)).toInt()

                if (dX > 0) {
                    paint.color = Color.argb(alpha, 56, 142, 60)
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), itemView.left + dX, itemView.bottom.toFloat(), paint)

                    icon = ContextCompat.getDrawable(this@Notifications, R.drawable.check)
                    icon?.setBounds(
                        itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + iconSize,
                        itemView.bottom - iconMargin
                    )
                    icon?.draw(c)

                } else if (dX < 0) {
                    paint.color = Color.argb(alpha, 211, 47, 47)
                    c.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)

                    icon = ContextCompat.getDrawable(this@Notifications, R.drawable.trash)
                    icon?.setBounds(
                        itemView.right - iconMargin - iconSize,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin
                    )
                    icon?.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun loadNotifications() {
        db.collection("UserNotifications")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {
                    Toast.makeText(this, "Network Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    noDataGif.visibility = View.VISIBLE

                    return@addSnapshotListener
                }

                notifications.clear()
                docIdMap.clear()

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val map = HashMap<String, Any>()
                        map["title"] = doc.getString("title") ?: ""
                        map["message"] = doc.getString("message") ?: ""
                        map["isRead"] = doc.getBoolean("isRead") ?: false

                        notifications.add(map)
                        docIdMap.add(doc.id)
                    }

                    noDataGif.visibility = View.GONE


                } else {

                    noDataGif.visibility = View.VISIBLE

                }

                adapter.notifyDataSetChanged()
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

                    noDataGif.visibility = View.VISIBLE
                    Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
