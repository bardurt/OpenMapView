package com.bardurt.omvlib.map.core

import android.util.Log
import com.bardurt.omvlib.map.core.Logger.Companion.SUPER_TAG

interface Logger {

    companion object {
        const val SUPER_TAG = "OpenMapView"
    }

    fun log(level: LogLevel, origin: String, message: String)

    fun log(level: LogLevel, origin: String, message: String, error: Throwable)

    enum class LogLevel {
        DEBUG,
        WARNING,
        INFO,
        ERROR
    }

}

object DebugLogger : Logger {

    override fun log(level: Logger.LogLevel, origin: String, message: String) {
        when (level) {
            Logger.LogLevel.DEBUG -> Log.d(SUPER_TAG, "$origin :: $message")
            Logger.LogLevel.WARNING -> Log.w(SUPER_TAG, "$origin :: $message")
            Logger.LogLevel.INFO -> Log.i(SUPER_TAG, "$origin :: $message")
            Logger.LogLevel.ERROR -> Log.e(SUPER_TAG, "$origin :: $message")
        }
    }

    override fun log(level: Logger.LogLevel, origin: String, message: String, error: Throwable) {
        when (level) {
            Logger.LogLevel.DEBUG -> Log.d(SUPER_TAG, "$origin :: $message", error)
            Logger.LogLevel.WARNING -> Log.w(SUPER_TAG, "$origin :: $message", error)
            Logger.LogLevel.INFO -> Log.i(SUPER_TAG, "$origin :: $message", error)
            Logger.LogLevel.ERROR -> Log.e(SUPER_TAG, "$origin :: $message", error)
        }
    }


}