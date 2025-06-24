package com.example.trinetraai.bottom_fragments.AllZonesAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.example.trinetraai.zoneDataClass.ZoneData_hp

class HotspotZoneAdapter(private val zoneData: List<ZoneData_hp>) :
    RecyclerView.Adapter<HotspotZoneAdapter.ZoneViewHolder>() {
        class ZoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val zonename : TextView = itemView.findViewById(R.id.hp_areazone)
            val zonecases : TextView = itemView.findViewById(R.id.hp_cases)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_zone_card, parent, false)
        return ZoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        val stat = zoneData[position]
        holder.zonename.text = "${stat.zoneId} - ${stat.area}"
        holder.zonecases.text = "${stat.count} cases"

        // color logic

        val count = stat.count.toIntOrNull() ?: 0
        val color = when {
            count > 15 -> R.color.highSeverity
            count > 10 -> R.color.medium
            else -> R.color.lowMedium
        }
        holder.zonecases.setTextColor(holder.itemView.context.getColor(color))
    }

    override fun getItemCount() = zoneData.size
}