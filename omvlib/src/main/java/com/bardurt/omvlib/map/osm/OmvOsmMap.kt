package com.bardurt.omvlib.map.osm

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.bardurt.omvlib.R
import com.bardurt.omvlib.map.core.GeoPosition
import com.bardurt.omvlib.map.core.OmvMap
import com.bardurt.omvlib.map.core.OmvMarker
import com.bardurt.omvlib.map.osm.overlay.CameraMoveOverlay
import com.bardurt.omvlib.map.osm.overlay.DoubleTapAndMoveOverlay
import com.bardurt.omvlib.map.osm.overlay.TwoPointerZoomOverlay
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class OmvOsmMap(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
    OmvMap,
    CameraMoveOverlay.OnMapMoveListener,
    DoubleTapAndMoveOverlay.Listener,
    TwoPointerZoomOverlay.Listener {

    companion object {
        private const val PREFERENCES_NAME = "com.bardurt.openmapview.mapconfig"
    }

    private var myLocationButton: View
    private var layerButton: View
    private var mapView: MapView
    private var controller: IMapController
    private var cameraMoveStartedListener: OmvMap.OnCameraMoveStartedListener? = null
    private var moveOverlay: CameraMoveOverlay
    private var idleListener: OmvMap.OnCameraIdleListener? = null

    private var doubleTapAndMoveOverlay: DoubleTapAndMoveOverlay
    private var twoPointerZoomOverlay: TwoPointerZoomOverlay
    private var myLocationOverlay: MyLocationNewOverlay
    private var locationProvider: IMyLocationProvider
    private var mapType = OmvMap.MapType.NORMAL


    init {
        inflate(context, R.layout.layout_omv_osm_map_view, this)
        mapView = findViewById(R.id.osm_map_view)
        myLocationButton = findViewById(R.id.buttonMyLocation)
        myLocationButton.visibility = GONE
        myLocationButton.setOnClickListener { moveToMyLocation() }
        layerButton = findViewById(R.id.buttonLayers)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        Configuration.getInstance().load(
            getContext(),
            getContext().getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        )
        controller = mapView.controller

        moveOverlay = CameraMoveOverlay(this, mapView)
        mapView.overlays.add(moveOverlay)

        doubleTapAndMoveOverlay = DoubleTapAndMoveOverlay(this)
        mapView.overlays.add(doubleTapAndMoveOverlay)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        twoPointerZoomOverlay = TwoPointerZoomOverlay(this)
        mapView.overlays.add(twoPointerZoomOverlay)

        locationProvider = GpsMyLocationProvider(context)
        (locationProvider as GpsMyLocationProvider).addLocationSource(LocationManager.NETWORK_PROVIDER)
        myLocationOverlay = MyLocationNewOverlay(locationProvider, mapView)
        myLocationOverlay.enableMyLocation(locationProvider)

        val myLocationIcon =
            BitmapFactory.decodeResource(context.resources, R.drawable.img_my_location)
        myLocationOverlay.setPersonIcon(myLocationIcon)
        myLocationOverlay.setDirectionIcon(myLocationIcon)
        myLocationOverlay.setPersonAnchor(0.5F, 0.5F)
    }

    override fun resume() {
        mapView.onResume()
    }

    override fun pause() {
        mapView.onPause()
    }

    override fun destroy() {
        mapView.onDetach()
    }

    override fun moveCamera(position: GeoPosition, zoom: Double) {
        controller.setZoom(zoom)
        controller.setCenter(GeoPoint(position.latitude, position.longitude))
    }

    override fun addMarker(marker: OmvMarker) {
        val geoPoint = GeoPoint(marker.position.latitude, marker.position.longitude)
        val osmMarker = Marker(mapView)

        if (marker.icon != null) {
            osmMarker.icon = marker.icon
        }

        if (marker.title.isNotEmpty()) {
            osmMarker.title = marker.title
        }

        osmMarker.position = geoPoint
        osmMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(osmMarker)
    }

    override fun setMapType(type: OmvMap.MapType) {
        mapType = type
        val tileSource = when (type) {
            OmvMap.MapType.NORMAL -> TileSourceFactory.MAPNIK
            OmvMap.MapType.SATELLITE -> TileSourceFactory.USGS_SAT
        }

        mapView.setTileSource(tileSource)
    }

    override fun getMapType(): OmvMap.MapType {
        return mapType
    }

    override fun getMapAsync(callback: OmvMap.OnMapReadyCallback) {
        callback.onMapReady()
    }

    override fun setBuildingsEnabled(enabled: Boolean) {

    }

    override fun snapShot(callback: OmvMap.SnapshotReadyCallback) {
        val bitmap = mapView.drawToBitmap()
        callback.onSnapshotReady(bitmap)
    }

    override fun setOnCameraMoveStartedListener(listener: OmvMap.OnCameraMoveStartedListener) {
        this.cameraMoveStartedListener = listener
    }

    override fun setOnCameraIdleListener(listener: OmvMap.OnCameraIdleListener) {
        idleListener = listener
    }

    override fun getCenter(): GeoPosition {
        val lat = mapView.mapCenter.latitude
        val lon = mapView.mapCenter.longitude

        return GeoPosition(latitude = lat, longitude = lon)
    }

    override fun show() {
        this.visibility = VISIBLE
    }

    override fun hide() {
        this.visibility = GONE
    }

    override fun setMyLocationEnabled(enabled: Boolean) {
        if (!hasPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) || !hasPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            throw IllegalStateException("Access to location has not been granted!")
        }

        Log.d("OsmMap", "My Location enabled : $enabled")

        if (enabled) {
            myLocationButton.visibility = VISIBLE
            myLocationOverlay.enableMyLocation(locationProvider)
            mapView.overlays.add(myLocationOverlay)
        } else {
            myLocationButton.visibility = GONE
            myLocationOverlay.disableMyLocation()
            mapView.overlays.remove(myLocationOverlay)
        }
    }

    override fun showLayerOptions(visible: Boolean) {
        if (visible) {
            layerButton.visibility = VISIBLE
        } else {
            layerButton.visibility = GONE
        }
    }

    override fun onMapMoveFinished() {
        idleListener?.onCameraIdle()
    }

    override fun onMapMoveStarted() {
        cameraMoveStartedListener?.onCameraMoveStarted()
    }

    override fun onDoubleTapAndMove(movementPercentage: Double) {
        val current = mapView.zoomLevelDouble
        val scaleFactor = 0.15
        val scaledMovement = movementPercentage * (scaleFactor * current)
        var zoomLevel = current + scaledMovement
        zoomLevel = zoomLevel.coerceIn(mapView.minZoomLevel, mapView.maxZoomLevel)

        controller.setZoom(zoomLevel)
    }

    override fun onZoom(movement: Int) {
        if (movement < 0) {
            controller.zoomOut()
        } else {
            controller.zoomIn()
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun moveToMyLocation() {
        controller.animateTo(myLocationOverlay.myLocation)
    }
}