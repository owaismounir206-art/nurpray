package com.nurpray.app.feature.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nurpray.app.MainActivity
import com.nurpray.app.R
import com.nurpray.app.core.notifications.NotificationHelper
import com.nurpray.app.core.alarm.PrayerAlarmScheduler

/**
 * Foreground Service for Adhan audio playback with audio focus and stop control.
 */
class AdhanAudioPlayerService : Service() {

    companion object {
        const val ACTION_PLAY_ADHAN = "com.nurpray.app.ACTION_PLAY_ADHAN"
        const val ACTION_STOP_ADHAN = "com.nurpray.app.ACTION_STOP_ADHAN"
        private const val FOREGROUND_SERVICE_ID = 9999
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ADHAN -> {
                stopAdhan()
                stopSelf()
            }
            ACTION_PLAY_ADHAN -> {
                val prayerType = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TYPE) ?: "Preghiera"
                startForeground(FOREGROUND_SERVICE_ID, createPlayingNotification(prayerType))
                playAdhanSound()
            }
        }
        return START_NOT_STICKY
    }

    private fun playAdhanSound() {
        try {
            requestAudioFocus()

            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(applicationContext, notificationUri)
                prepare()
                setOnCompletionListener {
                    stopAdhan()
                    stopSelf()
                }
                start()
            }
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopAdhan()
                        stopSelf()
                    }
                }
                .build()

            audioManager?.requestAudioFocus(focusRequest!!)
        }
    }

    private fun stopAdhan() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
            audioManager?.abandonAudioFocusRequest(focusRequest!!)
        }
    }

    private fun createPlayingNotification(prayerName: String): Notification {
        val stopIntent = Intent(this, AdhanAudioPlayerService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ADHAN)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Adhan in corso - $prayerName")
            .setContentText("Tocca per aprire l'app o ferma l'audio.")
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Ferma", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onDestroy() {
        stopAdhan()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
