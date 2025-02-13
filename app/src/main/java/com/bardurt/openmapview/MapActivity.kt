package com.bardurt.openmapview

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bardurt.omvlib.map.core.GeoPosition
import com.bardurt.omvlib.map.core.MapProvider
import com.bardurt.omvlib.map.core.OmvMap
import com.bardurt.omvlib.map.core.OmvMapView


class MapActivity : AppCompatActivity() {

    companion object {
        const val ANIMATION_DURATION: Int = 250
        val OVERSHOOT_INTERPOLATOR: OvershootInterpolator = OvershootInterpolator()
    }

    private lateinit var map: OmvMapView
    private lateinit var markerImage: View

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocationGranted =
                result[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val coarseLocationGranted =
                result[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted || coarseLocationGranted) {
                map.getMap().setMyLocationEnabled(true)
            } else {
                Toast.makeText(this, "Permission to location denied", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_map)

        markerImage = findViewById(R.id.marker_image_view)
        map = findViewById(R.id.map_view)
        setUpMapView()

    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpMapView() {
        map.getMap().getMapAsync(object : OmvMap.OnMapReadyCallback {
            override fun onMapReady() {
                map.getMap().moveCamera(GeoPosition(-12.080235951074854, -77.04036706431548), 13.0)
                map.getMap().setOnCameraMoveStartedListener(
                    object : OmvMap.OnCameraMoveStartedListener {
                        override fun onCameraMoveStarted() {
                            markerImage.animate()
                                .translationY(-75f)
                                .setInterpolator(OVERSHOOT_INTERPOLATOR)
                                .setDuration(ANIMATION_DURATION.toLong())
                                .start()
                        }

                    }
                )

                map.getMap().setOnCameraIdleListener(object : OmvMap.OnCameraIdleListener {
                    override fun onCameraIdle() {
                        markerImage.animate()
                            .translationY(0f)
                            .setInterpolator(OVERSHOOT_INTERPOLATOR)
                            .setDuration(ANIMATION_DURATION.toLong())
                            .start()

                    }
                })

                if (checkLocationPermission()) {
                    map.getMap().setMyLocationEnabled(true)
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        })


    }
}