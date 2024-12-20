package com.example.kotlinactivites.NavBar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import androidx.fragment.app.Fragment
import com.example.kotlinactivites.R
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style

class MapFragment : Fragment() {

    // Retain the MapView instance across Fragment recreation
    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mapView == null) {
            mapView = MapView(requireContext())
            mapView?.onCreate(savedInstanceState)

            // Configure the MapView only once
            mapView?.getMapAsync { loadedMapLibreMap ->
                mapLibreMap = loadedMapLibreMap
                mapLibreMap?.setStyle(
                    Style.Builder().fromUri("https://api.maptiler.com/maps/basic/style.json?key=sShjeVn7hIilAZkYXgYD")
                ) {
                    Log.d("MapFragment", "Map style loaded successfully")

                    // Set the initial camera position (only if it's the first load)
                    mapLibreMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(10.61923, 124.30813), // Latitude and longitude
                            12.0 // Zoom level
                        )
                    )
                }
            }
        }

        return mapView
    }

    // Lifecycle methods to delegate to MapView
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Do not destroy the MapView instance here to persist it
        (mapView?.parent as? ViewGroup)?.removeView(mapView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
}
