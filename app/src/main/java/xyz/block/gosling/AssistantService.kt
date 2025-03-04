package xyz.block.gosling

import android.content.Intent
import android.service.voice.VoiceInteractionService

class AssistantService : VoiceInteractionService() {
    override fun onReady() {
        super.onReady()

        val intent = Intent(this, SessionService::class.java)
        startService(intent)
    }
}
