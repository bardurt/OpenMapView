package com.bardurt.openmapview.map.osm;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

class OnMoveOverlay(
    private val onMapMoveListener: OnMapMoveListener,
    private val mapView: MapView
) : Overlay() {

    private var isMapMoving: Boolean = false
    private var lastLatLon: IGeoPoint = GeoPoint(0.0, 0.0)
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 50
    private val stopDelay: Long = 300
    private val movementThreshold: Int = 10
    private var startX: Float = 0f
    private var startY: Float = 0f

    private val checkMovementRunnable = object : Runnable {
        override fun run() {
            val currLatLon = mapView.mapCenter
            if (currLatLon != null && currLatLon == lastLatLon) {
                if (isMapMoving) {
                    isMapMoving = false
                    onMapMoveListener.mapMovingFinishedEvent()
                }
            } else {
                lastLatLon = currLatLon
                handler.postDelayed(this, checkInterval)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        super.onTouchEvent(event, mapView)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = Math.abs(event.x - startX)
                val deltaY = Math.abs(event.y - startY)
                if (!isMapMoving && (deltaX > movementThreshold || deltaY > movementThreshold)) {
                    isMapMoving = true
                    onMapMoveListener.mapMoveStartedEvent()
                    handler.postDelayed(checkMovementRunnable, checkInterval)
                }
            }

            MotionEvent.ACTION_UP -> {
                handler.postDelayed(checkMovementRunnable, stopDelay)
            }
        }
        return false
    }

    interface OnMapMoveListener {
        fun mapMovingFinishedEvent()
        fun mapMoveStartedEvent()
    }
}
