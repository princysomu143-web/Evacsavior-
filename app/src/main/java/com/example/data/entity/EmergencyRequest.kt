package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_requests")
data class EmergencyRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "Medical", "Fire", "Women Safety", "Accident", "Natural Disaster", "Crime", "Child Emergency", "Other"
    val latitude: Double,
    val longitude: Double,
    val batteryPercentage: Int,
    val phoneModel: String,
    val timestamp: Long,
    val status: String, // "Initiated", "Searching Responders", "Responders Dispatched", "Completed", "Cancelled"
    val responderName: String = "",
    val responderType: String = "", // "Volunteer", "Police", "Ambulance", "Fire Team", "Disaster Response Team"
    val responderPhone: String = "",
    val responderDistance: String = "",
    val responderEta: String = "",
    val voiceTranscript: String = "" // emergency audio transcript
)
