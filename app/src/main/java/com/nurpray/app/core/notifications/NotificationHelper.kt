package com.nurpray.app.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nurpray.app.MainActivity
import com.nurpray.app.R
import com.nurpray.app.domain.model.PrayerType

/**
 * Handles creation of Notification Channels and High-Priority Prayer Notifications.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ADHAN = "channel_adhan_high_priority"
        const val CHANNEL_PRE_ADHAN = "channel_pre_adhan"
        const val CHANNEL_TASBIH = "channel_tasbih"

        const val NOTIFICATION_ID_ADHAN_BASE = 1000
        const val NOTIFICATION_ID_PRE_ADHAN_BASE = 2000
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // High Priority Adhan Channel (Sound + Vibration + Heads-up)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val adhanChannel = NotificationChannel(
                CHANNEL_ADHAN,
                context.getString(R.string.adhan_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.adhan_channel_desc)
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Pre-Adhan Channel (Gentle reminder)
            val preAdhanChannel = NotificationChannel(
                CHANNEL_PRE_ADHAN,
                context.getString(R.string.pre_adhan_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.pre_adhan_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            notificationManager.createNotificationChannel(adhanChannel)
            notificationManager.createNotificationChannel(preAdhanChannel)
        }
    }

    /**
     * Builds and shows a High-Priority heads-up notification for the Adhan.
     */
    fun showAdhanNotification(prayerType: PrayerType, prayerTimeFormatted: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerType.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ADHAN)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Orario di ${prayerType.displayName}")
            .setContentText("È giunto l'orario della preghiera ($prayerTimeFormatted). حي على الصلاة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ADHAN_BASE + prayerType.ordinal, notification)
    }

    /**
     * Shows a gentle pre-adhan notification (e.g. 15 minutes before).
     */
    fun showPreAdhanNotification(prayerType: PrayerType, minutesRemaining: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerType.ordinal + 50,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PRE_ADHAN)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tra $minutesRemaining minuti: ${prayerType.displayName}")
            .setContentText("Preparati per la preghiera con l'abluzione (Wudu).")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_PRE_ADHAN_BASE + prayerType.ordinal, notification)
    }
}
