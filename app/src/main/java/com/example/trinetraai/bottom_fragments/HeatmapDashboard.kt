package com.example.trinetraai.bottom_fragments

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import com.example.trinetraai.R
import com.example.trinetraai.bottom_fragments.expandMap.FullScreenMap
import com.example.trinetraai.presetData.CrimeTypesData
import com.example.trinetraai.presetData.DateRangeData
import com.example.trinetraai.presetData.TimePeriodData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.button.MaterialButton

class HeatmapDashboard : Fragment() , OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private lateinit var mapOptionButton : ImageButton
    private lateinit var expandMap : ImageView



    private lateinit var crimeTypeSpinner : Spinner
    private lateinit var dateRangeSpinner : Spinner
    private lateinit var timePeriodSpinner : Spinner
    private lateinit var applyFilterButton : MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_heatmap_dashboard, container, false)

        crimeTypeSpinner = view.findViewById(R.id.crimeTypeSpinner)
        dateRangeSpinner = view.findViewById(R.id.dateRangeSpinner)
        timePeriodSpinner = view.findViewById(R.id.timePeriodSpinner)
        applyFilterButton = view.findViewById(R.id.btnApplyFilter)

        val crimeList = CrimeTypesData.crimeTypeMap
        val dateRangeList = DateRangeData.dateRanges
        val timePeriodList = TimePeriodData.timePeriods

        setCrimeTypeSpinner(crimeList , crimeTypeSpinner)
        setDateSpinner(dateRangeList , dateRangeSpinner)
        setTimePeriodSpinner(timePeriodList , timePeriodSpinner)


        //Map wala kaam
        // Load map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)




        expandMap = view.findViewById(R.id.ExpandMapHotspot)
        expandMap.setOnClickListener {
            val currentPosition = mGoogleMap?.cameraPosition

            val dialog = FullScreenMap()

            val bundle = Bundle()
            bundle.putDouble("lat", currentPosition?.target?.latitude ?: 28.6139)
            bundle.putDouble("lng", currentPosition?.target?.longitude ?: 77.2090)
            bundle.putFloat("zoom", currentPosition?.zoom ?: 13f)
            dialog.arguments = bundle

            dialog.show(parentFragmentManager, "map_fullscreen")
        }






        return view
    }



    private fun setTimePeriodSpinner(
        timeList: kotlin.collections.List<kotlin.String>,
        spinner: android.widget.Spinner
    ) {
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeList)
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        spinner.adapter = adapter
    }

    private fun setDateSpinner(
        dateList: kotlin.collections.List<kotlin.String>,
        spinner: android.widget.Spinner
    ) {
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateList)
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        spinner.adapter = adapter
    }

    private fun setCrimeTypeSpinner(
        crimeList: kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.String>>,
        spinner: android.widget.Spinner
    ) {
        val crimeTypes = listOf("All Crimes")+crimeList.keys.sorted()
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, crimeTypes)
        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        spinner.adapter = adapter
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        val delhi = LatLng(28.6139, 77.2090)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 10f))

        try {
            val styled = mGoogleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (styled == false) Log.e("MapStyle", "Style parsing failed")
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }

        // Optional: Add a marker
        googleMap.addMarker(
            MarkerOptions().position(delhi).title("Default Location: Delhi")
        )

        // Enable UI controls if needed
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

}