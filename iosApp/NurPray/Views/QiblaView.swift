import SwiftUI

public struct QiblaView: View {
    @ObservedObject var locationManager: LocationManager
    @State private var hasHapticFired = false

    public init(locationManager: LocationManager) {
        self.locationManager = locationManager
    }

    public var body: some View {
        ZStack {
            LiquidBackgroundView()

            VStack(spacing: 24) {
                // Header
                VStack(spacing: 4) {
                    Text("Bussola Qibla")
                        .font(.system(size: 28, weight: .bold, design: .rounded))
                        .foregroundColor(.primary)

                    Text(locationManager.cityName)
                        .font(.system(size: 15, weight: .medium, design: .rounded))
                        .foregroundColor(.secondary)
                }
                .padding(.top, 16)

                Spacer()

                // 3D Liquid Glass Compass Dial
                ZStack {
                    // Outer Bezel
                    Circle()
                        .fill(.ultraThinMaterial)
                        .frame(width: 290, height: 290)
                        .overlay(
                            Circle()
                                .strokeBorder(
                                    LinearGradient(
                                        colors: [.white.opacity(0.8), .clear, .white.opacity(0.3)],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    ),
                                    lineWidth: 2
                                )
                        )
                        .shadow(
                            color: locationManager.qiblaBearing.isAligned ? Color.emeraldLight.opacity(0.5) : Color.black.opacity(0.2),
                            radius: locationManager.qiblaBearing.isAligned ? 30 : 15
                        )

                    // Compass Rose (Rotates with device heading)
                    ZStack {
                        // Degree ticks
                        ForEach(0..<24) { i in
                            Rectangle()
                                .fill(i % 6 == 0 ? Color.primary : Color.secondary.opacity(0.3))
                                .frame(width: i % 6 == 0 ? 3 : 1.5, height: i % 6 == 0 ? 14 : 8)
                                .offset(y: -130)
                                .rotationEffect(.degrees(Double(i) * 15))
                        }

                        // Cardinal points
                        Text("N")
                            .font(.system(size: 18, weight: .heavy, design: .rounded))
                            .foregroundColor(.red)
                            .offset(y: -108)

                        Text("S")
                            .font(.system(size: 16, weight: .bold, design: .rounded))
                            .foregroundColor(.secondary)
                            .offset(y: 108)

                        Text("E")
                            .font(.system(size: 16, weight: .bold, design: .rounded))
                            .foregroundColor(.secondary)
                            .offset(x: 108)

                        Text("O")
                            .font(.system(size: 16, weight: .bold, design: .rounded))
                            .foregroundColor(.secondary)
                            .offset(x: -108)
                    }
                    .rotationEffect(.degrees(-locationManager.heading))
                    .animation(.spring(response: 0.35, dampingFraction: 0.7), value: locationManager.heading)

                    // Kaaba Needle / Pointer
                    ZStack {
                        // Kaaba Direction Marker (Gold & Emerald)
                        VStack(spacing: 0) {
                            // Kaaba icon
                            Text("🕋")
                                .font(.system(size: 26))
                                .shadow(color: .amberGold, radius: 8)
                                .offset(y: -110)

                            Spacer()
                        }
                    }
                    .rotationEffect(.degrees(locationManager.qiblaBearing.relativeAngleDegrees))
                    .animation(.spring(response: 0.4, dampingFraction: 0.75), value: locationManager.qiblaBearing.relativeAngleDegrees)

                    // Center Glass Orb
                    Circle()
                        .fill(locationManager.qiblaBearing.isAligned ? Color.emeraldLight : Color.amberGold)
                        .frame(width: 24, height: 24)
                        .overlay(Circle().stroke(Color.white, lineWidth: 3))
                        .shadow(color: locationManager.qiblaBearing.isAligned ? .emeraldLight : .amberGold, radius: 10)
                }

                Spacer()

                // Info Glass Card (Azimuth, Distance, Alignment)
                VStack(spacing: 14) {
                    HStack {
                        VStack(spacing: 2) {
                            Text("Direzione Kaaba")
                                .font(.system(size: 13, weight: .medium, design: .rounded))
                                .foregroundColor(.secondary)
                            Text("\(Int(locationManager.qiblaBearing.qiblaDirectionDegrees))°")
                                .font(.system(size: 22, weight: .bold, design: .rounded))
                                .foregroundColor(.emeraldLight)
                        }
                        .frame(maxWidth: .infinity)

                        Divider()
                            .frame(height: 36)

                        VStack(spacing: 2) {
                            Text("Distanza")
                                .font(.system(size: 13, weight: .medium, design: .rounded))
                                .foregroundColor(.secondary)
                            Text("\(Int(locationManager.qiblaBearing.distanceToKaabaKm)) km")
                                .font(.system(size: 22, weight: .bold, design: .rounded))
                                .foregroundColor(.emeraldLight)
                        }
                        .frame(maxWidth: .infinity)
                    }

                    // Alignment Pill
                    HStack(spacing: 8) {
                        Image(systemName: locationManager.qiblaBearing.isAligned ? "checkmark.circle.fill" : "safari.fill")
                            .foregroundColor(locationManager.qiblaBearing.isAligned ? .emeraldLight : .secondary)

                        Text(locationManager.qiblaBearing.isAligned ? "Allineato alla Qibla! 🕋" : "Ruota verso la Kaaba dorata")
                            .font(.system(size: 14, weight: locationManager.qiblaBearing.isAligned ? .bold : .medium, design: .rounded))
                            .foregroundColor(locationManager.qiblaBearing.isAligned ? .emeraldLight : .secondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .fill(locationManager.qiblaBearing.isAligned ? Color.emeraldLight.opacity(0.22) : Color.white.opacity(0.06))
                    )
                }
                .padding(16)
                .liquidGlassCard(
                    cornerRadius: 26,
                    isHighlighted: locationManager.qiblaBearing.isAligned,
                    highlightColor: .emeraldLight
                )
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
        .onChange(of: locationManager.qiblaBearing.isAligned) { isAligned in
            if isAligned && !hasHapticFired {
                let generator = UIImpactFeedbackGenerator(style: .rigid)
                generator.impactOccurred()
                hasHapticFired = true
            } else if !isAligned {
                hasHapticFired = false
            }
        }
    }
}
