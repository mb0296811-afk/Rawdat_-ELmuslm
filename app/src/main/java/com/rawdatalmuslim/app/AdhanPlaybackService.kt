package com.rawdatalmuslim.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AdhanPlaybackService : Service() {
    private var player: MediaPlayer? = null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel("adhan", "تنبيهات الصلاة", NotificationManager.IMPORTANCE_HIGH))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val name = intent?.getStringExtra("name") ?: "الصلاة"
        val audio = intent?.getStringExtra("audio") ?: "اذان عبدالباسط.mp3"
        val reminder = intent?.getBooleanExtra("reminder", false) ?: false
        val title = if (reminder) "اقترب موعد $name" else "حان الآن وقت $name"
        val notification: Notification = NotificationCompat.Builder(this, "adhan")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(if (reminder) "متبقي 10 دقائق على الأذان" else "روضة المسلم")
            .setOngoing(!reminder)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        startForeground(7, notification)
        if (!reminder) {
            playAsset(audio)
        } else {
            if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_DETACH) else @Suppress("DEPRECATION") stopForeground(false)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun playAsset(file: String) {
        try {
            player?.release()
            val afd = assets.openFd("www/$file")
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                setOnCompletionListener { stopSelf() }
                prepare(); start()
            }
            afd.close()
        } catch (_: Exception) { stopSelf() }
    }

    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
