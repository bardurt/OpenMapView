package com.bardurt.openmapview

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.bardurt.openmapview.map.core.GeoPosition
import com.bardurt.openmapview.map.core.OmvMap
import com.bardurt.openmapview.map.osm.OmvOsmMap

class MapActivity : AppCompatActivity() {

    companion object {
        const val ANIMATION_DURATION: Int = 250
        val OVERSHOOT_INTERPOLATOR: OvershootInterpolator = OvershootInterpolator()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_map)

        val markerImage = findViewById<View>(R.id.marker_image_view)
        val map: OmvOsmMap = findViewById(R.id.map_view)
        map.moveCamera(GeoPosition(-12.080235951074854, -77.04036706431548), 13.0)
        map.setOnCameraMoveStartedListener(
            object : OmvMap.OnCameraMoveStartedListener {
                override fun onCameraMoveStarted() {
                    markerImage.animate()
                        .translationY(-75f)
                        .setInterpolator(OVERSHOOT_INTERPOLATOR)
                        .setDuration(ANIMATION_DURATION.toLong())
                        .start()
                }

            }
        )

        map.setOnCameraIdleListener(object : OmvMap.OnCameraIdleListener {
            override fun onCameraIdle() {
                markerImage.animate()
                    .translationY(0f)
                    .setInterpolator(OVERSHOOT_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION.toLong())
                    .start()

            }
        })
    }
}