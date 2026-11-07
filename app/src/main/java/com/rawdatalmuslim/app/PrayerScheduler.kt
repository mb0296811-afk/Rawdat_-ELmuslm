
package com.rawdatalmuslim.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import org.json.JSONArray
import java.util.Calendar

object PrayerScheduler {

    const val PREFS = "rawdatalmuslim_prayer_prefs"

    fun scheduleAll(context: Context) {

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString("prayers", "[]") ?: "[]"

        val prayers = try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }

        val alarm = context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager

        // إلغاء التنبيهات القديمة أولاً
        for (i in 0 until prayers.length()) {
            cancel(context, i * 10 + 1)
            cancel(context, i * 10 + 2)
        }

        // فحص صلاحية التنبيهات الدقيقة
        if (Build.VERSION.SDK_INT >= 31 &&
            !alarm.canScheduleExactAlarms()
        ) {
            val permissionIntent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(permissionIntent)
            return
        }

        val now = System.currentTimeMillis()

        for (i in 0 until prayers.length()) {

            val p = prayers.optJSONObject(i) ?: continue

            val name = p.optString("name", "الصلاة")
            val hour12 = p.optInt("hour", 0)
            val minute = p.optInt("minute", 0)
            val period = p.optString("period", "AM")

            if (hour12 !in 1..12 || minute !in 0..59) continue

            val hour24 = when {
                period == "AM" && hour12 == 12 -> 0
                period == "PM" && hour12 == 12 -> 12
                period == "PM" -> hour12 + 12
                else -> hour12
            }

            val audio = p.optString(
                "audio",
                "اذان عبدالباسط.mp3"
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= now) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val prayerTime = calendar.timeInMillis

            // جدولة الأذان
            schedule(
                context,
                alarm,
                i * 10 + 1,
                prayerTime,
                name,
                audio,
                false
            )

            // التنبيه قبل الأذان بعشر دقائق
            if (prefs.getBoolean("preAlert", false)) {

                val reminderTime = prayerTime - 10 * 60 * 1000L

                if (reminderTime > now) {
                    schedule(
                        context,
                        alarm,
                        i * 10 + 2,
                        reminderTime,
                        name,
                        audio,
                        true
                    )
                }
            }
        }
    }

    private fun schedule(
        context: Context,
        alarm: AlarmManager,
        id: Int,
        at: Long,
        name: String,
        audio: String,
        reminder: Boolean
    ) {

        val intent = Intent(
            context,
            PrayerAlarmReceiver::class.java
        ).apply {
            putExtra("name", name)
            putExtra("audio", audio)
            putExtra("reminder", reminder)
        }

        val pi = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= 23) {
            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                at,
                pi
            )
        } else {
            alarm.setExact(
                AlarmManager.RTC_WAKEUP,
                at,
                pi
            )
        }
    }

    private fun cancel(context: Context, id: Int) {

        val intent = Intent(
            context,
            PrayerAlarmReceiver::class.java
        )

        val pi = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
        )

        pi?.let {
            val alarm = context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

            alarm.cancel(it)
            it.cancel()
        }
    }
}
