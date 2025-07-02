package com.example.trinetraai.drawer_activities.AllZoneWork

data class ZoneData(
    val zoneId: String = "",
    val areaName: String = "",
    val firCount: Int = 0,
    val location: Location = Location()
)

data class Location(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

