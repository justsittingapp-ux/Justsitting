package com.lawrence.justsitting

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class MeditationState(
    val context: Context,
    val prefs: SharedPreferences
) {
    var totalSecondsSet by mutableIntStateOf(600)
    var secondsLeft by mutableIntStateOf(600)
    var warmupSecondsSet by mutableIntStateOf(10)
    var currentWarmupSeconds by mutableIntStateOf(0)

    private val _isRunning = mutableStateOf(false)
    var isRunning: Boolean
        get() = _isRunning.value
        set(value) {
            if (value && !_isRunning.value) {
                startMeditation()
            } else if (!value && _isRunning.value) {
                stopMeditation()
            }
        }

    private val _isPaused = mutableStateOf(false)
    var isPaused: Boolean
        get() = _isPaused.value
        set(value) {
            if (_isPaused.value != value) {
                _isPaused.value = value
                if (value) {
                    MeditationManager.pauseSession(context, this)
                } else {
                    MeditationManager.resumeSession(context, this)
                }
            }
        }

    var isWarmupActive by mutableStateOf(false)

    private val _isFinished = mutableStateOf(false)
    var isFinished: Boolean
        get() = _isFinished.value
        set(value) {
            _isFinished.value = value
            if (!value) {
                MeditationManager.resetSession(context, this)
            }
        }

    fun syncFromPrefs(ctx: Context) {
        if (!_isRunning.value && !_isFinished.value) {
            totalSecondsSet = prefs.getInt("total_time", 600).let { if (it <= 0) 600 else it }
            secondsLeft = totalSecondsSet
            warmupSecondsSet = prefs.getInt("warmup_time", 10).let { if (it < 0) 0 else it }
            currentWarmupSeconds = 0
        }
    }

    fun playBell(scope: CoroutineScope, times: Int = 1) {
        MeditationManager.playBellAudio(context, times)
    }

    fun startMeditation() {
        val useDND = prefs.getBoolean("use_dnd", false)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (useDND && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!nm.isNotificationPolicyAccessGranted) {
                android.widget.Toast.makeText(context, "Enable DND access", android.widget.Toast.LENGTH_LONG).show()
                try {
                    val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) { e.printStackTrace() }
                return
            }
        }

        _isRunning.value = true
        _isPaused.value = false
        _isFinished.value = false
        MeditationManager.startSession(context, this)
    }

    fun stopMeditation() {
        _isRunning.value = false
        _isPaused.value = false
        MeditationManager.stopSession(context, this)
    }

    internal fun setRunningInternal(running: Boolean) {
        _isRunning.value = running
    }

    internal fun setPausedInternal(paused: Boolean) {
        _isPaused.value = paused
    }

    internal fun setFinishedInternal(finished: Boolean) {
        _isFinished.value = finished
    }
}

@Composable
fun rememberMeditationState(wakeLock: PowerManager.WakeLock? = null): MeditationState {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(MeditationManager.PREFS_NAME, Context.MODE_PRIVATE) }
    val state = remember { MeditationState(context, prefs) }

    // Reconciliere la pornire inițială
    LaunchedEffect(Unit) {
        MeditationManager.reconcile(context, state)
    }

    // Reconciliere ori de câte ori utilizatorul revine în aplicație (ecran aprins / deblocat)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                MeditationManager.reconcile(context, state)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Ascultăm schimbările de preferințe din ecranul de Settings
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "total_time" || key == "warmup_time") {
                state.syncFromPrefs(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return state
}

@Composable
fun TimerEngine(state: MeditationState) {
    val activity = state.context as? Activity
    val keepScreenOn = state.prefs.getBoolean("keep_screen_on", false)

    // Gestionează menținerea ecranului pornit în activitate dacă utilizatorul a bifat această opțiune
    LaunchedEffect(state.isRunning, state.isPaused, keepScreenOn) {
        if (keepScreenOn && state.isRunning && !state.isPaused) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Buclă de actualizare vizuală a secundelor sincronizată cu ceasul
    LaunchedEffect(state.isRunning, state.isPaused) {
        if (!state.isRunning || state.isPaused) return@LaunchedEffect

        while (state.isRunning && !state.isPaused) {
            val delayMs = MeditationManager.updateStateFromClock(state.context, state)
            delay(delayMs)
        }
    }
}
