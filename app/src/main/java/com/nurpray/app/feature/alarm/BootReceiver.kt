package com.nurpray.app.feature.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nurpray.app.core.alarm.PrayerAlarmScheduler
import com.nurpray.app.data.astronomical.CalculationParameters
import com.nurpray.app.data.astronomical.PrayerMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId

/**
 * Reschedules all alarms upon system boot, timezone change, or app update.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received broadcast: $action")

        val validActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )

        if (action in validActions) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val scheduler = PrayerAlarmScheduler(context)
                    // In a production setup, read saved location and preferences from DataStore
                    // Default fallback to Rome / Europe
                    val lat = 41.9028
                    val lon = 12.4964
                    val zoneId = ZoneId.systemDefault()
                    val params = CalculationParameters(method = PrayerMethod.MUSLIM_WORLD_LEAGUE)

                    scheduler.cancelAllAlarms()
                    scheduler.scheduleWeekAlarms(lat, lon, zoneId, params)
                    Log.d("BootReceiver", "Successfully rescheduled prayer alarms")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
