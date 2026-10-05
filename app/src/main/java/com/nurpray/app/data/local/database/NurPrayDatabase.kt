package com.nurpray.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nurpray.app.data.local.database.dao.CityDao
import com.nurpray.app.data.local.database.entity.CityEntity

@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class NurPrayDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var INSTANCE: NurPrayDatabase? = null

        fun getDatabase(context: Context): NurPrayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NurPrayDatabase::class.java,
                    "nurpray_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
