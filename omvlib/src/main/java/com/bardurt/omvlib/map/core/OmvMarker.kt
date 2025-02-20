package com.bardurt.omvlib.map.core

import android.graphics.drawable.Drawable

data class OmvMarker(
    val position: GeoPosition,
    val title : String = "",
    val icon : Drawable? = null,
)