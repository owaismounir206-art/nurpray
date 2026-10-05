import SwiftUI

public struct HomeView: View {
    @ObservedObject var locationManager: LocationManager
    @State private var params = CalculationParameters()
    @State private var currentDate = Date()
    @State private var countdownText: String = "00:00:00"
    @State private var nextPrayerName: String = "Fajr"
    @State private var nextPrayerTime: String = "--:--"
    @State private var progressRatio: Float = 0.0
    @State private var prayerItems: [PrayerTimeItem] = []
    @State private var currentPrayerType: PrayerType? = nil
    @State private var nextPrayerType: PrayerType = .fajr

    private let engine = PrayerCalculationEngine()
    let timer = Timer.publish(every: 1.0, on: .main, in: .common).autoconnect()

    public init(locationManager: LocationManager) {
        self.locationManager = locationManager
    }

    public var body: some View {
        ZStack {
            LiquidBackgroundView()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    // Header Bar
                    headerBar

                    // Hijri Date Banner
                    hijriBanner

                    // Hero Countdown Liquid Glass Card
                    heroCountdownCard

                    // Quick Actions
                    quickActionsBar

                    // Section Title
                    HStack {
                        Text("Orari delle Preghiere di Oggi")
                            .font(.system(size: 18, weight: .semibold, design: .rounded))
                            .foregroundColor(.primary)
                        Spacer()
                    }
                    .padding(.horizontal, 4)

                    // 6 Prayer Glass Cards
                    VStack(spacing: 10) {
                        ForEach(prayerItems) { item in
                            prayerCard(for: item)
                        }
                    }

                    Spacer(modifier: .height(30))
                }
                .padding(.horizontal, 16)
                .padding(.top, 10)
            }
        }
        .onAppear {
            recalculatePrayers()
        }
        .onReceive(timer) { _ in
            updateCountdown()
        }
    }

    // MARK: - Header
    private var headerBar: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text("NurPray")
                    .font(.system(size: 28, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)

                HStack(spacing: 4) {
                    Image(systemName: "location.fill")
                        .font(.system(size: 12))
                        .foregroundColor(.emeraldLight)
                    Text("\(locationManager.cityName), \(locationManager.countryName)")
                        .font(.system(size: 14, weight: .medium, design: .rounded))
                        .foregroundColor(.secondary)
                }
            }
            Spacer()

            Button(action: {
                locationManager.requestLocation()
            }) {
                Image(systemName: "location.circle.fill")
                    .font(.system(size: 26))
                    .foregroundColor(.emeraldLight)
            }
        }
        .padding(.horizontal, 4)
    }

    // MARK: - Hijri Banner
    private var hijriBanner: some View {
        let hDate = HijriCalendarHelper.gregorianToHijri(date: currentDate)

        return VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(hDate.formattedLatin)
                    .font(.system(size: 15, weight: .bold, design: .rounded))
                    .foregroundColor(.emeraldLight)
                Spacer()
                Text(hDate.formattedArabic)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.emeraldLight)
            }

            if let event = hDate.specialEvents.first {
                HStack(spacing: 6) {
                    Text("✨")
                    Text("\(event.title): \(event.description)")
                        .font(.system(size: 12, weight: .semibold, design: .rounded))
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(Color.amberGold.opacity(0.2))
                .cornerRadius(10)
            }
        }
        .padding(14)
        .liquidGlassCard(cornerRadius: 20)
    }

    // MARK: - Hero Countdown Card
    private var heroCountdownCard: some View {
        VStack(spacing: 0) {
            ZStack {
                LiquidProgressArc(progress: progressRatio, size: 210)

                VStack(spacing: 4) {
                    Text("Prossima • \(nextPrayerName)")
                        .font(.system(size: 13, weight: .bold, design: .rounded))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(Color.amberGold.opacity(0.2))
                        .foregroundColor(.amberGold)
                        .cornerRadius(10)

                    Text(countdownText)
                        .font(.system(size: 38, weight: .heavy, design: .rounded))
                        .foregroundColor(.white)
                        .monospacedDigit()

                    Text("Inizio alle \(nextPrayerTime)")
                        .font(.system(size: 14, weight: .medium, design: .rounded))
                        .foregroundColor(.white.opacity(0.85))
                }
            }
            .frame(height: 230)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .liquidGlassCard(cornerRadius: 32, isHighlighted: true, highlightColor: .amberGold)
    }

    // MARK: - Quick Actions
    private var quickActionsBar: some View {
        HStack(spacing: 12) {
            quickButton(title: "Qibla", icon: "safari.fill", color: .emeraldLight)
            quickButton(title: "Tasbih", icon: "hand.tap.fill", color: .amberGold)
            quickButton(title: "Corano", icon: "book.fill", color: Color(red: 100/255, green: 181/255, blue: 246/255))
        }
    }

    private func quickButton(title: String, icon: String, color: Color) -> some View {
        VStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 22))
                .foregroundColor(color)
            Text(title)
                .font(.system(size: 13, weight: .semibold, design: .rounded))
                .foregroundColor(.primary)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 74)
        .liquidGlassCard(cornerRadius: 20)
    }

    // MARK: - Prayer Card
    private func prayerCard(for item: PrayerTimeItem) -> some View {
        let isCurrent = item.type == currentPrayerType
        let isNext = item.type == nextPrayerType
        let highlight = isCurrent ? Color.emeraldLight : (isNext ? Color.amberGold : Color.clear)

        return HStack {
            HStack(spacing: 12) {
                Circle()
                    .fill(isCurrent ? Color.emeraldLight : (isNext ? Color.amberGold : Color.white.opacity(0.25)))
                    .frame(width: 12, height: 12)

                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text(item.type.displayName)
                            .font(.system(size: 16, weight: isCurrent || isNext ? .bold : .semibold, design: .rounded))
                            .foregroundColor(.primary)

                        if isCurrent {
                            Text("ORA")
                                .font(.system(size: 10, weight: .bold, design: .rounded))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.emeraldLight.opacity(0.25))
                                .foregroundColor(.emeraldLight)
                                .cornerRadius(6)
                        }
                    }

                    Text(item.type.arabicName)
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            Text(item.formattedTime)
                .font(.system(size: 20, weight: .bold, design: .rounded))
                .foregroundColor(isCurrent ? .emeraldLight : (isNext ? .amberGold : .primary))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .liquidGlassCard(cornerRadius: 20, isHighlighted: isCurrent || isNext, highlightColor: highlight)
    }

    // MARK: - Calculation & Timer Logic
    private func recalculatePrayers() {
        let times = engine.calculatePrayerTimes(
            date: currentDate,
            latitude: locationManager.latitude,
            longitude: locationManager.longitude,
            params: params
        )

        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"

        let order: [PrayerType] = [.fajr, .sunrise, .dhuhr, .asr, .maghrib, .isha]
        var list: [PrayerTimeItem] = []
        for type in order {
            if let date = times[type] {
                list.append(PrayerTimeItem(type: type, time: date, formattedTime: formatter.string(from: date)))
            }
        }
        self.prayerItems = list
        updateCountdown()
    }

    private func updateCountdown() {
        let now = Date()
        currentDate = now

        guard !prayerItems.isEmpty else { return }

        // Find current and next prayer
        var next: PrayerTimeItem? = nil
        var current: PrayerTimeItem? = nil

        for item in prayerItems {
            if item.time <= now {
                current = item
            } else if next == nil && item.time > now {
                next = item
            }
        }

        // If after Isha, next is Fajr tomorrow
        let targetNext = next ?? prayerItems[0]
        self.currentPrayerType = current?.type
        self.nextPrayerType = targetNext.type
        self.nextPrayerName = targetNext.type.displayName
        self.nextPrayerTime = targetNext.formattedTime

        var diffSeconds = Int(targetNext.time.timeIntervalSince(now))
        if diffSeconds < 0 {
            diffSeconds += 86400
        }

        let hours = diffSeconds / 3600
        let minutes = (diffSeconds % 3600) / 60
        let seconds = diffSeconds % 60
        self.countdownText = String(format: "%02d:%02d:%02d", hours, minutes, seconds)

        // Progress ratio
        if let currentItem = current {
            let total = targetNext.time.timeIntervalSince(currentItem.time)
            let elapsed = now.timeIntervalSince(currentItem.time)
            if total > 0 {
                self.progressRatio = Float(max(0.0, min(1.0, elapsed / total)))
            } else {
                self.progressRatio = 0.0
            }
        } else {
            self.progressRatio = 0.0
        }
    }
}
