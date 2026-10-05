package com.nurpray.app.data.astronomical

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import kotlin.math.floor

data class IslamicEvent(
    val title: String,
    val arabicTitle: String,
    val description: String,
    val isMajorHoliday: Boolean = false
)

data class HijriDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val monthName: String,
    val monthArabicName: String,
    val formattedLatin: String,
    val formattedArabic: String,
    val specialEvents: List<IslamicEvent>,
    val isWhiteDay: Boolean
)

/**
 * Astronomical and Umm Al-Qura Hijri Calendar Helper
 * with Lunar Sighting Offset (-2, -1, 0, +1, +2 days) and Holy Day detection.
 */
object HijriCalendarHelper {

    private val MONTH_NAMES = listOf(
        "Muharram" to "مُحَرَّم",
        "Safar" to "صَفَر",
        "Rabi' al-Awwal" to "رَبِيع الأَوَّل",
        "Rabi' al-Thani" to "رَبِيع الثَّانِي",
        "Jumada al-Ula" to "جُمَادَى الأُولَى",
        "Jumada al-Akhirah" to "جُمَادَى الآخِرَة",
        "Rajab" to "رَجَب",
        "Sha'ban" to "شَعْبَان",
        "Ramadan" to "رَمَضَان",
        "Shawwal" to "شَوَّال",
        "Dhu al-Qi'dah" to "ذُو القَعْدَة",
        "Dhu al-Hijjah" to "ذُو الحِجَّة"
    )

    /**
     * Converts a Gregorian [LocalDate] to [HijriDate] with optional visual lunar offset.
     */
    fun gregorianToHijri(date: LocalDate, lunarOffsetDays: Int = 0): HijriDate {
        val adjustedDate = date.plusDays(lunarOffsetDays.toLong())

        val (year, month, day) = try {
            // Standard Java 8+ Umm Al-Qura chronology
            val hijrahDate = HijrahDate.from(adjustedDate)
            Triple(
                hijrahDate.get(ChronoField.YEAR),
                hijrahDate.get(ChronoField.MONTH_OF_YEAR),
                hijrahDate.get(ChronoField.DAY_OF_MONTH)
            )
        } catch (e: Exception) {
            // High-precision Kuwaiti / Tabular astronomical algorithm fallback
            fallbackGregorianToHijri(adjustedDate)
        }

        val clampedMonth = month.coerceIn(1, 12)
        val (latinMonthName, arabicMonthName) = MONTH_NAMES[clampedMonth - 1]

        val events = detectEvents(clampedMonth, day)
        val isWhiteDay = day in 13..15

        val formattedLatin = "$day $latinMonthName $year AH"
        val formattedArabic = "${toArabicDigits(day)} $arabicMonthName ${toArabicDigits(year)} هـ"

        return HijriDate(
            year = year,
            month = clampedMonth,
            day = day,
            monthName = latinMonthName,
            monthArabicName = arabicMonthName,
            formattedLatin = formattedLatin,
            formattedArabic = formattedArabic,
            specialEvents = events,
            isWhiteDay = isWhiteDay
        )
    }

    /**
     * Converts Hijri year, month, day back to Gregorian [LocalDate].
     */
    fun hijriToGregorian(year: Int, month: Int, day: Int, lunarOffsetDays: Int = 0): LocalDate {
        val baseDate = try {
            val hijrahDate = HijrahDate.of(year, month, day)
            LocalDate.from(hijrahDate)
        } catch (e: Exception) {
            fallbackHijriToGregorian(year, month, day)
        }
        return baseDate.minusDays(lunarOffsetDays.toLong())
    }

    /**
     * Detects special Islamic events, holidays, and fasting recommendations.
     */
    fun detectEvents(month: Int, day: Int): List<IslamicEvent> {
        val events = mutableListOf<IslamicEvent>()

        when (month) {
            1 -> { // Muharram
                if (day == 1) {
                    events.add(
                        IslamicEvent(
                            title = "Capodanno Islamico",
                            arabicTitle = "رأس السنة الهجرية",
                            description = "Inizio del nuovo anno lunare Hijri (Mese sacro).",
                            isMajorHoliday = true
                        )
                    )
                }
                if (day == 9) {
                    events.add(
                        IslamicEvent(
                            title = "Giorno di Tasu'a",
                            arabicTitle = "يوم تاسوعاء",
                            description = "Giorno antecedente ad Ashura, digiuno raccomandato dalla Sunnah."
                        )
                    )
                }
                if (day == 10) {
                    events.add(
                        IslamicEvent(
                            title = "Giorno di Ashura",
                            arabicTitle = "يوم عاشوراء",
                            description = "Giorno sacro della salvezza del profeta Mosè (Musa), digiuno altamente meritorio.",
                            isMajorHoliday = true
                        )
                    )
                }
            }
            3 -> { // Rabi' al-Awwal
                if (day == 12) {
                    events.add(
                        IslamicEvent(
                            title = "Mawlid an-Nabi",
                            arabicTitle = "المولد النبوي الشريف",
                            description = "Nascita del Profeta Muhammad (pace e benedizioni su di lui)."
                        )
                    )
                }
            }
            7 -> { // Rajab (Mese sacro)
                if (day == 27) {
                    events.add(
                        IslamicEvent(
                            title = "Isra' e Mi'raj",
                            arabicTitle = "الإسراء والمعراج",
                            description = "Il Viaggio Notturno e l'Ascensione al Cielo del Profeta."
                        )
                    )
                }
            }
            8 -> { // Sha'ban
                if (day == 15) {
                    events.add(
                        IslamicEvent(
                            title = "Notte di Nisf Sha'ban",
                            arabicTitle = "ليلة النصف من شعبان",
                            description = "Notte del perdono a metà del mese di Sha'ban."
                        )
                    )
                }
            }
            9 -> { // Ramadan
                if (day == 1) {
                    events.add(
                        IslamicEvent(
                            title = "Primo Giorno di Ramadan",
                            arabicTitle = "بداية شهر رمضان المبارك",
                            description = "Inizio del mese sacro del digiuno obbligatorio (Sawm).",
                            isMajorHoliday = true
                        )
                    )
                }
                if (day in listOf(21, 23, 25, 27, 29)) {
                    events.add(
                        IslamicEvent(
                            title = "Possibile Laylat al-Qadr ($day Ramadan)",
                            arabicTitle = "ليلة القدر المباركة",
                            description = "Notte del Destino, migliore di mille mesi.",
                            isMajorHoliday = true
                        )
                    )
                }
            }
            10 -> { // Shawwal
                if (day == 1) {
                    events.add(
                        IslamicEvent(
                            title = "Eid al-Fitr (Festa della Rottura del Digiuno)",
                            arabicTitle = "عيد الفطر المبارك",
                            description = "Celebrazione del termine del mese di Ramadan.",
                            isMajorHoliday = true
                        )
                    )
                }
            }
            12 -> { // Dhu al-Hijjah (Mese sacro)
                if (day in 1..9) {
                    events.add(
                        IslamicEvent(
                            title = "I primi 10 giorni benedetti di Dhu al-Hijjah",
                            arabicTitle = "عشر ذي الحجة المباركة",
                            description = "I giorni più amati da Allah per le buone azioni e il digiuno."
                        )
                    )
                }
                if (day == 9) {
                    events.add(
                        IslamicEvent(
                            title = "Giorno di Arafah",
                            arabicTitle = "يوم عرفة",
                            description = "Culmine del Pellegrinaggio (Hajj). Digiuno altamente raccomandato per i non pellegrini.",
                            isMajorHoliday = true
                        )
                    )
                }
                if (day == 10) {
                    events.add(
                        IslamicEvent(
                            title = "Eid al-Adha (Festa del Sacrificio)",
                            arabicTitle = "عيد الأضحى المبارك",
                            description = "La festa maggiore dell'Islam.",
                            isMajorHoliday = true
                        )
                    )
                }
                if (day in 11..13) {
                    events.add(
                        IslamicEvent(
                            title = "Giorni di Tashriq ($day Dhu al-Hijjah)",
                            arabicTitle = "أيام التشريق",
                            description = "Giorni di festa, ricordo di Allah e banchetto (digiuno vietato)."
                        )
                    )
                }
            }
        }

        // Ayyam al-Beed (The White Days of fasting) for every lunar month
        if (day in 13..15 && month != 12) {
            events.add(
                IslamicEvent(
                    title = "Giorno Bianco ($day di Luna Piena)",
                    arabicTitle = "أيام البيض",
                    description = "Digiuno volontario raccomandato dalla Sunnah nei giorni 13, 14 e 15."
                )
            )
        }

        return events
    }

    /**
     * Mathematical algorithmic fallback for Gregorian to Hijri.
     */
    private fun fallbackGregorianToHijri(date: LocalDate): Triple<Int, Int, Int> {
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth

        val a = floor((14 - m) / 12.0).toInt()
        val yPrime = y + 4800 - a
        val mPrime = m + 12 * a - 3

        val jdn = d + floor((153 * mPrime + 2) / 5.0).toInt() + 365 * yPrime +
                floor(yPrime / 4.0).toInt() - floor(yPrime / 100.0).toInt() +
                floor(yPrime / 400.0).toInt() - 32045

        // Approximate Islamic epoch Julian Day: 1948440
        val l = jdn - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        val lPrime = l - 10631 * n + 354
        val j = (floor((10985 - lPrime) / 5316.0) * floor((50 * lPrime) / 17719.0) +
                floor(lPrime / 5670.0) * floor((43 * lPrime) / 15238.0)).toInt()
        val lDoublePrime = lPrime - (floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) +
                floor(j / 16.0) * floor((15238 * j) / 43.0)).toInt() + 29
        val month = floor((24 * lDoublePrime) / 709.0).toInt()
        val day = lDoublePrime - floor((709 * month) / 24.0).toInt()
        val year = 30 * n + j - 30

        return Triple(year, month, day)
    }

    private fun fallbackHijriToGregorian(year: Int, month: Int, day: Int): LocalDate {
        val n = floor((year - 1) / 30.0).toInt()
        val j = year - 30 * n
        val lPrime = floor((11 * j + 3) / 30.0).toInt() + 354 * (j - 1) +
                floor((month - 1) * 29.5 + 0.5).toInt() + day
        val jdn = lPrime + 1948440 - 10632 + 10631 * n

        val l = jdn + 68569
        val nPrime = floor((4 * l) / 146097.0).toInt()
        val lDoublePrime = l - floor((146097 * nPrime + 3) / 4.0).toInt()
        val i = floor((4000 * (lDoublePrime + 1)) / 1461001.0).toInt()
        val lTriplePrime = lDoublePrime - floor((1461 * i) / 4.0).toInt() + 31
        val jPrime = floor((80 * lTriplePrime) / 2447.0).toInt()
        val gDay = lTriplePrime - floor((2447 * jPrime) / 80.0).toInt()
        val lQuad = floor(jPrime / 11.0).toInt()
        val gMonth = jPrime + 2 - 12 * lQuad
        val gYear = 100 * (nPrime - 49) + i + lQuad

        return LocalDate.of(gYear, gMonth, gDay)
    }

    private fun toArabicDigits(number: Int): String {
        val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { char ->
            if (char.isDigit()) arabicNumerals[char.digitToInt()] else char
        }.joinToString("")
    }
}
