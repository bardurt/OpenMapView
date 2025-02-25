package com.bardurt.omvlib.map.osm.overlay

import android.view.MotionEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.abs
import kotlin.math.sqrt

class TwoPointerZoomOverlay(private val listener: Listener) : Overlay() {

    private var initialDistance: Float = 0f

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    initialDistance = getDistance(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 2) {
                    val currentDistance = getDistance(event)
                    val movement = (currentDistance - initialDistance).toInt()
                    if (abs(movement) > 10) {
                        listener.onZoom(movement)
                        initialDistance = currentDistance
                    }
                }
            }
        }
        return false
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return sqrt(x = (dx * dx + dy * dy).toDouble()).toFloat()
    }

    interface Listener {
        fun onZoom(movement: Int)
    }
}