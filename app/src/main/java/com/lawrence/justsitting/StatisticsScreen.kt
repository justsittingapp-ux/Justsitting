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
            if(parts.size == 2) MeditationSession(parts[0].toLong(), parts[1].toInt()) else null
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
            prefs.edit().putString("all_sessions_list", data).apply()
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
                prefs.edit().remove("all_sessions_list").apply()
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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Journey", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = onCreateBackup) { Icon(Icons.Default.Share, null, tint = Color(0xFF00BCD4).copy(alpha = 0.7f)) }
                IconButton(onClick = onRestoreBackup) { Icon(Icons.Default.Refresh, null, tint = Color(0xFFFFC107).copy(alpha = 0.7f)) }
                IconButton(onClick = onResetRequest) { Icon(Icons.Default.Delete, null, tint = Color.Gray.copy(alpha = 0.5f)) }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1.2f)) { StatCardCompact("STILLNESS", "${sessions.sumOf { it.durationSeconds } / 3600}h ${(sessions.sumOf { it.durationSeconds } % 3600) / 60}m", Color(0xFF00BCD4)) }
            Box(Modifier.weight(1f)) { StatCardCompact("SESSIONS", "${sessions.size}", Color(0xFFFFC107)) }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("HISTORY", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(modifier = Modifier.height(12.dp))

        sessions.forEach { session ->
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
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StatCardCompact(title: String, value: String, accentColor: Color) {
    Surface(
        color = Color.Transparent, 
        shape = RoundedCornerShape(20.dp), 
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)) // Border subtil reintrodus doar aici
    ) {
        Box(modifier = Modifier.background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF282828), Color(0xFF121212))))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, color = accentColor.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
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