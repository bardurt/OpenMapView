package com.bardurt.openmapview

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.bardurt.omvlib.map.core.GeoPosition
import com.bardurt.omvlib.map.core.OmvMap
import com.bardurt.omvlib.map.core.OmvMapView
import com.bardurt.omvlib.map.core.OmvMarker
import java.util.Locale
import java.util.concurrent.Executors


class MapActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_LAT = 37.386969927816004
        private const val DEFAULT_LON = -121.8824158705871
        private const val DEFAULT_ZOOM = 13.0
        const val ANIMATION_DURATION: Int = 250
        val OVERSHOOT_INTERPOLATOR: OvershootInterpolator = OvershootInterpolator()
    }

    private lateinit var mapView: OmvMapView
    private lateinit var miniMapView: OmvMapView
    private lateinit var markerImage: View
    private lateinit var geocoder: Geocoder
    private lateinit var addressView: TextView
    private lateinit var buttonSnapshot: View
    private lateinit var buttonAddMarker: View
    private lateinit var buttonMiniMap: View
    private val handler = Handler(Looper.getMainLooper())
    private var locationEnabled: Boolean = false

    private val executor = Executors.newSingleThreadExecutor()

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocationGranted =
                result[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val coarseLocationGranted =
                result[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted || coarseLocationGranted) {
                if (locationEnabled) {
                    mapView.getMap().setMyLocationEnabled(true)
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_to_location_denied), Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_map)
        locationEnabled = isLocationEnabled(this)

        markerImage = findViewById(R.id.marker_image_view)
        mapView = findViewById(R.id.mapView)
        miniMapView = findViewById(R.id.mapViewMini)
        addressView = findViewById(R.id.tv_address)
        geocoder = Geocoder(this, Locale.getDefault())
        buttonSnapshot = findViewById(R.id.buttonSnapshot)
        buttonSnapshot.setOnClickListener { takeSnapshot() }
        buttonAddMarker = findViewById(R.id.buttonAddMarker)
        buttonAddMarker.setOnClickListener { addMarker() }
        buttonMiniMap = findViewById(R.id.buttonMiniMap)
        buttonMiniMap.setOnClickListener {
            if (miniMapView.visibility == View.VISIBLE) {
                miniMapView.visibility = View.GONE
            } else {
                miniMapView.visibility = View.VISIBLE
            }
        }
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
        mapView.getMap().getMapAsync(object : OmvMap.OnMapReadyCallback {
            override fun onMapReady() {
                mapView.getMap().showLayerOptions(false)

                mapView.getMap()
                    .moveCamera(
                        GeoPosition(
                            latitude = DEFAULT_LAT,
                            longitude = DEFAULT_LON
                        ), zoom = DEFAULT_ZOOM
                    )

                mapView.getMap().setOnCameraMoveStartedListener(
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

                mapView.getMap().setOnCameraIdleListener(object : OmvMap.OnCameraIdleListener {
                    override fun onCameraIdle() {
                        markerImage.animate()
                            .translationY(0f)
                            .setInterpolator(OVERSHOOT_INTERPOLATOR)
                            .setDuration(ANIMATION_DURATION.toLong())
                            .start()

                        executor.submit(
                            AddressLookupTask(
                                mainThread = handler,
                                geocoder = geocoder,
                                textView = addressView,
                                latitude = mapView.getMap().getCenter().latitude,
                                longitude = mapView.getMap().getCenter().longitude
                            )
                        )
                    }
                })

                if (checkLocationPermission()) {
                    if (locationEnabled) {
                        mapView.getMap().setMyLocationEnabled(true)
                    }
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

        miniMapView.getMap().getMapAsync(object : OmvMap.OnMapReadyCallback {
            override fun onMapReady() {
                miniMapView.getMap()
                    .moveCamera(
                        GeoPosition(
                            latitude = DEFAULT_LAT,
                            longitude = DEFAULT_LON
                        ), zoom = DEFAULT_ZOOM
                    )
                miniMapView.getMap().showLayerOptions(visible = false)
                miniMapView.getMap().setMyLocationEnabled(enabled = false)
                miniMapView.getMap().setMapType(type = OmvMap.MapType.SATELLITE)
            }
        })
    }

    @Suppress("deprecation")
    private class AddressLookupTask(
        val mainThread: Handler,
        val geocoder: Geocoder,
        val textView: TextView,
        val latitude: Double,
        val longitude: Double
    ) : Runnable {

        override fun run() {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addressComplete = addressToString(addresses[0])
                val addressShort = addressToShortString(addresses[0])
                val text = "$addressShort\n$addressComplete"

                mainThread.post {
                    textView.text = text
                }
            }
        }

        private fun addressToString(address: Address?): String {
            return if (address != null) address.getAddressLine(0) else ""
        }

        private fun addressToShortString(address: Address?): String {
            if (address == null) return ""

            val addressLine = address.getAddressLine(0)
            val split =
                addressLine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            return if (split.size >= 3) {
                split[1].trim { it <= ' ' } + ", " + split[2].trim { it <= ' ' }
            } else if (split.size == 2) {
                split[1].trim { it <= ' ' }
            } else split[0].trim { it <= ' ' }
        }
    }

    private fun takeSnapshot() {
        mapView.getMap().snapShot(callback = object : OmvMap.SnapshotReadyCallback {
            override fun onSnapshotReady(bitmap: Bitmap) {
                val fragment = SnapshotFragment.newInstance(bitmap)
                fragment.show(this@MapActivity.supportFragmentManager, SnapshotFragment.TAG)
            }
        })

    }

    private fun addMarker() {
        mapView.getMap().addMarker(
            OmvMarker(
                position = GeoPosition(
                    latitude = mapView.getMap().getCenter().latitude,
                    longitude = mapView.getMap().getCenter().longitude
                ),
                icon = AppCompatResources.getDrawable(
                    this@MapActivity,
                    R.drawable.ic_map_marker
                ),
                title = "Test Title"
            )
        )

        miniMapView.getMap().moveCamera(
            position = mapView.getMap().getCenter(), zoom = DEFAULT_ZOOM
        )

    }

    @Suppress("deprecation")
    private fun isLocationEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager
            return lm.isLocationEnabled
        } else {
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            return (mode != Settings.Secure.LOCATION_MODE_OFF)
        }
    }
}