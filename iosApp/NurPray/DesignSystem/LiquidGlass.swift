import SwiftUI

// MARK: - Color Palette
public extension Color {
    static let emeraldDeep = Color(red: 15/255, green: 81/255, blue: 50/255)
    static let emeraldLight = Color(red: 25/255, green: 135/255, blue: 84/255)
    static let emeraldNeon = Color(red: 32/255, green: 201/255, blue: 151/255)
    static let amberGold = Color(red: 229/255, green: 169/255, blue: 60/255)
    static let amberLight = Color(red: 255/255, green: 213/255, blue: 107/255)
    static let sapphireNight = Color(red: 20/255, green: 54/255, blue: 66/255)
}

// MARK: - Liquid Background
/// GPU-accelerated animated organic mesh background with continuous 120Hz ProMotion flow
public struct LiquidBackgroundView: View {
    @State private var phase1: CGFloat = 0.0
    @State private var phase2: CGFloat = 0.0
    @Environment(\.colorScheme) var colorScheme

    public init() {}

    public var body: some View {
        TimelineView(.animation) { timeline in
            Canvas { context, size in
                let now = timeline.date.timeIntervalSinceReferenceDate
                let p1 = now * 0.4
                let p2 = now * 0.25

                let width = size.width
                let height = size.height

                // Orb 1: Emerald Life
                let orb1X = width * (0.35 + 0.22 * cos(p1))
                let orb1Y = height * (0.28 + 0.18 * sin(p1))
                let orb1Rect = CGRect(x: orb1X - width * 0.5, y: orb1Y - width * 0.5, width: width, height: width)
                context.fill(
                    Circle().path(in: orb1Rect),
                    with: .radialGradient(
                        Gradient(colors: [
                            Color.emeraldDeep.opacity(colorScheme == .dark ? 0.65 : 0.45),
                            Color.emeraldLight.opacity(0.2),
                            Color.clear
                        ]),
                        center: CGPoint(x: orb1X, y: orb1Y),
                        startRadius: 0,
                        endRadius: width * 0.55
                    )
                )

                // Orb 2: Oceanic Sapphire
                let orb2X = width * (0.72 + 0.18 * sin(p2))
                let orb2Y = height * (0.65 + 0.20 * cos(p2))
                let orb2Rect = CGRect(x: orb2X - width * 0.55, y: orb2Y - width * 0.55, width: width * 1.1, height: width * 1.1)
                context.fill(
                    Circle().path(in: orb2Rect),
                    with: .radialGradient(
                        Gradient(colors: [
                            Color.sapphireNight.opacity(colorScheme == .dark ? 0.75 : 0.4),
                            Color.cyan.opacity(0.15),
                            Color.clear
                        ]),
                        center: CGPoint(x: orb2X, y: orb2Y),
                        startRadius: 0,
                        endRadius: width * 0.6
                    )
                )

                // Orb 3: Warm Amber Sunrise
                let orb3X = width * (0.42 - 0.20 * cos(p1 * 0.7))
                let orb3Y = height * (0.85 + 0.14 * sin(p2 * 0.8))
                let orb3Rect = CGRect(x: orb3X - width * 0.45, y: orb3Y - width * 0.45, width: width * 0.9, height: width * 0.9)
                context.fill(
                    Circle().path(in: orb3Rect),
                    with: .radialGradient(
                        Gradient(colors: [
                            Color.amberGold.opacity(colorScheme == .dark ? 0.45 : 0.35),
                            Color.amberLight.opacity(0.12),
                            Color.clear
                        ]),
                        center: CGPoint(x: orb3X, y: orb3Y),
                        startRadius: 0,
                        endRadius: width * 0.5
                    )
                )
            }
        }
        .background(colorScheme == .dark ? Color(red: 9/255, green: 13/255, blue: 11/255) : Color(red: 242/255, green: 246/255, blue: 244/255))
        .ignoresSafeArea()
    }
}

// MARK: - Liquid Glass Card Modifier
public struct LiquidGlassCardModifier: ViewModifier {
    var cornerRadius: CGFloat
    var isHighlighted: Bool
    var highlightColor: Color

    @Environment(\.colorScheme) var colorScheme

    public func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(.ultraThinMaterial)
            )
            .background(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(
                        isHighlighted
                            ? highlightColor.opacity(colorScheme == .dark ? 0.18 : 0.12)
                            : (colorScheme == .dark ? Color.white.opacity(0.04) : Color.white.opacity(0.4))
                    )
            )
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .strokeBorder(
                        LinearGradient(
                            colors: isHighlighted ? [
                                highlightColor.opacity(0.9),
                                Color.white.opacity(0.6),
                                highlightColor.opacity(0.2),
                                Color.clear
                            ] : [
                                Color.white.opacity(colorScheme == .dark ? 0.45 : 0.85),
                                Color.white.opacity(0.12),
                                Color.clear,
                                Color.white.opacity(colorScheme == .dark ? 0.2 : 0.4)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: isHighlighted ? 1.4 : 1.0
                    )
            )
            .shadow(
                color: isHighlighted ? highlightColor.opacity(0.3) : Color.black.opacity(colorScheme == .dark ? 0.25 : 0.08),
                radius: isHighlighted ? 16 : 8,
                x: 0,
                y: isHighlighted ? 8 : 4
            )
    }
}

public extension View {
    func liquidGlassCard(
        cornerRadius: CGFloat = 24,
        isHighlighted: Bool = false,
        highlightColor: Color = .amberGold
    ) -> some View {
        modifier(LiquidGlassCardModifier(
            cornerRadius: cornerRadius,
            isHighlighted: isHighlighted,
            highlightColor: highlightColor
        ))
    }
}

// MARK: - Liquid Spring Button
public struct LiquidButton<Content: View>: View {
    let action: () -> Void
    @ViewBuilder let content: () -> Content

    @State private var isPressed: Bool = false

    public init(action: @escaping () -> Void, @ViewBuilder content: @escaping () -> Content) {
        self.action = action
        self.content = content
    }

    public var body: some View {
        Button(action: {
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
            action()
        }) {
            content()
        }
        .buttonStyle(ScaleSpringButtonStyle())
    }
}

public struct ScaleSpringButtonStyle: ButtonStyle {
    public func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.spring(response: 0.35, dampingFraction: 0.65), value: configuration.isPressed)
    }
}

// MARK: - Liquid Progress Arc
public struct LiquidProgressArc: View {
    public let progress: Float
    public var size: CGFloat = 210

    public init(progress: Float, size: CGFloat = 210) {
        self.progress = progress
        self.size = size
    }

    public var body: some View {
        ZStack {
            // Track
            Circle()
                .trim(from: 0.125, to: 0.875)
                .stroke(
                    Color.white.opacity(0.15),
                    style: StrokeStyle(lineWidth: 12, lineCap: .round)
                )
                .rotationEffect(.degrees(90))
                .frame(width: size, height: size)

            // Active Liquid Arc
            Circle()
                .trim(from: 0.125, to: 0.125 + CGFloat(min(1.0, max(0.0, progress))) * 0.75)
                .stroke(
                    AngularGradient(
                        gradient: Gradient(colors: [.amberGold, .amberLight, .emeraldNeon, .amberGold]),
                        center: .center
                    ),
                    style: StrokeStyle(lineWidth: 12, lineCap: .round)
                )
                .rotationEffect(.degrees(90))
                .frame(width: size, height: size)
                .animation(.spring(response: 0.6, dampingFraction: 0.8), value: progress)
        }
    }
}
