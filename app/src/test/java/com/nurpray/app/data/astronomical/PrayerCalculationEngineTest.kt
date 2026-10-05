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

    @Test
    fun testPistoiaUcoiiConventionAutumn() {
        // Pistoia: Lat ~43.93° N, Lon ~10.92° E, Europe/Rome
        val lat = 43.93
        val lon = 10.92
        val zone = ZoneId.of("Europe/Rome")
        val params = CalculationParameters(
            method = PrayerMethod.UCOII_ITALY,
            asrJuristicMethod = AsrJuristicMethod.SHAFI_MALIKI_HANBALI
        )

        // 1. Test September 20 (late summer rule: Isha = Maghrib + 100 min, Fajr 12°)
        val sepDate = LocalDate.of(2026, 9, 20)
        val sepResult = engine.calculatePrayerTimes(sepDate, lat, lon, zone, params)

        assertNotNull(sepResult)
        // In late September, sunrise in Pistoia is around 07:01-07:05 CEST
        assertEquals(7, sepResult.sunrise.hour)
        // Fajr (12° depression) is ~65-67 minutes before sunrise (~05:55-06:00)
        assertEquals(5, sepResult.fajr.hour)
        val minutesFajrToSunrise = java.time.Duration.between(sepResult.fajr, sepResult.sunrise).toMinutes()
        assertTrue("Fajr at 12° should be between 60 and 75 min before sunrise", minutesFajrToSunrise in 60..75)

        // Isha in summer/late Sep is Maghrib + 100 minutes
        val sepMaghribToIsha = java.time.Duration.between(sepResult.maghrib, sepResult.isha).toMinutes()
        assertEquals("Isha must be exactly 100 minutes after Maghrib up to late September", 100L, sepMaghribToIsha)

        // 2. Test October 1 (autumn rule: Isha = Maghrib + 90 min)
        val octDate = LocalDate.of(2026, 10, 1)
        val octResult = engine.calculatePrayerTimes(octDate, lat, lon, zone, params)

        assertNotNull(octResult)
        // On October 1st, actual astronomical sunrise in Pistoia is ~07:13-07:15 CEST
        assertEquals(7, octResult.sunrise.hour)
        assertTrue("Sunrise on Oct 1 in Pistoia is around 07:13", octResult.sunrise.minute in 10..18)

        // Isha switches to 90 minutes after Maghrib in October
        val octMaghribToIsha = java.time.Duration.between(octResult.maghrib, octResult.isha).toMinutes()
        assertEquals("Isha must be 90 minutes after Maghrib starting from late September/October", 90L, octMaghribToIsha)

        // Strict chronological progression
        assertTrue(octResult.fajr.isBefore(octResult.sunrise))
        assertTrue(octResult.sunrise.isBefore(octResult.dhuhr))
        assertTrue(octResult.dhuhr.isBefore(octResult.asr))
        assertTrue(octResult.asr.isBefore(octResult.maghrib))
        assertTrue(octResult.maghrib.isBefore(octResult.isha))
    }

    @Test
    fun testPrayerAdjustmentsFineTuning() {
        val date = LocalDate.of(2026, 10, 5)
        val lat = 43.93
        val lon = 10.92
        val zone = ZoneId.of("Europe/Rome")

        val baseParams = CalculationParameters(method = PrayerMethod.UCOII_ITALY)
        val baseResult = engine.calculatePrayerTimes(date, lat, lon, zone, baseParams)

        val adjustedParams = CalculationParameters(
            method = PrayerMethod.UCOII_ITALY,
            adjustments = PrayerAdjustments(
                fajrMinutes = 2,
                dhuhrMinutes = 3,
                asrMinutes = -1,
                maghribMinutes = 2,
                ishaMinutes = -5
            )
        )
        val adjustedResult = engine.calculatePrayerTimes(date, lat, lon, zone, adjustedParams)

        assertEquals(baseResult.fajr.plusMinutes(2), adjustedResult.fajr)
        assertEquals(baseResult.dhuhr.plusMinutes(3), adjustedResult.dhuhr)
        assertEquals(baseResult.asr.plusMinutes(-1), adjustedResult.asr)
        assertEquals(baseResult.maghrib.plusMinutes(2), adjustedResult.maghrib)
        assertEquals(baseResult.isha.plusMinutes(-5), adjustedResult.isha)
    }

    @Test
    fun testShiaConventionsUseTwilightForMaghrib() {
        val date = LocalDate.of(2026, 10, 5)
        val lat = 35.6892
        val lon = 51.3890
        val zone = ZoneId.of("Asia/Tehran")

        val standardParams = CalculationParameters(method = PrayerMethod.MUSLIM_WORLD_LEAGUE)
        val shiaParams = CalculationParameters(method = PrayerMethod.TEHRAN)

        val standardResult = engine.calculatePrayerTimes(date, lat, lon, zone, standardParams)
        val shiaResult = engine.calculatePrayerTimes(date, lat, lon, zone, shiaParams)

        // Shia Maghrib is based on twilight angle (4.5°), so it is later than standard sunset Maghrib
        assertTrue(
            "Shia Maghrib (twilight) must be after standard sunset Maghrib",
            shiaResult.maghrib.isAfter(standardResult.maghrib)
        )
    }

    @Test
    fun testAllWorldwideConventionsProduceValidSchedules() {
        val date = LocalDate.of(2026, 10, 5)
        val lat = 41.9028
        val lon = 12.4964
        val zone = ZoneId.of("Europe/Rome")

        for (method in PrayerMethod.entries) {
            val params = CalculationParameters(method = method)
            val result = engine.calculatePrayerTimes(date, lat, lon, zone, params)

            assertNotNull("Result for $method must not be null", result)
            assertTrue("Fajr before Sunrise for $method", result.fajr.isBefore(result.sunrise))
            assertTrue("Sunrise before Dhuhr for $method", result.sunrise.isBefore(result.dhuhr))
            assertTrue("Dhuhr before Asr for $method", result.dhuhr.isBefore(result.asr))
            assertTrue("Asr before Maghrib for $method", result.asr.isBefore(result.maghrib))
            assertTrue("Maghrib before Isha for $method", result.maghrib.isBefore(result.isha))
        }
    }
}
