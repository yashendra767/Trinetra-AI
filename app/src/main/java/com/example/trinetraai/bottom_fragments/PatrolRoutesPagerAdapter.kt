package com.example.trinetraai.bottom_fragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PatrolRoutesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryBasedPatrol()
            1 -> AIPredictionPatrol()
            else -> HistoryBasedPatrol()
        }
    }
}
