package com.example.data.dao

import androidx.room.*
import com.example.data.entity.EmergencyRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyRequestDao {
    @Query("SELECT * FROM emergency_requests ORDER BY timestamp DESC")
    fun getAllRequestsFlow(): Flow<List<EmergencyRequest>>

    @Query("SELECT * FROM emergency_requests WHERE status != 'Completed' AND status != 'Cancelled' ORDER BY timestamp DESC")
    fun getActiveRequestsFlow(): Flow<List<EmergencyRequest>>

    @Query("SELECT * FROM emergency_requests WHERE id = :id")
    suspend fun getRequestById(id: Long): EmergencyRequest?

    @Query("SELECT * FROM emergency_requests WHERE id = :id")
    fun getRequestByIdFlow(id: Long): Flow<EmergencyRequest?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: EmergencyRequest): Long

    @Update
    suspend fun updateRequest(request: EmergencyRequest)

    @Query("UPDATE emergency_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String)

    @Query("UPDATE emergency_requests SET voiceTranscript = :transcript WHERE id = :id")
    suspend fun updateVoiceTranscript(id: Long, transcript: String)
}
