package com.nurpray.app.data.astronomical

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HijriCalendarHelperTest {

    @Test
    fun testGregorianToHijriConversion() {
        val date = LocalDate.of(2026, 10, 5)
        val hijriDate = HijriCalendarHelper.gregorianToHijri(date, 0)

        assertNotNull(hijriDate)
        assertEquals(1448, hijriDate.year)
        assertTrue("Month should be between 1 and 12", hijriDate.month in 1..12)
        assertTrue("Day should be between 1 and 30", hijriDate.day in 1..30)
        assertFalse(hijriDate.formattedLatin.isEmpty())
        assertFalse(hijriDate.formattedArabic.isEmpty())
    }

    @Test
    fun testLunarOffsetShiftsDate() {
        val date = LocalDate.of(2026, 10, 5)
        val normal = HijriCalendarHelper.gregorianToHijri(date, 0)
        val plusOne = HijriCalendarHelper.gregorianToHijri(date, 1)
        val minusOne = HijriCalendarHelper.gregorianToHijri(date, -1)

        assertNotEquals(normal.day, plusOne.day)
        assertNotEquals(normal.day, minusOne.day)
    }

    @Test
    fun testHolyEventsDetection() {
        // Test 10 Muharram (Ashura)
        val ashuraEvents = HijriCalendarHelper.detectEvents(1, 10)
        assertTrue("Must contain Ashura", ashuraEvents.any { it.title.contains("Ashura") })

        // Test 1 Ramadan
        val ramadanEvents = HijriCalendarHelper.detectEvents(9, 1)
        assertTrue("Must contain Inizio Ramadan", ramadanEvents.any { it.title.contains("Ramadan") })

        // Test 1 Shawwal (Eid al-Fitr)
        val eidFitrEvents = HijriCalendarHelper.detectEvents(10, 1)
        assertTrue("Must contain Eid al-Fitr", eidFitrEvents.any { it.title.contains("Eid al-Fitr") })

        // Test 10 Dhu al-Hijjah (Eid al-Adha)
        val eidAdhaEvents = HijriCalendarHelper.detectEvents(12, 10)
        assertTrue("Must contain Eid al-Adha", eidAdhaEvents.any { it.title.contains("Eid al-Adha") })
    }

    @Test
    fun testWhiteDaysAyyamAlBeed() {
        val date13 = HijriCalendarHelper.detectEvents(2, 13)
        assertTrue("Day 13 is a White Day", date13.any { it.title.contains("Giorno Bianco") })

        val date14 = HijriCalendarHelper.detectEvents(2, 14)
        assertTrue("Day 14 is a White Day", date14.any { it.title.contains("Giorno Bianco") })

        val date15 = HijriCalendarHelper.detectEvents(2, 15)
        assertTrue("Day 15 is a White Day", date15.any { it.title.contains("Giorno Bianco") })

        val date10 = HijriCalendarHelper.detectEvents(2, 10)
        assertFalse("Day 10 is not a White Day", date10.any { it.title.contains("Giorno Bianco") })
    }

    @Test
    fun testElevationDipOfHorizonPostponesSunset() {
        val engine = PrayerCalculationEngine()
        val date = LocalDate.of(2026, 10, 5)
        val lat = 21.4225
        val lon = 39.8262
        val zone = ZoneId.of("Asia/Riyadh")

        val seaLevelParams = CalculationParameters(
            method = PrayerMethod.UMM_AL_QURA,
            elevationMeters = 0.0
        )
        val highMountainParams = CalculationParameters(
            method = PrayerMethod.UMM_AL_QURA,
            elevationMeters = 1500.0 // 1500m mountain
        )

        val seaLevelResult = engine.calculatePrayerTimes(date, lat, lon, zone, seaLevelParams)
        val mountainResult = engine.calculatePrayerTimes(date, lat, lon, zone, highMountainParams)

        // Sunrise should be earlier at high altitude
        assertTrue(
            "Sunrise on mountain must be earlier or equal to sea level",
            !mountainResult.sunrise.isAfter(seaLevelResult.sunrise)
        )

        // Sunset and Maghrib should be later at high altitude
        assertTrue(
            "Sunset/Maghrib on mountain must be later or equal to sea level",
            !mountainResult.maghrib.isBefore(seaLevelResult.maghrib)
        )
    }
}
