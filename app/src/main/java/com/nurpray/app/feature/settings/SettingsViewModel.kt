package com.nurpray.app.feature.settings

import androidx.lifecycle.ViewModel
import com.nurpray.app.data.astronomical.AsrJuristicMethod
import com.nurpray.app.data.astronomical.CalculationParameters
import com.nurpray.app.data.astronomical.HighLatitudeRule
import com.nurpray.app.data.astronomical.PrayerMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.nurpray.app.data.astronomical.PrayerAdjustments

data class SettingsUiState(
    val selectedMethod: PrayerMethod = PrayerMethod.UCOII_ITALY,
    val selectedAsrMethod: AsrJuristicMethod = AsrJuristicMethod.SHAFI_MALIKI_HANBALI,
    val selectedHighLatitudeRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
    val preAdhanMinutes: Int = 15,
    val isAutoDndEnabled: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val adjustments: PrayerAdjustments = PrayerAdjustments()
) {
    fun toCalculationParameters(): CalculationParameters {
        return CalculationParameters(
            method = selectedMethod,
            asrJuristicMethod = selectedAsrMethod,
            highLatitudeRule = selectedHighLatitudeRule,
            adjustments = adjustments
        )
    }
}

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateMethod(method: PrayerMethod) {
        _uiState.value = _uiState.value.copy(selectedMethod = method)
    }

    fun updateAsrMethod(method: AsrJuristicMethod) {
        _uiState.value = _uiState.value.copy(selectedAsrMethod = method)
    }

    fun updateHighLatitudeRule(rule: HighLatitudeRule) {
        _uiState.value = _uiState.value.copy(selectedHighLatitudeRule = rule)
    }

    fun updatePreAdhanMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(preAdhanMinutes = minutes)
    }

    fun toggleAutoDnd(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoDndEnabled = enabled)
    }

    fun toggleGps(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isGpsEnabled = enabled)
    }
}
