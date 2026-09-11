package com.linhavital.app.monitoring

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

object CheckInScheduler {
    const val CHANNEL_ID = "linha_vital_checkin"
    private const val REQUEST_CODE = 8801

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Check-ins de segurança",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes para confirmar que está tudo bem"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, delayMinutes: Long) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = SystemClock.elapsedRealtime() + delayMinutes.coerceAtLeast(1) * 60_000L
        val pending = pendingIntent(
            context,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            pending
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(
            context,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    fun isScheduled(context: Context): Boolean =
        pendingIntent(
            context,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) != null

    private fun pendingIntent(context: Context, flags: Int): PendingIntent? {
        val intent = Intent(context, CheckInReminderReceiver::class.java)
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }
}
