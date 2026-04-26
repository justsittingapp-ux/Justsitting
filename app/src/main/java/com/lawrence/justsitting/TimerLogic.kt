package com.lawrence.justsitting

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MeditationState(
    val context: Context,
    val prefs: SharedPreferences,
    val wakeLock: PowerManager.WakeLock?
) {
    var totalSecondsSet by mutableIntStateOf(prefs.getInt("total_time", 600).let { if (it <= 0) 600 else it })
    var secondsLeft by mutableIntStateOf(totalSecondsSet)
    var warmupSecondsSet by mutableIntStateOf(prefs.getInt("warmup_time", 10).let { if (it < 0) 0 else it })
    var currentWarmupSeconds by mutableIntStateOf(0)

    var isRunning by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var isWarmupActive by mutableStateOf(false)
    var isFinished by mutableStateOf(false)

    fun playBell(scope: CoroutineScope, times: Int = 1) {
        scope.launch {
            repeat(times) {
                try {
                    val mp = MediaPlayer.create(context, R.raw.bell)
                    mp?.setOnCompletionListener { it.release() }
                    mp?.start()
                } catch (e: Exception) { e.printStackTrace() }
                if (it < times - 1) delay(5000)
            }
        }
    }

    fun updateNotification(text: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, "timer_ch")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Just Sitting")
                .setContentText(text)
                .setOngoing(true).setSilent(true).build()
            nm.notify(1, notification)
        } catch (e: Exception) {}
    }

    fun stopMeditation() {
        isRunning = false
        isPaused = false
        isWarmupActive = false
        val stopIntent = Intent(context, TimerService::class.java).apply { action = "STOP_SERVICE" }
        context.startService(stopIntent)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (nm.isNotificationPolicyAccessGranted) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        (context as? Activity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        secondsLeft = totalSecondsSet
        currentWarmupSeconds = 0
    }
}

@Composable
fun rememberMeditationState(wakeLock: PowerManager.WakeLock?): MeditationState {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("meditation_prefs", Context.MODE_PRIVATE) }
    val state = remember { MeditationState(context, prefs, wakeLock) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "total_time") {
                state.totalSecondsSet = p.getInt("total_time", 600).let { if (it <= 0) 600 else it }
                if (!state.isRunning) state.secondsLeft = state.totalSecondsSet
            }
            if (key == "warmup_time") {
                state.warmupSecondsSet = p.getInt("warmup_time", 10).let { if (it < 0) 0 else it }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return state
}

@Composable
fun TimerEngine(state: MeditationState) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(state.isRunning, state.isWarmupActive, state.isPaused) {
        if (!state.isRunning || state.isPaused) return@LaunchedEffect

        val activity = state.context as? Activity
        val nm = state.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val useDND = state.prefs.getBoolean("use_dnd", false)
        val keepScreenOn = state.prefs.getBoolean("keep_screen_on", false)

        try {
            if (keepScreenOn) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            if (useDND) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!nm.isNotificationPolicyAccessGranted) {
                        state.isRunning = false
                        android.widget.Toast.makeText(state.context, "Enable DND access", android.widget.Toast.LENGTH_LONG).show()
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            state.context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return@LaunchedEffect
                    } else {
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    }
                }
            }

            ContextCompat.startForegroundService(state.context, Intent(state.context, TimerService::class.java))

            if (state.isWarmupActive) {
                state.currentWarmupSeconds = if (state.currentWarmupSeconds > 0) state.currentWarmupSeconds else state.warmupSecondsSet
                while (state.currentWarmupSeconds > 0 && state.isRunning && !state.isPaused) {
                    state.wakeLock?.acquire(2000L)
                    state.updateNotification("Preparing... ${state.currentWarmupSeconds} s")
                    delay(1000L)
                    state.currentWarmupSeconds--
                }
                if (state.isRunning && !state.isPaused && state.currentWarmupSeconds <= 0) {
                    state.isWarmupActive = false
                    state.playBell(scope, 1)
                }
            }

            if (!state.isWarmupActive && state.isRunning && !state.isPaused) {
                val useIntervalBell = state.prefs.getBoolean("interval_bell", false)
                val intervalSec = state.prefs.getInt("interval_minutes", 15) * 60

                while (state.secondsLeft > 0 && state.isRunning && !state.isPaused) {
                    state.wakeLock?.acquire(2000L)
                    delay(1000L)
                    state.secondsLeft--
                    val m = state.secondsLeft / 60
                    val s = state.secondsLeft % 60
                    state.updateNotification("Remaining: %02d:%02d".format(m, s))

                    if (useIntervalBell && intervalSec > 0) {
                        val totalElapsed = state.totalSecondsSet - state.secondsLeft
                        if (totalElapsed > 0 && totalElapsed % intervalSec == 0 && state.secondsLeft > 2) {
                            state.playBell(scope, 1)
                        }
                    }
                }
            }

            if (state.secondsLeft <= 0 && state.isRunning && !state.isPaused && !state.isWarmupActive) {
                state.isRunning = false
                state.isFinished = true
                state.playBell(scope, 3)
                val stopIntent = Intent(state.context, TimerService::class.java).apply { action = "STOP_SERVICE" }
                state.context.startService(stopIntent)

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (nm.isNotificationPolicyAccessGranted) {
                            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }

                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                val history = state.prefs.getString("all_sessions_list", "") ?: ""
                val entry = "${System.currentTimeMillis()},${state.totalSecondsSet}"
                state.prefs.edit()
                    .putInt("total_minutes", state.prefs.getInt("total_minutes", 0) + (state.totalSecondsSet / 60))
                    .putInt("sessions_count", state.prefs.getInt("sessions_count", 0) + 1)
                    .putString("all_sessions_list", if(history.isEmpty()) entry else "$history|$entry")
                    .apply()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}