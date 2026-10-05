package com.nurpray.app.feature.tasbih

import androidx.lifecycle.ViewModel
import com.nurpray.app.domain.model.DhikrItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TasbihUiState(
    val currentDhikrIndex: Int = 0,
    val dhikrList: List<DhikrItem> = listOf(
        DhikrItem("1", "سُبْحَانَ اللَّهِ", "SubhanAllah", "Gloria ad Allah", 0, 33),
        DhikrItem("2", "الْحَمْدُ لِلَّهِ", "Alhamdulillah", "La lode appartiene ad Allah", 0, 33),
        DhikrItem("3", "اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah è il Più Grande", 0, 34),
        DhikrItem("4", "أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah", "Chiedo perdono ad Allah", 0, 100),
        DhikrItem("5", "لَا إِلٰهَ إِلَّا اللَّهُ", "La ilaha illallah", "Non c'è divinità all'infuori di Allah", 0, 100)
    ),
    val totalCountLifetime: Int = 0
) {
    val activeDhikr: DhikrItem
        get() = dhikrList[currentDhikrIndex]
}

class TasbihViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TasbihUiState())
    val uiState: StateFlow<TasbihUiState> = _uiState.asStateFlow()

    fun incrementCount(): Boolean {
        val current = _uiState.value
        val active = current.activeDhikr
        val newCount = active.count + 1
        var targetReached = false

        if (active.target > 0 && newCount >= active.target) {
            targetReached = true
        }

        val updatedList = current.dhikrList.toMutableList()
        updatedList[current.currentDhikrIndex] = active.copy(count = newCount)

        _uiState.value = current.copy(
            dhikrList = updatedList,
            totalCountLifetime = current.totalCountLifetime + 1
        )

        return targetReached
    }

    fun resetActiveCount() {
        val current = _uiState.value
        val updatedList = current.dhikrList.toMutableList()
        val active = current.activeDhikr
        updatedList[current.currentDhikrIndex] = active.copy(count = 0)
        _uiState.value = current.copy(dhikrList = updatedList)
    }

    fun selectDhikr(index: Int) {
        if (index in _uiState.value.dhikrList.indices) {
            _uiState.value = _uiState.value.copy(currentDhikrIndex = index)
        }
    }
}
