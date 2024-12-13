package com.example.kotlinactivites.NavBar

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import androidx.fragment.app.Fragment
import com.example.kotlinactivites.R
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style

class MapFragment : Fragment() {

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)

        // Configure the MapView
        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.setStyle(Style.Builder().fromUri("https://api.maptiler.com/maps/basic/style.json?key=sShjeVn7hIilAZkYXgYD")) {
                Log.d("MapFragment", "Map style loaded successfully")

                // Set the initial camera position
                mapLibreMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(10.61923, 124.30813), // Latitude and longitude
                        12.0 // Zoom level
                    )
                )
            }
        }


        return view
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }
}
