package com.nurpray.app.feature.qibla

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurpray.app.domain.model.QiblaBearing
import com.nurpray.app.domain.usecase.CalculateQiblaBearingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QiblaUiState(
    val bearing: QiblaBearing = QiblaBearing(
        qiblaDirectionDegrees = 124f,
        deviceHeadingDegrees = 0f,
        relativeAngleDegrees = 124f,
        distanceToKaabaKm = 3600.0,
        isAligned = false
    ),
    val sensorAccuracy: Int = 3, // 3 = High accuracy
    val userLatitude: Double = 41.9028,
    val userLongitude: Double = 12.4964,
    val cityName: String = "Roma, Italia"
)

class QiblaViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = QiblaSensorManager(application)
    private val calculateQiblaBearingUseCase = CalculateQiblaBearingUseCase()

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    init {
        sensorManager.updateLocation(_uiState.value.userLatitude, _uiState.value.userLongitude)
        startCompass()
    }

    fun setLocation(lat: Double, lon: Double, city: String) {
        _uiState.value = _uiState.value.copy(
            userLatitude = lat,
            userLongitude = lon,
            cityName = city
        )
        sensorManager.updateLocation(lat, lon)
    }

    private fun startCompass() {
        sensorManager.startListening()

        viewModelScope.launch {
            sensorManager.azimuthFlow.collect { heading ->
                val current = _uiState.value
                val bearing = calculateQiblaBearingUseCase(
                    userLatitude = current.userLatitude,
                    userLongitude = current.userLongitude,
                    deviceHeading = heading
                )
                _uiState.value = current.copy(bearing = bearing)
            }
        }

        viewModelScope.launch {
            sensorManager.accuracyFlow.collect { accuracy ->
                _uiState.value = _uiState.value.copy(sensorAccuracy = accuracy)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
    }
}
