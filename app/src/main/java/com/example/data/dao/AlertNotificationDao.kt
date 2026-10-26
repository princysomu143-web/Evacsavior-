package com.example.data.dao

import androidx.room.*
import com.example.data.entity.AlertNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertNotificationDao {
    @Query("SELECT * FROM alert_notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<AlertNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AlertNotification): Long

    @Query("UPDATE alert_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM alert_notifications")
    suspend fun clearAll()
}
