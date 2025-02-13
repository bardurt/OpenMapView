package com.bardurt.openmapview.map.osm

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.drawToBitmap
import com.bardurt.openmapview.R
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.bardurt.openmapview.map.core.GeoPosition
import com.bardurt.openmapview.map.core.OmvMap
import com.bardurt.openmapview.map.core.OmvMarker
import org.osmdroid.views.CustomZoomButtonsController


class OmvOsmMap(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
    OmvMap,
    OnMoveOverlay.OnMapMoveListener,
    DoubleTapAndMoveOverlay.Listener,
    TwoPointerZoomOverlay.Listener {

    private var mapView: MapView
    private var controller: IMapController;
    private var cameraMoveStartedListener: OmvMap.OnCameraMoveStartedListener? = null
    private var moveOverlay: OnMoveOverlay
    private var idleListener: OmvMap.OnCameraIdleListener? = null

    private var doubleTapAndMoveOverlay: DoubleTapAndMoveOverlay
    private var twoPointerZoomOverlay: TwoPointerZoomOverlay


    init {
        inflate(context, R.layout.layout_omv_map_view, this)
        mapView = findViewById(R.id.osm_map_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        Configuration.getInstance().load(
            getContext(),
            getContext().getSharedPreferences("com.bardurt.openmapview.mapconfig", MODE_PRIVATE)
        )
        controller = mapView.controller

        moveOverlay = OnMoveOverlay(this, mapView)
        mapView.overlays.add(moveOverlay)

        doubleTapAndMoveOverlay = DoubleTapAndMoveOverlay(this)
        mapView.overlays.add(doubleTapAndMoveOverlay)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        twoPointerZoomOverlay = TwoPointerZoomOverlay(this)
        mapView.overlays.add(twoPointerZoomOverlay)
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
        val startPoint = GeoPoint(marker.position.latitude, marker.position.longitude)
        val osmMarker = Marker(mapView)
        osmMarker.position = startPoint
        osmMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(osmMarker)
    }

    override fun setMapType(type: OmvMap.MapType) {
    }

    override fun getMapAsync(callback: OmvMap.OnSignalMapReadyCallback) {
        callback.onSignalMapReady(this)
    }

    override fun setBuildingsEnabled(enabled: Boolean) {

    }

    override fun snapShot(callback: OmvMap.SnapshotReadyCallback) {
        val bitmap = mapView.drawToBitmap()
        callback.onSnapshotReady(bitmap)
    }

    override fun setOnMapLoadedCallback(callback: OmvMap.OnMapLoadedCallback) {
        callback.onMapLoaded()
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

    override fun mapMovingFinishedEvent() {
        idleListener?.onCameraIdle()
    }

    override fun mapMoveStartedEvent() {
        cameraMoveStartedListener?.onCameraMoveStarted()
    }

    override fun onMove(movement: Int) {
        if (movement > 0) {
            controller.zoomIn()
        } else {
            controller.zoomOut()
        }
    }

    override fun onZoom(movement: Int) {
        if (movement < 0) {
            controller.zoomIn()
        } else {
            controller.zoomOut()
        }
    }
}