package com.nurpray.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cities",
    indices = [
        Index(value = ["cityName"]),
        Index(value = ["countryName"])
    ]
)
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val countryName: String,
    val countryCode: String = "",
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String = "UTC",
    val isCustomOrGps: Boolean = false
)
