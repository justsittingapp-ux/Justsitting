package com.lawrence.justsitting

import com.lawrence.justsitting.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MeditationTimerScreen(state: MeditationState, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- ANIMAȚIE DE RESPIRAȚIE ---
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    val activePulsation = if (state.isRunning && !state.isPaused) breathScale else 1f

    val progress = when {
        state.isWarmupActive && state.warmupSecondsSet > 0 -> state.currentWarmupSeconds.toFloat() / state.warmupSecondsSet.toFloat()
        !state.isWarmupActive && state.totalSecondsSet > 0 -> state.secondsLeft.toFloat() / state.totalSecondsSet.toFloat()
        else -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "TimerProgress"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(320.dp)) {
            Surface(
                modifier = Modifier.size(260.dp * activePulsation),
                shape = CircleShape,
                color = (if(state.isWarmupActive) Color(0xFFFFC107) else Color(0xFF00BCD4)).copy(alpha = 0.1f * activePulsation)
            ) {}

            Canvas(modifier = Modifier.size(280.dp)) {
                drawCircle(color = Color.White.copy(alpha = 0.1f), style = Stroke(width = 8.dp.toPx()))
                val c1 = if(state.isWarmupActive) Color(0xFFFFC107) else Color(0xFF00BCD4)
                val c2 = if(state.isWarmupActive) Color(0xFFFF9800) else Color(0xFF3F51B5)
                if (state.isRunning && !state.isPaused) {
                    drawArc(
                        brush = Brush.sweepGradient(0f to c2, 0.5f to c1, 1f to c2),
                        startAngle = -90f, sweepAngle = 360f * animatedProgress,
                        useCenter = false, style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                        alpha = 0.3f
                    )
                }
                drawArc(
                    brush = Brush.sweepGradient(0f to c2, 0.5f to c1, 1f to c2),
                    startAngle = -90f, sweepAngle = 360f * animatedProgress,
                    useCenter = false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.isWarmupActive) {
                    Text("WARMUP", color = Color(0xFFFFC107), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("00:${state.currentWarmupSeconds.toString().padStart(2, '0')}", fontSize = 64.sp, color = Color.White, fontWeight = FontWeight.ExtraLight)
                } else {
                    Text(
                        text = "%02d:%02d".format(state.secondsLeft / 60, state.secondsLeft % 60),
                        fontSize = 72.sp, color = Color.White, fontWeight = FontWeight.ExtraLight,
                        modifier = Modifier.clickable(enabled = !state.isRunning) { onOpenSettings() }
                    )
                    if (state.isRunning) {
                        Text(
                            text = if(state.isPaused) "PAUSED" else "STILLNESS",
                            color = (if(state.isPaused) Color.Gray else Color(0xFF00BCD4)),
                            fontSize = 12.sp, letterSpacing = 6.sp, modifier = Modifier.offset(y = (-4).dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    if (state.isFinished) {
                        state.secondsLeft = state.totalSecondsSet
                        state.isFinished = false
                    } else if (state.isRunning) {
                        state.isPaused = !state.isPaused
                    } else {
                        if (state.warmupSecondsSet > 0) state.isWarmupActive = true else state.playBell(scope, 1)
                        state.isRunning = true
                        state.isPaused = false
                    }
                },
                modifier = Modifier.weight(1f).height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        state.isFinished -> Color(0xFF00BCD4)
                        state.isPaused -> Color(0xFF4CAF50)
                        state.isRunning -> Color(0xFFFF9800)
                        else -> Color(0xFF00BCD4)
                    }
                ),
                shape = CircleShape
            ) {
                Text(when {
                    state.isFinished -> "RESET"
                    state.isPaused -> "RESUME"
                    state.isRunning -> "PAUSE"
                    else -> "BEGIN"
                }, color = Color.Black, fontWeight = FontWeight.Bold)
            }

            if (state.isRunning || state.isPaused) {
                Button(
                    onClick = { state.stopMeditation() },
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63).copy(alpha = 0.8f)),
                    shape = CircleShape
                ) {
                    Text("STOP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}