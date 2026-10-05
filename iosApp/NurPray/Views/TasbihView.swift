import SwiftUI

public struct DhikrItem: Identifiable, Equatable {
    public var id: String { transliteration }
    public let arabic: String
    public let transliteration: String
    public let translation: String
    public let target: Int
}

public struct TasbihView: View {
    @State private var count: Int = 0
    @State private var selectedIndex: Int = 0
    @State private var isBouncing: Bool = false

    public let dhikrList: [DhikrItem] = [
        DhikrItem(arabic: "سُبْحَانَ ٱللَّٰهِ", transliteration: "SubhanAllah", translation: "Gloria ad Allah", target: 33),
        DhikrItem(arabic: "ٱلْحَمْدُ لِلَّٰهِ", transliteration: "Alhamdulillah", translation: "Lode ad Allah", target: 33),
        DhikrItem(arabic: "ٱللَّٰهُ أَكْبَرُ", transliteration: "Allahu Akbar", translation: "Allah è il Più Grande", target: 34),
        DhikrItem(arabic: "أَسْتَغْفِرُ ٱللَّٰهَ", transliteration: "Astaghfirullah", translation: "Chiedo perdono ad Allah", target: 100),
        DhikrItem(arabic: "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ", transliteration: "La ilaha illa Allah", translation: "Non c'è dio all'infuori di Allah", target: 100)
    ]

    public init() {}

    private var activeDhikr: DhikrItem {
        dhikrList[selectedIndex]
    }

    private var progress: CGFloat {
        if activeDhikr.target > 0 {
            return CGFloat(min(1.0, Double(count) / Double(activeDhikr.target)))
        }
        return 0
    }

    public var body: some View {
        ZStack {
            LiquidBackgroundView()

            VStack(spacing: 20) {
                // Top Bar
                HStack {
                    Text("Tasbih Digitale")
                        .font(.system(size: 28, weight: .bold, design: .rounded))
                        .foregroundColor(.primary)

                    Spacer()

                    Button(action: {
                        let gen = UIImpactFeedbackGenerator(style: .rigid)
                        gen.impactOccurred()
                        count = 0
                    }) {
                        Image(systemName: "arrow.counterclockwise.circle.fill")
                            .font(.system(size: 26))
                            .foregroundColor(.emeraldLight)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)

                // Dhikr Selector Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(0..<dhikrList.count, id: \.self) { idx in
                            let item = dhikrList[idx]
                            let isSelected = idx == selectedIndex

                            Button(action: {
                                selectedIndex = idx
                                count = 0
                            }) {
                                Text(item.transliteration)
                                    .font(.system(size: 14, weight: .semibold, design: .rounded))
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 8)
                                    .background(isSelected ? Color.emeraldLight : Color.white.opacity(0.1))
                                    .foregroundColor(isSelected ? .white : .primary)
                                    .cornerRadius(20)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // Calligraphy & Meaning Glass Card
                VStack(spacing: 8) {
                    Text(activeDhikr.arabic)
                        .font(.system(size: 32, weight: .bold))
                        .foregroundColor(.emeraldLight)

                    Text(activeDhikr.transliteration)
                        .font(.system(size: 17, weight: .semibold, design: .rounded))
                        .foregroundColor(.primary)

                    Text(activeDhikr.translation)
                        .font(.system(size: 14, weight: .medium, design: .rounded))
                        .foregroundColor(.secondary)
                }
                .padding(20)
                .frame(maxWidth: .infinity)
                .liquidGlassCard(cornerRadius: 26)
                .padding(.horizontal, 16)

                Spacer()

                // Giant Liquid Glass Counter Pebble
                Button(action: {
                    incrementCount()
                }) {
                    ZStack {
                        // Glass Surface
                        RoundedRectangle(cornerRadius: 44, style: .continuous)
                            .fill(.ultraThinMaterial)
                            .frame(maxWidth: .infinity)
                            .frame(height: 280)
                            .overlay(
                                RoundedRectangle(cornerRadius: 44, style: .continuous)
                                    .strokeBorder(
                                        LinearGradient(
                                            colors: [.white.opacity(0.8), .clear, .emeraldLight.opacity(0.4)],
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        ),
                                        lineWidth: 1.5
                                    )
                            )
                            .shadow(color: Color.emeraldLight.opacity(0.25), radius: 20)

                        // Circular Progress Ring
                        ZStack {
                            Circle()
                                .stroke(Color.white.opacity(0.12), lineWidth: 14)
                                .frame(width: 220, height: 220)

                            Circle()
                                .trim(from: 0.0, to: progress)
                                .stroke(
                                    AngularGradient(
                                        gradient: Gradient(colors: [.emeraldLight, .amberGold, .cyan, .emeraldLight]),
                                        center: .center
                                    ),
                                    style: StrokeStyle(lineWidth: 14, lineCap: .round)
                                )
                                .rotationEffect(.degrees(-90))
                                .frame(width: 220, height: 220)
                                .animation(.spring(response: 0.35, dampingFraction: 0.7), value: progress)
                        }

                        // Central Numbers
                        VStack(spacing: 4) {
                            Text("\(count)")
                                .font(.system(size: 64, weight: .heavy, design: .rounded))
                                .foregroundColor(.white)
                                .scaleEffect(isBouncing ? 1.12 : 1.0)
                                .animation(.spring(response: 0.25, dampingFraction: 0.5), value: isBouncing)

                            if activeDhikr.target > 0 {
                                Text("Obiettivo: \(activeDhikr.target)")
                                    .font(.system(size: 15, weight: .bold, design: .rounded))
                                    .foregroundColor(.amberGold)
                            }

                            Text("Tocca ovunque")
                                .font(.system(size: 12, weight: .medium, design: .rounded))
                                .foregroundColor(.white.opacity(0.6))
                                .padding(.top, 4)
                        }
                    }
                }
                .buttonStyle(PlainButtonStyle())
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
    }

    private func incrementCount() {
        count += 1
        isBouncing = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            isBouncing = false
        }

        if activeDhikr.target > 0 && count % activeDhikr.target == 0 {
            let gen = UINotificationFeedbackGenerator()
            gen.notificationOccurred(.success)
        } else {
            let gen = UIImpactFeedbackGenerator(style: .medium)
            gen.impactOccurred()
        }
    }
}
