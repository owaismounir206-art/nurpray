package com.nurpray.app.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurpray.app.core.alarm.PrayerAlarmScheduler
import com.nurpray.app.data.astronomical.CalculationParameters
import com.nurpray.app.data.local.database.entity.CityEntity
import com.nurpray.app.data.repository.CityRepository
import com.nurpray.app.domain.model.LocationCoordinates
import com.nurpray.app.domain.model.PrayerType
import com.nurpray.app.domain.usecase.GetTodayPrayerTimesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val getTodayPrayerTimesUseCase = GetTodayPrayerTimesUseCase()
    private val alarmScheduler = PrayerAlarmScheduler(application)
    private val cityRepository = CityRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _citiesList = MutableStateFlow<List<CityEntity>>(emptyList())
    val citiesList: StateFlow<List<CityEntity>> = _citiesList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isGpsLoading = MutableStateFlow(false)
    val isGpsLoading: StateFlow<Boolean> = _isGpsLoading.asStateFlow()

    private var calculationParams = CalculationParameters()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            cityRepository.ensureDatabasePopulated()
            searchCities("")
        }
        refreshPrayerSchedule()
        startCountdownTicker()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(200) // Debounce typing
            searchCities(query)
        }
    }

    private suspend fun searchCities(query: String) {
        cityRepository.searchCitiesWorldwide(query).collect { results ->
            _citiesList.value = results
        }
    }

    fun requestGpsLocation() {
        viewModelScope.launch {
            _isGpsLoading.value = true
            val coords = cityRepository.getCurrentGpsLocation()
            _isGpsLoading.value = false
            if (coords != null) {
                updateLocation(coords)
            }
        }
    }

    fun updateLocation(location: LocationCoordinates) {
        _uiState.value = _uiState.value.copy(location = location)
        refreshPrayerSchedule()
        rescheduleAlarms()
    }

    fun updateCalculationParameters(params: CalculationParameters) {
        this.calculationParams = params
        refreshPrayerSchedule()
        rescheduleAlarms()
    }

    fun refreshPrayerSchedule() {
        val loc = _uiState.value.location
        val zoneId = ZoneId.systemDefault()
        val schedule = getTodayPrayerTimesUseCase(
            latitude = loc.latitude,
            longitude = loc.longitude,
            zoneId = zoneId,
            date = LocalDate.now(zoneId),
            now = LocalTime.now(zoneId),
            params = calculationParams
        )

        _uiState.value = _uiState.value.copy(
            schedule = schedule,
            activePrayerType = schedule.currentPrayer?.type ?: PrayerType.FAJR
        )
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (isActive) {
                val currentSchedule = _uiState.value.schedule
                if (currentSchedule != null) {
                    val loc = _uiState.value.location
                    val zoneId = ZoneId.systemDefault()
                    val updatedSchedule = getTodayPrayerTimesUseCase(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        zoneId = zoneId,
                        date = LocalDate.now(zoneId),
                        now = LocalTime.now(zoneId),
                        params = calculationParams
                    )

                    val remainingMillis = updatedSchedule.timeRemainingMillis
                    val hours = remainingMillis / (1000 * 60 * 60)
                    val minutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (remainingMillis % (1000 * 60)) / 1000

                    val formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    _uiState.value = _uiState.value.copy(
                        schedule = updatedSchedule,
                        formattedCountdown = formatted,
                        activePrayerType = updatedSchedule.currentPrayer?.type ?: PrayerType.FAJR
                    )
                }
                delay(1000)
            }
        }
    }

    private fun rescheduleAlarms() {
        viewModelScope.launch {
            val loc = _uiState.value.location
            alarmScheduler.cancelAllAlarms()
            alarmScheduler.scheduleWeekAlarms(
                latitude = loc.latitude,
                longitude = loc.longitude,
                zoneId = ZoneId.systemDefault(),
                params = calculationParams
            )
        }
    }
}
