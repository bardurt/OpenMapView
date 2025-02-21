package com.bardurt.omvlib.map.core

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bardurt.omvlib.R
import com.bardurt.omvlib.map.osm.OmvOsmMap

class OmvMapView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
    MapProvider {

    companion object {
        private const val TAG = "MapProvider"
    }

    private val container: ViewGroup
    private var omvMap: OmvMap
    private var logger: Logger? = null

    init {
        inflate(context, R.layout.layout_omv_map_view, this)
        container = findViewById(R.id.container)
        omvMap = OmvOsmMap(context, attrs)
        container.addView(omvMap as OmvOsmMap)
    }

    override fun setProviderSource(source: MapProvider.Source) {
        container.removeAllViews()
        logger?.log(Logger.LogLevel.DEBUG, TAG, "Setting map source to $source")
        when (source) {
            MapProvider.Source.OSM -> {
                omvMap = OmvOsmMap(context, null)
                logger?.let {
                    omvMap.setLogger(it)
                }
                container.addView(omvMap as OmvOsmMap)
            }

        }
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
        omvMap.setLogger(logger)
    }

    override fun getMap(): OmvMap {
        return omvMap
    }

    override fun getMapAsync(callback: MapProvider.OnMapReadyCallback) {
        callback.onMapReady(omvMap)
    }

}