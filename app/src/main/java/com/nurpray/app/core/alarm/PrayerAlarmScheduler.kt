package com.nurpray.app.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.nurpray.app.MainActivity
import com.nurpray.app.data.astronomical.CalculationParameters
import com.nurpray.app.data.astronomical.PrayerCalculationEngine
import com.nurpray.app.domain.model.PrayerType
import com.nurpray.app.feature.alarm.PrayerAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Robust Alarm Scheduler using AlarmManager.setAlarmClock()
 * to guarantee exact execution even during Deep Doze mode.
 */
class PrayerAlarmScheduler(
    private val context: Context,
    private val calculationEngine: PrayerCalculationEngine = PrayerCalculationEngine()
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_IS_PRE_ALARM = "extra_is_pre_alarm"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
    }

    /**
     * Schedules exact alarms for the upcoming week for all prayers.
     */
    fun scheduleWeekAlarms(
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId,
        params: CalculationParameters,
        preAlarmMinutes: Int = 15
    ) {
        val today = LocalDate.now(zoneId)
        val now = LocalDateTime.now(zoneId)

        // Schedule for the next 7 days
        for (dayOffset in 0..6) {
            val targetDate = today.plusDays(dayOffset.toLong())
            val times = calculationEngine.calculatePrayerTimes(targetDate, latitude, longitude, zoneId, params)

            val prayerList = listOf(
                PrayerType.FAJR to times.fajr,
                PrayerType.DHUHR to times.dhuhr,
                PrayerType.ASR to times.asr,
                PrayerType.MAGHRIB to times.maghrib,
                PrayerType.ISHA to times.isha
            )

            for ((prayerType, localTime) in prayerList) {
                val prayerDateTime = LocalDateTime.of(targetDate, localTime)

                // Only schedule if in the future
                if (prayerDateTime.isAfter(now)) {
                    val epochMillis = prayerDateTime.atZone(zoneId).toInstant().toEpochMilli()
                    scheduleExactAlarm(
                        epochMillis = epochMillis,
                        prayerType = prayerType,
                        timeFormatted = localTime.toString(),
                        requestCode = generateRequestCode(dayOffset, prayerType.ordinal, false)
                    )

                    // Optional Pre-Adhan alarm
                    if (preAlarmMinutes > 0) {
                        val preAlarmDateTime = prayerDateTime.minusMinutes(preAlarmMinutes.toLong())
                        if (preAlarmDateTime.isAfter(now)) {
                            val preEpochMillis = preAlarmDateTime.atZone(zoneId).toInstant().toEpochMilli()
                            schedulePreAlarm(
                                epochMillis = preEpochMillis,
                                prayerType = prayerType,
                                minutesBefore = preAlarmMinutes,
                                requestCode = generateRequestCode(dayOffset, prayerType.ordinal, true)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun scheduleExactAlarm(
        epochMillis: Long,
        prayerType: PrayerType,
        timeFormatted: String,
        requestCode: Int
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
            putExtra(EXTRA_PRAYER_TIME, timeFormatted)
            putExtra(EXTRA_IS_PRE_ALARM, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // AlarmClockInfo guarantees waking up from Doze mode
        val alarmClockInfo = AlarmManager.AlarmClockInfo(epochMillis, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    private fun schedulePreAlarm(
        epochMillis: Long,
        prayerType: PrayerType,
        minutesBefore: Int,
        requestCode: Int
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_PRE_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
            putExtra(EXTRA_MINUTES_BEFORE, minutesBefore)
            putExtra(EXTRA_IS_PRE_ALARM, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pre-alarm uses setExactAndAllowWhileIdle
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            epochMillis,
            pendingIntent
        )
    }

    /**
     * Cancels all scheduled alarms.
     */
    fun cancelAllAlarms() {
        for (day in 0..6) {
            for (prayerIndex in 0..5) {
                cancelAlarm(generateRequestCode(day, prayerIndex, false))
                cancelAlarm(generateRequestCode(day, prayerIndex, true))
            }
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun generateRequestCode(dayOffset: Int, prayerOrdinal: Int, isPreAlarm: Boolean): Int {
        val base = (dayOffset * 100) + (prayerOrdinal * 10)
        return if (isPreAlarm) base + 1 else base
    }
}
