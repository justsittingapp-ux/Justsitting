package com.lawrence.justsitting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class MeditationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                withTimeout(5000L) {
                    when (action) {
                        MeditationManager.ACTION_WARMUP_END -> {
                            MeditationManager.handleWarmupEnd(context)
                        }
                        MeditationManager.ACTION_INTERVAL_BELL -> {
                            MeditationManager.handleIntervalBell(context)
                        }
                        MeditationManager.ACTION_SESSION_END -> {
                            MeditationManager.handleSessionEnd(context)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
