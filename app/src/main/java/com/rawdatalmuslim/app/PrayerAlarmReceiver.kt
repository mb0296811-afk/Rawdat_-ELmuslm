
package com.rawdatalmuslim.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val name = intent.getStringExtra("name") ?: "الصلاة"
        val audio = intent.getStringExtra("audio")
            ?: "اذان عبدالباسط.mp3"

        val reminder = intent.getBooleanExtra("reminder", false)

        val serviceIntent = Intent(
            context,
            AdhanService::class.java
        ).apply {
            putExtra("name", name)
            putExtra("audio", audio)
            putExtra("reminder", reminder)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
