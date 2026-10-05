package com.nurpray.app.data.astronomical

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

/**
 * Pure Kotlin astronomical prayer time calculation engine.
 * Implements high-precision solar positioning and atmospheric refraction calculations
 * with zero Android SDK dependencies.
 */
class PrayerCalculationEngine {

    companion object {
        private const val SUNRISE_SUNSET_ZENITH = 90.8333 // 90° 50' standard refraction + disk radius
    }

    /**
     * Calculates prayer times for a given date, geographic coordinates, and calculation parameters.
     *
     * @param date Calendar date for calculation.
     * @param latitude North latitude in degrees (positive for North, negative for South).
     * @param longitude East longitude in degrees (positive for East, negative for West).
     * @param zoneId Timezone of the location.
     * @param params Calculation parameters (method, juristic Asr, high-latitude rule).
     * @return [PrayerTimesResult] with exact [LocalTime] for each prayer.
     */
    fun calculatePrayerTimes(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId,
        params: CalculationParameters = CalculationParameters()
    ): PrayerTimesResult {
        val zonedDateTime = ZonedDateTime.of(date, LocalTime.NOON, zoneId)
        val timezoneOffsetHours = zonedDateTime.offset.totalSeconds / 3600.0

        // Step 1: Julian Day at 12:00 UTC
        val jd = calculateJulianDay(date.year, date.monthValue, date.dayOfMonth)

        // Step 2: Solar coordinates
        val solarCoords = calculateSolarCoordinates(jd)
        val declination = solarCoords.declinationDegrees
        val eot = solarCoords.equationOfTimeMinutes

        // Step 3: Base Solar Noon (Dhuhr)
        // Midday (Transit) = 12 + Timezone - Longitude/15 - EquationOfTime/60
        val baseTransitHours = 12.0 + timezoneOffsetHours - (longitude / 15.0) - (eot / 60.0)
        val dhuhrHours = baseTransitHours + (params.dhuhrSafetyMinutes / 60.0)

        // Step 4: Sunrise and Sunset
        val sunriseSunsetOffset = calculateHourAngleOffset(latitude, declination, SUNRISE_SUNSET_ZENITH)
        val sunriseHours = baseTransitHours - sunriseSunsetOffset
        val sunsetHours = baseTransitHours + sunriseSunsetOffset

        // Step 5: Asr (Shadow ratio 1:1 for Shafi'i/standard, 2:1 for Hanafi)
        val asrShadowMultiplier = params.asrJuristicMethod.shadowMultiplier
        val asrZenith = calculateAsrZenith(latitude, declination, asrShadowMultiplier)
        val asrOffset = calculateHourAngleOffset(latitude, declination, asrZenith)
        val asrHours = baseTransitHours + asrOffset

        // Step 6: Fajr
        val fajrAngle = params.fajrCustomAngle ?: params.method.fajrAngle
        val fajrZenith = 90.0 + fajrAngle
        val rawFajrOffset = calculateHourAngleOffset(latitude, declination, fajrZenith)
        var fajrHours = baseTransitHours - rawFajrOffset

        // Step 7: Maghrib and Isha
        val maghribHours = sunsetHours + (params.maghribSafetyMinutes / 60.0)

        val ishaHours: Double = when {
            params.ishaCustomMinutes != null -> {
                maghribHours + (params.ishaCustomMinutes / 60.0)
            }
            params.method.ishaMinutesAfterMaghrib != null -> {
                maghribHours + (params.method.ishaMinutesAfterMaghrib / 60.0)
            }
            else -> {
                val ishaAngle = params.ishaCustomAngle ?: params.method.ishaAngle ?: 17.0
                val ishaZenith = 90.0 + ishaAngle
                val rawIshaOffset = calculateHourAngleOffset(latitude, declination, ishaZenith)
                baseTransitHours + rawIshaOffset
            }
        }

        // Step 8: Apply High Latitude Adjustments if necessary
        val nightHours = if (sunsetHours < sunriseHours + 24.0) {
            (24.0 - sunsetHours) + sunriseHours
        } else {
            24.0 - (sunsetHours - sunriseHours)
        }

        val adjustedFajrAndIsha = applyHighLatitudeAdjustment(
            fajrHours = fajrHours,
            ishaHours = ishaHours,
            sunriseHours = sunriseHours,
            sunsetHours = sunsetHours,
            nightHours = nightHours,
            fajrAngle = fajrAngle,
            ishaAngle = params.ishaCustomAngle ?: params.method.ishaAngle ?: 17.0,
            rule = params.highLatitudeRule
        )

        fajrHours = adjustedFajrAndIsha.first
        val finalIshaHours = adjustedFajrAndIsha.second

        return PrayerTimesResult(
            date = date,
            fajr = decimalHoursToLocalTime(fajrHours),
            sunrise = decimalHoursToLocalTime(sunriseHours),
            dhuhr = decimalHoursToLocalTime(dhuhrHours),
            asr = decimalHoursToLocalTime(asrHours),
            maghrib = decimalHoursToLocalTime(maghribHours),
            isha = decimalHoursToLocalTime(finalIshaHours)
        )
    }

    /**
     * Calculates the Julian Day for a given Gregorian date and UT hour (default 12:00 UT).
     */
    fun calculateJulianDay(year: Int, month: Int, day: Int, hourOfDay: Double = 12.0): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val dayFraction = day.toDouble() + (hourOfDay / 24.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + dayFraction + b - 1524.5
    }

    /**
     * Computes solar coordinates: declination (degrees) and Equation of Time (minutes).
     * Algorithm based on Jean Meeus' Astronomical Algorithms.
     */
    fun calculateSolarCoordinates(julianDay: Double): SolarCoordinates {
        val t = (julianDay - 2451545.0) / 36525.0 // Julian Century

        // Geometric Mean Longitude of Sun (deg)
        val l0 = fixAngle(280.46646 + t * (36000.76983 + 0.0003032 * t))

        // Mean Anomaly of Sun (deg)
        val m = fixAngle(357.52911 + t * (35999.05029 - 0.0001537 * t))
        val mRad = Math.toRadians(m)

        // Equation of Center (deg)
        val c = (1.914602 - t * (0.004817 + 0.000014 * t)) * sin(mRad) +
                (0.019993 - 0.000101 * t) * sin(2.0 * mRad) +
                0.000289 * sin(3.0 * mRad)

        // Sun True Longitude (deg)
        val sunTrueLong = l0 + c

        // Apparent Longitude (deg)
        val omega = fixAngle(125.04 - 1934.136 * t)
        val omegaRad = Math.toRadians(omega)
        val lambda = sunTrueLong - 0.00569 - 0.00478 * sin(omegaRad)
        val lambdaRad = Math.toRadians(lambda)

        // Mean Obliquity of Ecliptic (deg)
        val u = t / 100.0
        val eps0 = 23.0 + 26.0 / 60.0 + 21.448 / 3600.0 -
                (4680.93 / 3600.0) * u -
                (1.55 / 3600.0) * u * u +
                (1999.25 / 3600.0) * u * u * u
        val eps = eps0 + 0.00256 * cos(omegaRad)
        val epsRad = Math.toRadians(eps)

        // Sun Declination
        val sinDeclination = sin(epsRad) * sin(lambdaRad)
        val declinationDegrees = Math.toDegrees(asin(sinDeclination.coerceIn(-1.0, 1.0)))

        // Sun Right Ascension (alpha in degrees)
        val alpha = Math.toDegrees(atan2(cos(epsRad) * sin(lambdaRad), cos(lambdaRad)))
        val alphaFixed = fixAngle(alpha)

        // Equation of Time (EoT) in minutes
        // EoT = (L0 - alpha) * 4 minutes per degree
        var eotDeg = l0 - alphaFixed
        if (eotDeg > 180.0) eotDeg -= 360.0
        if (eotDeg < -180.0) eotDeg += 360.0
        val equationOfTimeMinutes = eotDeg * 4.0

        return SolarCoordinates(
            declinationDegrees = declinationDegrees,
            equationOfTimeMinutes = equationOfTimeMinutes,
            apparentSolarNoonHours = 12.0 - (equationOfTimeMinutes / 60.0)
        )
    }

    /**
     * Calculates the hour angle offset in hours for a given zenith angle.
     * cos(H) = (cos(zenith) - sin(lat) * sin(dec)) / (cos(lat) * cos(dec))
     */
    private fun calculateHourAngleOffset(latitude: Double, declination: Double, zenithDegrees: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val zenithRad = Math.toRadians(zenithDegrees)

        val numerator = cos(zenithRad) - (sin(latRad) * sin(decRad))
        val denominator = cos(latRad) * cos(decRad)

        if (denominator == 0.0) return 0.0

        val cosH = (numerator / denominator).coerceIn(-1.0, 1.0)
        val hourAngleDeg = Math.toDegrees(acos(cosH))
        return hourAngleDeg / 15.0 // Convert degrees to decimal hours
    }

    /**
     * Calculates the zenith angle for Asr based on shadow length.
     * altitude = arccot(multiplier + tan(|lat - dec|))
     * zenith = 90° - altitude
     */
    private fun calculateAsrZenith(latitude: Double, declination: Double, shadowMultiplier: Double): Double {
        val latDecDiffRad = Math.toRadians(abs(latitude - declination))
        val tanDiff = tan(latDecDiffRad)
        val cotAltitude = shadowMultiplier + tanDiff
        val altitudeRad = atan(1.0 / cotAltitude)
        return 90.0 - Math.toDegrees(altitudeRad)
    }

    /**
     * Adjusts Fajr and Isha when night is too short or sun does not dip sufficiently.
     */
    private fun applyHighLatitudeAdjustment(
        fajrHours: Double,
        ishaHours: Double,
        sunriseHours: Double,
        sunsetHours: Double,
        nightHours: Double,
        fajrAngle: Double,
        ishaAngle: Double,
        rule: HighLatitudeRule
    ): Pair<Double, Double> {
        if (rule == HighLatitudeRule.NONE) {
            return Pair(fajrHours, ishaHours)
        }

        val fajrPortion = when (rule) {
            HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5 * nightHours
            HighLatitudeRule.ONE_SEVENTH -> (1.0 / 7.0) * nightHours
            HighLatitudeRule.ANGLE_BASED -> (fajrAngle / 60.0) * nightHours
            HighLatitudeRule.NONE -> 0.0
        }

        val ishaPortion = when (rule) {
            HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5 * nightHours
            HighLatitudeRule.ONE_SEVENTH -> (1.0 / 7.0) * nightHours
            HighLatitudeRule.ANGLE_BASED -> (ishaAngle / 60.0) * nightHours
            HighLatitudeRule.NONE -> 0.0
        }

        var adjustedFajr = fajrHours
        var adjustedIsha = ishaHours

        // If Fajr is earlier than the allowed limit from sunrise, cap it
        val fajrLimit = sunriseHours - fajrPortion
        if (adjustedFajr < fajrLimit || adjustedFajr.isNaN()) {
            adjustedFajr = fajrLimit
        }

        // If Isha is later than the allowed limit from sunset, cap it
        val ishaLimit = sunsetHours + ishaPortion
        if (adjustedIsha > ishaLimit || adjustedIsha.isNaN()) {
            adjustedIsha = ishaLimit
        }

        return Pair(adjustedFajr, adjustedIsha)
    }

    private fun fixAngle(deg: Double): Double {
        var angle = deg % 360.0
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun decimalHoursToLocalTime(hours: Double): LocalTime {
        var normalized = hours % 24.0
        if (normalized < 0) normalized += 24.0

        val h = floor(normalized).toInt()
        val remainderMinutes = (normalized - h) * 60.0
        val m = floor(remainderMinutes).toInt()
        val s = floor((remainderMinutes - m) * 60.0).toInt().coerceIn(0, 59)

        return LocalTime.of(h.coerceIn(0, 23), m.coerceIn(0, 59), s)
    }
}
