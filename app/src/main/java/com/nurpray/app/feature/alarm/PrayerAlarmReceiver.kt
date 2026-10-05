package com.nurpray.app.feature.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.nurpray.app.core.alarm.PrayerAlarmScheduler
import com.nurpray.app.core.notifications.NotificationHelper
import com.nurpray.app.domain.model.PrayerType

/**
 * BroadcastReceiver triggered by AlarmManager.
 * Acquires a partial wake lock to guarantee processing during sleep.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALARM = "com.nurpray.app.ACTION_PRAYER_ALARM"
        const val ACTION_PRE_PRAYER_ALARM = "com.nurpray.app.ACTION_PRE_PRAYER_ALARM"
        private const val WAKE_LOCK_TAG = "nurpray:PrayerAlarmReceiverWakeLock"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            setReferenceCounted(false)
            acquire(15_000) // 15 seconds max safety timeout
        }

        try {
            val notificationHelper = NotificationHelper(context)
            val prayerTypeName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TYPE) ?: PrayerType.FAJR.name
            val prayerType = try {
                PrayerType.valueOf(prayerTypeName)
            } catch (e: Exception) {
                PrayerType.FAJR
            }

            val isPreAlarm = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_PRE_ALARM, false)

            if (isPreAlarm) {
                val minutesBefore = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_MINUTES_BEFORE, 15)
                notificationHelper.showPreAdhanNotification(prayerType, minutesBefore)
            } else {
                val prayerTimeFormatted = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TIME) ?: ""
                notificationHelper.showAdhanNotification(prayerType, prayerTimeFormatted)

                // Trigger Adhan Audio Playback Service
                val serviceIntent = Intent(context, AdhanAudioPlayerService::class.java).apply {
                    action = AdhanAudioPlayerService.ACTION_PLAY_ADHAN
                    putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TYPE, prayerType.name)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
