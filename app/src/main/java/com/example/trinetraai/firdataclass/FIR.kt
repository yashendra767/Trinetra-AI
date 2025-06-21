package com.example.trinetraai.firdataclass

import com.google.firebase.Timestamp


data class FIR(
    val fir_id: String = "",
    val crime_type: String = "",
    val ipc_sections: List<String> = listOf(),
    val act_category: String = "",
    val location: LocationData = LocationData(),
    val timestamp: String = "",
    val zone: String = "",
    val status: String = "",
    val reporting_station: String = ""
)
