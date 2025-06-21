package com.example.trinetraai.bottom_fragments.expandMap

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.trinetraai.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class FullScreenMap : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fullscreen_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the SupportMapFragment from the layout
        val mapFragment = childFragmentManager.findFragmentById(R.id.fullscreenMap) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            // Get camera data passed from HeatmapDashboard
            val lat = arguments?.getDouble("lat", 28.6139) ?: 28.6139
            val lng = arguments?.getDouble("lng", 77.2090) ?: 77.2090
            val zoom = arguments?.getFloat("zoom", 13f) ?: 13f
            val delhi = LatLng(lat, lng)

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, zoom))

            // Apply custom style
            try {
                val styled = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
                )
                if (!styled) Log.e("MapStyle", "Style parsing failed")
            } catch (e: Resources.NotFoundException) {
                Log.e("MapStyle", "Map style resource not found", e)
            }

            // Optional marker
            googleMap.addMarker(
                MarkerOptions().position(delhi).title("Default Location: Delhi")
            )

            googleMap.uiSettings.isZoomControlsEnabled = true
        }

        // Close button logic
        view.findViewById<ImageView>(R.id.closeButton)?.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}
