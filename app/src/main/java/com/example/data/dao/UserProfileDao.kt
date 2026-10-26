package com.example.data.dao

import androidx.room.*
import com.example.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET volunteerStatus = :status WHERE phone = :phone")
    suspend fun updateVolunteerStatus(phone: String, status: Boolean)

    @Query("UPDATE user_profile SET rewardPoints = rewardPoints + :points WHERE phone = :phone")
    suspend fun addRewardPoints(phone: String, points: Int)
}
