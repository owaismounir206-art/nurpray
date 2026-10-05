package com.nurpray.app.data.local.database.dao

import androidx.room.*
import com.nurpray.app.data.local.database.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM cities WHERE cityName LIKE '%' || :query || '%' ORDER BY cityName ASC LIMIT 50")
    fun searchCities(query: String): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityEntity>)

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCityCount(): Int
}
