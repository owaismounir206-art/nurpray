package com.nurpray.app.domain.usecase

import com.nurpray.app.data.astronomical.CalculationParameters
import com.nurpray.app.data.astronomical.PrayerCalculationEngine
import com.nurpray.app.domain.model.PrayerTime
import com.nurpray.app.domain.model.PrayerType
import com.nurpray.app.domain.model.TodayPrayerSchedule
import java.time.*

class GetTodayPrayerTimesUseCase(
    private val engine: PrayerCalculationEngine = PrayerCalculationEngine()
) {

    operator fun invoke(
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId = ZoneId.systemDefault(),
        date: LocalDate = LocalDate.now(zoneId),
        now: LocalTime = LocalTime.now(zoneId),
        params: CalculationParameters = CalculationParameters()
    ): TodayPrayerSchedule {
        val result = engine.calculatePrayerTimes(date, latitude, longitude, zoneId, params)

        val prayerTimes = listOf(
            PrayerTime(PrayerType.FAJR, result.fajr),
            PrayerTime(PrayerType.SUNRISE, result.sunrise),
            PrayerTime(PrayerType.DHUHR, result.dhuhr),
            PrayerTime(PrayerType.ASR, result.asr),
            PrayerTime(PrayerType.MAGHRIB, result.maghrib),
            PrayerTime(PrayerType.ISHA, result.isha)
        )

        // Find current and next prayer
        val nextPrayerIndex = prayerTimes.indexOfFirst { it.time.isAfter(now) }

        val currentPrayer: PrayerTime?
        val nextPrayer: PrayerTime
        val progressRatio: Float
        val timeRemainingMillis: Long

        if (nextPrayerIndex == -1) {
            // After Isha -> Current is Isha, Next is tomorrow's Fajr
            currentPrayer = prayerTimes.last()
            val tomorrowResult = engine.calculatePrayerTimes(date.plusDays(1), latitude, longitude, zoneId, params)
            nextPrayer = PrayerTime(PrayerType.FAJR, tomorrowResult.fajr)

            val currentDateTime = LocalDateTime.of(date, currentPrayer.time)
            val nextDateTime = LocalDateTime.of(date.plusDays(1), nextPrayer.time)
            val nowDateTime = LocalDateTime.of(date, now)

            val totalDuration = Duration.between(currentDateTime, nextDateTime).toMillis()
            val elapsedDuration = Duration.between(currentDateTime, nowDateTime).toMillis()
            progressRatio = if (totalDuration > 0) (elapsedDuration.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f
            timeRemainingMillis = Duration.between(nowDateTime, nextDateTime).toMillis().coerceAtLeast(0)
        } else if (nextPrayerIndex == 0) {
            // Before Fajr -> Current was yesterday's Isha, Next is today's Fajr
            val yesterdayResult = engine.calculatePrayerTimes(date.minusDays(1), latitude, longitude, zoneId, params)
            currentPrayer = PrayerTime(PrayerType.ISHA, yesterdayResult.isha)
            nextPrayer = prayerTimes[0]

            val currentDateTime = LocalDateTime.of(date.minusDays(1), currentPrayer.time)
            val nextDateTime = LocalDateTime.of(date, nextPrayer.time)
            val nowDateTime = LocalDateTime.of(date, now)

            val totalDuration = Duration.between(currentDateTime, nextDateTime).toMillis()
            val elapsedDuration = Duration.between(currentDateTime, nowDateTime).toMillis()
            progressRatio = if (totalDuration > 0) (elapsedDuration.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f
            timeRemainingMillis = Duration.between(nowDateTime, nextDateTime).toMillis().coerceAtLeast(0)
        } else {
            currentPrayer = prayerTimes[nextPrayerIndex - 1]
            nextPrayer = prayerTimes[nextPrayerIndex]

            val currentDateTime = LocalDateTime.of(date, currentPrayer.time)
            val nextDateTime = LocalDateTime.of(date, nextPrayer.time)
            val nowDateTime = LocalDateTime.of(date, now)

            val totalDuration = Duration.between(currentDateTime, nextDateTime).toMillis()
            val elapsedDuration = Duration.between(currentDateTime, nowDateTime).toMillis()
            progressRatio = if (totalDuration > 0) (elapsedDuration.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f
            timeRemainingMillis = Duration.between(nowDateTime, nextDateTime).toMillis().coerceAtLeast(0)
        }

        return TodayPrayerSchedule(
            date = date,
            prayers = prayerTimes,
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            progressRatio = progressRatio,
            timeRemainingMillis = timeRemainingMillis
        )
    }
}
