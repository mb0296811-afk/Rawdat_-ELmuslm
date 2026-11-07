package com.rawdatalmuslim.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(PrayerScheduler.PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean("muted", false)) return
        val service = Intent(context, AdhanPlaybackService::class.java).apply {
            putExtra("name", intent.getStringExtra("name") ?: "الصلاة")
            putExtra("audio", intent.getStringExtra("audio") ?: "اذان عبدالباسط.mp3")
            putExtra("reminder", intent.getBooleanExtra("reminder", false))
        }
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service) else context.startService(service)
        if (!intent.getBooleanExtra("reminder", false)) PrayerScheduler.scheduleAll(context)
    }
}
