package com.example.trinetraai.bottom_fragments.expandMap

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import com.example.trinetraai.R
import com.example.trinetraai.presetData.ZoneData.delhiZones
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

class FullScreenMap : DialogFragment(), OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null

    private lateinit var zoneFIRMap: Map<String, Int>
    private lateinit var zoneLatLngMap: Map<String, LatLng>
    private var lat = 28.6139
    private var lng = 77.2090
    private var zoom = 13f

    private val zoneMarkers = mutableListOf<Marker>()
    private val zoneCenterMap = mutableMapOf<String, LatLng>()

    private var popupMarker : Marker? = null
    private val polygonZoneMap = mutableMapOf<Polygon, String>()


    private lateinit var toggle : SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)

        val gson = Gson()
        val zoneFIRJson = arguments?.getString("zoneFIRMap")
        val zoneLatLngJson = arguments?.getString("zoneLatLngMap")


        zoneFIRMap = gson.fromJson(zoneFIRJson, object : TypeToken<Map<String, Int>>() {}.type)
        zoneLatLngMap = gson.fromJson(zoneLatLngJson, object : TypeToken<Map<String, LatLng>>() {}.type)

        lat = arguments?.getDouble("lat", 28.6139) ?: 28.6139
        lng = arguments?.getDouble("lng", 77.2090) ?: 77.2090
        zoom = arguments?.getFloat("zoom", 13f) ?: 13f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fullscreen_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.fullscreenMap) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.fullscreen_map_container, it).commitNow()
            }

        mapFragment.getMapAsync(this)

        val closeButton = view.findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        val center = LatLng(lat, lng)
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom))
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true

        drawHeatMap(zoneFIRMap, zoneLatLngMap)




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

                    popupMarker = googleMap.addMarker(
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
                    .fillColor(0x330000FF)
                    .strokeWidth(2.5f)
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

    private fun drawHeatMap(
        zoneFIRMap: Map<String, Int>,
        zoneLatLngMap: Map<String, LatLng>
    ) {
        val maxCount = zoneFIRMap.values.maxOrNull() ?: 1
        val weightedPoints = mutableListOf<WeightedLatLng>()

        for ((zone, count) in zoneFIRMap) {
            val latLng = zoneLatLngMap[zone] ?: continue
            val normalizedIntensity = count.toDouble() / maxCount
            weightedPoints.add(WeightedLatLng(latLng, normalizedIntensity))
        }

        if (weightedPoints.isEmpty()) {
            Toast.makeText(requireContext(), "No data matched your filters.", Toast.LENGTH_SHORT).show()
            mGoogleMap?.clear()
            return
        }

        val heatmapProvider = HeatmapTileProvider.Builder()
            .weightedData(weightedPoints)
            .radius(50)
            .opacity(0.7)
            .build()

        mGoogleMap?.clear()
        mGoogleMap?.addTileOverlay(TileOverlayOptions().tileProvider(heatmapProvider))
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}
