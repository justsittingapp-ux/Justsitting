package com.lawrence.justsitting

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            .padding(horizontal = 20.dp, vertical = 12.dp) // Redus de la 24 la 12
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Settings",
            color = Color.White,
            fontSize = 24.sp, // Redus de la 28 la 24
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp) // Redus de la 24 la 12
        )

        // --- MEDITATION TIME ---
        Text("MEDITATION TIME", color = Color(0xFF00BCD4), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(4.dp)) // Redus de la 12 la 4
        Box(contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.width(140.dp).height(40.dp), verticalArrangement = Arrangement.SpaceBetween) {
                HorizontalDivider(thickness = 1.dp, color = Color(0xFF00BCD4).copy(alpha = 0.2f))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFF00BCD4).copy(alpha = 0.2f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                WheelNumberPicker(0..9, tempH, "h", isSmall = true) { tempH = it }
                Spacer(modifier = Modifier.width(10.dp))
                WheelNumberPicker(0..59, tempM, "m", isSmall = true) { tempM = it }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Redus de la 24 la 16

        // --- WARM-UP DELAY ---
        Text("WARM-UP DELAY", color = Color(0xFFFFC107), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.width(70.dp).height(40.dp), verticalArrangement = Arrangement.SpaceBetween) {
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFFFC107).copy(alpha = 0.2f))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFFFC107).copy(alpha = 0.2f))
            }
            WheelNumberPicker(0..60, tempWarmup, "sec", isSmall = true) { tempWarmup = it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- OPTIONS PANEL ---
        Surface(color = Color(0xFF161616), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(4.dp)) { // Padding intern redus
                OptionRow("Enable Do Not Disturb", useDND) { useDND = it }
                OptionRow("Keep screen always on", keepScreenOn) { keepScreenOn = it }
                OptionRow("Enable Interval Bell", useIntervalBell) { useIntervalBell = it }

                if (useIntervalBell) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Repeat every:", color = Color.Gray, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (intervalMin > 1) intervalMin-- }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color(0xFF00BCD4))
                            }
                            Text(text = "$intervalMin min", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (intervalMin < 60) intervalMin++ }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.KeyboardArrowRight, null, tint = Color(0xFF00BCD4))
                            }
                        }
                    }
                }
            }
        }

        // --- CALCULEAZĂ DACĂ TIMPUL ESTE VALID ---
        val isTimeValid = (tempH > 0 || tempM > 0)

        Spacer(modifier = Modifier.height(30.dp))

        // Butonul de Salvare
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
            // Aici blocăm interacțiunea dacă timpul e 0
            enabled = isTimeValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4), // Culoarea activă (Turcoaz)
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.12f), // Culoarea gri când e blocat
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isTimeValid) "SAVE SETTINGS" else "PICK A DURATION",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}