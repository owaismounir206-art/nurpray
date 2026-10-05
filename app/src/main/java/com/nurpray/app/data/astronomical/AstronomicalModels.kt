package com.nurpray.app.data.astronomical

import java.time.LocalDate
import java.time.LocalTime

/**
 * Calculation conventions recognized worldwide.
 */
enum class PrayerMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double? = null,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    MUSLIM_WORLD_LEAGUE("Muslim World League (MWL)", 18.0, 17.0),
    ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
    EGYPT("Egyptian General Authority of Survey", 19.5, 17.5),
    UMM_AL_QURA("Umm Al-Qura University, Makkah", 18.5, ishaMinutesAfterMaghrib = 90),
    KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0),
    UOIF_FRANCE("UOIF (Francia - 12°)", 12.0, 12.0),
    DIYANET_TURKEY("Diyanet İşleri Başkanlığı (Turchia)", 18.0, 17.0),
    TEHRAN("Institute of Geophysics, Univ. of Tehran", 17.7, 14.0),
    CUSTOM("Personalizzato", 18.0, 17.0)
}

/**
 * Juristic method for Asr prayer based on shadow length ratio.
 */
enum class AsrJuristicMethod(val shadowMultiplier: Double) {
    SHAFI_MALIKI_HANBALI(1.0),
    HANAFI(2.0)
}

/**
 * High latitude compensation rule for locations near or above the Arctic/Antarctic circles.
 */
enum class HighLatitudeRule {
    MIDDLE_OF_NIGHT,
    ONE_SEVENTH,
    ANGLE_BASED,
    NONE
}

/**
 * Calculation parameter container.
 */
data class CalculationParameters(
    val method: PrayerMethod = PrayerMethod.MUSLIM_WORLD_LEAGUE,
    val asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.SHAFI_MALIKI_HANBALI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
    val elevationMeters: Double = 0.0,
    val fajrCustomAngle: Double? = null,
    val ishaCustomAngle: Double? = null,
    val ishaCustomMinutes: Int? = null,
    val dhuhrSafetyMinutes: Int = 1,
    val maghribSafetyMinutes: Int = 2
)

/**
 * Coordinates and declination of the Sun at a given Julian day.
 */
data class SolarCoordinates(
    val declinationDegrees: Double,
    val equationOfTimeMinutes: Double,
    val apparentSolarNoonHours: Double
)

/**
 * Resulting prayer times for a single calendar day.
 */
data class PrayerTimesResult(
    val date: LocalDate,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime
)
