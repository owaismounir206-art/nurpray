import Foundation

public struct IslamicHolyEvent: Identifiable, Equatable {
    public var id: String { title }
    public let title: String
    public let description: String
    public let hijriMonth: Int
    public let hijriDay: Int
}

public struct HijriDateInfo: Equatable {
    public let year: Int
    public let month: Int
    public let day: Int
    public let monthNameArabic: String
    public let monthNameLatin: String
    public let formattedArabic: String
    public let formattedLatin: String
    public let specialEvents: [IslamicHolyEvent]
    public let isWhiteDay: BooleanLiteralType
}

public class HijriCalendarHelper {

    public static let islamicMonthsArabic = [
        "المحرّم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    ]

    public static let islamicMonthsLatin = [
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    ]

    public static let holyEvents: [IslamicHolyEvent] = [
        IslamicHolyEvent(title: "Capodanno Islamico", description: "1° Muharram, inizio del nuovo anno dell'Egira", hijriMonth: 1, hijriDay: 1),
        IslamicHolyEvent(title: "Giorno di Ashura", description: "10 Muharram, giorno di digiuno e salvezza di Mosè", hijriMonth: 1, hijriDay: 10),
        IslamicHolyEvent(title: "Mawlid an-Nabi", description: "12 Rabi' al-Awwal, nascita del Profeta Muhammad (ﷺ)", hijriMonth: 3, hijriDay: 12),
        IslamicHolyEvent(title: "Isra e Mi'raj", description: "27 Rajab, viaggio notturno e ascensione celeste", hijriMonth: 7, hijriDay: 27),
        IslamicHolyEvent(title: "Metà di Sha'ban (Laylat al-Bara'at)", description: "15 Sha'ban, notte del perdono e della misericordia", hijriMonth: 8, hijriDay: 15),
        IslamicHolyEvent(title: "Inizio del Ramadan", description: "1° Ramadan, mese sacro di digiuno e rivelazione", hijriMonth: 9, hijriDay: 1),
        IslamicHolyEvent(title: "Laylat al-Qadr (Notte del Destino)", description: "27 Ramadan, notte migliore di mille mesi", hijriMonth: 9, hijriDay: 27),
        IslamicHolyEvent(title: "Eid al-Fitr (Festa della Rottura del Digiuno)", description: "1° Shawwal, celebrazione della fine del Ramadan", hijriMonth: 10, hijriDay: 1),
        IslamicHolyEvent(title: "Giorno di Arafah", description: "9 Dhu al-Hijjah, culmine del pellegrinaggio Hajj", hijriMonth: 12, hijriDay: 9),
        IslamicHolyEvent(title: "Eid al-Adha (Festa del Sacrificio)", description: "10 Dhu al-Hijjah, grande festa del sacrificio", hijriMonth: 12, hijriDay: 10)
    ]

    public static func gregorianToHijri(date: Date, lunarOffsetDays: Int = 0) -> HijriDateInfo {
        let islamicCalendar = Calendar(identifier: .islamicUmmAlQura)
        let shiftedDate = Calendar.current.date(byAdding: .day, value: lunarOffsetDays, to: date) ?? date

        let comps = islamicCalendar.dateComponents([.year, .month, .day], from: shiftedDate)
        let year = comps.year ?? 1448
        let month = comps.month ?? 1
        let day = comps.day ?? 1

        let monthIndex = max(0, min(11, month - 1))
        let monthAr = islamicMonthsArabic[monthIndex]
        let monthLat = islamicMonthsLatin[monthIndex]

        let formattedAr = "\(day) \(monthAr) \(year) هـ"
        let formattedLat = "\(day) \(monthLat) \(year) AH"

        let events = holyEvents.filter { $0.hijriMonth == month && $0.hijriDay == day }
        let isWhiteDay = (day == 13 || day == 14 || day == 15)

        return HijriDateInfo(
            year: year,
            month: month,
            day: day,
            monthNameArabic: monthAr,
            monthNameLatin: monthLat,
            formattedArabic: formattedAr,
            formattedLatin: formattedLat,
            specialEvents: events,
            isWhiteDay: isWhiteDay
        )
    }
}
