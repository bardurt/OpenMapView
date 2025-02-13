package com.bardurt.omvlib.map.osm.overlay


import android.os.SystemClock
import android.view.MotionEvent

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.abs

class DoubleTapAndMoveOverlay(private val listener: Listener) : Overlay() {

    companion object {
        private const val DOUBLE_TAP_THRESHOLD = 300
        private const val MOVE_THRESHOLD = 20
    }

    private var lastTapTime: Long = 0
    private var isDoubleTap = false
    private var startY: Float = 0f

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val currentTime = SystemClock.uptimeMillis()
                if (currentTime - lastTapTime < DOUBLE_TAP_THRESHOLD) {
                    isDoubleTap = true
                    startY = event.y
                    return true
                }
                lastTapTime = currentTime
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDoubleTap) {
                    val movement = (event.y - startY).toInt()
                    if (abs(movement) > MOVE_THRESHOLD) {
                        listener.onMove(movement)
                        isDoubleTap = false
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                isDoubleTap = false
                return true
            }
        }
        return false
    }

    interface Listener {
        fun onMove(movement: Int)
    }
}