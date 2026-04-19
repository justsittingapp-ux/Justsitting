package com.lawrence.justsitting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun WheelNumberPicker(
    range: IntRange,
    start: Int,
    label: String,
    isSmall: Boolean = false,
    onValueChange: (Int) -> Unit
) {
    val itemHeight = if (isSmall) 40.dp else 50.dp
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = range.indexOf(start).coerceAtLeast(0))

    LaunchedEffect(listState.firstVisibleItemIndex) {
        onValueChange(range.elementAtOrNull(listState.firstVisibleItemIndex) ?: range.first)
        // Eliminat feedback haptic (vibrație) conform cerinței
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(itemHeight * 3),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = itemHeight),
                flingBehavior = rememberSnapFlingBehavior(listState)
            ) {
                items(range.last - range.first + 1) { index ->
                    val isSelected = listState.firstVisibleItemIndex == index
                    Box(
                        Modifier
                            .height(itemHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (range.first + index).toString().padStart(2, '0'),
                            fontSize = if (isSelected) 22.sp else 16.sp,
                            color = if (isSelected) Color.White else Color.Gray.copy(0.4f)
                        )
                    }
                }
            }
        }
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun TimeStepper(
    value: Int,
    range: IntRange,
    label: String,
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Buton Minus
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Default.Remove, null, tint = Color(0xFF00BCD4))
        }

        // Afișaj Central (Stil similar cu imaginea)
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(100.dp)
                .height(56.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value.toString().padStart(2, '0'),
                    color = Color(0xFF00BCD4),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = label,
                    color = Color(0xFF00BCD4).copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }
        }

        // Buton Plus
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Default.Add, null, tint = Color(0xFF00BCD4))
        }
    }
}

@Composable
fun OptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BCD4))
        )
        Text(label, color = Color.LightGray, fontSize = 13.sp)
    }
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= 33) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}