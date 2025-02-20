package com.bardurt.omvlib.map.core

interface MapProvider {

    fun setProviderSource(source: Source)

    fun getMap(): OmvMap

    fun getMapAsync(callback: OnMapReadyCallback)

    interface OnMapReadyCallback {
        fun onMapReady(omvMap: OmvMap)
    }

    enum class Source {
        OSM
    }
}