package com.example.trinetraai.bottom_fragments.expandMap


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView

import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.trinetraai.HotspotZone
import com.example.trinetraai.R

import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import org.json.JSONObject


class expandH_Map : DialogFragment(), OnMapReadyCallback {


    private lateinit var selectedZones: List<HotspotZone>
    private var googleMap: GoogleMap? = null
    private val apiKey = "AIzaSyCtR6Ly2xen0veKWOsMa5__pcSkj_JOHeQ"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expand_h__map, container, false)
        return view
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(28.6139, 77.2090), 11f))
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        drawFullPatrolRoute(selectedZones)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.fullHscreenMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val closeBtn = view.findViewById<ImageView>(R.id.closeHButton)
        closeBtn.setOnClickListener {
            dismiss()
        }

        val json = arguments?.getString("routeZonesJson")
        if (json.isNullOrEmpty()) {
            dismiss()
            return
        }
        selectedZones = Gson().fromJson(json, object : TypeToken<List<HotspotZone>>() {}.type)
    }

    private fun drawFullPatrolRoute(zones: List<HotspotZone>) {
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

                    googleMap?.addPolyline(
                        PolylineOptions()
                            .addAll(pathPoints)
                            .color(Color.BLUE)
                            .width(6f)

                    )

                } catch (e: Exception) {
                    Log.e("ROUTE_ERROR", e.message.toString())
                }
            }, {
                Log.e("ROUTE_FAIL", "Failed for ${origin.areaName} → ${destination.areaName}")
            })

            queue.add(request)
        }
    }

    private fun buildDirectionUrl(originLat: Double, originLng: Double, destLat: Double, destLng: Double): String {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$originLat,$originLng&destination=$destLat,$destLng&mode=driving&key=$apiKey"
    }

}
