package xyz.block.gosling

import android.app.Application

class GoslingApplication : Application() {
    companion object {
        var isMainActivityRunning = false
    }
}