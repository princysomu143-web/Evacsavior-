package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val phone: String,
    val name: String,
    val aadhaarId: String,
    val role: String, // "Citizen", "Volunteer", "Police", "Ambulance", "Fire Department", "Disaster Response", "Admin"
    val bloodGroup: String,
    val allergies: String,
    val emergencyContacts: String, // comma separated phone numbers
    val volunteerStatus: Boolean, // online/offline
    val rewardPoints: Int = 0,
    val isVerified: Boolean = false
)
