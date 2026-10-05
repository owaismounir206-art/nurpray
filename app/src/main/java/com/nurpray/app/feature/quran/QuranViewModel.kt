package com.nurpray.app.feature.quran

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SurahModel(
    val number: Int,
    val arabicName: String,
    val transliteration: String,
    val englishMeaning: String,
    val totalVerses: Int,
    val revelationType: String // Meccan / Medinan
)

data class AyahModel(
    val numberInSurah: Int,
    val arabicText: String,
    val italianTranslation: String
)

data class QuranUiState(
    val surahs: List<SurahModel> = listOf(
        SurahModel(1, "الفاتحة", "Al-Fatihah", "L'Aprente", 7, "Meccana"),
        SurahModel(2, "البقرة", "Al-Baqarah", "La Giovenca", 286, "Medinese"),
        SurahModel(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Meccana"),
        SurahModel(55, "الرحمن", "Ar-Rahman", "Il Compassionevole", 78, "Medinese"),
        SurahModel(67, "الملك", "Al-Mulk", "La Sovranità", 30, "Meccana"),
        SurahModel(112, "الإخلاص", "Al-Ikhlas", "Il Monoteismo Puro", 4, "Meccana"),
        SurahModel(113, "الفلق", "Al-Falaq", "L'Alba Nascente", 5, "Meccana"),
        SurahModel(114, "الناس", "An-Nas", "Gli Uomini", 6, "Meccana")
    ),
    val selectedSurah: SurahModel? = null,
    val ayahs: List<AyahModel> = emptyList(),
    val searchQuery: String = ""
)

class QuranViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    fun selectSurah(surah: SurahModel) {
        // Sample Surah Al-Fatihah ayahs
        val sampleAyahs = if (surah.number == 1) {
            listOf(
                AyahModel(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Nel nome di Allah, il Compassionevole, il Misericordioso"),
                AyahModel(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "La lode appartiene ad Allah, Signore dei mondi"),
                AyahModel(3, "الرَّحْمَٰنِ الرَّحِيمِ", "Il Compassionevole, il Misericordioso"),
                AyahModel(4, "مَالِكِ يَوْمِ الدِّينِ", "Sovrano del Giorno del Giudizio"),
                AyahModel(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Te solo noi adoriamo e a Te solo chiediamo aiuto"),
                AyahModel(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guidaci sulla retta via"),
                AyahModel(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "La via di coloro che hai colmato di grazia, non di coloro che sono incorsi nella Tua ira, né degli sviati.")
            )
        } else if (surah.number == 112) {
            listOf(
                AyahModel(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Di': «Egli, Allah, è Unico»"),
                AyahModel(2, "اللَّهُ الصَّمَدُ", "«Allah è l'Assoluto»"),
                AyahModel(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "«Non ha generato, non è stato generato»"),
                AyahModel(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "«E nessuno è uguale a Lui»")
            )
        } else {
            listOf(
                AyahModel(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Nel nome di Allah, il Compassionevole, il Misericordioso"),
                AyahModel(2, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Benedetto Colui nelle cui mani è il regno, ed Egli su ogni cosa ha potere.")
            )
        }

        _uiState.value = _uiState.value.copy(
            selectedSurah = surah,
            ayahs = sampleAyahs
        )
    }

    fun backToSurahList() {
        _uiState.value = _uiState.value.copy(selectedSurah = null, ayahs = emptyList())
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
