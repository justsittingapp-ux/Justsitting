package com.lawrence.justsitting

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class MeditationSession(val timestamp: Long, val durationSeconds: Int)

private fun getLocalEpochDay(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
    val offset = timeZone.getOffset(timestamp)
    return (timestamp + offset) / (24 * 60 * 60 * 1000L)
}

private fun calculateLongestStreak(sessions: List<MeditationSession>): Int {
    if (sessions.isEmpty()) return 0
    val distinctDays = sessions
        .map { getLocalEpochDay(it.timestamp) }
        .distinct()
        .sorted()

    if (distinctDays.isEmpty()) return 0

    var maxStreak = 1
    var currentStreak = 1

    for (i in 1 until distinctDays.size) {
        if (distinctDays[i] == distinctDays[i - 1] + 1L) {
            currentStreak++
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak
            }
        } else {
            currentStreak = 1
        }
    }
    return maxStreak
}

@Composable
fun StatisticsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("meditation_prefs", Context.MODE_PRIVATE)

    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var sessions by remember { mutableStateOf<List<MeditationSession>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        delay(600)

        val raw = prefs.getString("all_sessions_list", "") ?: ""
        val processed = raw.split("|").filter { it.isNotEmpty() }.mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) {
                val ts = parts[0].toLongOrNull()
                val dur = parts[1].toIntOrNull()
                if (ts != null && dur != null) MeditationSession(ts, dur) else null
            } else null
        }.reversed()

        sessions = processed
        isLoading = false
    }

    val createDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let { context.contentResolver.openOutputStream(it)?.use { s -> s.write((prefs.getString("all_sessions_list", "") ?: "").toByteArray()) } }
    }

    val openDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { context.contentResolver.openInputStream(it)?.use { s ->
            val data = s.bufferedReader().readText()
            val loadedSessions = data.split("|").filter { it.isNotEmpty() }.mapNotNull {
                val parts = it.split(",")
                if (parts.size == 2) {
                    val ts = parts[0].toLongOrNull()
                    val dur = parts[1].toIntOrNull()
                    if (ts != null && dur != null) MeditationSession(ts, dur) else null
                } else null
            }
            val totalMinutes = loadedSessions.sumOf { it.durationSeconds } / 60
            val count = loadedSessions.size
            prefs.edit()
                .putString("all_sessions_list", data)
                .putInt("total_minutes", totalMinutes)
                .putInt("sessions_count", count)
                .apply()
            refreshTrigger++
        } }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        if (isLoading) {
            LoadingAnimation()
        } else {
            JourneyContent(
                sessions = sessions,
                onCreateBackup = { createDocLauncher.launch("just_sitting_backup.txt") },
                onRestoreBackup = { openDocLauncher.launch(arrayOf("text/plain")) },
                onResetRequest = { showDeleteDialog = true }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Reset Data", color = Color.White) },
            text = { Text("Delete all progress?", color = Color.Gray) },
            confirmButton = { TextButton(onClick = {
                prefs.edit()
                    .remove("all_sessions_list")
                    .putInt("total_minutes", 0)
                    .putInt("sessions_count", 0)
                    .apply()
                refreshTrigger++
                showDeleteDialog = false
            }) { Text("DELETE", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") } }
        )
    }
}

@Composable
fun JourneyContent(
    sessions: List<MeditationSession>,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onResetRequest: () -> Unit
) {
    val totalSeconds = sessions.sumOf { it.durationSeconds }
    val stillnessHours = totalSeconds / 3600
    val stillnessMinutes = (totalSeconds % 3600) / 60
    val stillnessText = "${stillnessHours}h ${stillnessMinutes}m"

    val totalSessions = sessions.size

    val distinctDays = sessions.map { getLocalEpochDay(it.timestamp) }.distinct()
    val totalDays = distinctDays.size

    val longestStreak = calculateLongestStreak(sessions)
    val streakText = when (longestStreak) {
        0 -> "0 days"
        1 -> "1 day"
        else -> "$longestStreak days"
    }

    val now = System.currentTimeMillis()
    val todayEpochDay = getLocalEpochDay(now)
    val sevenDaysCutoffDay = todayEpochDay - 6
    val recentSessions = sessions.filter { getLocalEpochDay(it.timestamp) >= sevenDaysCutoffDay }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Journey", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = onCreateBackup) { Icon(Icons.Default.Share, null, tint = Color(0xFF00BCD4).copy(alpha = 0.7f)) }
                IconButton(onClick = onRestoreBackup) { Icon(Icons.Default.Refresh, null, tint = Color(0xFFFFC107).copy(alpha = 0.7f)) }
                IconButton(onClick = onResetRequest) { Icon(Icons.Default.Delete, null, tint = Color.Gray.copy(alpha = 0.5f)) }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Grid 2x2 cu statistici
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                StatCardCompact("STILLNESS", stillnessText, Color(0xFF00BCD4))
            }
            Box(Modifier.weight(1f)) {
                StatCardCompact("SESSIONS", "$totalSessions", Color(0xFFFFC107))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                StatCardCompact("DAYS", "$totalDays", Color(0xFF4DB6AC))
            }
            Box(Modifier.weight(1f)) {
                StatCardCompact("BEST STREAK", streakText, Color(0xFFBA68C8))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RECENT (LAST 7 DAYS)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            if (recentSessions.isNotEmpty()) {
                Text(
                    "${recentSessions.size} ${if (recentSessions.size == 1) "session" else "sessions"}",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (recentSessions.isEmpty()) {
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.verticalGradient(listOf(Color(0xFF222222), Color(0xFF101010))))
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (sessions.isEmpty()) "No sessions recorded yet" else "No sessions in the last 7 days",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Take your seat whenever you are ready.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            recentSessions.forEach { session ->
                val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(session.timestamp))
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(session.timestamp))
                
                Surface(
                    color = Color.Transparent, 
                    shape = RoundedCornerShape(10.dp), 
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF222222), Color(0xFF101010))
                        ))) {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically) {
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(dateStr, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(" • ", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                                Text(timeStr, color = Color.Gray, fontSize = 13.sp)
                            }
                            
                            Text(
                                "${session.durationSeconds / 60}m", 
                                color = Color(0xFF00BCD4), 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StatCardCompact(title: String, value: String, accentColor: Color) {
    Surface(
        color = Color.Transparent, 
        shape = RoundedCornerShape(20.dp), 
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Box(modifier = Modifier.background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF282828), Color(0xFF121212))))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, color = accentColor.copy(alpha = 0.85f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val color by infiniteTransition.animateColor(
        initialValue = Color(0xFF00BCD4),
        targetValue = Color(0xFF673AB7),
        animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse),
        label = "color"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = color, strokeWidth = 2.dp, strokeCap = StrokeCap.Round)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Recalling your stillness...", color = Color.Gray, fontSize = 12.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Light)
    }
}
