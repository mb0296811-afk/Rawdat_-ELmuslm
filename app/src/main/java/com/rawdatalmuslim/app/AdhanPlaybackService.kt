
package com.rawdatalmuslim.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.content.Context

class AdhanService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val CHANNEL_ID = "adhan_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val name = intent?.getStringExtra("name") ?: "الصلاة"
        val audio = intent?.getStringExtra("audio")
            ?: "اذان عبدالباسط.mp3"

        val reminder = intent?.getBooleanExtra("reminder", false)
            ?: false

        val title = if (reminder) {
            "اقترب موعد الصلاة"
        } else {
            "حان وقت الصلاة"
        }

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(name)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(!reminder)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        stopAudio()

        if (reminder) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            val fileName = audio.substringBeforeLast(".")
            val resourceId = resources.getIdentifier(
                fileName,
                "raw",
                packageName
            )

            if (resourceId == 0) {
                stopSelf()
                return START_NOT_STICKY
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                val descriptor = resources.openRawResourceFd(resourceId)
                    ?: throw IllegalStateException("Audio file not found")

                setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length
                )

                descriptor.close()

                setOnCompletionListener {
                    stopAudio()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                setOnErrorListener { _, _, _ ->
                    stopAudio()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    true
                }

                prepare()
                start()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            stopAudio()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun stopAudio() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (_: Exception) {
            }
        }
        mediaPlayer = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تنبيهات الأذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات مواعيد الصلاة والأذان"
            }

            val manager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAudio()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
