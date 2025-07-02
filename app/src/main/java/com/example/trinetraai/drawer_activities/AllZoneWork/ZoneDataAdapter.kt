package com.example.trinetraai.drawer_activities.AllZoneWork

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R

class ZoneDataAdapter(private val zoneList: List<ZoneData>) :
    RecyclerView.Adapter<ZoneDataAdapter.ZoneViewHolder>() {

    inner class ZoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val zoneId: TextView = itemView.findViewById(R.id.zoneIdText)
        val areaName: TextView = itemView.findViewById(R.id.areaNameText)
        val firCount: TextView = itemView.findViewById(R.id.firCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.zone_card, parent, false)
        return ZoneViewHolder(view)
    }



    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        val zone = zoneList[position]
        holder.zoneId.text = zone.zoneId
        holder.areaName.text = zone.areaName
        holder.firCount.text = "FIRs: ${zone.firCount}"
    }

    override fun getItemCount(): Int = zoneList.size
}
