package com.example.kotlinactivites

import android.app.Application
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize MapLibre without an API key
        MapLibre.getInstance(
            this,
            "sShjeVn7hIilAZkYXgYD", // API key is null for public tiles
            WellKnownTileServer.MapLibre
        )
    }
}
