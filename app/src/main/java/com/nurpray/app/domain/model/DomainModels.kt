package com.nurpray.app.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class PrayerType(
    val displayName: String,
    val arabicName: String,
    val isFardh: Boolean = true
) {
    FAJR("Fajr", "الفجر", true),
    SUNRISE("Alba (Shuruq)", "الشروق", false),
    DHUHR("Dhuhr", "الظهر", true),
    ASR("Asr", "العصر", true),
    MAGHRIB("Maghrib", "المغرب", true),
    ISHA("Isha", "العشاء", true)
}

data class PrayerTime(
    val type: PrayerType,
    val time: LocalTime,
    val isNotificationEnabled: Boolean = true
) {
    val formattedTime: String
        get() = time.format(DateTimeFormatter.ofPattern("HH:mm"))
}

data class TodayPrayerSchedule(
    val date: LocalDate,
    val prayers: List<PrayerTime>,
    val currentPrayer: PrayerTime?,
    val nextPrayer: PrayerTime,
    val progressRatio: Float, // 0.0 to 1.0 between current and next prayer
    val timeRemainingMillis: Long
)

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryName: String,
    val isAutomaticGps: Boolean = true
)

data class QiblaBearing(
    val qiblaDirectionDegrees: Float, // Absolute compass bearing to Mecca
    val deviceHeadingDegrees: Float,   // Device's current compass azimuth
    val relativeAngleDegrees: Float,   // Angle difference to rotate dial
    val distanceToKaabaKm: Double,     // Great circle distance
    val isAligned: Boolean             // True if pointing within ±3° of Kaaba
)

data class DhikrItem(
    val id: String,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val count: Int,
    val target: Int // 33, 99, 100, 0 for infinite
)
