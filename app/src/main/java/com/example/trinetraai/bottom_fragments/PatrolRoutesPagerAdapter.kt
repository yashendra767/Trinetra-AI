package com.example.trinetraai.bottom_fragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.trinetraai.bottom_fragments.patrolFragment.AIPredictionPatrol
import com.example.trinetraai.bottom_fragments.patrolFragment.HistoryBasedPatrol

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
