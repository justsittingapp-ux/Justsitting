package com.lawrence.justsitting

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDefaultPrefs()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("timer_ch", "Meditation Status", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "JustSitting::TimerWakeLock")
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent { MaterialTheme { RequestNotificationPermission(); MainNavigation(wakeLock) } }
    }

    private fun setupDefaultPrefs() {
        val prefs = getSharedPreferences("meditation_prefs", Context.MODE_PRIVATE)
        if (!prefs.contains("total_time")) {
            prefs.edit()
                .putInt("total_time", 600)
                .putInt("warmup_time", 10)
                .putBoolean("use_dnd", false)
                .putBoolean("keep_screen_on", false)
                .putInt("interval_minutes", 15)
                .apply()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val newConfig = Configuration(newBase.resources.configuration)
        newConfig.fontScale = 1.0f
        applyOverrideConfiguration(newConfig)
        super.attachBaseContext(newBase)
    }
}

@Composable
fun MainNavigation(wakeLock: PowerManager.WakeLock?) {
    var currentScreen by remember { mutableIntStateOf(0) }
    
    // Inițializăm starea globală a meditației
    val meditationState = rememberMeditationState(wakeLock)
    
    // Motorul timer-ului rulează aici, la nivel global
    TimerEngine(meditationState)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0A0A),
                tonalElevation = 0.dp,
                modifier = Modifier.height(84.dp)
            ) {
                // 0: TIMER
                NavigationBarItem(
                    selected = currentScreen == 0,
                    onClick = { currentScreen = 0 },
                    icon = { 
                        Icon(
                            imageVector = if (currentScreen == 0) Icons.Default.SelfImprovement else Icons.Outlined.SelfImprovement,
                            contentDescription = null,
                            modifier = Modifier.size(38.dp)
                        ) 
                    },
                    label = { Text("Timer", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                // 1: WISDOM
                NavigationBarItem(
                    selected = currentScreen == 1,
                    onClick = { currentScreen = 1 },
                    icon = { 
                        Icon(
                            imageVector = if (currentScreen == 1) Icons.Default.AutoStories else Icons.Outlined.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        ) 
                    },
                    label = { Text("Wisdom", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                // 2: JOURNEY
                NavigationBarItem(
                    selected = currentScreen == 2,
                    onClick = { currentScreen = 2 },
                    icon = { 
                        Icon(
                            imageVector = if (currentScreen == 2) Icons.Default.Insights else Icons.Outlined.Insights,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        ) 
                    },
                    label = { Text("Journey", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                // 3: ABOUT
                NavigationBarItem(
                    selected = currentScreen == 3,
                    onClick = { currentScreen = 3 },
                    icon = { 
                        Icon(
                            imageVector = if (currentScreen == 3) Icons.Default.Info else Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        ) 
                    },
                    label = { Text("About", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                // 4: SETTINGS
                NavigationBarItem(
                    selected = currentScreen == 4,
                    onClick = { currentScreen = 4 },
                    icon = { 
                        Icon(
                            imageVector = if (currentScreen == 4) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        ) 
                    },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        },
        containerColor = Color(0xFF0A0A0A)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (slideInHorizontally { it / 2 } + fadeIn(tween(500))) togetherWith
                    (slideOutHorizontally { -it / 2 } + fadeOut(tween(500)))
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when(targetScreen) {
                    0 -> MeditationTimerScreen(meditationState, onOpenSettings = { currentScreen = 4 })
                    1 -> WisdomScreen()
                    2 -> StatisticsScreen()
                    3 -> AboutScreen()
                    4 -> SettingsScreen(onSaveSuccess = { currentScreen = 0 })
                }
            }
        }
    }
}