package com.bardurt.omvlib.map.osm.overlay;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import kotlin.math.abs

class CameraMoveOverlay(
    private val onMapMoveListener: OnMapMoveListener,
    private val mapView: MapView
) : Overlay() {

    companion object {
        private const val MOVE_THRESHOLD = 10
        private const val STOP_DELAY = 300L
        private const val CHECK_INTERVAL = 50L
    }


    private var isMapMoving: Boolean = false
    private var lastLatLon: IGeoPoint = GeoPoint(0.0, 0.0)
    private val handler = Handler(Looper.getMainLooper())
    private var startX: Float = 0f
    private var startY: Float = 0f

    private val checkMovementRunnable = object : Runnable {
        override fun run() {
            val currLatLon = mapView.mapCenter
            if (currLatLon != null && currLatLon == lastLatLon) {
                if (isMapMoving) {
                    isMapMoving = false
                    onMapMoveListener.onMapMoveFinished()
                }
            } else {
                lastLatLon = currLatLon
                handler.postDelayed(this, CHECK_INTERVAL)
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
                val deltaX = abs(event.x - startX)
                val deltaY = abs(event.y - startY)
                if (!isMapMoving && (deltaX > MOVE_THRESHOLD || deltaY > MOVE_THRESHOLD)) {
                    isMapMoving = true
                    onMapMoveListener.onMapMoveStarted()
                    handler.postDelayed(checkMovementRunnable, CHECK_INTERVAL)
                }
            }

            MotionEvent.ACTION_UP -> {
                handler.postDelayed(checkMovementRunnable, STOP_DELAY)
            }
        }
        return false
    }

    interface OnMapMoveListener {
        fun onMapMoveFinished()
        fun onMapMoveStarted()
    }
}
