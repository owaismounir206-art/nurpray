package com.nurpray.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nurpray_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_CALCULATION_METHOD = stringPreferencesKey("key_calculation_method")
        val KEY_ASR_METHOD = stringPreferencesKey("key_asr_method")
        val KEY_HIGH_LATITUDE_RULE = stringPreferencesKey("key_high_lat_rule")
        val KEY_PRE_ADHAN_MINUTES = intPreferencesKey("key_pre_adhan_minutes")
        val KEY_AUTO_DND = booleanPreferencesKey("key_auto_dnd")
        val KEY_LATITUDE = doublePreferencesKey("key_latitude")
        val KEY_LONGITUDE = doublePreferencesKey("key_longitude")
        val KEY_CITY_NAME = stringPreferencesKey("key_city_name")
        val KEY_COUNTRY_NAME = stringPreferencesKey("key_country_name")
    }

    val calculationMethodFlow: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_CALCULATION_METHOD] ?: "MUSLIM_WORLD_LEAGUE" }

    suspend fun setCalculationMethod(methodName: String) {
        context.dataStore.edit { it[KEY_CALCULATION_METHOD] = methodName }
    }

    suspend fun saveLocation(latitude: Double, longitude: Double, city: String, country: String) {
        context.dataStore.edit {
            it[KEY_LATITUDE] = latitude
            it[KEY_LONGITUDE] = longitude
            it[KEY_CITY_NAME] = city
            it[KEY_COUNTRY_NAME] = country
        }
    }
}
