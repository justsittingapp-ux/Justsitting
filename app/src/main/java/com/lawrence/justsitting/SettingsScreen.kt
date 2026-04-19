package com.lawrence.justsitting

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(onSaveSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("meditation_prefs", Context.MODE_PRIVATE)

    var tempH by remember { mutableIntStateOf(prefs.getInt("total_time", 600) / 3600) }
    var tempM by remember { mutableIntStateOf((prefs.getInt("total_time", 600) % 3600) / 60) }
    var tempWarmup by remember { mutableIntStateOf(prefs.getInt("warmup_time", 10)) }
    var useDND by remember { mutableStateOf(prefs.getBoolean("use_dnd", false)) }
    var keepScreenOn by remember { mutableStateOf(prefs.getBoolean("keep_screen_on", false)) }
    var useIntervalBell by remember { mutableStateOf(prefs.getBoolean("interval_bell", false)) }
    var intervalMin by remember { mutableIntStateOf(prefs.getInt("interval_minutes", 15)) }

    val isTimeValid = (tempH * 3600 + tempM * 60) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Settings",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        // --- SECTION: TIME & PREPARATION ---
        SettingsSectionTitle("TIME & PREPARATION")
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                // Meditation Duration
                SettingItemHeader(Icons.Default.Timer, "Meditation Duration", "Set your session length")
                Spacer(modifier = Modifier.height(16.dp))
                
                // NOU SELECTOR: ORE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hours", color = Color.Gray, fontSize = 14.sp)
                    TimeStepper(value = tempH, range = 0..9, label = "h") { tempH = it }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // NOU SELECTOR: MINUTE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Minutes", color = Color.Gray, fontSize = 14.sp)
                    TimeStepper(value = tempM, range = 0..59, label = "m") { tempM = it }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.White.copy(alpha = 0.05f))
                
                // Warm-up Delay
                SettingItemHeader(Icons.Default.HourglassEmpty, "Warm-up Delay", "Preparation time (seconds)")
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeStepper(value = tempWarmup, range = 0..60, label = "sec") { tempWarmup = it }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION: FOCUS & ALERTS ---
        SettingsSectionTitle("FOCUS & ALERTS")
        SettingsCard {
            Column {
                SettingsToggleRow(
                    icon = Icons.Default.NotificationsOff,
                    title = "Do Not Disturb",
                    description = "Silence alerts during session",
                    checked = useDND,
                    onCheckedChange = { useDND = it }
                )
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))
                
                SettingsToggleRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Interval Bell",
                    description = "Chime during your meditation",
                    checked = useIntervalBell,
                    onCheckedChange = { useIntervalBell = it }
                )
                
                AnimatedVisibility(
                    visible = useIntervalBell,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Repeat every:", color = Color.Gray, fontSize = 13.sp)
                        TimeStepper(value = intervalMin, range = 1..60, label = "min") { intervalMin = it }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION: DISPLAY ---
        SettingsSectionTitle("DISPLAY")
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Default.LightMode,
                title = "Keep Screen On",
                description = "Prevent screen from dimming",
                checked = keepScreenOn,
                onCheckedChange = { keepScreenOn = it }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                prefs.edit()
                    .putInt("total_time", tempH * 3600 + tempM * 60)
                    .putInt("warmup_time", tempWarmup)
                    .putBoolean("keep_screen_on", keepScreenOn)
                    .putBoolean("interval_bell", useIntervalBell)
                    .putInt("interval_minutes", intervalMin)
                    .putBoolean("use_dnd", useDND)
                    .apply()
                onSaveSuccess()
            },
            enabled = isTimeValid,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4),
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isTimeValid) "SAVE SETTINGS" else "SELECT DURATION",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.background(
            brush = Brush.verticalGradient(colors = listOf(Color(0xFF1A1A1A), Color(0xFF121212)))
        )) {
            content()
        }
    }
}

@Composable
fun SettingItemHeader(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF00BCD4).copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(description, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00BCD4),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF2A2A2A),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}