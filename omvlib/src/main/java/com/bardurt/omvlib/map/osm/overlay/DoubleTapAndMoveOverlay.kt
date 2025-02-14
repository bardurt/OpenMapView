package com.bardurt.omvlib.map.osm.overlay


import android.graphics.Canvas
import android.os.SystemClock
import android.view.MotionEvent

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.abs

class DoubleTapAndMoveOverlay(private val listener: Listener) : Overlay() {

    companion object {
        private const val DOUBLE_TAP_THRESHOLD = 200
        private const val MOVE_THRESHOLD = 100
    }

    private var lastTapTime: Long = 0
    private var isDoubleTap = false
    private var startY: Float = 0f
    private var height: Double = 0.0

    override fun draw(pCanvas: Canvas?, pMapView: MapView?, pShadow: Boolean) {
        super.draw(pCanvas, pMapView, pShadow)
        pCanvas?.let {
            height = it.height.toDouble()
        }
    }

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
                        val percentage = movement / height
                        listener.onDoubleTapAndMove(percentage)
                    }
                    return true
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
        fun onDoubleTapAndMove(movementPercentage: Double)
    }
}