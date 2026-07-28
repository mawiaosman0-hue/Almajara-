package com.example.util

import android.content.Context
import android.media.RingtoneManager

object NotificationSoundUtils {
    /**
     * Plays the official standard device notification sound once for new alert events.
     */
    fun playNotificationSound(context: Context) {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
