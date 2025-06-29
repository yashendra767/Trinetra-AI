package com.example.trinetraai.drawer_activities.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trinetraai.R
import com.google.android.material.imageview.ShapeableImageView

class TeamPagerAdapter(private val members: List<TeamMember>) :
    RecyclerView.Adapter<TeamPagerAdapter.TeamViewHolder>() {

    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.team_name)
        val role: TextView = itemView.findViewById(R.id.team_role)
        val contribution: TextView = itemView.findViewById(R.id.team_contribution)
        val image: ShapeableImageView = itemView.findViewById(R.id.image_profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_card, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val member = members[position]
        holder.name.text = member.name
        holder.role.text = member.role
        holder.contribution.text = member.contribution
        holder.image.setImageResource(member.imageResId)
    }

    override fun getItemCount(): Int = members.size
}