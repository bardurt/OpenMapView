package com.bardurt.openmapview.map.osm


import android.os.SystemClock
import android.view.MotionEvent

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.abs

class DoubleTapAndMoveOverlay(private val listener: Listener) : Overlay() {

    private var lastTapTime: Long = 0
    private var isDoubleTap = false
    private var startY: Float = 0f
    private val moveThreshold = 20 // Minimum movement in pixels to consider as scroll

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val currentTime = SystemClock.uptimeMillis()
                if (currentTime - lastTapTime < 300) { // 300ms threshold for double-tap
                    isDoubleTap = true
                    startY = event.y
                    return true
                }
                lastTapTime = currentTime
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDoubleTap) {
                    val movement = (event.y - startY).toInt()
                    if (abs(movement) > moveThreshold) { // Check if movement exceeds threshold
                        listener.onMove(movement)
                        isDoubleTap = false // Reset after a valid move
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