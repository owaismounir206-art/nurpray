import SwiftUI

public enum AppTab: Int, CaseIterable, Identifiable {
    case home = 0
    case qibla = 1
    case quran = 2
    case tasbih = 3
    case settings = 4

    public var id: Int { rawValue }

    public var title: String {
        switch self {
        case .home: return "Home"
        case .qibla: return "Qibla"
        case .quran: return "Corano"
        case .tasbih: return "Tasbih"
        case .settings: return "Opzioni"
        }
    }

    public var icon: String {
        switch self {
        case .home: return "house.fill"
        case .qibla: return "safari.fill"
        case .quran: return "book.fill"
        case .tasbih: return "hand.tap.fill"
        case .settings: return "gearshape.fill"
        }
    }
}

public struct ContentView: View {
    @StateObject private var locationManager = LocationManager()
    @State private var selectedTab: AppTab = .home
    @State private var selectedMethod: PrayerMethod = .ucoiiItaly
    @State private var selectedAsrMethod: AsrJuristicMethod = .shafiMalikiHanbali

    public init() {}

    public var body: some View {
        ZStack(alignment: .bottom) {
            // Main Screen Content
            Group {
                switch selectedTab {
                case .home:
                    HomeView(locationManager: locationManager)
                case .qibla:
                    QiblaView(locationManager: locationManager)
                case .quran:
                    QuranView()
                case .tasbih:
                    TasbihView()
                case .settings:
                    SettingsView(
                        selectedMethod: $selectedMethod,
                        selectedAsrMethod: $selectedAsrMethod
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Floating Liquid Glass Tab Bar
            HStack(spacing: 0) {
                ForEach(AppTab.allCases) { tab in
                    let isSelected = tab == selectedTab

                    Button(action: {
                        let gen = UIImpactFeedbackGenerator(style: .light)
                        gen.impactOccurred()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                            selectedTab = tab
                        }
                    }) {
                        VStack(spacing: 4) {
                            Image(systemName: tab.icon)
                                .font(.system(size: 20, weight: isSelected ? .bold : .medium))
                                .foregroundColor(isSelected ? .emeraldLight : .secondary)
                                .scaleEffect(isSelected ? 1.15 : 1.0)

                            Text(tab.title)
                                .font(.system(size: 11, weight: isSelected ? .bold : .medium, design: .rounded))
                                .foregroundColor(isSelected ? .emeraldLight : .secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(
                Capsule()
                    .fill(.ultraThinMaterial)
            )
            .overlay(
                Capsule()
                    .strokeBorder(
                        LinearGradient(
                            colors: [.white.opacity(0.8), .clear, .white.opacity(0.25)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 1.2
                    )
            )
            .shadow(color: Color.black.opacity(0.18), radius: 20, x: 0, y: 10)
            .padding(.horizontal, 20)
            .padding(.bottom, 12)
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .onAppear {
            locationManager.requestLocation()
        }
    }
}
