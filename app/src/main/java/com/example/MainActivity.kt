package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FloatingPanicButton
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge as mandated in guidelines
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(dynamicColor = false) { // custom premium red theme
                val screenState by viewModel.currentScreen.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (screenState) {
                        "Splash" -> SplashScreen(viewModel)
                        "Auth" -> AuthScreen(viewModel)
                        "Main" -> MainNavigationContainer(viewModel)
                        "SOS_Countdown" -> ActiveCountdownScreen(viewModel)
                        "SOS_Live" -> ActiveSosScreen(viewModel)
                        else -> SplashScreen(viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContainer(viewModel: MainViewModel) {
    val activeTab by viewModel.currentMainTab.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeSos by viewModel.activeSosRequest.collectAsStateWithLifecycle()

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF64748B),
        unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF64748B)
    )

    Scaffold(
        floatingActionButton = {
            if (activeSos == null && activeTab != "Map") {
                FloatingPanicButton(viewModel = viewModel)
            }
        },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GOVERNMENT OF EMERGENCY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFF64748B), // Slate-500
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EVAC SAVIOUR",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    // Profile button / avatar placeholder linking to Profile
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(androidx.compose.ui.graphics.Color.White, CircleShape)
                            .border(1.dp, androidx.compose.ui.graphics.Color(0xFFE2E8F0), CircleShape)
                            .clickable { viewModel.setMainTab("Profile") },
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(androidx.compose.ui.graphics.Color(0xFFE2E8F0), CircleShape)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars // Safe navigation bar margins
            ) {
                // Map Tab
                NavigationBarItem(
                    selected = activeTab == "Map",
                    onClick = { viewModel.setMainTab("Map") },
                    icon = { Icon(Icons.Default.Map, contentDescription = "SOS Map") },
                    label = { Text("SOS Map") },
                    colors = navItemColors
                )
                // Alerts Tab
                NavigationBarItem(
                    selected = activeTab == "Alerts",
                    onClick = { viewModel.setMainTab("Alerts") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts") },
                    colors = navItemColors
                )
                // AI Assistant Tab
                NavigationBarItem(
                    selected = activeTab == "AI Assistant",
                    onClick = { viewModel.setMainTab("AI Assistant") },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Saviour AI") },
                    label = { Text("Saviour AI") },
                    colors = navItemColors
                )
                // Role Specific Dashboard Tab
                val dashboardLabel = when (profile?.role) {
                    "Volunteer" -> "Rescue"
                    "Police" -> "Dispatch"
                    "Admin" -> "Control"
                    else -> "Missions"
                }
                NavigationBarItem(
                    selected = activeTab == "Dashboard",
                    onClick = { viewModel.setMainTab("Dashboard") },
                    icon = { Icon(Icons.Default.Shield, contentDescription = dashboardLabel) },
                    label = { Text(dashboardLabel) },
                    colors = navItemColors
                )
                // Profile Tab
                NavigationBarItem(
                    selected = activeTab == "Profile",
                    onClick = { viewModel.setMainTab("Profile") },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = navItemColors
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Map" -> CitizenHomeScreen(viewModel)
                "Alerts" -> AlertsTabFeed(viewModel)
                "AI Assistant" -> AiAssistantTab(viewModel)
                "Dashboard" -> {
                    when (profile?.role) {
                        "Volunteer" -> VolunteerDashboardTab(viewModel)
                        "Police" -> PoliceDashboardTab(viewModel)
                        "Admin" -> AdminDashboardTab(viewModel)
                        else -> VolunteerDashboardTab(viewModel) // default fallback
                    }
                }
                "Profile" -> ProfileTab(viewModel)
                else -> CitizenHomeScreen(viewModel)
            }
        }
    }
}
