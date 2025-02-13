package com.bardurt.omvlib.map.core

import android.graphics.Bitmap

interface OmvMap {

    fun resume()
    fun pause()
    fun destroy()

    fun moveCamera(position: GeoPosition, zoom: Double)
    fun addMarker(marker: OmvMarker)
    fun setMapType(type: MapType)
    fun getMapAsync(callback: OnMapReadyCallback)
    fun setBuildingsEnabled(enabled: Boolean)
    fun snapShot(callback: SnapshotReadyCallback)
    fun setOnCameraMoveStartedListener(listener: OnCameraMoveStartedListener)
    fun setOnCameraIdleListener(listener: OnCameraIdleListener)
    fun show()
    fun hide()
    fun getCenter(): GeoPosition
    fun setMyLocationEnabled(enabled: Boolean)

    enum class MapType {
        NORMAL,
        SATELLITE
    }

    interface OnMapReadyCallback {
        fun onMapReady()
    }

    interface SnapshotReadyCallback {
        fun onSnapshotReady(var1: Bitmap?)
    }

    interface OnCameraMoveStartedListener {
        fun onCameraMoveStarted()
    }

    interface OnCameraIdleListener {
        fun onCameraIdle()
    }
}