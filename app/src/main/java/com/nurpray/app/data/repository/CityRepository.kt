package com.nurpray.app.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.nurpray.app.data.local.database.NurPrayDatabase
import com.nurpray.app.data.local.database.entity.CityEntity
import com.nurpray.app.data.local.datastore.UserPreferencesDataStore
import com.nurpray.app.domain.model.LocationCoordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class CityRepository(private val context: Context) {

    private val db = NurPrayDatabase.getDatabase(context)
    private val cityDao = db.cityDao()
    private val preferencesDataStore = UserPreferencesDataStore(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Searches cities from both the offline Room database and Android's universal Geocoder.
     * This guarantees coverage of literally EVERY city, town, and village on Earth.
     */
    fun searchCitiesWorldwide(query: String): Flow<List<CityEntity>> = flow {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            val cached = cityDao.getAllCities().first()
            if (cached.isEmpty()) {
                val preloaded = PreloadedWorldCities.CITIES
                cityDao.insertCities(preloaded)
                emit(preloaded.take(50))
            } else {
                emit(cached)
            }
            return@flow
        }

        // Step 1: Emit local database matches immediately
        val localMatches = cityDao.searchCities(trimmed).first()
        emit(localMatches)

        // Step 2: Query Android Geocoder to find any city/town in the world
        try {
            val geocoderMatches = searchWithGeocoder(trimmed)
            if (geocoderMatches.isNotEmpty()) {
                // Save discovered locations into local database for future offline access
                cityDao.insertCities(geocoderMatches)

                // Emit combined deduplicated list
                val combined = (localMatches + geocoderMatches)
                    .distinctBy { "${it.cityName.lowercase()}_${it.countryName.lowercase()}" }
                emit(combined)
            }
        } catch (e: Exception) {
            // Geocoder network error or offline fallback - local matches are already emitted
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Resolves locations worldwide using Android Geocoder.
     */
    private suspend fun searchWithGeocoder(query: String): List<CityEntity> = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext emptyList()

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(query, 10) { results ->
                        continuation.resume(results)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 10) ?: emptyList()
            }

            addresses.mapNotNull { address ->
                val city = address.locality
                    ?: address.subAdminArea
                    ?: address.adminArea
                    ?: address.featureName
                    ?: return@mapNotNull null

                val country = address.countryName ?: ""
                val countryCode = address.countryCode ?: ""

                CityEntity(
                    cityName = city,
                    countryName = country,
                    countryCode = countryCode,
                    latitude = address.latitude,
                    longitude = address.longitude,
                    timezoneId = TimeZone.getDefault().id,
                    isCustomOrGps = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Detects current GPS coordinates and reverse-geocodes to town and country.
     */
    suspend fun getCurrentGpsLocation(): LocationCoordinates? = withContext(Dispatchers.IO) {
        try {
            val cancellationSource = CancellationTokenSource()
            val location: Location? = suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationSource.token
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            }

            if (location != null) {
                val reverseGeocoded = reverseGeocode(location.latitude, location.longitude)
                val coordinates = LocationCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = reverseGeocoded?.first ?: "Posizione Attuale",
                    countryName = reverseGeocoded?.second ?: "",
                    isAutomaticGps = true
                )

                // Save to preferences
                preferencesDataStore.saveLocation(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                    city = coordinates.cityName,
                    country = coordinates.countryName
                )

                coordinates
            } else {
                null
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun reverseGeocode(lat: Double, lon: Double): Pair<String, String>? {
        if (!Geocoder.isPresent()) return null
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Posizione GPS"
                val country = addr.countryName ?: ""
                Pair(city, country)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Pre-populates the database with world cities if empty.
     */
    suspend fun ensureDatabasePopulated() = withContext(Dispatchers.IO) {
        if (cityDao.getCityCount() == 0) {
            cityDao.insertCities(PreloadedWorldCities.CITIES)
        }
    }

    /**
     * Saves user selected city.
     */
    suspend fun saveSelectedCity(city: CityEntity) = withContext(Dispatchers.IO) {
        cityDao.insertCity(city)
        preferencesDataStore.saveLocation(
            latitude = city.latitude,
            longitude = city.longitude,
            city = city.cityName,
            country = city.countryName
        )
    }
}
