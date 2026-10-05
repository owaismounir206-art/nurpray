import Foundation

/// Prayer Types
public enum PrayerType: String, CaseIterable, Identifiable, Codable {
    case fajr = "Fajr"
    case sunrise = "Sunrise"
    case dhuhr = "Dhuhr"
    case asr = "Asr"
    case maghrib = "Maghrib"
    case isha = "Isha"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .fajr: return "Fajr"
        case .sunrise: return "Alba"
        case .dhuhr: return "Dhuhr"
        case .asr: return "Asr"
        case .maghrib: return "Maghrib"
        case .isha: return "Isha"
        }
    }

    public var arabicName: String {
        switch self {
        case .fajr: return "الفجر"
        case .sunrise: return "الشروق"
        case .dhuhr: return "الظهر"
        case .asr: return "العصر"
        case .maghrib: return "المغرب"
        case .isha: return "العشاء"
        }
    }
}

/// 19 Worldwide Calculation Conventions
public enum PrayerMethod: String, CaseIterable, Identifiable, Codable {
    case ucoiiItaly = "UCOII_ITALY"
    case uoifFrance = "UOIF_FRANCE"
    case ecfrEurope = "ECFR_EUROPE"
    case muslimWorldLeague = "MUSLIM_WORLD_LEAGUE"
    case isna = "ISNA"
    case egypt = "EGYPT"
    case ummAlQura = "UMM_AL_QURA"
    case karachi = "KARACHI"
    case diyanetTurkey = "DIYANET_TURKEY"
    case gulfUae = "GULF_UAE"
    case kuwait = "KUWAIT"
    case qatar = "QATAR"
    case singaporeMuis = "SINGAPORE_MUIS"
    case jakimMalaysia = "JAKIM_MALAYSIA"
    case kemenagIndonesia = "KEMENAG_INDONESIA"
    case moonsightingUk = "MOONSIGHTING_UK"
    case tehran = "TEHRAN"
    case shiaIthnaAshari = "SHIA_ITHNA_ASHARI"
    case custom = "CUSTOM"

    public var id: String { rawValue }

    public var title: String {
        switch self {
        case .ucoiiItaly:
            return "UCOII / Italia & Europa (Fajr 12° • Isha dinamico)"
        case .uoifFrance:
            return "UOIF / Musulmans de France (12° / 12°)"
        case .ecfrEurope:
            return "European Council for Fatwa and Research (18° / 15°)"
        case .muslimWorldLeague:
            return "Muslim World League (MWL - 18° / 17°)"
        case .isna:
            return "ISNA (Nord America - 15° / 15°)"
        case .egypt:
            return "Egyptian General Authority of Survey (19.5° / 17.5°)"
        case .ummAlQura:
            return "Umm Al-Qura University, Makkah (18.5° / +90 min)"
        case .karachi:
            return "University of Islamic Sciences, Karachi (18° / 18°)"
        case .diyanetTurkey:
            return "Diyanet İşleri Başkanlığı (Turchia - 18° / 17°)"
        case .gulfUae:
            return "General Authority of Islamic Affairs, UAE (18.2° / +90 min)"
        case .kuwait:
            return "Ministry of Awqaf, Kuwait (18° / 17.5°)"
        case .qatar:
            return "Ministry of Awqaf, Qatar (18° / +90 min)"
        case .singaporeMuis:
            return "MUIS (Singapore - 20° / 18°)"
        case .jakimMalaysia:
            return "JAKIM (Malesia - 20° / 18°)"
        case .kemenagIndonesia:
            return "KEMENAG (Indonesia - 20° / 18°)"
        case .moonsightingUk:
            return "Moonsighting Committee Worldwide / UK (18° / 18°)"
        case .tehran:
            return "Institute of Geophysics, Univ. of Tehran (17.7° / 14°)"
        case .shiaIthnaAshari:
            return "Shia Ithna Ashari / Leva Institute, Qum (16° / 14°)"
        case .custom:
            return "Personalizzato (Angoli ed intervalli liberi)"
        }
    }

    public var fajrAngle: Double {
        switch self {
        case .ucoiiItaly, .uoifFrance: return 12.0
        case .isna: return 15.0
        case .shiaIthnaAshari: return 16.0
        case .tehran: return 17.7
        case .karachi, .ecfrEurope, .muslimWorldLeague, .diyanetTurkey, .kuwait, .qatar, .moonsightingUk, .custom: return 18.0
        case .gulfUae: return 18.2
        case .ummAlQura: return 18.5
        case .egypt: return 19.5
        case .singaporeMuis, .jakimMalaysia, .kemenagIndonesia: return 20.0
        }
    }

    public var ishaAngle: Double? {
        switch self {
        case .ucoiiItaly, .ummAlQura, .gulfUae, .qatar:
            return nil // uses minutes offset
        case .uoifFrance: return 12.0
        case .tehran, .shiaIthnaAshari: return 14.0
        case .isna, .ecfrEurope: return 15.0
        case .muslimWorldLeague, .diyanetTurkey, .custom: return 17.0
        case .kuwait, .egypt: return 17.5
        case .karachi, .singaporeMuis, .jakimMalaysia, .kemenagIndonesia, .moonsightingUk: return 18.0
        }
    }

    public var ishaMinutesAfterMaghrib: Int? {
        switch self {
        case .ucoiiItaly, .ummAlQura, .gulfUae, .qatar: return 90
        default: return nil
        }
    }

    public var maghribAngle: Double? {
        switch self {
        case .tehran: return 4.5
        case .shiaIthnaAshari: return 4.0
        default: return nil
        }
    }
}

public enum AsrJuristicMethod: String, CaseIterable, Identifiable, Codable {
    case shafiMalikiHanbali = "SHAFI_MALIKI_HANBALI"
    case hanafi = "HANAFI"

    public var id: String { rawValue }

    public var title: String {
        switch self {
        case .shafiMalikiHanbali: return "Shafi'i / Maliki / Hanbali (Ombra 1:1)"
        case .hanafi: return "Hanafi (Ombra 2:1)"
        }
    }

    public var shadowFactor: Double {
        switch self {
        case .shafiMalikiHanbali: return 1.0
        case .hanafi: return 2.0
        }
    }
}

public enum HighLatitudeRule: String, CaseIterable, Identifiable, Codable {
    case angleBased = "ANGLE_BASED"
    case middleOfNight = "MIDDLE_OF_NIGHT"
    case oneSeventh = "ONE_SEVENTH"
    case none = "NONE"

    public var id: String { rawValue }
}

public struct PrayerAdjustments: Codable, Equatable {
    public var fajrMinutes: Int = 0
    public var sunriseMinutes: Int = 0
    public var dhuhrMinutes: Int = 0
    public var asrMinutes: Int = 0
    public var maghribMinutes: Int = 0
    public var ishaMinutes: Int = 0

    public init(
        fajrMinutes: Int = 0,
        sunriseMinutes: Int = 0,
        dhuhrMinutes: Int = 0,
        asrMinutes: Int = 0,
        maghribMinutes: Int = 0,
        ishaMinutes: Int = 0
    ) {
        self.fajrMinutes = fajrMinutes
        self.sunriseMinutes = sunriseMinutes
        self.dhuhrMinutes = dhuhrMinutes
        self.asrMinutes = asrMinutes
        self.maghribMinutes = maghribMinutes
        self.ishaMinutes = ishaMinutes
    }
}

public struct CalculationParameters: Codable, Equatable {
    public var method: PrayerMethod
    public var asrJuristicMethod: AsrJuristicMethod
    public var highLatitudeRule: HighLatitudeRule
    public var elevationMeters: Double
    public var dhuhrSafetyMinutes: Int
    public var maghribSafetyMinutes: Int
    public var adjustments: PrayerAdjustments

    public init(
        method: PrayerMethod = .ucoiiItaly,
        asrJuristicMethod: AsrJuristicMethod = .shafiMalikiHanbali,
        highLatitudeRule: HighLatitudeRule = .angleBased,
        elevationMeters: Double = 0.0,
        dhuhrSafetyMinutes: Int = 2,
        maghribSafetyMinutes: Int = 2,
        adjustments: PrayerAdjustments = PrayerAdjustments()
    ) {
        self.method = method
        self.asrJuristicMethod = asrJuristicMethod
        self.highLatitudeRule = highLatitudeRule
        self.elevationMeters = elevationMeters
        self.dhuhrSafetyMinutes = dhuhrSafetyMinutes
        self.maghribSafetyMinutes = maghribSafetyMinutes
        self.adjustments = adjustments
    }
}

public struct PrayerTimeItem: Identifiable, Equatable {
    public var id: String { type.rawValue }
    public let type: PrayerType
    public let time: Date
    public let formattedTime: String
}

public struct TodayPrayerSchedule: Equatable {
    public let date: Date
    public let prayers: [PrayerTimeItem]
    public let currentPrayer: PrayerTimeItem?
    public let nextPrayer: PrayerTimeItem
    public let countdownSeconds: Int
    public let progressRatio: Float
}
