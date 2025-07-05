package com.example.trinetraai.bottom_fragments.patrolFragment

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.trinetraai.R
import com.example.trinetraai.api.ApiClient
import com.example.trinetraai.api.RequestData
import com.example.trinetraai.api.ResponseData

import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class predictionresult(val type: String, val zone: String)

class AIPredictionPatrol : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var gifProgressDialog: AlertDialog
    private lateinit var dialogProgressText: TextView
    private lateinit var routeCardsContainer: LinearLayout
    private var cancelRequested = false
    private val apiKey = "AIzaSyCtR6Ly2xen0veKWOsMa5__pcSkj_JOHeQ"
    private var selectedCardIndex = -1
    private var zoneLatLngMap = mutableMapOf<String, LatLng>()
    private var groupedZoneRoutes: List<List<String>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_a_i_prediction_patrol, container, false)

        mapView = view.findViewById(R.id.mapViewPreviewPatrolAI)
        routeCardsContainer = view.findViewById(R.id.routeCardsContainer)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        startPredictionProcess()

        view.findViewById<MaterialButton>(R.id.btnPredictRoutes).setOnClickListener {
            startPredictionProcess()
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(28.6139, 77.2090), 10f))
    }

    private fun startPredictionProcess() {
        showGifProgressDialog()

        Firebase.firestore.collection("FIR_Records").limit(200).get()
            .addOnSuccessListener { docs ->
                val firList = docs.mapNotNull { doc ->
                    val location = doc.get("location") as? Map<*, *> ?: return@mapNotNull null
                    val lat = location["lat"] as? Double ?: return@mapNotNull null
                    val lng = location["lng"] as? Double ?: return@mapNotNull null
                    val zone = location["area"] as? String ?: return@mapNotNull null

                    zoneLatLngMap[zone] = LatLng(lat, lng)

                    RequestData(
                        timestamp = doc.getString("timestamp") ?: "",
                        lat = lat,
                        lng = lng,
                        ipc_sections = (doc.get("ipc_sections") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList(),
                        area = zone,
                        act_category = doc.getString("act_category") ?: "IPC",
                        status = doc.getString("status") ?: "Pending",
                        reporting_station = doc.getString("reporting_station") ?: "Unknown"
                    )
                }

                val predictionList = mutableListOf<predictionresult>()
                val total = firList.size

                lifecycleScope.launch {
                    var done = 0
                    for (fir in firList) {
                        if (cancelRequested) break
                        val result = predictAndGet(fir)
                        result?.let {
                            predictionList.add(it)
                            zoneLatLngMap.putIfAbsent(it.zone, LatLng(fir.lat, fir.lng))
                        }
                        done++
                        updateGifProgressText((done * 100) / total)
                    }

                    dismissGifProgressDialog()
                    if (!cancelRequested) {
                        showPredictedRoutes(predictionList)
                    }
                }
            }
            .addOnFailureListener {
                dismissGifProgressDialog()
                Toast.makeText(requireContext(), "Failed to fetch FIRs", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun predictAndGet(request: RequestData): predictionresult? {
        return withContext(Dispatchers.IO) {
            try {
                val res: ResponseData = ApiClient.service.predict(request)
                predictionresult(res.type, res.zone)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun showPredictedRoutes(predictions: List<predictionresult>) {
        val uniqueZones = predictions.map { it.zone }.distinct().shuffled()


        val groupSize = 3
        groupedZoneRoutes = uniqueZones.chunked(groupSize).take(5)

        routeCardsContainer.removeAllViews()

        groupedZoneRoutes.forEachIndexed { index, group ->
            val card = layoutInflater.inflate(R.layout.route_card_layout, routeCardsContainer, false)
            val routeId = card.findViewById<TextView>(R.id.routeId)
            val routePath = card.findViewById<TextView>(R.id.routePath)

            routeId.text = "#PR${index + 1}"
            routePath.text = group.joinToString(" → ")

            card.setOnClickListener {
                selectedCardIndex = index
                drawFullRouteOnMap(group)
            }

            routeCardsContainer.addView(card)
        }

        if (groupedZoneRoutes.isEmpty()) {
            Toast.makeText(requireContext(), "Not enough zones to form routes", Toast.LENGTH_SHORT).show()
        }
    }


    private fun drawFullRouteOnMap(zones: List<String>) {
        val map = googleMap ?: return
        map.clear()
        val loopZones = zones + zones.first()

        val queue = Volley.newRequestQueue(requireContext())

        for (i in 0 until loopZones.size - 1) {
            val start = zoneLatLngMap[loopZones[i]] ?: continue
            val end = zoneLatLngMap[loopZones[i + 1]] ?: continue

            val url = buildDirectionUrl(start.latitude, start.longitude, end.latitude, end.longitude)

            val request = StringRequest(Request.Method.GET, url, { response ->
                try {
                    val json = JSONObject(response)
                    val routes = json.getJSONArray("routes")
                    if (routes.length() == 0) return@StringRequest

                    val points = mutableListOf<LatLng>()
                    val steps = routes.getJSONObject(0)
                        .getJSONArray("legs")
                        .getJSONObject(0)
                        .getJSONArray("steps")

                    for (j in 0 until steps.length()) {
                        val polyline = steps.getJSONObject(j)
                            .getJSONObject("polyline")
                            .getString("points")
                        points.addAll(PolyUtil.decode(polyline))
                    }

                    map.addPolyline(
                        PolylineOptions().addAll(points).color(Color.BLUE).width(6f)
                    )
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 11f))

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, {
                it.printStackTrace()
            })

            queue.add(request)
        }
    }

    private fun buildDirectionUrl(originLat: Double, originLng: Double, destLat: Double, destLng: Double): String {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$originLat,$originLng&destination=$destLat,$destLng" +
                "&mode=driving&key=$apiKey"
    }

    private fun showGifProgressDialog() {
        cancelRequested = false
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        dialogProgressText = dialogView.findViewById(R.id.dailogprogressText)
        dialogView.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            cancelRequested = true
            dismissGifProgressDialog()
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
        }

        gifProgressDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        gifProgressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        gifProgressDialog.show()
    }

    private fun updateGifProgressText(progress: Int) {
        dialogProgressText.text = "$progress% done"
    }

    private fun dismissGifProgressDialog() {
        gifProgressDialog.dismiss()
    }


    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { mapView.onLowMemory(); super.onLowMemory() }
}
