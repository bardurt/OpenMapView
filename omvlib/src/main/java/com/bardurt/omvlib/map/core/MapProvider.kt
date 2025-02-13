package com.bardurt.omvlib.map.core

interface MapProvider {

    fun setProviderSource(source: Source)

    fun getMap(): OmvMap

    enum class Source {
        OSM
    }
}