package com.example.trinetraai.drawer_activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R

class NotificationAdapter(
    private val notifications: MutableList<HashMap<String, Any>>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.titleText)
        val messageText: TextView = view.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        holder.titleText.text = item["title"] as String
        holder.messageText.text = item["message"] as String
        val isRead = item["isRead"] as Boolean
        holder.itemView.alpha = if (isRead) 0.5f else 1.0f
    }

    override fun getItemCount(): Int = notifications.size
}
