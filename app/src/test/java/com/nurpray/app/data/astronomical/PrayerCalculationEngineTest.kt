package com.nurpray.app.data.astronomical

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerCalculationEngineTest {

    private lateinit var engine: PrayerCalculationEngine

    @Before
    fun setUp() {
        engine = PrayerCalculationEngine()
    }

    @Test
    fun testJulianDayCalculation() {
        // Standard astronomical benchmark: 2000-01-01 at 12:00 UT is JD 2451545.0
        val jd = engine.calculateJulianDay(2000, 1, 1)
        assertEquals(2451545.0, jd, 0.0001)
    }

    @Test
    fun testSolarCoordinates() {
        // At J2000.0 epoch
        val coords = engine.calculateSolarCoordinates(2451545.0)
        // Around Jan 1, Sun declination is approximately -23°
        assertTrue("Declination should be around -23 degrees", coords.declinationDegrees in -23.5..-22.5)
        // Equation of time should be around -3 to -4 minutes in early January
        assertTrue("EoT should be between -4 and -2 minutes", coords.equationOfTimeMinutes in -4.5..-2.0)
    }

    @Test
    fun testMakkahPrayerTimes() {
        val date = LocalDate.of(2026, 10, 5)
        val lat = 21.4225
        val lon = 39.8262
        val zone = ZoneId.of("Asia/Riyadh")

        val params = CalculationParameters(
            method = PrayerMethod.UMM_AL_QURA,
            asrJuristicMethod = AsrJuristicMethod.SHAFI_MALIKI_HANBALI
        )

        val result = engine.calculatePrayerTimes(date, lat, lon, zone, params)

        assertNotNull(result)
        // Fajr around 4:50 - 5:10
        assertTrue("Fajr in Makkah in Oct should be around 04:50-05:15", result.fajr.hour in 4..5)
        // Sunrise around 6:00 - 6:20
        assertEquals(6, result.sunrise.hour)
        // Dhuhr around 12:00 - 12:20
        assertEquals(12, result.dhuhr.hour)
        // Asr around 15:20 - 15:40
        assertEquals(15, result.asr.hour)
        // Maghrib around 18:00 - 18:20
        assertEquals(18, result.maghrib.hour)
        // Isha 90 minutes after Maghrib (around 19:30 - 19:50)
        assertEquals(19, result.isha.hour)

        // Strict chronological progression
        assertTrue(result.fajr.isBefore(result.sunrise))
        assertTrue(result.sunrise.isBefore(result.dhuhr))
        assertTrue(result.dhuhr.isBefore(result.asr))
        assertTrue(result.asr.isBefore(result.maghrib))
        assertTrue(result.maghrib.isBefore(result.isha))
    }

    @Test
    fun testRomePrayerTimesMWL() {
        val date = LocalDate.of(2026, 10, 5)
        val lat = 41.9028
        val lon = 12.4964
        val zone = ZoneId.of("Europe/Rome")

        val params = CalculationParameters(
            method = PrayerMethod.MUSLIM_WORLD_LEAGUE,
            asrJuristicMethod = AsrJuristicMethod.SHAFI_MALIKI_HANBALI
        )

        val result = engine.calculatePrayerTimes(date, lat, lon, zone, params)

        assertNotNull(result)
        assertTrue(result.fajr.isBefore(result.sunrise))
        assertTrue(result.sunrise.isBefore(result.dhuhr))
        assertTrue(result.dhuhr.isBefore(result.asr))
        assertTrue(result.asr.isBefore(result.maghrib))
        assertTrue(result.maghrib.isBefore(result.isha))
    }

    @Test
    fun testHanafiAsrIsLaterThanShafi() {
        val date = LocalDate.of(2026, 10, 5)
        val lat = 24.8607
        val lon = 67.0011
        val zone = ZoneId.of("Asia/Karachi")

        val shafiParams = CalculationParameters(
            method = PrayerMethod.KARACHI,
            asrJuristicMethod = AsrJuristicMethod.SHAFI_MALIKI_HANBALI
        )
        val hanafiParams = CalculationParameters(
            method = PrayerMethod.KARACHI,
            asrJuristicMethod = AsrJuristicMethod.HANAFI
        )

        val shafiResult = engine.calculatePrayerTimes(date, lat, lon, zone, shafiParams)
        val hanafiResult = engine.calculatePrayerTimes(date, lat, lon, zone, hanafiParams)

        assertTrue(
            "Hanafi Asr must be strictly after Shafi Asr",
            hanafiResult.asr.isAfter(shafiResult.asr)
        )
    }

    @Test
    fun testHighLatitudeLondonSummerSolstice() {
        // June 21 in London (high latitude twilight)
        val date = LocalDate.of(2026, 6, 21)
        val lat = 51.5074
        val lon = -0.1278
        val zone = ZoneId.of("Europe/London")

        val params = CalculationParameters(
            method = PrayerMethod.MUSLIM_WORLD_LEAGUE,
            highLatitudeRule = HighLatitudeRule.ANGLE_BASED
        )

        val result = engine.calculatePrayerTimes(date, lat, lon, zone, params)

        assertNotNull(result)
        assertTrue("Fajr must be before sunrise even at high latitude", result.fajr.isBefore(result.sunrise))
        assertTrue("Isha must be after maghrib even at high latitude", result.isha.isAfter(result.maghrib))
    }
}
