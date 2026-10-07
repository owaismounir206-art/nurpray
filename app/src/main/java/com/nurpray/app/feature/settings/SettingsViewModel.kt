package com.nurpray.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

    // --- In-App Auto-Updater Logic ---
    private val updateManager = com.nurpray.app.core.updater.AppUpdateManager()

    private val _updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateUiState.Checking
            when (val result = updateManager.checkForUpdates()) {
                is com.nurpray.app.core.updater.UpdateCheckResult.UpdateAvailable -> {
                    _updateState.value = UpdateUiState.Available(
                        version = result.latestVersion,
                        notes = result.releaseNotes,
                        url = result.downloadUrl,
                        sizeMb = result.apkSizeMb
                    )
                }
                is com.nurpray.app.core.updater.UpdateCheckResult.UpToDate -> {
                    _updateState.value = UpdateUiState.UpToDate
                }
                is com.nurpray.app.core.updater.UpdateCheckResult.Error -> {
                    _updateState.value = UpdateUiState.Error(result.message)
                }
            }
        }
    }

    fun downloadAndInstall(context: android.content.Context, url: String) {
        viewModelScope.launch {
            _updateState.value = UpdateUiState.Downloading(0f)
            val result = updateManager.downloadAndInstallApk(
                context = context,
                downloadUrl = url,
                onProgress = { progress ->
                    _updateState.value = UpdateUiState.Downloading(progress)
                }
            )
            if (result.isFailure) {
                _updateState.value = UpdateUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Errore download APK"
                )
            }
        }
    }

    private val _currentLanguage = MutableStateFlow(getCurrentLanguageTag())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(languageCode: String) {
        _currentLanguage.value = languageCode
        if (languageCode.isEmpty()) {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.getEmptyLocaleList()
            )
        } else {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
            )
        }
    }

    fun getCurrentLanguageTag(): String {
        val locales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "" else locales.toLanguageTags()
    }
}

data class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
)

val APP_SUPPORTED_LANGUAGES = listOf(
    SupportedLanguage("", "System Default", "Predefinita di sistema"),
    SupportedLanguage("en", "English", "English"),
    SupportedLanguage("ar", "Arabic", "العربية"),
    SupportedLanguage("ur", "Urdu", "اردو"),
    SupportedLanguage("id", "Indonesian", "Bahasa Indonesia"),
    SupportedLanguage("tr", "Turkish", "Türkçe"),
    SupportedLanguage("fr", "French", "Français"),
    SupportedLanguage("es", "Spanish", "Español"),
    SupportedLanguage("it", "Italian", "Italiano"),
    SupportedLanguage("de", "German", "Deutsch"),
    SupportedLanguage("ru", "Russian", "Русский"),
    SupportedLanguage("bn", "Bengali", "বাংলা"),
    SupportedLanguage("hi", "Hindi", "हिन्दी"),
    SupportedLanguage("fa", "Persian", "فارسی"),
    SupportedLanguage("zh", "Chinese", "简体中文"),
    SupportedLanguage("pt", "Portuguese", "Português"),
    SupportedLanguage("ja", "Japanese", "日本語")
)

sealed class UpdateUiState {
    data object Idle : UpdateUiState()
    data object Checking : UpdateUiState()
    data class Available(val version: String, val notes: String, val url: String, val sizeMb: Double) : UpdateUiState()
    data class Downloading(val progress: Float) : UpdateUiState()
    data object UpToDate : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

