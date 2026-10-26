package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_notifications")
data class AlertNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "System", "Weather", "Disaster", "Earthquake", "Flood"
    val timestamp: Long,
    val isRead: Boolean = false
)
