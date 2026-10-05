import Foundation

/// Pure Swift Astronomical Prayer Calculation Engine
/// High precision solar positioning with horizon dip correction,
/// dynamic European / UCOII seasonal Isha adjustment, and all 19 global conventions.
public class PrayerCalculationEngine {

    public init() {}

    public func calculateJulianDay(year: Int, month: Int, day: Int, hourOfDay: Double = 12.0) -> Double {
        var y = year
        var m = month
        if m <= 2 {
            y -= 1
            m += 12
        }
        let a = floor(Double(y) / 100.0)
        let b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * Double(y + 4716)) + floor(30.6001 * Double(m + 1)) + Double(day) + (hourOfDay / 24.0) + b - 1524.5
    }

    public struct SolarCoordinates {
        public let declinationDegrees: Double
        public let equationOfTimeMinutes: Double
        public let transitFractionOfDay: Double
    }

    public func calculateSolarCoordinates(julianDay: Double) -> SolarCoordinates {
        let t = (julianDay - 2451545.0) / 36525.0

        let l0 = fmod(280.46646 + 36000.76983 * t + 0.0003032 * (t * t), 360.0)
        let m = fmod(357.52911 + 35999.05029 * t - 0.0001537 * (t * t), 360.0)
        let mRad = m * .pi / 180.0

        let c = (1.914602 - 0.004817 * t - 0.000014 * (t * t)) * sin(mRad)
            + (0.019993 - 0.000101 * t) * sin(2.0 * mRad)
            + 0.000289 * sin(3.0 * mRad)

        let trueLongitude = l0 + c
        let omega = 125.04 - 1934.136 * t
        let lambda = trueLongitude - 0.00569 - 0.00478 * sin(omega * .pi / 180.0)
        let lambdaRad = lambda * .pi / 180.0

        let eps0 = 23.439291 - 0.013004167 * t - 0.000000164 * (t * t) + 0.0005036 * (t * t * t)
        let epsilon = eps0 + 0.00256 * cos(omega * .pi / 180.0)
        let epsilonRad = epsilon * .pi / 180.0

        let sinDec = sin(epsilonRad) * sin(lambdaRad)
        let decRad = asin(sinDec)
        let decDegrees = decRad * 180.0 / .pi

        let yCoeff = tan(epsilonRad / 2.0) * tan(epsilonRad / 2.0)
        let eotRad = yCoeff * sin(2.0 * (l0 * .pi / 180.0))
            - 2.0 * 0.016708634 * sin(mRad)
            + 4.0 * 0.016708634 * yCoeff * sin(mRad) * cos(2.0 * (l0 * .pi / 180.0))
            - 0.5 * (yCoeff * yCoeff) * sin(4.0 * (l0 * .pi / 180.0))
            - 1.25 * (0.016708634 * 0.016708634) * sin(2.0 * mRad)

        let eotMinutes = (eotRad * 180.0 / .pi) * 4.0
        return SolarCoordinates(declinationDegrees: decDegrees, equationOfTimeMinutes: eotMinutes, transitFractionOfDay: 0.5)
    }

    private func calculateHourAngleOffset(latDeg: Double, decDeg: Double, zenithDeg: Double) -> Double {
        let latRad = latDeg * .pi / 180.0
        let decRad = decDeg * .pi / 180.0
        let zenithRad = zenithDeg * .pi / 180.0

        let cosHA = (cos(zenithRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        let clampedCosHA = min(1.0, max(-1.0, cosHA))
        let haRad = acos(clampedCosHA)
        return (haRad * 180.0 / .pi) / 15.0
    }

    public func calculatePrayerTimes(
        date: Date,
        latitude: Double,
        longitude: Double,
        timeZone: TimeZone = .current,
        params: CalculationParameters = CalculationParameters()
    ) -> [PrayerType: Date] {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone

        let comps = calendar.dateComponents([.year, .month, .day, .dayOfYear], from: date)
        guard let year = comps.year, let month = comps.month, let day = comps.day else { return [:] }

        let jd = calculateJulianDay(year: year, month: month, day: day)
        let solar = calculateSolarCoordinates(julianDay: jd)
        let declination = solar.declinationDegrees
        let eot = solar.equationOfTimeMinutes

        // Solar transit (Dhuhr)
        let tzOffsetSeconds = Double(timeZone.secondsFromGMT(for: date))
        let tzOffsetHours = tzOffsetSeconds / 3600.0
        let baseTransitHours = 12.0 + tzOffsetHours - (longitude / 15.0) - (eot / 60.0)
        let dhuhrHours = baseTransitHours + (Double(params.dhuhrSafetyMinutes) / 60.0)

        // Sunrise & Sunset with Horizon Dip correction (0.0347 * sqrt(h))
        let horizonDip = 0.0347 * sqrt(max(0.0, params.elevationMeters))
        let standardZenith = 90.8333 + horizonDip
        let sunOffset = calculateHourAngleOffset(latDeg: latitude, decDeg: declination, zenithDeg: standardZenith)
        let sunriseHours = baseTransitHours - sunOffset
        let sunsetHours = baseTransitHours + sunOffset

        // Asr
        let shadowRatio = params.asrJuristicMethod.shadowFactor
        let latRad = latitude * .pi / 180.0
        let decRad = declination * .pi / 180.0
        let asrAngleRad = atan(1.0 / (shadowRatio + tan(abs(latRad - decRad))))
        let asrZenithDeg = 90.0 - (asrAngleRad * 180.0 / .pi)
        let asrOffset = calculateHourAngleOffset(latDeg: latitude, decDeg: declination, zenithDeg: asrZenithDeg)
        let asrHours = baseTransitHours + asrOffset

        // Fajr (12° for UCOII)
        let fajrZenith = 90.0 + params.method.fajrAngle
        let fajrOffset = calculateHourAngleOffset(latDeg: latitude, decDeg: declination, zenithDeg: fajrZenith)
        var fajrHours = baseTransitHours - fajrOffset

        // Maghrib
        var maghribHours = sunsetHours + (Double(params.maghribSafetyMinutes) / 60.0)
        if let maghribAngle = params.method.maghribAngle {
            let magZenith = 90.0 + maghribAngle
            let magOffset = calculateHourAngleOffset(latDeg: latitude, decDeg: declination, zenithDeg: magZenith)
            maghribHours = baseTransitHours + magOffset
        }

        // Isha
        var ishaHours: Double
        if params.method == .ucoiiItaly {
            // UCOII / European Fatwa guidelines:
            // 100 min during summer/early autumn (approx May 20 to Sep 26, day of year 140..269), 90 min in winter
            let doy = comps.dayOfYear ?? 1
            let minutes = (doy >= 140 && doy <= 269) ? 100 : 90
            ishaHours = maghribHours + (Double(minutes) / 60.0)
        } else if let fixedMinutes = params.method.ishaMinutesAfterMaghrib {
            ishaHours = maghribHours + (Double(fixedMinutes) / 60.0)
        } else {
            let ishaAngle = params.method.ishaAngle ?? 17.0
            let ishaZenith = 90.0 + ishaAngle
            let ishaOffset = calculateHourAngleOffset(latDeg: latitude, decDeg: declination, zenithDeg: ishaZenith)
            ishaHours = baseTransitHours + ishaOffset
        }

        // High Latitude Adjustment if needed
        let nightHours = (24.0 - sunsetHours) + sunriseHours
        if params.highLatitudeRule == .angleBased {
            let fajrMax = (params.method.fajrAngle / 60.0) * nightHours
            if (baseTransitHours - fajrHours) > fajrMax {
                fajrHours = sunriseHours - fajrMax
            }
            let ishaAngle = params.method.ishaAngle ?? 17.0
            let ishaMax = (ishaAngle / 60.0) * nightHours
            if (ishaHours - baseTransitHours) > ishaMax {
                ishaHours = sunsetHours + ishaMax
            }
        }

        func toDate(decimalHours: Double, adjustmentsMinutes: Int) -> Date {
            var norm = fmod(decimalHours, 24.0)
            if norm < 0 { norm += 24.0 }
            let totalSeconds = Int(norm * 3600.0) + (adjustmentsMinutes * 60)
            let h = (totalSeconds / 3600) % 24
            let m = (totalSeconds % 3600) / 60
            let s = totalSeconds % 60

            var c = calendar.dateComponents([.year, .month, .day], from: date)
            c.hour = h
            c.minute = m
            c.second = s
            return calendar.date(from: c) ?? date
        }

        return [
            .fajr: toDate(decimalHours: fajrHours, adjustmentsMinutes: params.adjustments.fajrMinutes),
            .sunrise: toDate(decimalHours: sunriseHours, adjustmentsMinutes: params.adjustments.sunriseMinutes),
            .dhuhr: toDate(decimalHours: dhuhrHours, adjustmentsMinutes: params.adjustments.dhuhrMinutes),
            .asr: toDate(decimalHours: asrHours, adjustmentsMinutes: params.adjustments.asrMinutes),
            .maghrib: toDate(decimalHours: maghribHours, adjustmentsMinutes: params.adjustments.maghribMinutes),
            .isha: toDate(decimalHours: ishaHours, adjustmentsMinutes: params.adjustments.ishaMinutes)
        ]
    }
}
