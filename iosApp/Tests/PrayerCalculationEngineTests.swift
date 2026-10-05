import XCTest
@testable import NurPrayCore

final class PrayerCalculationEngineTests: XCTestCase {

    var engine: PrayerCalculationEngine!

    override func setUp() {
        super.setUp()
        engine = PrayerCalculationEngine()
    }

    func testJulianDayCalculation() {
        let jd = engine.calculateJulianDay(year: 2000, month: 1, day: 1)
        XCTAssertEqual(jd, 2451545.0, accuracy: 0.0001)
    }

    func testSolarCoordinates() {
        let solar = engine.calculateSolarCoordinates(julianDay: 2451545.0)
        XCTAssertTrue(solar.declinationDegrees < -22.5 && solar.declinationDegrees > -23.5)
        XCTAssertTrue(solar.equationOfTimeMinutes < -2.0 && solar.equationOfTimeMinutes > -4.5)
    }

    func testPistoiaUcoiiAutumnConvention() {
        let calendar = Calendar(identifier: .gregorian)
        var comps = DateComponents()
        comps.year = 2026
        comps.month = 9
        comps.day = 20
        comps.timeZone = TimeZone(identifier: "Europe/Rome")
        let sepDate = calendar.date(from: comps)!

        let lat = 43.93
        let lon = 10.92
        let params = CalculationParameters(method: .ucoiiItaly)

        let times = engine.calculatePrayerTimes(
            date: sepDate,
            latitude: lat,
            longitude: lon,
            timeZone: TimeZone(identifier: "Europe/Rome")!,
            params: params
        )

        guard let fajr = times[.fajr],
              let sunrise = times[.sunrise],
              let maghrib = times[.maghrib],
              let isha = times[.isha] else {
            XCTFail("Prayer times must not be nil")
            return
        }

        // Fajr (12°) must be 60-75 min before sunrise
        let fajrDiff = sunrise.timeIntervalSince(fajr) / 60.0
        XCTAssertTrue(fajrDiff >= 60.0 && fajrDiff <= 75.0, "Fajr at 12° should be ~65 min before sunrise, got \(fajrDiff)")

        // Isha in late September is Maghrib + 100 min
        let ishaDiff = isha.timeIntervalSince(maghrib) / 60.0
        XCTAssertEqual(round(ishaDiff), 100.0, "Isha must be 100 min after Maghrib up to late September")
    }

    func testHijriCalendarUmmAlQura() {
        let calendar = Calendar(identifier: .gregorian)
        var comps = DateComponents()
        comps.year = 2026
        comps.month = 10
        comps.day = 5
        let date = calendar.date(from: comps)!

        let hijri = HijriCalendarHelper.gregorianToHijri(date: date)
        XCTAssertTrue(hijri.year >= 1448)
        XCTAssertFalse(hijri.formattedLatin.isEmpty)
        XCTAssertFalse(hijri.formattedArabic.isEmpty)
    }
}
