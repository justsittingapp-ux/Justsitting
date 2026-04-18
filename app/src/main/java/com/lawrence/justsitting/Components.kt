package com.lawrence.justsitting

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
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
    val view = LocalView.current

    LaunchedEffect(listState.firstVisibleItemIndex) {
        onValueChange(range.elementAtOrNull(listState.firstVisibleItemIndex) ?: range.first)
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
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