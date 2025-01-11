package com.example.kotlinactivities.userPage.navBar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.R
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style

class MapFragment : Fragment() {

    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private var loadingAnimation: LottieAnimationView? = null // Reference to the LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize the MapView and LottieAnimationView
        mapView = rootView.findViewById(R.id.mapView)
        loadingAnimation = rootView.findViewById(R.id.loadingAnimation)

        mapView?.onCreate(savedInstanceState)

        // Start the Lottie animation when loading starts
        loadingAnimation?.visibility = View.VISIBLE
        loadingAnimation?.playAnimation()

        mapView?.getMapAsync { loadedMapLibreMap ->
            mapLibreMap = loadedMapLibreMap

            // Set the map style
            mapLibreMap?.setStyle(
                Style.Builder().fromUri("https://api.maptiler.com/maps/basic/style.json?key=sShjeVn7hIilAZkYXgYD")
            ) { style ->
                Log.d("MapFragment", "Map style loaded successfully")

                // Delay hiding the Lottie animation by 2 seconds after the map style is loaded
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingAnimation?.visibility = View.GONE
                    loadingAnimation?.cancelAnimation()
                }, 2000) // 2000ms = 2 seconds

                // Set the initial camera position
                mapLibreMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(10.61923, 124.30813), // Latitude and longitude
                        12.0 // Zoom level
                    )
                )
            }
        }

        return rootView
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
