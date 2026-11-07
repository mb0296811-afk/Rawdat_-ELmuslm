package com.rawdatalmuslim.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { PrayerScheduler.scheduleAll(context) }
}
