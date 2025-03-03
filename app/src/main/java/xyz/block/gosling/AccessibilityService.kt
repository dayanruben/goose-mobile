package xyz.block.gosling

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent

class GoslingAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: GoslingAccessibilityService? = null
        fun getInstance(): GoslingAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_VIEW_FOCUSED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Handle accessibility events here if needed
    }

    override fun onInterrupt() {
        // Handle interruptions if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
