package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.dao.AlertNotificationDao
import com.example.data.dao.EmergencyRequestDao
import com.example.data.dao.UserProfileDao
import com.example.data.entity.AlertNotification
import com.example.data.entity.UserProfile
import com.example.data.entity.EmergencyRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EvacRepository(
    private val profileDao: UserProfileDao,
    private val requestDao: EmergencyRequestDao,
    private val alertDao: AlertNotificationDao
) {
    val userProfile: Flow<UserProfile?> = profileDao.getProfileFlow()
    val allActiveRequests: Flow<List<EmergencyRequest>> = requestDao.getActiveRequestsFlow()
    val allNotifications: Flow<List<AlertNotification>> = alertDao.getAllNotificationsFlow()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // OkHttpClient with 60s timeout as requested in gemini-api SKILL.md
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getProfile(): UserProfile? = withContext(Dispatchers.IO) {
        profileDao.getProfileDirect()
    }

    suspend fun saveProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.insertProfile(profile)
    }

    suspend fun updateVolunteerStatus(phone: String, status: Boolean) = withContext(Dispatchers.IO) {
        profileDao.updateVolunteerStatus(phone, status)
    }

    suspend fun addRewardPoints(phone: String, points: Int) = withContext(Dispatchers.IO) {
        profileDao.addRewardPoints(phone, points)
    }

    suspend fun insertRequest(request: EmergencyRequest): Long = withContext(Dispatchers.IO) {
        requestDao.insertRequest(request)
    }

    suspend fun updateRequest(request: EmergencyRequest) = withContext(Dispatchers.IO) {
        requestDao.updateRequest(request)
    }

    suspend fun cancelRequest(id: Long) = withContext(Dispatchers.IO) {
        requestDao.updateRequestStatus(id, "Cancelled")
    }

    suspend fun completeRequest(id: Long) = withContext(Dispatchers.IO) {
        requestDao.updateRequestStatus(id, "Completed")
    }

    fun getRequestByIdFlow(id: Long): Flow<EmergencyRequest?> {
        return requestDao.getRequestByIdFlow(id)
    }

    suspend fun insertNotification(notification: AlertNotification) = withContext(Dispatchers.IO) {
        alertDao.insertNotification(notification)
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        alertDao.markAsRead(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        alertDao.clearAll()
    }

    suspend fun updateVoiceTranscript(id: Long, transcript: String) = withContext(Dispatchers.IO) {
        requestDao.updateVoiceTranscript(id, transcript)
    }

    /**
     * Prepulate mock notifications if DB is empty
     */
    suspend fun prepopulateDbIfNeeded() = withContext(Dispatchers.IO) {
        val currentProfile = profileDao.getProfileDirect()
        if (currentProfile == null) {
            profileDao.insertProfile(
                UserProfile(
                    phone = "+91 98765 43210",
                    name = "Arjun Sharma",
                    aadhaarId = "1234-5678-9012",
                    role = "Citizen",
                    bloodGroup = "O+",
                    allergies = "Penicillin, Dust",
                    emergencyContacts = "+91 98765 00001, +91 98765 00002",
                    volunteerStatus = false,
                    rewardPoints = 120,
                    isVerified = true
                )
            )
        }

        // Check alerts
        // Simple direct flow check can be done, but let's just insert some default alerts to feel alive!
        val defaultAlerts = listOf(
            AlertNotification(
                title = "Red Alert: Severe Flood Warning",
                message = "IMD issues red alert for coastal regions. Heavy to very heavy rainfall expected in the next 24 hours. Safe shelters have been activated.",
                type = "Flood",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30
            ),
            AlertNotification(
                title = "Earthquake Tremors Detected",
                message = "A 4.8 magnitude tremor detected 15km North. Safe structures are advised. Contact rescue if trapped.",
                type = "Earthquake",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120
            ),
            AlertNotification(
                title = "Emergency Siren Testing Scheduled",
                message = "The government will test the civic emergency broadcast sirens at 12:00 PM today. Please do not panic.",
                type = "System",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 300
            )
        )
        for (alert in defaultAlerts) {
            alertDao.insertNotification(alert)
        }
    }

    /**
     * Call the Gemini API directly via OkHttp to avoid any Moshi-codegen discrepancies.
     * This follows "Option B: Direct REST API (Default for Prototypes)" from gemini-api/SKILL.md.
     */
    suspend fun getGeminiFirstAidResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "You are currently offline or Gemini API Key is not configured in the Secrets Panel. Here are some offline First Aid Tips:\n\n" +
                    "1. **CPR**: Push hard and fast in the center of the chest (100-120 compressions/min).\n" +
                    "2. **Severe Bleeding**: Apply firm, continuous pressure with a clean cloth. Elevate if possible.\n" +
                    "3. **Choking**: Perform abdominal thrusts (Heimlich maneuver) above the belly button.\n" +
                    "4. **Burns**: Cool with running water for 10-20 minutes. Do not apply ice or grease.\n" +
                    "5. **Fracture**: Keep the limb still and supported. Do not try to realign."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        // Formulate the JSON body payload with system instructions for emergency first aid.
        val systemInstruction = "You are EVAC SAVIOUR, an expert emergency responder and medical first-aid bot. " +
                "Respond in a highly structured, urgent yet calming tone. Provide clear, step-by-step instructions (bold bullet points) " +
                "that can be performed under stress. Keep it extremely concise so that it is readable in critical moments."

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3) // Lower temperature for factual accuracy
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error fetching guidance from server. Error code: ${response.code}. Please follow standard first aid measures."
                }
                val bodyStr = response.body?.string() ?: return@withContext "Failed to receive response. Please administer basic first aid."
                
                // Parse Gemini API Response
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                val textResponse = firstPart?.optString("text")

                if (!textResponse.isNullOrBlank()) {
                    textResponse
                } else {
                    "Unable to generate instructions. Please check on the patient and remain calm."
                }
            }
        } catch (e: Exception) {
            Log.e("EvacRepository", "Gemini API failed", e)
            "Network error: Unable to contact EVAC emergency services. Please keep the victim calm and warm until help arrives."
        }
    }
}
