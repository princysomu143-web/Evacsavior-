package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AlertNotificationDao
import com.example.data.dao.EmergencyRequestDao
import com.example.data.dao.UserProfileDao
import com.example.data.entity.AlertNotification
import com.example.data.entity.EmergencyRequest
import com.example.data.entity.UserProfile

@Database(
    entities = [UserProfile::class, EmergencyRequest::class, AlertNotification::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun emergencyRequestDao(): EmergencyRequestDao
    abstract fun alertNotificationDao(): AlertNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "evac_saviour_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
