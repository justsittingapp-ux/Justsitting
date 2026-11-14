package com.lawrence.justsitting

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

object MeditationManager {
    const val PREFS_NAME = "meditation_prefs"

    private val audioMutex = Mutex()
    @Volatile
    private var activeAudioJob: Job? = null
    private val activeMediaPlayers = java.util.Collections.synchronizedList(mutableListOf<MediaPlayer>())
    @Volatile
    private var activeWakeLock: PowerManager.WakeLock? = null
    @Volatile
    private var originalSystemAlarmVolume: Int? = null

    const val ACTION_WARMUP_END = "com.lawrence.justsitting.ACTION_WARMUP_END"
    const val ACTION_INTERVAL_BELL = "com.lawrence.justsitting.ACTION_INTERVAL_BELL"
    const val ACTION_SESSION_END = "com.lawrence.justsitting.ACTION_SESSION_END"

    const val KEY_SESSION_STATE = "session_state" // "IDLE", "WARMUP", "MEDITATING", "PAUSED", "FINISHED"
    const val KEY_WARMUP_END_TIME = "warmup_end_time"
    const val KEY_SESSION_START_TIME = "session_start_time"
    const val KEY_SESSION_END_TIME = "session_end_time"
    const val KEY_TOTAL_SECONDS_SET = "total_seconds_set"
    const val KEY_WARMUP_SECONDS_SET = "warmup_seconds_set"
    const val KEY_PAUSED_SECONDS_LEFT = "paused_seconds_left"
    const val KEY_PAUSED_MILLIS_LEFT = "paused_millis_left"
    const val KEY_WAS_IN_WARMUP = "was_in_warmup"
    const val KEY_WARMUP_BELL_PLAYED = "warmup_bell_played"
    const val KEY_LAST_INTERVAL_INDEX = "last_interval_index"
    const val KEY_SAVED_DND_FILTER = "saved_dnd_filter"
    const val KEY_SESSION_RECORDED = "session_recorded"
    const val KEY_BELL_VOLUME = "bell_volume"
    const val KEY_SAVED_ALARM_VOLUME = "saved_system_alarm_volume"

    fun startSession(context: Context, state: MeditationState? = null) {
        // Oprim orice sunet activ și garantăm restaurarea volumului
        stopAudio(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val totalTime = prefs.getInt("total_time", 600).let { if (it <= 0) 600 else it }
        val warmupTime = prefs.getInt("warmup_time", 10).let { if (it < 0) 0 else it }
        val now = System.currentTimeMillis()

        // 1. Activăm Do Not Disturb dacă este configurat
        activateDnd(context, prefs)

        // 2. Curățăm orice alarmă rămasă dinainte
        cancelAllAlarms(context)

        // 3. Configurăm starea sesiunii
        if (warmupTime > 0) {
            val warmupEnd = now + (warmupTime * 1000L)
            val sessionEnd = warmupEnd + (totalTime * 1000L)

            prefs.edit()
                .putString(KEY_SESSION_STATE, "WARMUP")
                .putLong(KEY_WARMUP_END_TIME, warmupEnd)
                .putLong(KEY_SESSION_START_TIME, warmupEnd)
                .putLong(KEY_SESSION_END_TIME, sessionEnd)
                .putInt(KEY_TOTAL_SECONDS_SET, totalTime)
                .putInt(KEY_WARMUP_SECONDS_SET, warmupTime)
                .putBoolean(KEY_SESSION_RECORDED, false)
                .putBoolean("history_recorded_for_session", false)
                .putBoolean(KEY_WAS_IN_WARMUP, true)
                .putBoolean(KEY_WARMUP_BELL_PLAYED, false)
                .putInt(KEY_LAST_INTERVAL_INDEX, 0)
                .apply()

            state?.let {
                it.totalSecondsSet = totalTime
                it.secondsLeft = totalTime
                it.warmupSecondsSet = warmupTime
                it.currentWarmupSeconds = warmupTime
                it.isWarmupActive = true
                it.setRunningInternal(true)
                it.setPausedInternal(false)
                it.setFinishedInternal(false)
            }

            // Programăm alarma pentru sfârșitul perioadei de warmup
            scheduleAlarm(context, ACTION_WARMUP_END, 1, warmupEnd)
            // Pre-programăm alarma finală ca plasă de siguranță
            scheduleAlarm(context, ACTION_SESSION_END, 3, sessionEnd)
        } else {
            val sessionEnd = now + (totalTime * 1000L)

            prefs.edit()
                .putString(KEY_SESSION_STATE, "MEDITATING")
                .putLong(KEY_SESSION_START_TIME, now)
                .putLong(KEY_SESSION_END_TIME, sessionEnd)
                .putInt(KEY_TOTAL_SECONDS_SET, totalTime)
                .putInt(KEY_WARMUP_SECONDS_SET, 0)
                .putBoolean(KEY_SESSION_RECORDED, false)
                .putBoolean("history_recorded_for_session", false)
                .putBoolean(KEY_WAS_IN_WARMUP, false)
                .putBoolean(KEY_WARMUP_BELL_PLAYED, true)
                .putInt(KEY_LAST_INTERVAL_INDEX, 0)
                .apply()

            state?.let {
                it.totalSecondsSet = totalTime
                it.secondsLeft = totalTime
                it.warmupSecondsSet = 0
                it.currentWarmupSeconds = 0
                it.isWarmupActive = false
                it.setRunningInternal(true)
                it.setPausedInternal(false)
                it.setFinishedInternal(false)
            }

            // Fără warmup: sună gongul de start imediat
            playBellAudio(context, 1)

            // Programăm alarma de final de sesiune
            scheduleAlarm(context, ACTION_SESSION_END, 3, sessionEnd)

            // Programăm primul gong intermediar dacă este activat
            scheduleNextIntervalBell(context, prefs, now, sessionEnd)
        }
    }

    fun pauseSession(context: Context, state: MeditationState? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sessionState = prefs.getString(KEY_SESSION_STATE, "IDLE") ?: "IDLE"
        if (sessionState != "WARMUP" && sessionState != "MEDITATING") return

        val now = System.currentTimeMillis()
        val wasInWarmup = (sessionState == "WARMUP")

        val remainingMs = if (wasInWarmup) {
            val warmupEnd = prefs.getLong(KEY_WARMUP_END_TIME, now)
            maxOf(0L, warmupEnd - now)
        } else {
            val sessionEnd = prefs.getLong(KEY_SESSION_END_TIME, now)
            maxOf(0L, sessionEnd - now)
        }
        val remainingSec = maxOf(0, ((remainingMs + 999) / 1000).toInt())

        prefs.edit()
            .putString(KEY_SESSION_STATE, "PAUSED")
            .putInt(KEY_PAUSED_SECONDS_LEFT, remainingSec)
            .putLong(KEY_PAUSED_MILLIS_LEFT, remainingMs)
            .putBoolean(KEY_WAS_IN_WARMUP, wasInWarmup)
            .apply()

        cancelAllAlarms(context)

        state?.let {
            it.setPausedInternal(true)
            if (wasInWarmup) {
                it.currentWarmupSeconds = remainingSec
            } else {
                it.secondsLeft = remainingSec
            }
        }
    }

    fun resumeSession(context: Context, state: MeditationState? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_SESSION_STATE, "IDLE") != "PAUSED") return

        val wasInWarmup = prefs.getBoolean(KEY_WAS_IN_WARMUP, false)
        val pausedSec = prefs.getInt(KEY_PAUSED_SECONDS_LEFT, 0)
        val pausedMs = prefs.getLong(KEY_PAUSED_MILLIS_LEFT, pausedSec * 1000L)
        val totalSeconds = prefs.getInt(KEY_TOTAL_SECONDS_SET, 600)
        val now = System.currentTimeMillis()

        if (wasInWarmup) {
            val warmupEnd = now + pausedMs
            val sessionEnd = warmupEnd + (totalSeconds * 1000L)

            prefs.edit()
                .putString(KEY_SESSION_STATE, "WARMUP")
                .putLong(KEY_WARMUP_END_TIME, warmupEnd)
                .putLong(KEY_SESSION_START_TIME, warmupEnd)
                .putLong(KEY_SESSION_END_TIME, sessionEnd)
                .putBoolean(KEY_WAS_IN_WARMUP, true)
                .putBoolean(KEY_WARMUP_BELL_PLAYED, false)
                .putInt(KEY_LAST_INTERVAL_INDEX, 0)
                .apply()

            state?.let {
                it.setPausedInternal(false)
                it.isWarmupActive = true
                it.currentWarmupSeconds = pausedSec
            }

            scheduleAlarm(context, ACTION_WARMUP_END, 1, warmupEnd)
            scheduleAlarm(context, ACTION_SESSION_END, 3, sessionEnd)
        } else {
            val sessionEnd = now + pausedMs
            val elapsedMs = maxOf(0L, (totalSeconds * 1000L) - pausedMs)
            val virtualStartTime = now - elapsedMs
            val intervalMinutes = prefs.getInt("interval_minutes", 15)
            val elapsedSec = (elapsedMs / 1000L).toInt()
            val currentIntervalIndex = if (intervalMinutes > 0) elapsedSec / (intervalMinutes * 60) else 0

            prefs.edit()
                .putString(KEY_SESSION_STATE, "MEDITATING")
                .putLong(KEY_SESSION_START_TIME, virtualStartTime)
                .putLong(KEY_SESSION_END_TIME, sessionEnd)
                .putBoolean(KEY_WAS_IN_WARMUP, false)
                .putBoolean(KEY_WARMUP_BELL_PLAYED, true)
                .putInt(KEY_LAST_INTERVAL_INDEX, currentIntervalIndex)
                .apply()

            state?.let {
                it.setPausedInternal(false)
                it.isWarmupActive = false
                it.secondsLeft = pausedSec
            }

            scheduleAlarm(context, ACTION_SESSION_END, 3, sessionEnd)
            scheduleNextIntervalBell(context, prefs, virtualStartTime, sessionEnd)
        }
    }

    fun stopSession(context: Context, state: MeditationState? = null) {
        stopAudio(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cancelAllAlarms(context)
        restoreDnd(context, prefs)

        prefs.edit()
            .putString(KEY_SESSION_STATE, "IDLE")
            .putBoolean(KEY_SESSION_RECORDED, false)
            .putBoolean("history_recorded_for_session", false)
            .putBoolean(KEY_WARMUP_BELL_PLAYED, false)
            .putInt(KEY_LAST_INTERVAL_INDEX, 0)
            .apply()

        state?.let {
            it.setRunningInternal(false)
            it.setPausedInternal(false)
            it.isWarmupActive = false
            it.setFinishedInternal(false)
            it.syncFromPrefs(context)
        }
    }

    fun resetSession(context: Context, state: MeditationState? = null) {
        stopAudio(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SESSION_STATE, "IDLE")
            .putBoolean(KEY_SESSION_RECORDED, false)
            .putBoolean("history_recorded_for_session", false)
            .putBoolean(KEY_WARMUP_BELL_PLAYED, false)
            .putInt(KEY_LAST_INTERVAL_INDEX, 0)
            .apply()

        state?.let {
            it.setRunningInternal(false)
            it.setPausedInternal(false)
            it.isWarmupActive = false
            it.setFinishedInternal(false)
            it.syncFromPrefs(context)
        }
    }

    fun reconcile(context: Context, state: MeditationState) {
        restoreSavedSystemAlarmVolume(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sessionState = prefs.getString(KEY_SESSION_STATE, "IDLE") ?: "IDLE"
        val now = System.currentTimeMillis()

        when (sessionState) {
            "IDLE" -> {
                state.setRunningInternal(false)
                state.setPausedInternal(false)
                state.isWarmupActive = false
                state.setFinishedInternal(false)
                state.syncFromPrefs(context)
            }
            "PAUSED" -> {
                val wasInWarmup = prefs.getBoolean(KEY_WAS_IN_WARMUP, false)
                val pausedSec = prefs.getInt(KEY_PAUSED_SECONDS_LEFT, 0)
                state.setRunningInternal(true)
                state.setPausedInternal(true)
                state.setFinishedInternal(false)
                state.totalSecondsSet = prefs.getInt(KEY_TOTAL_SECONDS_SET, 600)
                if (wasInWarmup) {
                    state.isWarmupActive = true
                    state.warmupSecondsSet = prefs.getInt(KEY_WARMUP_SECONDS_SET, 10)
                    state.currentWarmupSeconds = pausedSec
                    state.secondsLeft = state.totalSecondsSet
                } else {
                    state.isWarmupActive = false
                    state.secondsLeft = pausedSec
                }
            }
            "FINISHED" -> {
                state.setRunningInternal(false)
                state.setPausedInternal(false)
                state.isWarmupActive = false
                state.setFinishedInternal(true)
                state.secondsLeft = 0
                state.totalSecondsSet = prefs.getInt(KEY_TOTAL_SECONDS_SET, 600)
            }
            "WARMUP" -> {
                val warmupEnd = prefs.getLong(KEY_WARMUP_END_TIME, 0L)
                val sessionEnd = prefs.getLong(KEY_SESSION_END_TIME, 0L)
                state.totalSecondsSet = prefs.getInt(KEY_TOTAL_SECONDS_SET, 600)
                state.warmupSecondsSet = prefs.getInt(KEY_WARMUP_SECONDS_SET, 10)

                if (now < warmupEnd) {
                    state.setRunningInternal(true)
                    state.setPausedInternal(false)
                    state.isWarmupActive = true
                    state.setFinishedInternal(false)
                    state.currentWarmupSeconds = maxOf(0, ((warmupEnd - now + 999) / 1000).toInt())
                    state.secondsLeft = state.totalSecondsSet
                } else if (now < sessionEnd) {
                    state.setRunningInternal(true)
                    state.setPausedInternal(false)
                    state.isWarmupActive = false
                    state.setFinishedInternal(false)
                    state.currentWarmupSeconds = 0
                    state.secondsLeft = maxOf(0, ((sessionEnd - now + 999) / 1000).toInt())
                    if (!prefs.getBoolean(KEY_WARMUP_BELL_PLAYED, false)) {
                        CoroutineScope(Dispatchers.Default).launch {
                            handleWarmupEnd(context)
                        }
                    } else {
                        prefs.edit().putString(KEY_SESSION_STATE, "MEDITATING").apply()
                    }
                } else {
                    finalizeFinishedSession(context, prefs)
                    state.setRunningInternal(false)
                    state.setPausedInternal(false)
                    state.isWarmupActive = false
                    state.setFinishedInternal(true)
                    state.secondsLeft = 0
                }
            }
            "MEDITATING" -> {
                val sessionEnd = prefs.getLong(KEY_SESSION_END_TIME, 0L)
                state.totalSecondsSet = prefs.getInt(KEY_TOTAL_SECONDS_SET, 600)

                if (now < sessionEnd) {
                    state.setRunningInternal(true)
                    state.setPausedInternal(false)
                    state.isWarmupActive = false
                    state.setFinishedInternal(false)
                    state.secondsLeft = maxOf(0, ((sessionEnd - now + 999) / 1000).toInt())
                } else {
                    finalizeFinishedSession(context, prefs)
                    state.setRunningInternal(false)
                    state.setPausedInternal(false)
                    state.isWarmupActive = false
                    state.setFinishedInternal(true)
                    state.secondsLeft = 0
                }
            }
        }
    }

    fun updateStateFromClock(context: Context, state: MeditationState): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sessionState = prefs.getString(KEY_SESSION_STATE, "IDLE") ?: "IDLE"
        val now = System.currentTimeMillis()

        if (sessionState == "WARMUP") {
            val warmupEnd = prefs.getLong(KEY_WARMUP_END_TIME, 0L)
            val remainingWarmupMs = warmupEnd - now
            val remainingWarmup = ((remainingWarmupMs + 999) / 1000).toInt()
            if (remainingWarmup > 0) {
                state.currentWarmupSeconds = remainingWarmup
                state.isWarmupActive = true
                val millisUntilNext = ((remainingWarmupMs - 1) % 1000L) + 1L
                return (millisUntilNext + 20L).coerceIn(50L, 1050L)
            } else {
                state.currentWarmupSeconds = 0
                state.isWarmupActive = false

                if (!prefs.getBoolean(KEY_WARMUP_BELL_PLAYED, false)) {
                    CoroutineScope(Dispatchers.Default).launch {
                        handleWarmupEnd(context)
                    }
                } else {
                    prefs.edit().putString(KEY_SESSION_STATE, "MEDITATING").apply()
                }

                val sessionEnd = prefs.getLong(KEY_SESSION_END_TIME, 0L)
                val remainingSessionMs = sessionEnd - now
                val remainingSec = maxOf(0, ((remainingSessionMs + 999) / 1000).toInt())
                state.secondsLeft = remainingSec
                val millisUntilNext = if (remainingSessionMs > 0) {
                    ((remainingSessionMs - 1) % 1000L) + 1L
                } else {
                    1000L
                }
                return (millisUntilNext + 20L).coerceIn(50L, 1050L)
            }
        } else if (sessionState == "MEDITATING") {
            val sessionEnd = prefs.getLong(KEY_SESSION_END_TIME, 0L)
            val totalSeconds = prefs.getInt(KEY_TOTAL_SECONDS_SET, 600)
            val remainingSessionMs = sessionEnd - now
            val remainingSec = ((remainingSessionMs + 999) / 1000).toInt()
            if (remainingSec > 0) {
                state.secondsLeft = remainingSec
                state.isWarmupActive = false

                // Verificăm dacă a sosit momentul pentru un gong intermediar pe ecran
                val useIntervalBell = prefs.getBoolean("interval_bell", false)
                val intervalMinutes = prefs.getInt("interval_minutes", 15)
                if (useIntervalBell && intervalMinutes > 0 && remainingSec > 5) {
                    val intervalSec = intervalMinutes * 60
                    val elapsedSec = totalSeconds - remainingSec
                    if (elapsedSec > 0) {
                        val intervalIndex = elapsedSec / intervalSec
                        val lastIndex = prefs.getInt(KEY_LAST_INTERVAL_INDEX, 0)
                        if (intervalIndex > lastIndex) {
                            prefs.edit().putInt(KEY_LAST_INTERVAL_INDEX, intervalIndex).apply()
                            playBellAudio(context, 1)
                        }
                    }
                }

                val millisUntilNext = ((remainingSessionMs - 1) % 1000L) + 1L
                return (millisUntilNext + 20L).coerceIn(50L, 1050L)
            } else {
                state.secondsLeft = 0
                state.setRunningInternal(false)
                state.setFinishedInternal(true)
                if (!prefs.getBoolean(KEY_SESSION_RECORDED, false)) {
                    CoroutineScope(Dispatchers.Default).launch {
                        handleSessionEnd(context)
                    }
                }
                return 1000L
            }
        }
        return 500L
    }

    suspend fun handleWarmupEnd(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sessionState = prefs.getString(KEY_SESSION_STATE, "IDLE") ?: "IDLE"
        val wasInWarmup = prefs.getBoolean(KEY_WAS_IN_WARMUP, false)

        synchronized(this) {
            val warmupBellPlayed = prefs.getBoolean(KEY_WARMUP_BELL_PLAYED, false)
            if (warmupBellPlayed) return
            if (sessionState != "WARMUP" && !wasInWarmup && sessionState != "MEDITATING") return

            // 1. Trecem sesiunea în starea MEDITATING și marcăm că s-a terminat warmup
            prefs.edit()
                .putString(KEY_SESSION_STATE, "MEDITATING")
                .putBoolean(KEY_WAS_IN_WARMUP, false)
                .putBoolean(KEY_WARMUP_BELL_PLAYED, true)
                .commit()
        }

        // 2. Timpul de final (KEY_SESSION_END_TIME) este DEJA calculat corect din startSession (warmupEnd + totalSeconds).
        // Nu recalculăm sessionEnd = now + totalSeconds pentru a nu da timpul peste cap!
        val sessionEnd = prefs.getLong(KEY_SESSION_END_TIME, 0L)
        val sessionStart = prefs.getLong(KEY_SESSION_START_TIME, prefs.getLong(KEY_WARMUP_END_TIME, System.currentTimeMillis()))

        // 3. Programăm primul gong intermediar dacă este activat
        scheduleNextIntervalBell(context, prefs, sessionStart, sessionEnd)

        // 4. Sună gongul de începere a meditației în mod asincron (fără a bloca receiver-ul sau UI-ul)
        playBellAudio(context, 1)
    }

    suspend fun handleIntervalBell(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_SESSION_STATE, "IDLE") != "MEDITATING") return

        val now = System.currentTimeMillis()
        val sessionStart = prefs.getLong(KEY_SESSION_START_TIME, now)
        val sessionEnd = prefs.getLong(KEY_SESSION_END_TIME, 0L)
        val intervalMinutes = prefs.getInt("interval_minutes", 15)
        if (intervalMinutes <= 0 || sessionEnd <= now) return

        val intervalMs = intervalMinutes * 60 * 1000L
        val elapsedMs = maxOf(0L, now - sessionStart)
        val currentIndex = ((elapsedMs + (intervalMs / 2)) / intervalMs).toInt()
        val lastIndex = prefs.getInt(KEY_LAST_INTERVAL_INDEX, 0)

        // Sunăm gongul intermediar dacă nu a fost deja sunat de UI pe ecran
        if (currentIndex > lastIndex && now < sessionEnd - 5000L) {
            prefs.edit().putInt(KEY_LAST_INTERVAL_INDEX, currentIndex).apply()
            playBellAudio(context, 1)
        }

        // Programăm următorul interval ancorat întotdeauna de sessionStart (fără drift cumulativ!)
        val nextIndex = maxOf(currentIndex + 1, lastIndex + 1)
        val nextTrigger = sessionStart + (nextIndex * intervalMs)
        if (nextTrigger < sessionEnd - 5000L) {
            scheduleAlarm(context, ACTION_INTERVAL_BELL, 2, nextTrigger)
        }
    }

    suspend fun handleSessionEnd(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sessionState = prefs.getString(KEY_SESSION_STATE, "IDLE") ?: "IDLE"
        if (sessionState != "MEDITATING" && sessionState != "WARMUP" && sessionState != "FINISHED") return

        synchronized(this) {
            if (prefs.getBoolean(KEY_SESSION_RECORDED, false)) return
            // Marcăm imediat sesiunea ca înregistrată pentru a evita apeluri multiple
            prefs.edit().putBoolean(KEY_SESSION_RECORDED, true).commit()
        }

        // 1. Înregistrăm sesiunea în istoric și salvăm restul stării (stare FINISHED, alarme anulate, DND restaurat)
        finalizeFinishedSession(context, prefs)

        // 2. Sună cele 3 gonguri finale în fundal (cu wakelock garantat, fără a bloca receiver-ul sau UI-ul)
        playBellAudio(context, 3)
    }

    fun scheduleNextIntervalBell(context: Context, prefs: SharedPreferences, sessionStartTime: Long, sessionEndTime: Long) {
        val useIntervalBell = prefs.getBoolean("interval_bell", false)
        val intervalMinutes = prefs.getInt("interval_minutes", 15)
        if (!useIntervalBell || intervalMinutes <= 0) return

        val intervalMs = intervalMinutes * 60 * 1000L
        val now = System.currentTimeMillis()
        val elapsedMs = maxOf(0L, now - sessionStartTime)
        val nextIndex = (elapsedMs / intervalMs).toInt() + 1
        val nextTrigger = sessionStartTime + (nextIndex * intervalMs)

        if (nextTrigger < sessionEndTime - 5000L) {
            scheduleAlarm(context, ACTION_INTERVAL_BELL, 2, nextTrigger)
        }
    }

    fun scheduleAlarm(context: Context, action: String, requestCode: Int, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MeditationAlarmReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
                    alarmManager.setAlarmClock(info, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
                alarmManager.setAlarmClock(info, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val actions = listOf(
            ACTION_WARMUP_END to 1,
            ACTION_INTERVAL_BELL to 2,
            ACTION_SESSION_END to 3
        )
        for ((action, code) in actions) {
            try {
                val intent = Intent(context, MeditationAlarmReceiver::class.java).apply {
                    this.action = action
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    code,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun finalizeFinishedSession(context: Context, prefs: SharedPreferences) {
        if (!prefs.getBoolean("history_recorded_for_session", false)) {
            recordSessionHistory(prefs)
            prefs.edit().putBoolean("history_recorded_for_session", true).apply()
        }
        restoreDnd(context, prefs)
        cancelAllAlarms(context)
        prefs.edit()
            .putString(KEY_SESSION_STATE, "FINISHED")
            .putBoolean(KEY_SESSION_RECORDED, true)
            .apply()
    }

    fun recordSessionHistory(prefs: SharedPreferences) {
        val totalSeconds = prefs.getInt(KEY_TOTAL_SECONDS_SET, 600)
        val history = prefs.getString("all_sessions_list", "") ?: ""
        val entry = "${System.currentTimeMillis()},$totalSeconds"
        val newHistory = if (history.isEmpty()) entry else "$history|$entry"
        val minutesToAdd = totalSeconds / 60
        val currentMinutes = prefs.getInt("total_minutes", 0)
        val currentCount = prefs.getInt("sessions_count", 0)

        prefs.edit()
            .putInt("total_minutes", currentMinutes + minutesToAdd)
            .putInt("sessions_count", currentCount + 1)
            .putString("all_sessions_list", newHistory)
            .putBoolean(KEY_SESSION_RECORDED, true)
            .apply()
    }

    fun activateDnd(context: Context, prefs: SharedPreferences) {
        val useDND = prefs.getBoolean("use_dnd", false)
        if (!useDND) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                if (nm.isNotificationPolicyAccessGranted) {
                    val currentFilter = nm.currentInterruptionFilter
                    prefs.edit().putInt(KEY_SAVED_DND_FILTER, currentFilter).apply()
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreDnd(context: Context, prefs: SharedPreferences) {
        val useDND = prefs.getBoolean("use_dnd", false)
        if (!useDND) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                if (nm.isNotificationPolicyAccessGranted) {
                    val savedFilter = prefs.getInt(KEY_SAVED_DND_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL)
                    nm.setInterruptionFilter(savedFilter)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isAudioActive(): Boolean = activeAudioJob?.isActive == true

    fun stopAudio(context: Context) {
        synchronized(this) {
            try {
                activeAudioJob?.cancel()
                activeAudioJob = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                synchronized(activeMediaPlayers) {
                    for (mp in activeMediaPlayers) {
                        try {
                            if (mp.isPlaying) mp.stop()
                            mp.release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    activeMediaPlayers.clear()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                activeWakeLock?.let {
                    if (it.isHeld) {
                        it.release()
                    }
                }
                activeWakeLock = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            restoreSavedSystemAlarmVolume(context, force = true)
        }
    }

    fun restoreSavedSystemAlarmVolume(context: Context, force: Boolean = false) {
        if (!force && isAudioActive()) {
            // Nu restaurăm volumul telefonului în timp ce gongurile sunt în plină redare (ex: când utilizatorul aprinde ecranul)
            return
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val volumeToRestore = originalSystemAlarmVolume ?: run {
            if (prefs.contains(KEY_SAVED_ALARM_VOLUME)) {
                prefs.getInt(KEY_SAVED_ALARM_VOLUME, -1).takeIf { it >= 0 }
            } else null
        }

        if (volumeToRestore != null) {
            try {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val safeVolume = volumeToRestore.coerceIn(0, maxVolume)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, safeVolume, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                originalSystemAlarmVolume = null
                prefs.edit().remove(KEY_SAVED_ALARM_VOLUME).apply()
            }
        }
    }

    fun playBellAudio(context: Context, times: Int = 1) {
        val job = CoroutineScope(Dispatchers.Default).launch {
            playBellAudioSuspending(context, times)
        }
        activeAudioJob = job
    }

    suspend fun playBellAudioSuspending(context: Context, times: Int = 1) {
        audioMutex.withLock {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "JustSitting::BellSuspendingWakeLock"
            ).apply {
                setReferenceCounted(false)
            }
            activeWakeLock = wakeLock
            val durationMs = if (times > 1) 50_000L else 15_000L
            try {
                wakeLock.acquire(durationMs)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Citim volumul setat de utilizator (implicit 80%, range 1-100)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val bellVolumePercent = prefs.getInt(KEY_BELL_VOLUME, 80).coerceIn(1, 100)
            val desiredFraction = bellVolumePercent / 100f

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val currentFraction = currentAlarmVolume.toFloat() / maxAlarmVolume.coerceAtLeast(1)

            // Verificăm dacă volumul curent al alarmei telefonului este suficient pentru a reda procentul dorit
            // fără a modifica volumul din setările de sistem ale telefonului.
            var changedSystemVolume = false
            val fineVolume: Float

            if (currentFraction >= desiredFraction && currentAlarmVolume > 0) {
                // Volumul alarmei telefonului este deja suficient (ex: 100%, iar gongul e setat la 50%).
                // NU atingem volumul de sistem al telefonului! Calibrăm doar volumul intern MediaPlayer.
                fineVolume = (desiredFraction / currentFraction).coerceIn(0.01f, 1.0f)
            } else {
                // Volumul alarmei telefonului este mai mic decât volumul dorit pentru gong (sau este pe 0/mute).
                // Doar în acest caz ridicăm temporar stream-ul de alarmă pentru a garanta audibilitatea gongului.
                val targetAlarmVolume = (maxAlarmVolume * desiredFraction).roundToInt().coerceIn(1, maxAlarmVolume)

                // Salvăm volumul original DOAR dacă nu a fost deja salvat anterior
                if (originalSystemAlarmVolume == null) {
                    val savedFromPrefs = if (prefs.contains(KEY_SAVED_ALARM_VOLUME)) {
                        prefs.getInt(KEY_SAVED_ALARM_VOLUME, currentAlarmVolume)
                    } else null
                    val originalToSave = savedFromPrefs ?: currentAlarmVolume
                    originalSystemAlarmVolume = originalToSave
                    prefs.edit().putInt(KEY_SAVED_ALARM_VOLUME, originalToSave).apply()
                }

                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetAlarmVolume, 0)
                    changedSystemVolume = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val activeStream = audioManager.getStreamVolume(AudioManager.STREAM_ALARM).coerceAtLeast(1)
                val activeFraction = activeStream.toFloat() / maxAlarmVolume.coerceAtLeast(1)
                fineVolume = (desiredFraction / activeFraction).coerceIn(0.01f, 1.0f)
            }

            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                repeat(times) { index ->
                    if (!coroutineContext.isActive) return@repeat

                    val completionSignal = CompletableDeferred<Unit>()
                    var trackDurationMs = 8000L

                    try {
                        val mp = MediaPlayer.create(context, R.raw.bell, audioAttributes, 0)
                        if (mp != null) {
                            activeMediaPlayers.add(mp)
                            val dur = mp.duration
                            if (dur > 0) {
                                trackDurationMs = dur.toLong()
                            }
                            mp.setVolume(fineVolume, fineVolume)
                            mp.setOnCompletionListener {
                                it.release()
                                activeMediaPlayers.remove(it)
                                completionSignal.complete(Unit)
                            }
                            mp.setOnErrorListener { it, _, _ ->
                                it.release()
                                activeMediaPlayers.remove(it)
                                completionSignal.complete(Unit)
                                true
                            }
                            mp.start()
                        } else {
                            completionSignal.complete(Unit)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        completionSignal.complete(Unit)
                    }

                    if (index < times - 1) {
                        // Distanță firească și muzicală între gongurile de final (3.5 secunde).
                        // Gongul anterior continuă să rezoneze armonios în fundal fără să fie tăiat brusc.
                        delay(3500L)
                    } else {
                        // Pentru ultimul gong, așteptăm să își termine întreaga vibrație sonoră înainte de a restaura volumul
                        withTimeoutOrNull(trackDurationMs + 1500L) {
                            completionSignal.await()
                        }
                    }
                }
            } finally {
                // Curățăm orice player rămas activ
                synchronized(activeMediaPlayers) {
                    for (mp in activeMediaPlayers) {
                        try {
                            if (mp.isPlaying) mp.stop()
                            mp.release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    activeMediaPlayers.clear()
                }

                // Restaurăm volumul original al alarmei dacă a fost modificat temporar
                if (changedSystemVolume) {
                    restoreSavedSystemAlarmVolume(context, force = true)
                }
                try {
                    if (wakeLock.isHeld) {
                        wakeLock.release()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (activeWakeLock == wakeLock) {
                    activeWakeLock = null
                }
            }
        }
    }
}
