package com.bardurt.omvlib.map.osm

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import org.osmdroid.views.MapView

class TouchableMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MapView(context, attrs) {

    private var isUserInteractionEnabled = true

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (!isUserInteractionEnabled) {
            return false
        }
        return super.dispatchTouchEvent(event)
    }

    fun setUserInteractionEnabled(isUserInteractionEnabled: Boolean) {
        this.isUserInteractionEnabled = isUserInteractionEnabled
    }
}