import SwiftUI

public struct SurahSummary: Identifiable, Equatable {
    public var id: Int { number }
    public let number: Int
    public let transliteration: String
    public let englishMeaning: String
    public let arabicName: String
    public let totalVerses: Int
}

public struct AyahItem: Identifiable, Equatable {
    public var id: Int { numberInSurah }
    public let numberInSurah: Int
    public let arabicText: String
    public let italianTranslation: String
}

public struct QuranView: View {
    @State private var selectedSurah: SurahSummary? = nil
    @State private var searchText: String = ""

    public let surahs: [SurahSummary] = [
        SurahSummary(number: 1, transliteration: "Al-Fatihah", englishMeaning: "L'Apertura", arabicName: "الفاتحة", totalVerses: 7),
        SurahSummary(number: 2, transliteration: "Al-Baqarah", englishMeaning: "La Giovenca", arabicName: "البقرة", totalVerses: 286),
        SurahSummary(number: 3, transliteration: "Al-Imran", englishMeaning: "La Famiglia di Imran", arabicName: "آل عمران", totalVerses: 200),
        SurahSummary(number: 18, transliteration: "Al-Kahf", englishMeaning: "La Caverna", arabicName: "الكهف", totalVerses: 110),
        SurahSummary(number: 36, transliteration: "Ya-Sin", englishMeaning: "Ya Sin", arabicName: "يس", totalVerses: 83),
        SurahSummary(number: 55, transliteration: "Ar-Rahman", englishMeaning: "Il Misericordioso", arabicName: "الرحمن", totalVerses: 78),
        SurahSummary(number: 67, transliteration: "Al-Mulk", englishMeaning: "Il Dominio", arabicName: "الملك", totalVerses: 30),
        SurahSummary(number: 112, transliteration: "Al-Ikhlas", englishMeaning: "La Fede Pura", arabicName: "الإخلاص", totalVerses: 4),
        SurahSummary(number: 113, transliteration: "Al-Falaq", englishMeaning: "L'Alba Nascente", arabicName: "الفلق", totalVerses: 5),
        SurahSummary(number: 114, transliteration: "An-Nas", englishMeaning: "Gli Uomini", arabicName: "الناس", totalVerses: 6)
    ]

    public let sampleAyahs: [AyahItem] = [
        AyahItem(numberInSurah: 1, arabicText: "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", italianTranslation: "In nome di Allah, il Compassionevole, il Misericordioso."),
        AyahItem(numberInSurah: 2, arabicText: "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", italianTranslation: "La lode appartiene ad Allah, Signore dei mondi,"),
        AyahItem(numberInSurah: 3, arabicText: "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", italianTranslation: "il Compassionevole, il Misericordioso,"),
        AyahItem(numberInSurah: 4, arabicText: "مَٰلِكِ يَوْمِ ٱلدِّينِ", italianTranslation: "Re del Giorno del Giudizio."),
        AyahItem(numberInSurah: 5, arabicText: "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", italianTranslation: "Te solo noi adoriamo e a Te solo chiediamo soccorso."),
        AyahItem(numberInSurah: 6, arabicText: "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", italianTranslation: "Guidaci sulla retta via,"),
        AyahItem(numberInSurah: 7, arabicText: "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", italianTranslation: "la via di coloro che hai colmato di grazia, non di coloro che sono incorsi nella Tua ira, né degli sviati.")
    ]

    public init() {}

    private var filteredSurahs: [SurahSummary] {
        if searchText.isEmpty { return surahs }
        return surahs.filter {
            $0.transliteration.localizedCaseInsensitiveContains(searchText) ||
            $0.englishMeaning.localizedCaseInsensitiveContains(searchText) ||
            "\($0.number)".contains(searchText)
        }
    }

    public var body: some View {
        ZStack {
            LiquidBackgroundView()

            VStack(spacing: 16) {
                // Header Bar
                HStack {
                    if let surah = selectedSurah {
                        Button(action: {
                            selectedSurah = nil
                        }) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(.emeraldLight)
                        }

                        Text("\(surah.number). \(surah.transliteration)")
                            .font(.system(size: 24, weight: .bold, design: .rounded))
                            .foregroundColor(.primary)
                    } else {
                        Text("Sacro Corano")
                            .font(.system(size: 28, weight: .bold, design: .rounded))
                            .foregroundColor(.primary)
                    }

                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)

                if selectedSurah == nil {
                    // Search Bar
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.secondary)
                        TextField("Cerca surah...", text: $searchText)
                            .font(.system(size: 15, design: .rounded))
                    }
                    .padding(12)
                    .background(Color.white.opacity(0.08))
                    .cornerRadius(16)
                    .padding(.horizontal, 16)

                    // Surah List
                    ScrollView(showsIndicators: false) {
                        LazyVStack(spacing: 10) {
                            ForEach(filteredSurahs) { surah in
                                Button(action: {
                                    selectedSurah = surah
                                }) {
                                    HStack {
                                        ZStack {
                                            Circle()
                                                .fill(Color.amberGold.opacity(0.2))
                                                .frame(width: 36, height: 36)
                                            Text("\(surah.number)")
                                                .font(.system(size: 14, weight: .bold, design: .rounded))
                                                .foregroundColor(.amberGold)
                                        }

                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(surah.transliteration)
                                                .font(.system(size: 16, weight: .bold, design: .rounded))
                                                .foregroundColor(.primary)
                                            Text("\(surah.englishMeaning) • \(surah.totalVerses) versetti")
                                                .font(.system(size: 12, weight: .medium, design: .rounded))
                                                .foregroundColor(.secondary)
                                        }

                                        Spacer()

                                        Text(surah.arabicName)
                                            .font(.system(size: 20, weight: .bold))
                                            .foregroundColor(.emeraldLight)
                                    }
                                    .padding(14)
                                    .liquidGlassCard(cornerRadius: 20)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                } else {
                    // Ayah Reader
                    ScrollView(showsIndicators: false) {
                        LazyVStack(spacing: 16) {
                            ForEach(sampleAyahs) { ayah in
                                VStack(alignment: .leading, spacing: 12) {
                                    HStack {
                                        Text("Versetto \(ayah.numberInSurah)")
                                            .font(.system(size: 11, weight: .bold, design: .rounded))
                                            .padding(.horizontal, 8)
                                            .padding(.vertical, 4)
                                            .background(Color.emeraldLight.opacity(0.25))
                                            .foregroundColor(.emeraldLight)
                                            .cornerRadius(8)
                                        Spacer()
                                    }

                                    Text(ayah.arabicText)
                                        .font(.system(size: 24, weight: .bold))
                                        .multilineTextAlignment(.trailing)
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                        .lineSpacing(10)
                                        .foregroundColor(.primary)

                                    Divider()
                                        .background(Color.white.opacity(0.1))

                                    Text(ayah.italianTranslation)
                                        .font(.system(size: 14, weight: .medium, design: .rounded))
                                        .foregroundColor(.secondary)
                                        .lineSpacing(4)
                                }
                                .padding(16)
                                .liquidGlassCard(cornerRadius: 24)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }
        }
    }
}
