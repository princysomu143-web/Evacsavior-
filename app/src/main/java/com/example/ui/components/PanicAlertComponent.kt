package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.MainViewModel

/**
 * High-Visibility Panic Alert Button & Banner Component.
 *
 * Designed for life-critical distress situations where every second matters.
 * Triggers an immediate emergency signal state directly without any countdown delay.
 * Includes visual strobe animations, dual mode (Loud Siren vs Silent Duress),
 * and tactile haptic response.
 */

// Panic Color Palette
private val EmergencyCrimsonDark = Color(0xFF8B0000)
private val EmergencyCrimson = Color(0xFFD32F2F)
private val EmergencyCrimsonBright = Color(0xFFFF1744)
private val EmergencyCrimsonGlow = Color(0xFFFF5252)
private val WarningAmber = Color(0xFFFF9800)

/**
 * 1. High-Visibility Hero Panic Banner.
 * Designed to be prominently positioned at the top of the citizen home portal.
 */
@Composable
fun PanicAlertBanner(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onCustomTrigger: ((panicType: String, isSilent: Boolean) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var showCategoryDialog by remember { mutableStateOf(false) }
    var isSilentDuressMode by remember { mutableStateOf(false) }

    // Pulsating Beacon Animation
    val infiniteTransition = rememberInfiniteTransition(label = "panic_beacon_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            EmergencyCrimsonBright.copy(alpha = glowAlpha),
                            WarningAmber.copy(alpha = glowAlpha),
                            EmergencyCrimsonBright.copy(alpha = glowAlpha)
                        )
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E0B0B),
                            Color(0xFF2C0F0F),
                            Color(0xFF140505)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: Status badge & Silent toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Live Radar Blinking Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(EmergencyCrimsonBright.copy(alpha = glowAlpha), CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                        Text(
                            text = "PRIORITY 1 IMMEDIATE SIGNAL",
                            color = EmergencyCrimsonGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Silent Duress Switch Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSilentDuressMode) WarningAmber.copy(alpha = 0.2f) else Color(0xFF331616),
                        border = BorderStroke(
                            1.dp,
                            if (isSilentDuressMode) WarningAmber else Color(0xFF5A2525)
                        ),
                        modifier = Modifier.clickable {
                            isSilentDuressMode = !isSilentDuressMode
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSilentDuressMode) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (isSilentDuressMode) WarningAmber else Color(0xFFCBD5E1),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isSilentDuressMode) "Silent Duress ON" else "Siren Mode",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSilentDuressMode) WarningAmber else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Panic Alert Button Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Massive High-Visibility Panic Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (onCustomTrigger != null) {
                                onCustomTrigger(
                                    if (isSilentDuressMode) "Silent Duress Panic" else "Critical Panic Alert",
                                    isSilentDuressMode
                                )
                            } else {
                                viewModel.triggerImmediatePanicAlert(
                                    panicType = if (isSilentDuressMode) "Silent Duress Panic Alert" else "CRITICAL PANIC ALERT",
                                    isSilentDuress = isSilentDuressMode
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .drawBehind {
                                drawCircle(
                                    color = EmergencyCrimsonBright.copy(alpha = 0.2f),
                                    radius = (size.height / 1.7f) * pulseScale
                                )
                            }
                            .testTag("panic_alert_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSilentDuressMode) Color(0xFFB71C1C) else EmergencyCrimson
                        ),
                        border = BorderStroke(2.dp, Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Panic Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (isSilentDuressMode) "SILENT PANIC" else "PANIC ALERT",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "INSTANT BROADCAST • NO DELAY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }

                    // Quick Panic Category Launcher Button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCategoryDialog = true
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF331616))
                            .border(1.dp, Color(0xFF5A2525), RoundedCornerShape(16.dp))
                            .testTag("panic_options_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Panic Options",
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "OPTIONS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer note
                Text(
                    text = if (isSilentDuressMode)
                        "⚡ Instant silent dispatch: Telemetry transmitted silently to Rapid Police units."
                    else
                        "⚡ Dispatches instant GPS coordinates to nearest Rapid Action police & ambulance.",
                    fontSize = 10.5.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 14.sp
                )
            }
        }
    }

    // Modal Dialog for Specific Panic Situations
    if (showCategoryDialog) {
        PanicCategorySelectionDialog(
            isSilentDuress = isSilentDuressMode,
            onDismiss = { showCategoryDialog = false },
            onSelectPanicType = { type ->
                showCategoryDialog = false
                viewModel.triggerImmediatePanicAlert(
                    panicType = type,
                    isSilentDuress = isSilentDuressMode
                )
            }
        )
    }
}

/**
 * 2. Standalone Compact Circular/Capsule Panic Alert Button.
 * Can be embedded anywhere in custom screens or toolbars.
 */
@Composable
fun PanicAlertButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = true,
    label: String = "PANIC ALERT"
) {
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_button")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .drawBehind {
                if (isPulsing) {
                    drawCircle(
                        color = EmergencyCrimsonBright.copy(alpha = pulseAlpha),
                        radius = size.width / 1.7f
                    )
                }
            }
            .testTag("panic_alert_standalone_button"),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmergencyCrimson),
        border = BorderStroke(2.dp, Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "IMMEDIATE SIGNAL",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * 3. High-Visibility Floating Panic Action Button (FAB).
 * Persistent high-priority floating trigger available across tabs.
 */
@Composable
fun FloatingPanicButton(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .padding(16.dp)
            .testTag("floating_panic_alert_button")
    ) {
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.triggerImmediatePanicAlert(panicType = "CRITICAL PANIC ALERT")
            },
            modifier = Modifier
                .height(52.dp)
                .drawBehind {
                    drawCircle(
                        color = EmergencyCrimsonBright.copy(alpha = 0.25f),
                        radius = (size.height / 1.6f) * pulseScale
                    )
                },
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyCrimson),
            border = BorderStroke(2.dp, Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Panic Trigger",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "PANIC",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Modal Dialog for Rapid Critical Panic Selection.
 */
@Composable
private fun PanicCategorySelectionDialog(
    isSilentDuress: Boolean,
    onDismiss: () -> Unit,
    onSelectPanicType: (String) -> Unit
) {
    val panicScenarios = listOf(
        Pair("Physical Threat / Assault", Icons.Default.Shield),
        Pair("Kidnapping / Stalking", Icons.Default.Person),
        Pair("Home Intrusion / Robbery", Icons.Default.Lock),
        Pair("Sudden Medical Collapse", Icons.Default.LocalHospital),
        Pair("Accident / Vehicle Crisis", Icons.Default.Warning)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1111)),
            border = BorderStroke(2.dp, EmergencyCrimsonBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(EmergencyCrimson.copy(alpha = 0.2f), CircleShape)
                        .border(1.5.dp, EmergencyCrimsonBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = EmergencyCrimsonBright,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CRITICAL PANIC TRIGGER",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isSilentDuress)
                        "Select crisis type. Police will be silently notified immediately."
                    else
                        "Select crisis type for immediate priority broadcast. No countdown will occur.",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // List of Emergency Crisis Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    panicScenarios.forEach { scenario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPanicType(scenario.first) }
                                .testTag("panic_scenario_${scenario.first.take(10).replace(" ", "_")}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1616)),
                            border = BorderStroke(1.dp, Color(0xFF4B2323))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(EmergencyCrimsonBright.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = scenario.second,
                                        contentDescription = null,
                                        tint = EmergencyCrimsonBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = scenario.first,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Immediate Tactical Dispatch",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CANCEL",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
