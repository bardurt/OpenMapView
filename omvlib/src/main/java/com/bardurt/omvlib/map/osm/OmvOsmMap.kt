package com.bardurt.omvlib.map.osm

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.bardurt.omvlib.R
import com.bardurt.omvlib.map.core.GeoPosition
import com.bardurt.omvlib.map.core.Logger
import com.bardurt.omvlib.map.core.OmvMap
import com.bardurt.omvlib.map.core.OmvMarker
import com.bardurt.omvlib.map.osm.overlay.CameraMoveOverlay
import com.bardurt.omvlib.map.osm.overlay.DoubleTapAndMoveOverlay
import com.bardurt.omvlib.map.osm.overlay.TwoPointerZoomOverlay
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class OmvOsmMap(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
    OmvMap,
    CameraMoveOverlay.OnMapMoveListener,
    DoubleTapAndMoveOverlay.Listener,
    TwoPointerZoomOverlay.Listener {

    companion object {
        private const val TAG = "OsmMap"
        private const val PREFERENCES_NAME = "com.bardurt.openmapview.mapconfig"
        private const val DEFAULT_ZOOM = 18.0
    }

    private var logger: Logger? = null
    private var myLocationButton: View
    private var layerButton: View
    private var mapView: TouchableMapView
    private var controller: IMapController
    private var cameraMoveStartedListener: OmvMap.OnCameraMoveStartedListener? = null
    private var cameraMoveOverlay: CameraMoveOverlay
    private var idleListener: OmvMap.OnCameraIdleListener? = null

    private var doubleTapAndMoveOverlay: DoubleTapAndMoveOverlay
    private var twoPointerZoomOverlay: TwoPointerZoomOverlay
    private var myLocationOverlay: MyLocationNewOverlay
    private var locationProvider: IMyLocationProvider
    private var mapType = OmvMap.MapType.NORMAL
    private var overlays : MutableList<Overlay> = mutableListOf()

    init {
        inflate(context, R.layout.layout_omv_osm_map_view, this)
        mapView = findViewById(R.id.osm_map_view)
        mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
        myLocationButton = findViewById(R.id.buttonMyLocation)
        myLocationButton.visibility = GONE
        myLocationButton.setOnClickListener { moveToMyLocation() }
        layerButton = findViewById(R.id.buttonLayers)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        Configuration.getInstance().load(
            getContext(),
            getContext().getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        )
        mapView.setMultiTouchControls(false)
        controller = mapView.controller

        cameraMoveOverlay = CameraMoveOverlay(this, mapView)
        mapView.overlays.add(cameraMoveOverlay)

        doubleTapAndMoveOverlay = DoubleTapAndMoveOverlay(this)
        twoPointerZoomOverlay = TwoPointerZoomOverlay(this)

        mapView.overlays.add(doubleTapAndMoveOverlay)
        mapView.overlays.add(twoPointerZoomOverlay)

        locationProvider =
            GpsMyLocationProvider(context)
        (locationProvider as GpsMyLocationProvider).addLocationSource(LocationManager.NETWORK_PROVIDER)
        myLocationOverlay =
            MyLocationNewOverlay(
                locationProvider,
                mapView
            )
        myLocationOverlay.enableMyLocation(locationProvider)

        val myLocationIcon =
            BitmapFactory.decodeResource(context.resources, R.drawable.img_my_location)
        myLocationOverlay.setPersonIcon(myLocationIcon)
        myLocationOverlay.setDirectionIcon(myLocationIcon)
        myLocationOverlay.setPersonAnchor(0.5F, 0.5F)

        overlays.addAll(mapView.overlays)
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
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
        controller.setCenter(
            GeoPoint(
                position.latitude,
                position.longitude
            )
        )
    }

    override fun addMarker(marker: OmvMarker) {
        val geoPoint = GeoPoint(
            marker.position.latitude,
            marker.position.longitude
        )
        val osmMarker = Marker(mapView)

        if (marker.icon != null) {
            osmMarker.icon = marker.icon
        } else {
            osmMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_default_marker)
        }

        if (marker.title.isNotEmpty()) {
            osmMarker.title = marker.title
        }

        osmMarker.position = geoPoint
        osmMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(osmMarker)
        logger?.log(Logger.LogLevel.DEBUG, TAG, "Marker added to map")
    }

    override fun setMapType(type: OmvMap.MapType) {
        logger?.log(Logger.LogLevel.DEBUG, TAG, "Setting map type $type")
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

    override fun setMultiToucheControlsEnabled(enabled: Boolean) {
        mapView.setUserInteractionEnabled(enabled)
    }

    override fun setBuildingsEnabled(enabled: Boolean) {

    }

    override fun snapShot(callback: OmvMap.SnapshotReadyCallback) {
        logger?.log(Logger.LogLevel.DEBUG, TAG, "Creating snapshot")
        val bitmap = if (mapView.width < 1) {
            logger?.log(Logger.LogLevel.DEBUG, TAG, "View is not laid out, using default size")
            drawToBitmap(mapView, 512, 512)
        } else {
            logger?.log(
                Logger.LogLevel.DEBUG,
                TAG,
                "View is laid out, using view size w: ${mapView.width}, h: ${mapView.height}"
            )
            drawToBitmap(mapView, mapView.width, mapView.height)
        }

        if (bitmap == null) {
            logger?.log(Logger.LogLevel.DEBUG, TAG, "Bitmap is null")
            throw IllegalStateException("Unable to draw bitmap")
        }
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

    override fun setMyLocationEnabled(enabled: Boolean) {
        if (!hasPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) || !hasPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            throw IllegalStateException("Access to location has not been granted!")
        }

        logger?.log(Logger.LogLevel.DEBUG, TAG, "My Location enabled : $enabled")

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

    override fun setOnMapLoadedCallback(callback: OmvMap.MapLoadedCallback) {

        if (mapView.isLayoutOccurred) {
            logger?.log(Logger.LogLevel.DEBUG, TAG, "Map loaded!")
            callback.onMapLoaded()
        } else {
            logger?.log(Logger.LogLevel.DEBUG, TAG, "Map not loaded, waiting for load!")
            mapView.addOnFirstLayoutListener { v, left, top, right, bottom ->
                run {
                    logger?.log(Logger.LogLevel.DEBUG, TAG, "Map loaded!")
                    callback.onMapLoaded()
                }
            }
        }

    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun moveToMyLocation() {
        controller.animateTo(myLocationOverlay.myLocation, DEFAULT_ZOOM, 1200)
    }

    private fun drawToBitmap(viewToDrawFrom: View, width: Int, height: Int): Bitmap? {
        var newWidth = width
        var newHeight = height
        val wasDrawingCacheEnabled = viewToDrawFrom.isDrawingCacheEnabled
        if (!wasDrawingCacheEnabled) viewToDrawFrom.isDrawingCacheEnabled = true
        if (newWidth <= 0 || newHeight <= 0) {
            if (viewToDrawFrom.width <= 0 || viewToDrawFrom.height <= 0) {
                viewToDrawFrom.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                newWidth = viewToDrawFrom.measuredWidth
                newHeight = viewToDrawFrom.measuredHeight
            }
            if (newWidth <= 0 || newHeight <= 0) {
                val bmp = viewToDrawFrom.drawingCache
                val result = if (bmp == null) null else Bitmap.createBitmap(bmp)
                if (!wasDrawingCacheEnabled) viewToDrawFrom.isDrawingCacheEnabled = false
                return result
            }
            viewToDrawFrom.layout(0, 0, width, height)
        } else {
            viewToDrawFrom.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
            viewToDrawFrom.layout(0, 0, viewToDrawFrom.measuredWidth, viewToDrawFrom.measuredHeight)
        }
        val drawingCache = viewToDrawFrom.drawingCache
        val bmp = ThumbnailUtils.extractThumbnail(drawingCache, newHeight, newHeight)
        val result = if (bmp == null || bmp != drawingCache) {
            bmp
        } else {
            Bitmap.createBitmap(bmp)
        }

        if (!wasDrawingCacheEnabled) {
            viewToDrawFrom.isDrawingCacheEnabled = false
        }
        return result
    }

}