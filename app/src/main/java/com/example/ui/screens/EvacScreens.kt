package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AlertNotification
import com.example.data.entity.EmergencyRequest
import com.example.data.entity.UserProfile
import com.example.ui.components.PanicAlertBanner
import com.example.ui.components.PanicAlertButton
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.MainViewModel

// --- COMMON CANVAS RADAR / MAP COMPONENT ---
@Composable
fun SimulatedRadarMap(
    modifier: Modifier = Modifier,
    pulseColor: Color = Color(0xFFD32F2F),
    responderLat: Double = 0.0,
    responderLng: Double = 0.0,
    isSatellite: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Draw Map Background Grid/Radar
        val backgroundGridColor = if (isSatellite) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        val gridLinesColor = if (isSatellite) Color(0xFF334155) else Color(0xFFCBD5E1)

        drawRect(color = backgroundGridColor)

        // Draw grid lines
        val step = 40.dp.toPx()
        for (x in 0..width.toInt() step step.toInt()) {
            drawLine(gridLinesColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), height), strokeWidth = 1f)
        }
        for (y in 0..height.toInt() step step.toInt()) {
            drawLine(gridLinesColor, Offset(0f, y.toFloat()), Offset(width, y.toFloat()), strokeWidth = 1f)
        }

        // Draw radar circles
        drawCircle(
            color = pulseColor.copy(alpha = 0.1f),
            radius = 120.dp.toPx(),
            center = center,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = pulseColor.copy(alpha = 0.15f),
            radius = 80.dp.toPx(),
            center = center,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = pulseColor.copy(alpha = 0.2f),
            radius = 40.dp.toPx(),
            center = center,
            style = Stroke(width = 2f)
        )

        // Draw pulsating expanding ring (SOS)
        drawCircle(
            color = pulseColor.copy(alpha = pulseAlpha),
            radius = pulseRadius,
            center = center,
            style = Stroke(width = 4f)
        )

        // Draw shelters (Green spots)
        drawCircle(Color(0xFF2E7D32), radius = 10f, center = Offset(width * 0.2f, height * 0.3f))
        drawCircle(Color(0xFF2E7D32), radius = 10f, center = Offset(width * 0.75f, height * 0.25f))

        // Draw victim (Self - Center Red Pin)
        drawCircle(color = Color.White, radius = 18f, center = center)
        drawCircle(color = pulseColor, radius = 12f, center = center)

        // Draw Responder if en-route
        if (responderLat != 0.0 || responderLng != 0.0) {
            // Map the coordinates relative to center
            // Let's place responder at some offset from center representing dynamic tracking movement
            val offsetLat = (responderLat - 28.6139) * 10000f
            val offsetLng = (responderLng - 77.2090) * 10000f
            
            val responderPos = Offset(
                center.x + offsetLng.toFloat(),
                center.y - offsetLat.toFloat()
            )

            // Draw directional indicator for responder
            drawCircle(color = Color.White, radius = 16f, center = responderPos)
            drawCircle(color = Color(0xFF1565C0), radius = 12f, center = responderPos)

            // Draw line connecting victim and responder
            drawLine(
                color = Color(0xFF1565C0).copy(alpha = 0.5f),
                start = center,
                end = responderPos,
                strokeWidth = 4f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        }
    }
}

// --- 1. SPLASH SCREEN ---
@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_logo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB71C1C), Color(0xFF7F0000))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emergency Shield Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = size.width / 2 * pulseScale
                        )
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "EVAC SAVIOUR LOGO",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "EVAC SAVIOUR",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Every Second Saves a Life",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SECURE GOVERNMENT RESPONSE PORTAL",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

// --- 2. AUTHENTICATION & PROFILE SETUP SCREEN ---
@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Citizen") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var allergies by remember { mutableStateOf("") }
    var emergencyContacts by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }

    val roles = listOf("Citizen", "Volunteer", "Police", "Ambulance", "Admin")

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "EVAC GATEWAY",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Secure Emergency Authentication & Registration",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isOtpSent) {
                // Step 1: Phone and Role details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Primary Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name (as in Aadhaar)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = aadhaar,
                            onValueChange = { aadhaar = it },
                            label = { Text("Aadhaar Number (Optional Verification)") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Role Block
                Text(
                    text = "Select Portal Access Role",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { role ->
                        val isSelected = selectedRole == role
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRole = role },
                            label = { Text(role, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("role_chip_$role")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional Medical Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Medical Profile & Contacts (Critical in SOS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = { bloodGroup = it },
                                label = { Text("Blood Group") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = allergies,
                                onValueChange = { allergies = it },
                                label = { Text("Allergies") },
                                modifier = Modifier.weight(2f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emergencyContacts,
                            onValueChange = { emergencyContacts = it },
                            label = { Text("Emergency Contacts (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (phone.isNotBlank()) {
                            isOtpSent = true
                        } else {
                            isOtpSent = true // Fast bypass for verification testing
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SEND SECURITY OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

            } else {
                // OTP VERIFICATION STEP
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Verification Code Sent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter the 6-digit OTP sent to your secure number to establish identity.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("Enter OTP Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { isOtpSent = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back")
                            }

                            Button(
                                onClick = {
                                    viewModel.registerUser(
                                        name = name,
                                        phone = phone,
                                        aadhaar = aadhaar,
                                        role = selectedRole,
                                        bloodGroup = bloodGroup,
                                        allergies = allergies,
                                        emergencyContacts = emergencyContacts
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("verify_otp_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("VERIFY & REGISTER", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 3. CITIZEN HOME PORTAL (MAPS, SOS RADAR, ACTIONS) ---
@Composable
fun CitizenHomeScreen(viewModel: MainViewModel) {
    val activeSos by viewModel.activeSosRequest.collectAsState()
    val isSatelliteMap by viewModel.isSatelliteMapEnabled.collectAsState()

    val quickActions = listOf(
        Pair("Medical Emergency", Icons.Default.LocalHospital),
        Pair("Fire Rescue", Icons.Default.LocalFireDepartment),
        Pair("Women Safety", Icons.Default.Shield),
        Pair("Severe Accident", Icons.Default.Campaign),
        Pair("Natural Disaster", Icons.Default.Warning),
        Pair("Child Safety", Icons.Default.Person),
        Pair("Crime Alert", Icons.Default.Info)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Geometric Balance System Status Block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // slate-200 border
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pulse Green Indicator Dot
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_green")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF2E7D32).copy(alpha = pulseAlpha), CircleShape)
                            .border(1.dp, Color(0xFF1B5E20), CircleShape)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "System Active",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Connected to 142 nearby responders",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B) // slate-500
                        )
                    }

                    // GPS LOCK status badge (Blue text, blue-50 bg)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE3F2FD), // Blue-50
                        border = BorderStroke(1.dp, Color(0xFFBBDEFB))
                    ) {
                        Text(
                            text = "GPS LOCK",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0) // EmergencyBlue
                        )
                    }
                }
            }
        }

        // High-Visibility 'Panic Alert' Button Component (Immediate Emergency Signal State)
        item {
            PanicAlertBanner(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Upper Radar Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                SimulatedRadarMap(
                    modifier = Modifier.fillMaxWidth(),
                    isSatellite = isSatelliteMap
                )
            }
        }

        // Geometric Balanced SOS Button Section with concentric rings
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        // Concentric Decorative Ring 1 (Outer)
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .border(1.5.dp, Color(0xFFD32F2F).copy(alpha = 0.06f), CircleShape)
                        )
                        // Concentric Decorative Ring 2 (Inner)
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .border(2.dp, Color(0xFFD32F2F).copy(alpha = 0.12f), CircleShape)
                        )

                        // Giant SOS Button (Emergency Red, White Border)
                        val pulseTransition = rememberInfiniteTransition(label = "sos_scale")
                        val pulseScale by pulseTransition.animateFloat(
                            initialValue = 0.96f,
                            targetValue = 1.04f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

                        Button(
                            onClick = { viewModel.startSosCountdown("General SOS Alert") },
                            modifier = Modifier
                                .size(126.dp)
                                .border(6.dp, Color.White, CircleShape)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                                        radius = (size.width / 2) * pulseScale
                                    )
                                }
                                .testTag("sos_giant_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = CircleShape,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SOS",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "5S COUNTDOWN",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Standard Emergency • 5s Cancel Window",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B) // Slate-500
                    )
                }
            }
        }

        item {
            Text(
                text = "OR CHOOSE SPECIFIC EMERGENCY TYPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        // Quick Grid Actions
        val chunked = quickActions.chunked(2)
        items(chunked) { rowActions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { action ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clickable { viewModel.startSosCountdown(action.first) }
                            .testTag("action_tile_${action.first.replace(" ", "_")}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.second,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = action.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
                if (rowActions.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- 4. COUNTDOWN INTERFACE ---
@Composable
fun ActiveCountdownScreen(viewModel: MainViewModel) {
    val count by viewModel.countdownValue.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "countdown_radar")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB71C1C))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BROADCASTING EMERGENCY IN...",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Massive Pulsating Count
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = pulseAlpha),
                            radius = size.width / 2 + 50f
                        )
                    }
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Automatically dispatching GPS location, medical profile details, and setting up emergency responders.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = { viewModel.cancelSosCountdown() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("cancel_sos_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CANCEL BROADCAST",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// --- 5. LIVE EMERGENCY / RESPONDERS STATUS SCREEN ---
@Composable
fun ActiveSosScreen(viewModel: MainViewModel) {
    val activeSos by viewModel.activeSosRequest.collectAsState()
    val responderLat by viewModel.responderLatitude.collectAsState()
    val responderLng by viewModel.responderLongitude.collectAsState()
    val isSirenPlaying by viewModel.isSirenPlaying.collectAsState()
    val isFlashlightSosActive by viewModel.isFlashlightSosActive.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.isPanicModeActive.collectAsState().value) "CRITICAL PANIC BROADCAST ACTIVE" else "LIVE SOS EMERGENCY BROADCAST",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Red,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = activeSos?.type ?: "Emergency",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // High-Visibility Panic Banner if in Panic Mode
            val isPanicActive by viewModel.isPanicModeActive.collectAsState()
            if (isPanicActive || (activeSos?.type?.contains("PANIC", ignoreCase = true) == true)) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
                    border = BorderStroke(2.dp, Color(0xFFFF5252)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text(
                                text = "PRIORITY 1: IMMEDIATE SIGNAL TRIGGERED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Instant GPS beacon active. Rapid police intervention squad dispatched without countdown.",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated Tracking Map
            SimulatedRadarMap(
                modifier = Modifier.fillMaxWidth(),
                pulseColor = Color.Red,
                responderLat = responderLat,
                responderLng = responderLng,
                isSatellite = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISPATCH STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = activeSos?.status ?: "Initiated",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Responder Card details
                    if (!activeSos?.responderName.isNullOrBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeSos?.responderName ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = activeSos?.responderType ?: "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = activeSos?.responderDistance ?: "",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "ETA: ${activeSos?.responderEta ?: ""}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { /* Dial simulated number */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { /* Open simulated chat */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // Searching loader
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Identifying nearest ambulance and safe shelter options...", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secure Audio Voice Room Block (with transcription terminal)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Green, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECURE VOICE CHANNELS ACTIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transcription Board
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = activeSos?.voiceTranscript?.ifBlank { "Channel synchronized. Waiting for speech transcription...\n" } 
                                ?: "Channel synchronized. Waiting for speech transcription...\n",
                            color = Color(0xFF34D399),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Mute", tint = Color.White)
                        }
                        IconButton(
                            onClick = {},
                            modifier = Modifier.background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Noise Cancelled (AI ON)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tactical controls (Flashlight and Siren beacon toggles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.isSirenPlaying.value = !isSirenPlaying },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSirenPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (isSirenPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSirenPlaying) "Stop Siren" else "Play Siren",
                        fontSize = 11.sp,
                        color = if (isSirenPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.isFlashlightSosActive.value = !isFlashlightSosActive },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFlashlightSosActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isFlashlightSosActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = null,
                        tint = if (isFlashlightSosActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFlashlightSosActive) "SOS Flash Off" else "SOS Flash On",
                        fontSize = 11.sp,
                        color = if (isFlashlightSosActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.cancelSosEmergency() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CANCEL")
                }

                Button(
                    onClick = { viewModel.completeSosEmergency() },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("safe_complete_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I AM SAFE")
                }
            }
        }
    }
}

// --- 6. DISASTER ALERTS TAB FEED ---
@Composable
fun AlertsTabFeed(viewModel: MainViewModel) {
    val alerts by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DISASTER & CRISIS FEEDS",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${alerts.size} Active Alerts",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No disaster alerts active in your region.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts) { alert ->
                    val isCritical = alert.type == "Flood" || alert.type == "Earthquake"
                    val cardBorder = if (isCritical) {
                        BorderStroke(2.dp, Color.Red)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = cardBorder
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isCritical) Color.Red else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = alert.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isCritical) Color.Red else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isCritical) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = alert.type.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCritical) Color.Red else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = alert.message,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Broadcasted: Nearby Citizens",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )

                                Text(
                                    text = "30 mins ago",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 7. GEMINI AI ASSISTANT EMERGENCY CHATBOT ---
@Composable
fun AiAssistantTab(viewModel: MainViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var inputQuery by remember { mutableStateOf("") }

    val suggestedPrompts = listOf(
        "CPR Steps",
        "Stop Severe Bleeding",
        "Choking Hazard Rescue",
        "Earthquake Guidance"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EMERGENCY AI HELPDESK",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            TextButton(onClick = { viewModel.clearChat() }) {
                Text("Clear Feed", color = MaterialTheme.colorScheme.primary)
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { message ->
                when (message) {
                    is ChatMessage.User -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    modifier = Modifier.padding(12.dp),
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    is ChatMessage.Assistant -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (message.isOfflineFallback) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "OFFLINE PROTOCOL",
                                                color = Color.Gray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = message.text,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text("Saviour AI is searching emergency procedures...", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Suggestions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestedPrompts.forEach { hint ->
                SuggestionChip(
                    onClick = { viewModel.sendChatMessage("Give me urgent step by step guidelines for: $hint") },
                    label = { Text(hint, fontSize = 11.sp) }
                )
            }
        }

        // Query input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask Savior AI (e.g., 'patient choking')") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        viewModel.sendChatMessage(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag("send_ai_button")
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// --- 8. VOLUNTEER EMERGENCY DASHBOARD ---
@Composable
fun VolunteerDashboardTab(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val activeList by viewModel.activeRequests.collectAsState()
    var selectedMission by remember { mutableStateOf<EmergencyRequest?>(null) }
    var stepState by remember { mutableStateOf("Ready") } // "Ready", "Accepted", "Arrived"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Toggle Switch online offline
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = profile?.name ?: "Rescue Volunteer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (profile?.volunteerStatus == true) "READY FOR RESCUE (ONLINE)" else "OFFLINE",
                        color = if (profile?.volunteerStatus == true) Color(0xFF2E7D32) else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Switch(
                    checked = profile?.volunteerStatus ?: false,
                    onCheckedChange = { viewModel.toggleVolunteerStatus(it) },
                    modifier = Modifier.testTag("online_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (profile?.volunteerStatus != true) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Go Online to receive local emergency missions.",
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // Online list
            Text(
                text = "NEARBY INCIDENTS REQUIRING ASSISTANCE",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedMission == null) {
                if (activeList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active citizen emergencies nearby. Checking feeds...", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeList) { request ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = request.type,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color.Red
                                        )
                                        Text(
                                            text = "1.8 km",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Citizen requires immediate assistance. GPS coordinates locked.",
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            selectedMission = request
                                            stepState = "Accepted"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("accept_mission_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("ACCEPT RESCUE MISSION")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE RESCUE SCREEN FOR VOLUNTEER
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ACTIVE MISSION TRACKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedMission?.type ?: "Emergency",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Simulated Map
                        SimulatedRadarMap(
                            modifier = Modifier.fillMaxWidth(),
                            pulseColor = Color.Red
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (stepState == "Accepted") {
                            Text(
                                text = "Navigation route established. Move towards citizen location.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = { stepState = "Arrived" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("confirm_arrival_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("CONFIRM ARRIVAL ON SCENE")
                            }
                        } else {
                            Text(
                                text = "Secure rescue in progress. Ensure patient is stabilized.",
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    viewModel.awardPoints(50)
                                    selectedMission = null
                                    stepState = "Ready"
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("complete_mission_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("MARK SECURE (GAIN +50 REWARDS)")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 9. POLICE DISPATCHER DASHBOARD ---
@Composable
fun PoliceDashboardTab(viewModel: MainViewModel) {
    val activeList by viewModel.activeRequests.collectAsState()
    var selectedReport by remember { mutableStateOf<EmergencyRequest?>(null) }
    var customLogReport by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "POLICE DISPATCH QUEUE",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Box(
                modifier = Modifier
                    .background(Color.Red, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("HIGH PRIORITY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedReport == null) {
            if (activeList.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No police dispatcher requests active.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeList) { report ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReport = report },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(report.type, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Red)
                                    Text("PRIORITY: CRITICAL", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aadhaar Verified Citizen. Device battery: ${report.batteryPercentage}%.", fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    Text("Dispatch Responder >", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Detailed Incident report screen
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("INCIDENTS COORDINATION & CLOSURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(selectedReport?.type ?: "Emergency", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customLogReport,
                        onValueChange = { customLogReport = it },
                        label = { Text("Generate Incident Report") },
                        placeholder = { Text("Record final security log details...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("report_log_input")
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = { selectedReport = null }, modifier = Modifier.weight(1f)) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                viewModel.completeSosEmergency()
                                selectedReport = null
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("close_case_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("CLOSE SECURE CASE")
                        }
                    }
                }
            }
        }
    }
}

// --- 10. GOVERNMENT ADMIN ANALYTICS PANEL ---
@Composable
fun AdminDashboardTab(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("GOVERNMENT CONTROL PORTAL", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        Text("System Status, Resource Analytics, & Incident Logs", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Grid KPIs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ACTIVE SOS", fontSize = 10.sp, color = Color.Gray)
                    Text("1", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Red)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("RESPONDERS", fontSize = 10.sp, color = Color.Gray)
                    Text("142", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF1565C0))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("SYSTEM HEALTH", fontSize = 10.sp, color = Color.Gray)
                    Text("99.8%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("SAFE SHELTERS", fontSize = 10.sp, color = Color.Gray)
                    Text("12", fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heat map on Canvas
        Text("INCIDENT DENSITY HEATMAP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            drawRect(Color(0xFF0F172A))
            // draw visual circles representing thermal density
            drawCircle(Color.Red.copy(alpha = 0.5f), radius = 100f, center = Offset(size.width * 0.4f, size.height * 0.5f))
            drawCircle(Color.Red.copy(alpha = 0.3f), radius = 180f, center = Offset(size.width * 0.4f, size.height * 0.5f))
            drawCircle(Color.Yellow.copy(alpha = 0.4f), radius = 60f, center = Offset(size.width * 0.75f, size.height * 0.3f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System notifications logs
        Text("LIVE LOGS FEED", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.Black)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("[INFO] System status: green. Encryption: active.", color = Color.Green, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp)
                Text("[WARN] Flood alerts issued for Coast Road Sector 4.", color = Color.Yellow, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp)
                Text("[SOS] Active broadcast generated by Node #1092.", color = Color.Red, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp)
            }
        }
    }
}

// --- 11. CITIZEN MEDICAL PROFILE & UTILITIES TAB ---
@Composable
fun ProfileTab(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val isSatelliteMap by viewModel.isSatelliteMapEnabled.collectAsState()
    val isWearableConnected by viewModel.isWearableConnected.collectAsState()
    val isCrashDetectionEnabled by viewModel.isCrashDetectionEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Upper Profile Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = profile?.name ?: "User Profile", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(text = "Verified Citizen ID (Aadhaar Checked)", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Medical Details Card
        Text("VITAL MEDICAL DETAILS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Blood Group", fontWeight = FontWeight.SemiBold)
                    Text(profile?.bloodGroup ?: "O+", fontWeight = FontWeight.Black, color = Color.Red)
                }
                Divider(modifier = Modifier.padding(vertical = 10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Allergies", fontWeight = FontWeight.SemiBold)
                    Text(profile?.allergies?.ifBlank { "None Recorded" } ?: "None", fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Emergency Contacts", fontWeight = FontWeight.SemiBold)
                    Text(profile?.emergencyContacts ?: "Not Setup", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Additional smart features
        Text("ADDITIONAL SMART FEATURES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Satellite Map
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Satellite Map View", fontWeight = FontWeight.SemiBold)
                    }
                    Switch(checked = isSatelliteMap, onCheckedChange = { viewModel.isSatelliteMapEnabled.value = it })
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Wearable Connection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Smart Wearable Sync", fontWeight = FontWeight.SemiBold)
                    }
                    Switch(checked = isWearableConnected, onCheckedChange = { viewModel.isWearableConnected.value = it })
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Vehicle crash SOS detection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Vehicle Crash SOS", fontWeight = FontWeight.SemiBold)
                    }
                    Switch(checked = isCrashDetectionEnabled, onCheckedChange = { viewModel.isCrashDetectionEnabled.value = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text("LOGOUT SECURE PORTAL")
        }
    }
}
