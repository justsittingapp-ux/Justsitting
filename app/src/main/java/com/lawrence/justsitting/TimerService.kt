package com.lawrence.justsitting

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            cleanupAndStop()
            return START_NOT_STICKY
        }

        // Pornim Foreground imediat (Android 12+ cere asta)
        val notification = NotificationCompat.Builder(this, "timer_ch")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Just Sitting")
            .setContentText("Sesiunea de meditație este activă")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        // Activăm WakeLock-ul în siguranță
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "JustSitting::ServiceWakeLock")
        }

        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire(120 * 60 * 1000L)
        }

        return START_NOT_STICKY
    }

    private fun cleanupAndStop() {
        // 1. Resetăm DND (Do Not Disturb) la normal
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (nm.isNotificationPolicyAccessGranted) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Eliberăm WakeLock
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Eliminăm notificarea și oprim serviciul
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Apelat când utilizatorul închide aplicația cu swipe-out
        cleanupAndStop()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        cleanupAndStop()
        super.onDestroy()
    }
}