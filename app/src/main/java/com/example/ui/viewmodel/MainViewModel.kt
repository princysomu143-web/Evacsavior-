package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.AlertNotification
import com.example.data.entity.EmergencyRequest
import com.example.data.entity.UserProfile
import com.example.data.repository.EvacRepository
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ChatMessage {
    data class User(val text: String) : ChatMessage
    data class Assistant(val text: String, val isOfflineFallback: Boolean = false) : ChatMessage
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EvacRepository

    // UI Navigation States
    private val _currentScreen = MutableStateFlow<String>("Splash") // "Splash", "Auth", "Main", "SOS_Countdown", "SOS_Live"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Current logged in user profile (if any)
    val userProfile: StateFlow<UserProfile?>

    // Alerts Feed
    val notifications: StateFlow<List<AlertNotification>>

    // All local requests (for tracking and volunteer view)
    val activeRequests: StateFlow<List<EmergencyRequest>>

    // Chatbot States
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage.Assistant("Hello, I am your EVAC Emergency AI assistant. Tell me what is happening. For example: 'How to perform CPR on an adult?' or 'What to do during an earthquake?'")
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // SOS Details State
    private val _activeSosRequest = MutableStateFlow<EmergencyRequest?>(null)
    val activeSosRequest: StateFlow<EmergencyRequest?> = _activeSosRequest.asStateFlow()

    private val _countdownValue = MutableStateFlow(5)
    val countdownValue: StateFlow<Int> = _countdownValue.asStateFlow()

    // Siren and Flashlight States (Additional Smart Features)
    val isSirenPlaying = MutableStateFlow(false)
    val isFlashlightSosActive = MutableStateFlow(false)
    val isSatelliteMapEnabled = MutableStateFlow(false)
    val isWearableConnected = MutableStateFlow(true)
    val isCrashDetectionEnabled = MutableStateFlow(true)
    val isPanicModeActive = MutableStateFlow(false)

    // Current UI Tabs for bottom navigation on Main Screen
    private val _currentMainTab = MutableStateFlow("Map") // "Map", "Alerts", "AI Assistant", "Dashboard", "Profile"
    val currentMainTab: StateFlow<String> = _currentMainTab.asStateFlow()

    // Simulation Jobs
    private var countdownJob: Job? = null
    private var trackingSimulationJob: Job? = null
    private var transcriptionJob: Job? = null

    // Simulated Responder Coordinates
    val responderLatitude = MutableStateFlow(0.0)
    val responderLongitude = MutableStateFlow(0.0)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EvacRepository(
            database.userProfileDao(),
            database.emergencyRequestDao(),
            database.alertNotificationDao()
        )

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        notifications = repository.allNotifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeRequests = repository.allActiveRequests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.prepopulateDbIfNeeded()
            delay(2000)
            // Automatically transition from Splash to Auth if no profile exists, otherwise to Main
            val profile = repository.getProfile()
            if (profile == null) {
                _currentScreen.value = "Auth"
            } else {
                _currentScreen.value = "Main"
            }
        }
    }

    fun setMainTab(tab: String) {
        _currentMainTab.value = tab
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Authentication Flow ---
    fun registerUser(
        name: String,
        phone: String,
        aadhaar: String,
        role: String,
        bloodGroup: String,
        allergies: String,
        emergencyContacts: String
    ) {
        viewModelScope.launch {
            val profile = UserProfile(
                phone = phone.ifBlank { "+91 98765 43210" },
                name = name.ifBlank { "Arjun Sharma" },
                aadhaarId = aadhaar.ifBlank { "1234-5678-9012" },
                role = role,
                bloodGroup = bloodGroup.ifBlank { "O+" },
                allergies = allergies.ifBlank { "None" },
                emergencyContacts = emergencyContacts.ifBlank { "+91 98765 00001" },
                volunteerStatus = false,
                rewardPoints = 120,
                isVerified = true
            )
            repository.saveProfile(profile)
            _currentScreen.value = "Main"
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearAllNotifications()
            // Keep a clean slate or prepopulate default alert
            _currentScreen.value = "Auth"
        }
    }

    // --- Volunteer / Police Toggle Operations ---
    fun toggleVolunteerStatus(status: Boolean) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.updateVolunteerStatus(profile.phone, status)
        }
    }

    fun awardPoints(points: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.addRewardPoints(profile.phone, points)
        }
    }

    // --- AI Chatbot Flow ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage.User(text)
        _chatMessages.value = _chatMessages.value + userMsg

        _isChatLoading.value = true
        viewModelScope.launch {
            val response = repository.getGeminiFirstAidResponse(text)
            val isFallback = response.contains("offline First Aid Tips")
            _chatMessages.value = _chatMessages.value + ChatMessage.Assistant(response, isFallback)
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage.Assistant("Hello, I am your EVAC Emergency AI assistant. Tell me what is happening. For example: 'How to perform CPR on an adult?'")
        )
    }

    // --- SOS Emergency Management & Live Tracking Simulation ---
    fun startSosCountdown(type: String) {
        _countdownValue.value = 5
        _currentScreen.value = "SOS_Countdown"
        
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_countdownValue.value > 0) {
                delay(1000)
                _countdownValue.value -= 1
            }
            triggerSosEmergency(type)
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        _currentScreen.value = "Main"
    }

    // --- Immediate Panic Alert Emergency Trigger (Immediate Signal State, No Countdown Delay) ---
    fun triggerImmediatePanicAlert(
        panicType: String = "CRITICAL PANIC ALERT",
        isSilentDuress: Boolean = false
    ) {
        countdownJob?.cancel()
        isPanicModeActive.value = true
        if (!isSilentDuress) {
            isSirenPlaying.value = true
            isFlashlightSosActive.value = true
        }

        viewModelScope.launch {
            val statusText = if (isSilentDuress) {
                "Silent Duress Signal Active • Stealth Police Node"
            } else {
                "PRIORITY 1: IMMEDIATE PANIC SIGNAL ACTIVE"
            }

            val request = EmergencyRequest(
                type = panicType,
                latitude = 28.6139,
                longitude = 77.2090,
                batteryPercentage = 84,
                phoneModel = "Priority Panic Beacon (Instant Relay)",
                timestamp = System.currentTimeMillis(),
                status = statusText
            )
            val id = repository.insertRequest(request)
            val insertedRequest = request.copy(id = id)
            _activeSosRequest.value = insertedRequest
            _currentScreen.value = "SOS_Live"

            // Instant Broadcast notification
            repository.insertNotification(
                AlertNotification(
                    title = "🚨 PANIC ALERT: Immediate Signal Broadcasted",
                    message = if (isSilentDuress)
                        "Silent distress signal sent with live GPS telemetry directly to Rapid Action Police dispatch."
                    else
                        "CRITICAL PANIC SIGNAL BROADCASTED: High-priority beacon active. Rapid response units and emergency teams mobilized immediately.",
                    type = "System",
                    timestamp = System.currentTimeMillis()
                )
            )

            startTrackingSimulation(id, panicType)
        }
    }

    private suspend fun triggerSosEmergency(type: String) {
        val request = EmergencyRequest(
            type = type,
            latitude = 28.6139, // Default simulation: New Delhi coordinates
            longitude = 77.2090,
            batteryPercentage = 84,
            phoneModel = "Android Emergency Node",
            timestamp = System.currentTimeMillis(),
            status = "Searching Responders..."
        )
        val id = repository.insertRequest(request)
        val insertedRequest = request.copy(id = id)
        _activeSosRequest.value = insertedRequest
        _currentScreen.value = "SOS_Live"

        // Broadcast notification
        repository.insertNotification(
            AlertNotification(
                title = "SOS Alert Triggered",
                message = "Your $type Emergency SOS has been broadcasted to emergency rescue teams and nearby volunteers.",
                type = "System",
                timestamp = System.currentTimeMillis()
            )
        )

        startTrackingSimulation(id, type)
    }

    private fun startTrackingSimulation(requestId: Long, type: String) {
        trackingSimulationJob?.cancel()
        transcriptionJob?.cancel()

        // Set initial responder starting position (offset from user)
        responderLatitude.value = 28.6250
        responderLongitude.value = 77.2200

        trackingSimulationJob = viewModelScope.launch {
            // Step 1: Searching for responders (3 seconds)
            delay(3000)
            var currentReq = repository.getRequestByIdFlow(requestId).firstOrNull() ?: _activeSosRequest.value ?: return@launch
            
            val responderName: String
            val responderType: String
            val responderPhone: String
            
            when {
                type.contains("PANIC", ignoreCase = true) -> {
                    responderName = "Rapid Intervention Police Unit (PCR-Echo 7)"
                    responderType = "Tactical Emergency Dispatch"
                    responderPhone = "+91 112"
                }
                type == "Medical Emergency" -> {
                    responderName = "Ambulance DL-3C-8192"
                    responderType = "Ambulance Dispatch"
                    responderPhone = "+91 98110 55500"
                }
                type == "Fire" -> {
                    responderName = "Fire Crew Station 14"
                    responderType = "Fire Department"
                    responderPhone = "+91 98110 44400"
                }
                type in listOf("Police", "Crime", "Women Safety") -> {
                    responderName = "PCR Vehicle Romeo-9"
                    responderType = "Police Unit"
                    responderPhone = "+91 98110 11100"
                }
                else -> {
                    responderName = "Rahul Verma (Verified First-Responder)"
                    responderType = "Volunteer"
                    responderPhone = "+91 98765 12345"
                }
            }

            currentReq = currentReq.copy(
                status = "Responders Dispatched",
                responderName = responderName,
                responderType = responderType,
                responderPhone = responderPhone,
                responderDistance = "2.4 km",
                responderEta = "6 mins"
            )
            repository.updateRequest(currentReq)
            _activeSosRequest.value = currentReq

            repository.insertNotification(
                AlertNotification(
                    title = "Responder Dispatched",
                    message = "$responderName has accepted your emergency request and is rushing to your location.",
                    type = "System",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Live voice room transcription simulation
            startTranscriptionSimulation(requestId, responderName)

            // Step 2: Simulate real-time movement (reduce distance and ETA every 4 seconds)
            val pathSteps = listOf(
                Pair("1.8 km", "4 mins"),
                Pair("1.1 km", "3 mins"),
                Pair("0.5 km", "1 min"),
                Pair("0.2 km", "Nearby"),
                Pair("0.0 km", "Arrived")
            )

            for (step in pathSteps) {
                delay(6000)
                currentReq = currentReq.copy(
                    responderDistance = step.first,
                    responderEta = step.second
                )
                
                // Animate responder coordinates closer to victim (28.6139, 77.2090)
                val lerpFactor = when(step.first) {
                    "1.8 km" -> 0.2
                    "1.1 km" -> 0.5
                    "0.5 km" -> 0.8
                    "0.2 km" -> 0.95
                    else -> 1.0
                }
                responderLatitude.value = 28.6250 + (28.6139 - 28.6250) * lerpFactor
                responderLongitude.value = 77.2200 + (77.2090 - 77.2200) * lerpFactor

                if (step.second == "Arrived") {
                    currentReq = currentReq.copy(status = "Completed")
                    repository.insertNotification(
                        AlertNotification(
                            title = "Responder Arrived",
                            message = "Emergency responder $responderName has reached your location. Stay calm and cooperate.",
                            type = "System",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }

                repository.updateRequest(currentReq)
                _activeSosRequest.value = currentReq
            }
        }
    }

    private fun startTranscriptionSimulation(requestId: Long, responderName: String) {
        val transcriptFlow = listOf(
            "[System] Secure Audio Channel Activated. Responders listening...",
            "[Citizen Profile] Arjun Sharma, Blood Group O+, No Drug Allergies.",
            "[Transcribing Citizen] \"I'm trapped on the first floor near the window!\"",
            "[$responderName] \"Copy that, Arjun. I'm arriving in 3 minutes. Keep a wet cloth over your nose.\"",
            "[Transcribing Citizen] \"The smoke is thick, but I'm holding on!\"",
            "[$responderName] \"We are pulling up now. Stay where you are. Ladder crew dispatched!\""
        )

        transcriptionJob = viewModelScope.launch {
            var fullTranscript = ""
            for (line in transcriptFlow) {
                delay(5000)
                fullTranscript += "$line\n\n"
                _activeSosRequest.value = _activeSosRequest.value?.copy(voiceTranscript = fullTranscript)
                repository.updateVoiceTranscript(requestId, fullTranscript)
            }
        }
    }

    fun cancelSosEmergency() {
        val activeReq = _activeSosRequest.value
        if (activeReq != null) {
            viewModelScope.launch {
                repository.cancelRequest(activeReq.id)
                repository.insertNotification(
                    AlertNotification(
                        title = "SOS Emergency Cancelled",
                        message = "You have manually cancelled the active emergency broadcast.",
                        type = "System",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        cleanupSos()
        _currentScreen.value = "Main"
    }

    fun completeSosEmergency() {
        val activeReq = _activeSosRequest.value
        if (activeReq != null) {
            viewModelScope.launch {
                repository.completeRequest(activeReq.id)
            }
        }
        cleanupSos()
        _currentScreen.value = "Main"
    }

    private fun cleanupSos() {
        countdownJob?.cancel()
        trackingSimulationJob?.cancel()
        transcriptionJob?.cancel()
        _activeSosRequest.value = null
        isPanicModeActive.value = false
    }

    override fun onCleared() {
        super.onCleared()
        cleanupSos()
    }
}
