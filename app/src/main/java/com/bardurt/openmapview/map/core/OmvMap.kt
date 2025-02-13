package com.bardurt.openmapview.map.core

import android.graphics.Bitmap

interface OmvMap {

    fun resume()
    fun pause()
    fun destroy()

    fun moveCamera(position: GeoPosition, zoom: Double)
    fun addMarker(marker: OmvMarker)
    fun setMapType(type: MapType)
    fun getMapAsync(callback: OnSignalMapReadyCallback)
    fun setBuildingsEnabled(enabled: Boolean)
    fun snapShot(callback: SnapshotReadyCallback)
    fun setOnMapLoadedCallback(callback: OnMapLoadedCallback)
    fun setOnCameraMoveStartedListener(listener: OnCameraMoveStartedListener)
    fun setOnCameraIdleListener(listener: OnCameraIdleListener)
    fun show()
    fun hide()
    fun getCenter(): GeoPosition

    enum class MapType {
        NORMAL
    }

    interface OnSignalMapReadyCallback {
        fun onSignalMapReady(map: OmvMap)
    }

    interface SnapshotReadyCallback {
        fun onSnapshotReady(var1: Bitmap?)
    }

    interface OnMapLoadedCallback {
        fun onMapLoaded()
    }

    interface OnCameraMoveStartedListener {
        fun onCameraMoveStarted()
    }

    interface OnCameraIdleListener {
        fun onCameraIdle()
    }
}