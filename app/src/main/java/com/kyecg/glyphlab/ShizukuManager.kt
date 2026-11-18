
package com.kyecg.glyphlab

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku

class ShizukuManager {

    enum class State {
        SERVICE_NOT_RUNNING,
        PERMISSION_REQUIRED,
        PERMISSION_GRANTED,
        UNSUPPORTED,
        ERROR
    }

    fun refresh(): State {
        return try {
            if (!Shizuku.pingBinder()) {
                State.SERVICE_NOT_RUNNING
            } else if (Shizuku.isPreV11()) {
                State.UNSUPPORTED
            } else if (
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            ) {
                State.PERMISSION_GRANTED
            } else {
                State.PERMISSION_REQUIRED
            }
        } catch (e: Exception) {
            Log.e("GlyphLabShizuku", "Status check failed", e)
            State.ERROR
        }
    }

    fun requestPermission() {
        try {
            val serviceAvailable = Shizuku.pingBinder()

            if (!serviceAvailable) {
                Log.w("GlyphLabShizuku", "Shizuku service is not running")
                return
            }

            val permissionGranted =
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                Shizuku.requestPermission(1001)
            }
        } catch (e: Exception) {
            Log.e("GlyphLabShizuku", "Permission request failed", e)
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (!Shizuku.pingBinder()) {
                false
            } else {
                Shizuku.checkSelfPermission() ==
                    PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isServiceRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }
}
