package com.nurpray.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String
)
