package com.nurpray.app.data.astronomical

import java.time.LocalDate
import java.time.LocalTime

/**
 * Calculation conventions recognized worldwide by official Islamic authorities.
 */
enum class PrayerMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double? = null,
    val ishaMinutesAfterMaghrib: Int? = null,
    val maghribAngle: Double? = null // For Shia conventions (Tehran/Qum)
) {
    UCOII_ITALY(
        title = "UCOII (Italia / Europa - Fajr 12°, Isha 90-100 min)",
        fajrAngle = 12.0,
        ishaMinutesAfterMaghrib = 90
    ),
    UOIF_FRANCE(
        title = "UOIF / Musulmans de France (12° / 12°)",
        fajrAngle = 12.0,
        ishaAngle = 12.0
    ),
    ECFR_EUROPE(
        title = "European Council for Fatwa and Research (ECFR - 18° / 15°)",
        fajrAngle = 18.0,
        ishaAngle = 15.0
    ),
    MUSLIM_WORLD_LEAGUE(
        title = "Muslim World League (MWL - 18° / 17°)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    ISNA(
        title = "Islamic Society of North America (ISNA - 15° / 15°)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    EGYPT(
        title = "Egyptian General Authority of Survey (19.5° / 17.5°)",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    UMM_AL_QURA(
        title = "Umm Al-Qura University, Makkah (18.5° / 90 min)",
        fajrAngle = 18.5,
        ishaMinutesAfterMaghrib = 90
    ),
    KARACHI(
        title = "University of Islamic Sciences, Karachi (18° / 18°)",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    DIYANET_TURKEY(
        title = "Diyanet İşleri Başkanlığı (Turchia - 18° / 17°)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    GULF_UAE(
        title = "General Authority of Islamic Affairs, UAE (18.2° / 90 min)",
        fajrAngle = 18.2,
        ishaMinutesAfterMaghrib = 90
    ),
    KUWAIT(
        title = "Ministry of Awqaf, Kuwait (18° / 17.5°)",
        fajrAngle = 18.0,
        ishaAngle = 17.5
    ),
    QATAR(
        title = "Ministry of Awqaf, Qatar (18° / 90 min)",
        fajrAngle = 18.0,
        ishaMinutesAfterMaghrib = 90
    ),
    SINGAPORE_MUIS(
        title = "MUIS (Singapore - 20° / 18°)",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    ),
    JAKIM_MALAYSIA(
        title = "JAKIM (Malesia - 20° / 18°)",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    ),
    KEMENAG_INDONESIA(
        title = "KEMENAG (Indonesia - 20° / 18°)",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    ),
    MOONSIGHTING_UK(
        title = "Moonsighting Committee Worldwide / UK (18° / 18°)",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    TEHRAN(
        title = "Institute of Geophysics, Univ. of Tehran (17.7° / 14°)",
        fajrAngle = 17.7,
        ishaAngle = 14.0,
        maghribAngle = 4.5
    ),
    SHIA_ITHNA_ASHARI(
        title = "Shia Ithna Ashari / Leva Institute, Qum (16° / 14°)",
        fajrAngle = 16.0,
        ishaAngle = 14.0,
        maghribAngle = 4.0
    ),
    CUSTOM(
        title = "Personalizzato (Angoli ed intervalli liberi)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    )
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
 * Fine-tuning adjustments in minutes for each prayer (e.g. ±1 to ±15 min)
 * to match exact printed mosque timetables.
 */
data class PrayerAdjustments(
    val fajrMinutes: Int = 0,
    val sunriseMinutes: Int = 0,
    val dhuhrMinutes: Int = 0,
    val asrMinutes: Int = 0,
    val maghribMinutes: Int = 0,
    val ishaMinutes: Int = 0
)

/**
 * Calculation parameter container.
 */
data class CalculationParameters(
    val method: PrayerMethod = PrayerMethod.UCOII_ITALY,
    val asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.SHAFI_MALIKI_HANBALI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
    val elevationMeters: Double = 0.0,
    val fajrCustomAngle: Double? = null,
    val ishaCustomAngle: Double? = null,
    val ishaCustomMinutes: Int? = null,
    val dhuhrSafetyMinutes: Int = 2,
    val maghribSafetyMinutes: Int = 2,
    val adjustments: PrayerAdjustments = PrayerAdjustments()
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
