import SwiftUI

public struct SettingsView: View {
    @Binding var selectedMethod: PrayerMethod
    @Binding var selectedAsrMethod: AsrJuristicMethod
    @State private var preAdhanMinutes: Double = 15
    @State private var isAutoDndEnabled: Bool = false
    @State private var showMethodSheet: Bool = false

    public init(
        selectedMethod: Binding<PrayerMethod> = .constant(.ucoiiItaly),
        selectedAsrMethod: Binding<AsrJuristicMethod> = .constant(.shafiMalikiHanbali)
    ) {
        self._selectedMethod = selectedMethod
        self._selectedAsrMethod = selectedAsrMethod
    }

    public var body: some View {
        ZStack {
            LiquidBackgroundView()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 20) {
                    // Header
                    HStack {
                        Text("Impostazioni")
                            .font(.system(size: 28, weight: .bold, design: .rounded))
                            .foregroundColor(.primary)
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)

                    // Astronomical Calculation Section
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Calcolo Astronomico")
                            .font(.system(size: 14, weight: .bold, design: .rounded))
                            .foregroundColor(.emeraldLight)
                            .padding(.horizontal, 4)

                        VStack(spacing: 0) {
                            Button(action: {
                                showMethodSheet = true
                            }) {
                                HStack {
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text("Convenzione di Calcolo")
                                            .font(.system(size: 16, weight: .semibold, design: .rounded))
                                            .foregroundColor(.primary)
                                        Text(selectedMethod.title)
                                            .font(.system(size: 13, weight: .medium, design: .rounded))
                                            .foregroundColor(.secondary)
                                            .multilineTextAlignment(.leading)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(.secondary)
                                }
                                .padding(16)
                            }
                            .buttonStyle(PlainButtonStyle())

                            Divider()
                                .background(Color.white.opacity(0.1))

                            HStack {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Metodo Giuridico Asr")
                                        .font(.system(size: 16, weight: .semibold, design: .rounded))
                                        .foregroundColor(.primary)
                                    Text(selectedAsrMethod.title)
                                        .font(.system(size: 13, weight: .medium, design: .rounded))
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Picker("", selection: $selectedAsrMethod) {
                                    Text("Shafi'i (1:1)").tag(AsrJuristicMethod.shafiMalikiHanbali)
                                    Text("Hanafi (2:1)").tag(AsrJuristicMethod.hanafi)
                                }
                                .pickerStyle(MenuPickerStyle())
                            }
                            .padding(16)
                        }
                        .liquidGlassCard(cornerRadius: 24)
                    }
                    .padding(.horizontal, 16)

                    // Alarms & Pre-Adhan Section
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Allarmi e Notifiche")
                            .font(.system(size: 14, weight: .bold, design: .rounded))
                            .foregroundColor(.emeraldLight)
                            .padding(.horizontal, 4)

                        VStack(spacing: 16) {
                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    Text("Pre-Allarme Adhan")
                                        .font(.system(size: 16, weight: .semibold, design: .rounded))
                                    Spacer()
                                    Text("\(Int(preAdhanMinutes)) min prima")
                                        .font(.system(size: 14, weight: .bold, design: .rounded))
                                        .foregroundColor(.emeraldLight)
                                }
                                Slider(value: $preAdhanMinutes, in: 0...30, step: 5)
                                    .accentColor(.emeraldLight)
                            }

                            Divider()
                                .background(Color.white.opacity(0.1))

                            Toggle(isOn: $isAutoDndEnabled) {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Silenzioso durante la preghiera")
                                        .font(.system(size: 16, weight: .semibold, design: .rounded))
                                    Text("Attiva Non Disturbare per 20 minuti")
                                        .font(.system(size: 12, weight: .medium, design: .rounded))
                                        .foregroundColor(.secondary)
                                }
                            }
                            .tint(.emeraldLight)
                        }
                        .padding(18)
                        .liquidGlassCard(cornerRadius: 24)
                    }
                    .padding(.horizontal, 16)

                    // Privacy & Open Source Banner
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "lock.shield.fill")
                                .foregroundColor(.emeraldLight)
                            Text("Privacy & Open Source")
                                .font(.system(size: 16, weight: .bold, design: .rounded))
                                .foregroundColor(.emeraldLight)
                        }

                        Text("NurPray per iOS è 100% offline-first, gratuito e privo di tracker o pubblicità. Tutti i calcoli solari e della Qibla avvengono localmente sul tuo iPhone.")
                            .font(.system(size: 13, weight: .medium, design: .rounded))
                            .foregroundColor(.secondary)
                            .lineSpacing(3)

                        Text("Versione 1.3.0 • Design Liquid Glass con animazioni fluide a 120Hz ProMotion")
                            .font(.system(size: 11, weight: .semibold, design: .rounded))
                            .foregroundColor(.secondary.opacity(0.8))
                            .padding(.top, 4)
                    }
                    .padding(18)
                    .liquidGlassCard(cornerRadius: 24)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 32)
                }
            }
        }
        .sheet(isPresented: $showMethodSheet) {
            NavigationView {
                ZStack {
                    LiquidBackgroundView()

                    List(PrayerMethod.allCases) { method in
                        Button(action: {
                            selectedMethod = method
                            showMethodSheet = false
                        }) {
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(method.title)
                                        .font(.system(size: 15, weight: method == selectedMethod ? .bold : .medium, design: .rounded))
                                        .foregroundColor(method == selectedMethod ? .emeraldLight : .primary)
                                }
                                Spacer()
                                if method == selectedMethod {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(.emeraldLight)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                    }
                    .scrollContentBackground(.hidden)
                }
                .navigationTitle("Convenzioni Mondiali")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Chiudi") { showMethodSheet = false }
                    }
                }
            }
        }
    }
}
