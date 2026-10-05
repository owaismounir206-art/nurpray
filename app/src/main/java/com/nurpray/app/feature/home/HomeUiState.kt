package com.nurpray.app.feature.home

import com.nurpray.app.domain.model.LocationCoordinates
import com.nurpray.app.domain.model.PrayerType
import com.nurpray.app.domain.model.TodayPrayerSchedule
import java.time.LocalDate
import java.time.LocalTime

data class HomeUiState(
    val isLoading: Boolean = false,
    val location: LocationCoordinates = LocationCoordinates(
        latitude = 41.9028,
        longitude = 12.4964,
        cityName = "Roma",
        countryName = "Italia"
    ),
    val schedule: TodayPrayerSchedule? = null,
    val formattedCountdown: String = "00:00:00",
    val activePrayerType: PrayerType = PrayerType.DHUHR,
    val errorMessage: String? = null
)
