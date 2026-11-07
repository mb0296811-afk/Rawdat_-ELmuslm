package com.rawdatalmuslim.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONArray
import java.util.Calendar

object PrayerScheduler {
    const val PREFS = "rawdatalmuslim_prayer_prefs"

    fun scheduleAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString("prayers", "[]") ?: "[]"
        val prayers = try { JSONArray(raw) } catch (_: Exception) { JSONArray() }
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until prayers.length()) {
            val p = prayers.optJSONObject(i) ?: continue
            val name = p.optString("name", "الصلاة")
            val hour12 = p.optInt("hour", 0)
            val minute = p.optInt("minute", 0)
            val period = p.optString("period", "AM")
            val hour24 = when {
                period == "AM" && hour12 == 12 -> 0
                period == "PM" && hour12 == 12 -> 12
                period == "PM" -> hour12 + 12
                else -> hour12
            }
            val audio = p.optString("audio", "اذان عبدالباسط.mp3")
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            schedule(context, alarm, i * 10 + 1, calendar.timeInMillis, name, audio, false)
            if (prefs.getBoolean("preAlert", false)) {
                schedule(context, alarm, i * 10 + 2, calendar.timeInMillis - 10 * 60 * 1000L, name, audio, true)
            }
        }
    }

    private fun schedule(context: Context, alarm: AlarmManager, id: Int, at: Long, name: String, audio: String, reminder: Boolean) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("name", name); putExtra("audio", audio); putExtra("reminder", reminder)
        }
        val pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= 23) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        else alarm.setExact(AlarmManager.RTC_WAKEUP, at, pi)
    }
}
