package com.nurpray.app.data.local.database.dao

import androidx.room.*
import com.nurpray.app.data.local.database.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE cityName LIKE '%' || :query || '%' OR countryName LIKE '%' || :query || '%' ORDER BY cityName ASC LIMIT 100")
    fun searchCities(query: String): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities ORDER BY cityName ASC LIMIT 100")
    fun getAllCities(): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCityCount(): Int

    @Query("SELECT * FROM cities WHERE cityName = :name AND countryName = :country LIMIT 1")
    suspend fun findCityByNameAndCountry(name: String, country: String): CityEntity?
}
