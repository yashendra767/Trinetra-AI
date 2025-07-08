package com.example.trinetraai.bottom_fragments.patrolFragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.trinetraai.HotspotZone
import com.example.trinetraai.R
import com.example.trinetraai.presetData.ZoneData.delhiZones
import org.json.JSONObject

class HistoryBasedPatrol : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val apiKey = "AIzaSyCtR6Ly2xen0veKWOsMa5__pcSkj_JOHeQ"
    private var groupedZoneRoutes: List<List<HotspotZone>> = emptyList()
    private var selectedCardIndex: Int = 0



    private lateinit var toggle : SwitchCompat
    private val zoneMarkers = mutableListOf<Marker>()
    private val polygonZoneMap = mutableMapOf<Polygon, String>()
    private val zoneCenterMap = mutableMapOf<String, LatLng>()
    private var popupMarker: Marker? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_history_based_patrol, container, false)


        //Load Map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.isTiltGesturesEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        map.setMinZoomPreference(5f)
        map.setMaxZoomPreference(20f)

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(28.6139, 77.2090), 10f))

//        try {
//            val styled = map?.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
//            )
//            if (styled == false) Log.e("MapStyle", "Style parsing failed")
//        } catch (e: Resources.NotFoundException) {
//            Log.e("MapStyle", "Can't find style. Error: ", e)
//        }

       googleMap?.uiSettings?.isZoomControlsEnabled = true

        val drawnPolygons = mutableListOf<Polygon>()
        toggle = view?.findViewById(R.id.markerToggle)!!
        toggle?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                drawnPolygons.clear()
                drawnPolygons.addAll(drawZoneBoxesOnMap(googleMap, false))

                googleMap?.setOnPolygonClickListener { polygon ->
                    val zoneId = polygonZoneMap[polygon] ?: "Unknown Zone"
                    val center = getPolygonCenterPoint(polygon.points)

                    popupMarker?.remove()

                    popupMarker = googleMap?.addMarker(
                        MarkerOptions()
                            .position(center)
                            .title(zoneId)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )

                    popupMarker?.showInfoWindow()
                }

            } else {
                for (polygon in drawnPolygons) {
                    polygon.remove()
                }
                drawnPolygons.clear()
            }
        }

        fetchZonesFromFirestore { zones ->
            // use the list of HotspotZone objects
            for (zone in zones) {
                Log.d("Zone", "${zone.areaName} has ${zone.firCount} location ${zone.lng} ${zone.lat}FIRs")

            }
        }

        fetchZonesFromFirestore { zones ->
            val grouped = groupZones(zones)
            updateRouteListUI(grouped)
        }

    }

     private fun getPolygonCenterPoint(polygonPoints: List<LatLng>): LatLng {
        var lat = 0.0
        var lng = 0.0
        for (point in polygonPoints) {
            lat += point.latitude
            lng += point.longitude
        }
        val size = polygonPoints.size
        return LatLng(lat / size, lng / size)
    }

    private fun drawZoneBoxesOnMap(googleMap: GoogleMap?, showMarkers: Boolean): List<Polygon> {
        val boxSize = 0.011
        val polygons = mutableListOf<Polygon>()
        zoneMarkers.clear()

        delhiZones.forEach { zone ->
            val lat = zone.lat
            val lng = zone.lng
            val box = listOf(
                LatLng(lat, lng),
                LatLng(lat, lng + boxSize),
                LatLng(lat - boxSize, lng + boxSize),
                LatLng(lat - boxSize, lng)
            )

            val polygon = googleMap?.addPolygon(
                PolygonOptions()
                    .addAll(box)
                    .strokeColor(Color.GRAY)
                    .fillColor(0x3300FF00)
                    .strokeWidth(2f)
                    .clickable(true)
            )

            polygon?.let { polygons.add(it)
                polygonZoneMap[it] = "Zone ${zone.id}"
            }

            zoneCenterMap["Zone ${zone.id}"] = LatLng(lat - boxSize / 2, lng + boxSize / 2)

            if (showMarkers) {
                val marker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(zoneCenterMap["Zone ${zone.id}"]!!)
                        .title("Zone ${zone.id}")
                        .snippet(zone.name)
                )
                marker?.let { zoneMarkers.add(it) }
            }
        }

        googleMap?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        return polygons
    }

    private fun fetchZonesFromFirestore(onResult: (List<HotspotZone>) -> Unit) {
        Firebase.firestore.collection("hotspot_data")
            .get()
            .addOnSuccessListener { result ->
                val zones = result.mapNotNull { doc ->
                    try {
                        val location = doc.get("location") as? Map<*, *>
                        val lat = location?.get("lat") as? Double ?: 0.0
                        val lng = location?.get("lng") as? Double ?: 0.0

                        HotspotZone(
                            zoneId = doc.getString("zoneId") ?: "",
                            areaName = doc.getString("areaName") ?: "",
                            lat = lat,
                            lng = lng,
                            firCount = doc.getLong("firCount")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Parse error: ${e.message}")
                        null
                    }
                }
                onResult(zones)
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error fetching zones", it)
            }
    }



    private fun groupZones(zones: List<HotspotZone>): List<List<HotspotZone>> {
        val sorted = zones.sortedByDescending { it.firCount }
        val total = sorted.size
        val groupSize = 3
        val numGroups = total / groupSize + if (total % groupSize == 0) 0 else 1

        val groups = mutableListOf<List<HotspotZone>>()
        var startIndex = 0

        for (i in 0 until numGroups) {
            val remaining = total - startIndex
            val remainingGroups = numGroups - i
            val size = remaining / remainingGroups + if (remaining % remainingGroups != 0) 1 else 0

            val endIndex = (startIndex + size).coerceAtMost(total)
            groups.add(sorted.subList(startIndex, endIndex))
            startIndex = endIndex
        }

        return groups
    }


    private fun drawFullPatrolRoute(zones: List<HotspotZone>) {
        val map = googleMap ?: return
        val loopZones = zones + zones.first()
        val queue = Volley.newRequestQueue(requireContext())

        for (i in 0 until loopZones.size - 1) {
            val origin = loopZones[i]
            val destination = loopZones[i + 1]

            val url = buildDirectionUrl(origin.lat, origin.lng, destination.lat, destination.lng)
            val request = StringRequest(Request.Method.GET, url, { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") != "OK") return@StringRequest

                    val steps = json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONArray("legs")
                        .getJSONObject(0)
                        .getJSONArray("steps")

                    val pathPoints = mutableListOf<LatLng>()
                    for (j in 0 until steps.length()) {
                        val polyline = steps.getJSONObject(j)
                            .getJSONObject("polyline")
                            .getString("points")
                        pathPoints.addAll(PolyUtil.decode(polyline))
                    }

                    map.addPolyline(
                        PolylineOptions()
                            .addAll(pathPoints)
                            .color(Color.BLUE)
                            .width(5f)
                    )

                } catch (e: Exception) {
                    Log.e("ROUTE_ERROR", e.message.toString())
                }
            }, {
                Log.e("ROUTE_FAIL", "Failed for ${origin.areaName} → ${destination.areaName}")
            })

            queue.add(request)
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(zones[0].lat, zones[0].lng), 11f))
    }

    private fun buildDirectionUrl(
        originLat: Double, originLng: Double,
        destLat: Double, destLng: Double
    ): String {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$originLat,$originLng" +
                "&destination=$destLat,$destLng" +
                "&mode=driving" +
                "&key=$apiKey"
    }

    private fun updateRouteListUI(groups: List<List<HotspotZone>>) {
        val container = view?.findViewById<LinearLayout>(R.id.routeCardsContainer) ?: return
        container.removeAllViews()
        groupedZoneRoutes = groups

        groups.forEachIndexed { index, group ->
            val card = layoutInflater.inflate(R.layout.route_card_layout, container, false)
            val routeId = card.findViewById<TextView>(R.id.routeId)
            val routePath = card.findViewById<TextView>(R.id.routePath)
            val cardView = card as com.google.android.material.card.MaterialCardView

            routeId.text = "#PR${index + 1}"

            val fullRouteNames = group.map {"${it.areaName} (${it.zoneId})"}
            routePath.text = fullRouteNames.joinToString(" -> ")

            if (index == selectedCardIndex) {
                cardView.strokeColor = requireContext().getColor(R.color.textSecondary)
                cardView.strokeWidth = 1
            } else {
                cardView.strokeWidth = 0
            }
            card.setOnClickListener {
                selectedCardIndex = index
                googleMap?.clear()
                drawFullPatrolRoute(group)
                updateRouteListUI(groups)  
            }

            container.addView(card)
        }
    }



}
